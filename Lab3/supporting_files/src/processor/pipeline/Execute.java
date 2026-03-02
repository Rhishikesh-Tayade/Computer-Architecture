package processor.pipeline;

import processor.Processor;

public class Execute {
	Processor containingProcessor;
	OF_EX_LatchType OF_EX_Latch;
	EX_MA_LatchType EX_MA_Latch;
	EX_IF_LatchType EX_IF_Latch;

	public Execute(Processor containingProcessor, OF_EX_LatchType oF_EX_Latch, EX_MA_LatchType eX_MA_Latch,
			EX_IF_LatchType eX_IF_Latch) {
		this.containingProcessor = containingProcessor;
		this.OF_EX_Latch = oF_EX_Latch;
		this.EX_MA_Latch = eX_MA_Latch;
		this.EX_IF_Latch = eX_IF_Latch;
	}

	public void performEX() {
		if (OF_EX_Latch.isEX_enable()) {
			generic.Instruction.OperationType opcode = OF_EX_Latch.getOpcode();
			int rs1Val = OF_EX_Latch.getRs1Val();
			int rs2Val = OF_EX_Latch.getRs2Val();
			int imm = OF_EX_Latch.getImmediate();
			int rd = OF_EX_Latch.getRd();
			int pc = OF_EX_Latch.getPc();

			int aluResult = 0;
			boolean branchTaken = false;
			int branchTarget = 0;

			// Execute based on opcode
			switch (opcode) {
				// Arithmetic R3I
				case add:
					aluResult = rs1Val + rs2Val;
					break;
				case sub:
					aluResult = rs1Val - rs2Val;
					break;
				case mul:
					aluResult = rs1Val * rs2Val;
					break;
				case div:
					aluResult = rs1Val / rs2Val;
					// Store remainder in x31 as per ToyRISC ISA
					containingProcessor.getRegisterFile().setValue(31, rs1Val % rs2Val);
					break;

				// Logical R3I
				case and:
					aluResult = rs1Val & rs2Val;
					break;
				case or:
					aluResult = rs1Val | rs2Val;
					break;
				case xor:
					aluResult = rs1Val ^ rs2Val;
					break;

				// Shift R3I
				case sll:
					aluResult = rs1Val << rs2Val;
					break;
				case srl:
					aluResult = rs1Val >>> rs2Val;
					break;
				case sra:
					aluResult = rs1Val >> rs2Val;
					break;

				// Comparison R3I
				case slt:
					aluResult = (rs1Val < rs2Val) ? 1 : 0;
					break;

				// Arithmetic R2I
				case addi:
					aluResult = rs1Val + imm;
					break;
				case subi:
					aluResult = rs1Val - imm;
					break;
				case muli:
					aluResult = rs1Val * imm;
					break;
				case divi:
					aluResult = rs1Val / imm;
					// Store remainder in x31 as per ToyRISC ISA
					containingProcessor.getRegisterFile().setValue(31, rs1Val % imm);
					break;

				// Logical R2I
				case andi:
					aluResult = rs1Val & imm;
					break;
				case ori:
					aluResult = rs1Val | imm;
					break;
				case xori:
					aluResult = rs1Val ^ imm;
					break;

				// Shift R2I
				case slli:
					aluResult = rs1Val << imm;
					break;
				case srli:
					aluResult = rs1Val >>> imm;
					break;
				case srai:
					aluResult = rs1Val >> imm;
					break;

				// Comparison R2I
				case slti:
					aluResult = (rs1Val < imm) ? 1 : 0;
					break;

				// Load/Store - compute address
				case load:
				case store:
					aluResult = rs1Val + imm;
					break;

				// Branches - compare and compute target
				case beq:
					if (rs1Val == rs2Val) {
						branchTaken = true;
						branchTarget = pc + imm;
					}
					break;
				case bne:
					if (rs1Val != rs2Val) {
						branchTaken = true;
						branchTarget = pc + imm;
					}
					break;
				case blt:
					if (rs1Val < rs2Val) {
						branchTaken = true;
						branchTarget = pc + imm;
					}
					break;
				case bgt:
					if (rs1Val > rs2Val) {
						branchTaken = true;
						branchTarget = pc + imm;
					}
					break;

				// Jump
				case jmp:
					branchTaken = true;
					// Check if jump to label (imm != 0) or register (rs1Val has target)
					if (imm != 0 || OF_EX_Latch.getRd() == 0) {
						branchTarget = pc + imm; // Jump to label
					} else {
						branchTarget = rs1Val; // Jump to register value
					}
					break;

				// End
				case end:
					break;

				default:
					break;
			}

			// Handle branch/jump
			if (branchTaken) {
				EX_IF_Latch.setIsBranch(true);
				EX_IF_Latch.setBranchTarget(branchTarget);
				containingProcessor.getRegisterFile().setProgramCounter(branchTarget);
			} else {
				EX_IF_Latch.setIsBranch(false);
			}

			// Pass data to MA stage
			EX_MA_Latch.setOpcode(opcode);
			EX_MA_Latch.setAluResult(aluResult);
			EX_MA_Latch.setRd(rd);
			EX_MA_Latch.setRs2Val(rs2Val); // For store instruction

			OF_EX_Latch.setEX_enable(false);
			EX_MA_Latch.setMA_enable(true);
		}
	}

}
