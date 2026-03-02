package processor.pipeline;

public class EX_IF_LatchType {

	boolean isBranch;
	int branchTarget;

	public EX_IF_LatchType() {
		isBranch = false;
	}

	public boolean isBranch() {
		return isBranch;
	}

	public void setIsBranch(boolean isBranch) {
		this.isBranch = isBranch;
	}

	public int getBranchTarget() {
		return branchTarget;
	}

	public void setBranchTarget(int branchTarget) {
		this.branchTarget = branchTarget;
	}

}
