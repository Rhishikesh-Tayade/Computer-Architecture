package processor.pipeline;

import generic.Instruction;

public class MA_RW_LatchType {
	
	boolean RW_enable;
	int aluResult;
	Instruction inst;
	
	public int getAluResult() {
		return aluResult;
	}
	public void setAluResult(int aluResult) {
		this.aluResult = aluResult;
	}
	public Instruction getInstruction() {
		return inst;
	}
	public void setInstruction(Instruction inst) {
		this.inst = inst;
	}
	public MA_RW_LatchType()
	{
		RW_enable = false;
	}

	public boolean isRW_enable() {
		return RW_enable;
	}

	public void setRW_enable(boolean rW_enable) {
		RW_enable = rW_enable;
	}

}