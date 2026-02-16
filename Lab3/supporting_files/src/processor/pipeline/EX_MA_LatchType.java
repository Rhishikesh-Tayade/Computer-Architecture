package processor.pipeline;

import generic.Instruction;

public class EX_MA_LatchType {
	Instruction inst;
	int aluResult;
	boolean MA_enable;
	
	public EX_MA_LatchType(){
		MA_enable = false;
	}
	public void setMA_enable(boolean mA_enable) {
		MA_enable = mA_enable;
	}
	public Instruction getInstruction() {
		return inst;
	}
	public void setInstruction(Instruction inst) {
		this.inst = inst;
	}
	public void setAluResult(int aluResult) {
		this.aluResult = aluResult;
	}
	public int getAluResult() {
		return aluResult;
	}
	public boolean isMA_enable() {
		return MA_enable;
	}
}
