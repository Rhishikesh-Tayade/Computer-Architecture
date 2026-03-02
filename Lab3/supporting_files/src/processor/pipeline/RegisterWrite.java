package processor.pipeline;

import generic.Simulator;
import processor.Processor;

public class RegisterWrite {
	Processor containingProcessor;
	MA_RW_LatchType MA_RW_Latch;
	IF_EnableLatchType IF_EnableLatch;

	public RegisterWrite(Processor containingProcessor, MA_RW_LatchType mA_RW_Latch,
			IF_EnableLatchType iF_EnableLatch) {
		this.containingProcessor = containingProcessor;
		this.MA_RW_Latch = mA_RW_Latch;
		this.IF_EnableLatch = iF_EnableLatch;
	}

	public void performRW() {
		if (MA_RW_Latch.isRW_enable()) {
			generic.Instruction.OperationType opcode = MA_RW_Latch.getOpcode();
			int result = MA_RW_Latch.getResult();
			int rd = MA_RW_Latch.getRd();

			// Debug print
			System.out.println("Cycle " + processor.Clock.getCurrentTime() + ": Completed " + opcode + ", Result="
					+ result + ", Rd=" + rd);

			// Write result to register (except for store, branch, jmp, end)
			if (opcode != generic.Instruction.OperationType.store &&
					opcode != generic.Instruction.OperationType.beq &&
					opcode != generic.Instruction.OperationType.bne &&
					opcode != generic.Instruction.OperationType.blt &&
					opcode != generic.Instruction.OperationType.bgt &&
					opcode != generic.Instruction.OperationType.jmp &&
					opcode != generic.Instruction.OperationType.end) {
				containingProcessor.getRegisterFile().setValue(rd, result);
			}

			// Update statistics
			generic.Statistics.numberOfInstructions++;

			// Check for end instruction
			if (opcode == generic.Instruction.OperationType.end) {
				Simulator.setSimulationComplete(true);
			}

			MA_RW_Latch.setRW_enable(false);
			IF_EnableLatch.setIF_enable(true);
		}
	}

}
