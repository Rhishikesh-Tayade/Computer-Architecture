package processor.pipeline;

import generic.Instruction.OperationType;

public class EX_MA_LatchType {

	boolean MA_enable;
	boolean EX_busy; // true while functional unit is computing (multi-cycle)
	OperationType opcode;
	int aluResult;
	int rd;
	int rs2Val;

	public EX_MA_LatchType() {
		MA_enable = false;
		EX_busy = false;
	}

	public boolean isMA_enable() {
		return MA_enable;
	}

	public void setMA_enable(boolean mA_enable) {
		MA_enable = mA_enable;
	}

	public boolean isEX_busy() {
		return EX_busy;
	}

	public void setEX_busy(boolean eX_busy) {
		EX_busy = eX_busy;
	}

	public OperationType getOpcode() {
		return opcode;
	}

	public void setOpcode(OperationType opcode) {
		this.opcode = opcode;
	}

	public int getAluResult() {
		return aluResult;
	}

	public void setAluResult(int aluResult) {
		this.aluResult = aluResult;
	}

	public int getRd() {
		return rd;
	}

	public void setRd(int rd) {
		this.rd = rd;
	}

	public int getRs2Val() {
		return rs2Val;
	}

	public void setRs2Val(int rs2Val) {
		this.rs2Val = rs2Val;
	}

}
