package processor.pipeline;

import generic.Instruction;
import generic.Simulator;
import processor.Processor;

public class RegisterWrite {
	Processor containingProcessor;
	MA_RW_LatchType MA_RW_Latch;
	IF_EnableLatchType IF_EnableLatch;
	
	public RegisterWrite(Processor containingProcessor, MA_RW_LatchType mA_RW_Latch, IF_EnableLatchType iF_EnableLatch)
	{
		this.containingProcessor = containingProcessor;
		this.MA_RW_Latch = mA_RW_Latch;
		this.IF_EnableLatch = iF_EnableLatch;
	}
	
	public void performRW()
	{
		if(MA_RW_Latch.isRW_enable())
		{
			//TODO
			
			Instruction inst = MA_RW_Latch.getInstruction();
			int aluResult = MA_RW_Latch.getAluResult();
			System.out.println("=======\nRW Stage\nOperation Type: "+inst.getOperationType().toString());
			switch (inst.getOperationType()) {
				case end:
					System.out.println("End instruction encounered");
					Simulator.setSimulationComplete(true);
					break;
				case bgt: case blt: case beq: case bne: case jmp: case store:
					System.out.println("Branching instruction");
					break;
				default:
					containingProcessor.getRegisterFile().setValue(inst.getDestinationOperand().getValue(), aluResult);
					System.out.println("Written to register: "+inst.getDestinationOperand().getValue()+"\nValue: "+aluResult);
					break;
			}
			
			System.out.println("=========");
			MA_RW_Latch.setRW_enable(false);
			IF_EnableLatch.setIF_enable(true);
		}
	}

}
