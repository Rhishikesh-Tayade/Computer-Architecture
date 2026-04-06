package processor.pipeline;

import generic.Instruction.OperationType;

public class OF_EX_LatchType {

	boolean EX_enable;
	OperationType opcode;
	int rs1Val;
	int rs2Val;
	int immediate;
	int rd;
	int pc;

	public OF_EX_LatchType() {
		EX_enable = false;
	}

	public boolean isEX_enable() {
		return EX_enable;
	}

	public void setEX_enable(boolean eX_enable) {
		EX_enable = eX_enable;
	}

	public OperationType getOpcode() {
		return opcode;
	}

	public void setOpcode(OperationType opcode) {
		this.opcode = opcode;
	}

	public int getRs1Val() {
		return rs1Val;
	}

	public void setRs1Val(int rs1Val) {
		this.rs1Val = rs1Val;
	}

	public int getRs2Val() {
		return rs2Val;
	}

	public void setRs2Val(int rs2Val) {
		this.rs2Val = rs2Val;
	}

	public int getImmediate() {
		return immediate;
	}

	public void setImmediate(int immediate) {
		this.immediate = immediate;
	}

	public int getRd() {
		return rd;
	}

	public void setRd(int rd) {
		this.rd = rd;
	}

	public int getPc() {
		return pc;
	}

	public void setPc(int pc) {
		this.pc = pc;
	}

}
