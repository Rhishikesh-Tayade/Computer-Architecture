package processor.pipeline;

import generic.Instruction.OperationType;
import processor.Processor;

public class OperandFetch {
	Processor containingProcessor;
	IF_OF_LatchType IF_OF_Latch;
	OF_EX_LatchType OF_EX_Latch;
	IF_EnableLatchType IF_EnableLatch;
	EX_MA_LatchType EX_MA_Latch;
	MA_RW_LatchType MA_RW_Latch;

	public OperandFetch(Processor containingProcessor, IF_OF_LatchType iF_OF_Latch, OF_EX_LatchType oF_EX_Latch,
			IF_EnableLatchType iF_EnableLatch, EX_MA_LatchType eX_MA_Latch, MA_RW_LatchType mA_RW_Latch) {
		this.containingProcessor = containingProcessor;
		this.IF_OF_Latch = iF_OF_Latch;
		this.OF_EX_Latch = oF_EX_Latch;
		this.IF_EnableLatch = iF_EnableLatch;
		this.EX_MA_Latch = eX_MA_Latch;
		this.MA_RW_Latch = mA_RW_Latch;
	}

	private boolean isWriting(OperationType opcode) {
		if (opcode == null)
			return false;
		int idx = opcode.ordinal();
		return idx <= 22; // add to load (0 to 22)
	}

