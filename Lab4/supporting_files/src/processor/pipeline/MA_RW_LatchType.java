package processor.pipeline;

import generic.Instruction.OperationType;

public class MA_RW_LatchType {

	boolean RW_enable;
	OperationType opcode;
	int result;
	int rd;

	public MA_RW_LatchType() {
		RW_enable = false;
	}

	public boolean isRW_enable() {
		return RW_enable;
	}

	public void setRW_enable(boolean rW_enable) {
		RW_enable = rW_enable;
	}

	public OperationType getOpcode() {
		return opcode;
	}

	public void setOpcode(OperationType opcode) {
		this.opcode = opcode;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public int getRd() {
		return rd;
	}

	public void setRd(int rd) {
		this.rd = rd;
	}

}
