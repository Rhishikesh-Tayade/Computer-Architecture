package processor.pipeline;

import processor.Processor;

public class MemoryAccess {
	Processor containingProcessor;
	EX_MA_LatchType EX_MA_Latch;
	MA_RW_LatchType MA_RW_Latch;

	public MemoryAccess(Processor containingProcessor, EX_MA_LatchType eX_MA_Latch, MA_RW_LatchType mA_RW_Latch) {
		this.containingProcessor = containingProcessor;
		this.EX_MA_Latch = eX_MA_Latch;
		this.MA_RW_Latch = mA_RW_Latch;
	}

	public void performMA() {
		if (EX_MA_Latch.isMA_enable()) {
			generic.Instruction.OperationType opcode = EX_MA_Latch.getOpcode();
			int aluResult = EX_MA_Latch.getAluResult();
			int rd = EX_MA_Latch.getRd();
			int result = aluResult;

			// Handle memory operations
			if (opcode == generic.Instruction.OperationType.load) {
				// Load word from memory
				result = containingProcessor.getMainMemory().getWord(aluResult);
			} else if (opcode == generic.Instruction.OperationType.store) {
				// Store word to memory
				int storeValue = EX_MA_Latch.getRs2Val();
				containingProcessor.getMainMemory().setWord(aluResult, storeValue);
			}

			// Pass data to RW stage
			MA_RW_Latch.setOpcode(opcode);
			MA_RW_Latch.setResult(result);
			MA_RW_Latch.setRd(rd);
			EX_MA_Latch.setMA_enable(false); // Consume the latch
			MA_RW_Latch.setRW_enable(true);
		}
	}

}
