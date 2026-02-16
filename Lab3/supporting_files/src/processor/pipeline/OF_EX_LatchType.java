package processor.pipeline;

import generic.Instruction;





public class OF_EX_LatchType {
	
	Instruction inst;
	boolean EX_enable;
	
	public OF_EX_LatchType()
	{
		EX_enable = false;
	}
	public Instruction getInstruction(){
		return inst;
	}
	public void setInstruction(Instruction in){
		this.inst = in;
	}
	public boolean isEX_enable() {
		return EX_enable;
	}

	public void setEX_enable(boolean eX_enable) {
		EX_enable = eX_enable;
	}
}
