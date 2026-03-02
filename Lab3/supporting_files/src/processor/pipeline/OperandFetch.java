package processor.pipeline;

import processor.Processor;

public class OperandFetch {
	Processor containingProcessor;
	IF_OF_LatchType IF_OF_Latch;
	OF_EX_LatchType OF_EX_Latch;

	public OperandFetch(Processor containingProcessor, IF_OF_LatchType iF_OF_Latch, OF_EX_LatchType oF_EX_Latch) {
		this.containingProcessor = containingProcessor;
		this.IF_OF_Latch = iF_OF_Latch;
		this.OF_EX_Latch = oF_EX_Latch;
	}

	public void performOF() {
		if (IF_OF_Latch.isOF_enable()) {
			int instruction = IF_OF_Latch.getInstruction();

			// Decode instruction - extract opcode (bits 31-27)
			int opcodeInt = (instruction >> 27) & 0x1F;
			generic.Instruction.OperationType opcode = generic.Instruction.OperationType.values()[opcodeInt];

			int rs1 = 0, rs2 = 0, rd = 0, imm = 0;
			int rs1Val = 0, rs2Val = 0;

			// Decode based on instruction format
			switch (opcode) {
				// R3I format: opcode(5) | rs1(5) | rs2(5) | rd(5) | 0(12)
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
					rs1Val = containingProcessor.getRegisterFile().getValue(rs1);
					rs2Val = containingProcessor.getRegisterFile().getValue(rs2);
					imm = 0;
					break;

				// R2I format: opcode(5) | rs1(5) | rd(5) | imm(17)
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
					// Sign extend 17-bit immediate
					if ((imm & 0x10000) != 0) {
						imm |= 0xFFFE0000;
					}
					rs1Val = containingProcessor.getRegisterFile().getValue(rs1);
					break;

				// Store: opcode(5) | rs1(5) | rd(5) | imm(17)
				// ANALYSIS: Debugging execution showed that for "store %src, off, %base",
				// the Assembler puts %src in rs1 (bits 22-26) and %base in rd (bits 17-21).
				// This conflicts with "load" where rs1 is base.
				// We must map:
				// rs1 (22-26) -> Source Register Index
				// rd (17-21) -> Base Address Register Index
				//
				// To be compatible with Execute stage which expects:
				// rs1Val -> Base Address (for ALU calc)
				// rs2Val -> Value to Store
				case store:
					rs1 = (instruction >> 22) & 0x1F; // Source register index
					rd = (instruction >> 17) & 0x1F; // Base register index
					imm = instruction & 0x1FFFF;
					// Sign extend 17-bit immediate
					if ((imm & 0x10000) != 0) {
						imm |= 0xFFFE0000;
					}
					rs1Val = containingProcessor.getRegisterFile().getValue(rd); // Base address value
					rs2Val = containingProcessor.getRegisterFile().getValue(rs1); // Value to store
					break;

				// Branch: opcode(5) | rs1(5) | rs2(5) | offset(17)
				case beq:
				case bne:
				case blt:
				case bgt:
					rs1 = (instruction >> 22) & 0x1F;
					rs2 = (instruction >> 17) & 0x1F;
					imm = instruction & 0x1FFFF;
					// Sign extend 17-bit offset
					if ((imm & 0x10000) != 0) {
						imm |= 0xFFFE0000;
					}
					rs1Val = containingProcessor.getRegisterFile().getValue(rs1);
					rs2Val = containingProcessor.getRegisterFile().getValue(rs2);
					break;

				// Jump: check for label or register
				case jmp:
					int middleBits = (instruction >> 17) & 0x1F;
					if (middleBits == 31) {
						// Jump to label: opcode(5) | 0(5) | 31(5) | offset(17)
						imm = instruction & 0x1FFFF;
						// Sign extend 17-bit offset
						if ((imm & 0x10000) != 0) {
							imm |= 0xFFFE0000;
						}
					} else {
						// Jump to register: opcode(5) | rd(5) | 0(17)
						rd = (instruction >> 22) & 0x1F;
						rs1Val = containingProcessor.getRegisterFile().getValue(rd);
					}
					break;

				// End: no operands
				case end:
					break;

				default:
					break;
			}

			// Store decoded instruction in OF_EX latch
			OF_EX_Latch.setOpcode(opcode);
			OF_EX_Latch.setRs1Val(rs1Val);
			OF_EX_Latch.setRs2Val(rs2Val);
			OF_EX_Latch.setImmediate(imm);
			OF_EX_Latch.setRd(rd);
			OF_EX_Latch.setPc(containingProcessor.getRegisterFile().getProgramCounter() - 1); // PC was already
																								// incremented in IF

			IF_OF_Latch.setOF_enable(false);
			OF_EX_Latch.setEX_enable(true);
		}
	}

}
