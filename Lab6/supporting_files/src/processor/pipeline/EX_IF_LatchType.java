package processor.pipeline;

public class EX_IF_LatchType {

	boolean isBranchTaken;
	int branchTarget;

	public EX_IF_LatchType() {
		isBranchTaken = false;
	}

	public boolean isBranch() {
		return isBranchTaken;
	}

	public void setIsBranch(boolean isBranchTaken) {
		this.isBranchTaken = isBranchTaken;
	}

	public boolean getIsBranchTaken(){
		return isBranchTaken;
	}

	public int getBranchTarget() {
		return branchTarget;
	}

	public void setBranchTarget(int branchTarget) {
		this.branchTarget = branchTarget;
	}

}
