package processor.pipeline;

import generic.Instruction;
import generic.Instruction.OperationType;
import processor.Processor;

public class MemoryAccess {
	Processor containingProcessor;
	EX_MA_LatchType EX_MA_Latch;
	MA_RW_LatchType MA_RW_Latch;
	
	public MemoryAccess(Processor containingProcessor, EX_MA_LatchType eX_MA_Latch, MA_RW_LatchType mA_RW_Latch)
	{
		this.containingProcessor = containingProcessor;
		this.EX_MA_Latch = eX_MA_Latch;
		this.MA_RW_Latch = mA_RW_Latch;
	}
	
	public void performMA()
	{
		Instruction inst = EX_MA_Latch.getInstruction();
		int aluResult = EX_MA_Latch.getAluResult();
		OperationType operationType = inst.getOperationType();
		//TODO
		if(EX_MA_Latch.isMA_enable()){
			System.out.println("\nMA Stage\nOperation type: "+operationType.toString());
			if(operationType==OperationType.store){
				int rs1 = containingProcessor.getRegisterFile().getValue(inst.getSourceOperand1().getValue());
				containingProcessor.getMainMemory().setWord(aluResult, rs1);
				System.out.println("rs1="+rs1+"\nvalue: "+containingProcessor.getMainMemory().getWord(aluResult));
			}else{
				if(operationType==OperationType.load){
					MA_RW_Latch.setAluResult(containingProcessor.getMainMemory().getWord(aluResult));
					System.out.println("Load instruction alu: "+MA_RW_Latch.getAluResult());
				}else{
					MA_RW_Latch.setAluResult(aluResult);
				}
			}
			EX_MA_Latch.setMA_enable(false);
			MA_RW_Latch.setRW_enable(true);
			MA_RW_Latch.setInstruction(inst);
			System.out.println("ALU Result set: "+MA_RW_Latch.getAluResult());
		}
	}

}