	public void performOF() {
		if (IF_OF_Latch.isOF_enable()) {
			// Branch Hazard: If a branch was taken in EX stage, flush the current
			// instruction in OF
			if (containingProcessor.getEX_IF_Latch().isBranch()) {
				// Note: using direct path through processor to be sure, or just EX_IF_Latch if
				// visible
				// Actually, we can use the field if we have it or the processor's one.
				// Let's use the one in containingProcessor.
				OF_EX_Latch.setEX_enable(false);
				IF_OF_Latch.setOF_enable(false);
				generic.Statistics.numberOfWrongBranchInstructions++;
				generic.Statistics.numberOfControlHazards++;
				return;
			}

			int instruction = IF_OF_Latch.getInstruction();
			int opcodeInt = (instruction >>> 27) & 0x1F;
			OperationType opcode = OperationType.values()[opcodeInt];

			int rs1 = -1, rs2 = -1, rd = -1, imm = 0;
			int rs1Val = 0, rs2Val = 0;
			int baseReg = -1; // Specific for store/jmp/load

			// Decode based on format to identify source registers
			switch (opcode) {
				case add:
				case sub:
				case mul:
				case div:
				case and:
				case or:
				case xor:
				case slt:
				case sll:
				case srl:
				case sra:
					rs1 = (instruction >> 22) & 0x1F;
					rs2 = (instruction >> 17) & 0x1F;
					rd = (instruction >> 12) & 0x1F;
					break;

				case addi:
				case subi:
				case muli:
				case divi:
				case andi:
				case ori:
				case xori:
				case slti:
				case slli:
				case srli:
				case srai:
				case load:
					rs1 = (instruction >> 22) & 0x1F;
					rd = (instruction >> 17) & 0x1F;
					imm = instruction & 0x1FFFF;
					if ((imm & 0x10000) != 0)
						imm |= 0xFFFE0000; // Sign extend
					break;

				case store:
					rs1 = (instruction >> 22) & 0x1F; // Source register value
					baseReg = (instruction >> 17) & 0x1F; // Base address register
					rd = -1; // Store doesn't write to a register
					imm = instruction & 0x1FFFF;
					if ((imm & 0x10000) != 0)
						imm |= 0xFFFE0000;
					break;

				case beq:
				case bne:
				case blt:
				case bgt:
					rs1 = (instruction >> 22) & 0x1F;
					rs2 = (instruction >> 17) & 0x1F;
					imm = instruction & 0x1FFFF;
					if ((imm & 0x10000) != 0)
						imm |= 0xFFFE0000;
					break;

				case jmp:
					int middleBits = (instruction >> 17) & 0x1F;
					if (middleBits == 31) {
						imm = instruction & 0x1FFFF;
						if ((imm & 0x10000) != 0)
							imm |= 0xFFFE0000;
						rd = 0; // Satisfy Execute.java: if(imm != 0 || rd == 0)
					} else {
						baseReg = (instruction >> 22) & 0x1F;
						rd = baseReg;
					}
					break;

				case end:
					break;
			}

			// Data Hazard Detection
			boolean hazard = false;
			// Registers that are read by this instruction: rs1, rs2, and potentially
			// baseReg (for store/jmp)

			// Check against instruction in EX_MA latch (just finished EX)
			if (EX_MA_Latch.isMA_enable()) {
				int rdEX = EX_MA_Latch.getRd();
				OperationType opEX = EX_MA_Latch.getOpcode();
				if (isWriting(opEX) && rdEX != 0) {
					if (rs1 == rdEX || rs2 == rdEX || baseReg == rdEX)
						hazard = true;
				}
				// Special check for x31 from div/divi
				if (!hazard && (opEX == OperationType.div || opEX == OperationType.divi)) {
					if (rs1 == 31 || rs2 == 31 || baseReg == 31)
						hazard = true;
				}
			}
			// Check against instruction in MA_RW latch (just finished MA)
			if (!hazard && MA_RW_Latch.isRW_enable()) {
				int rdMA = MA_RW_Latch.getRd();
				OperationType opMA = MA_RW_Latch.getOpcode();
				if (isWriting(opMA) && rdMA != 0) {
					if (rs1 == rdMA || rs2 == rdMA || baseReg == rdMA)
						hazard = true;
				}
				// Special check for x31 from div/divi
				if (!hazard && (opMA == OperationType.div || opMA == OperationType.divi)) {
					if (rs1 == 31 || rs2 == 31 || baseReg == 31)
						hazard = true;
				}
			}

			if (hazard) {
				// Each instruction that is stalled counts as ONE data hazard event
				if (IF_EnableLatch.isIF_enable()) {
					generic.Statistics.numberOfDataHazards++;
				}
				// Stall IF and insert bubble in EX
				IF_EnableLatch.setIF_enable(false);
				OF_EX_Latch.setEX_enable(false);
				generic.Statistics.numberOfStalls++;
				return;
			}

			// No hazard, proceed
			IF_EnableLatch.setIF_enable(true);

			// Get values from Register File
			if (rs1 != -1)
				rs1Val = containingProcessor.getRegisterFile().getValue(rs1);
			if (rs2 != -1)
				rs2Val = containingProcessor.getRegisterFile().getValue(rs2);

			// Handle special register sources for store and jmp
			if (opcode == OperationType.store) {
				rs1Val = containingProcessor.getRegisterFile().getValue(baseReg); // Address base
				rs2Val = containingProcessor.getRegisterFile().getValue(rs1); // Value to store
			} else if (opcode == OperationType.jmp && baseReg != -1) {
				rs1Val = containingProcessor.getRegisterFile().getValue(baseReg); // Target base
			} else if (opcode == OperationType.load) {
				// rs1 is already set as base address
			}

			// Populate OF_EX latch for the next cycle
			OF_EX_Latch.setOpcode(opcode);
			OF_EX_Latch.setRs1Val(rs1Val);
			OF_EX_Latch.setRs2Val(rs2Val);
			OF_EX_Latch.setImmediate(imm);
			OF_EX_Latch.setRd(rd);
			OF_EX_Latch.setPc(containingProcessor.getRegisterFile().getProgramCounter() - 1);
			OF_EX_Latch.setEX_enable(true);
			IF_OF_Latch.setOF_enable(false);

			if (opcode == OperationType.end) {
				IF_EnableLatch.setIF_enable(false);
			}
		}
	}
}
