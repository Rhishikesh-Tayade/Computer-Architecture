package processor.pipeline;

public class EX_IF_LatchType {
	boolean EX_IF_enable;
	int PC;

	
	public EX_IF_LatchType(){
		EX_IF_enable = false;
	}
	
	public void setEX_IF_enable(boolean eX_IF_enable) {
		EX_IF_enable = eX_IF_enable;
	}
	public void setPC(int pC) {
		PC = pC;
	}
	public boolean isEX_IF_enable(){
		return EX_IF_enable;
	}
	public int getPC() {
		return PC;
	}
	public void setEX_IF_enable(boolean eX_IF_enable, int pc) {
		EX_IF_enable = eX_IF_enable;
		PC = pc;
	}
	

}
