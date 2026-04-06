package processor.memorysystem;

public class CacheLine {
	int tag;
	int data;
	boolean valid;
	long lastUsedTime;

	public CacheLine() {
		this.tag = -1;
		this.data = 0;
		this.valid = false;
		this.lastUsedTime = 0;
	}

	public int getTag() {
		return tag;
	}

	public void setTag(int tag) {
		this.tag = tag;
	}

	public int getData() {
		return data;
	}

	public void setData(int data) {
		this.data = data;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public long getLastUsedTime() {
		return lastUsedTime;
	}

	public void setLastUsedTime(long lastUsedTime) {
		this.lastUsedTime = lastUsedTime;
	}
}
