package de.sb.broker.model;

public class BaseEntity implements Comparable<BaseEntity> {

	private long identity;
	private int version;
	private long creationTimeStamp;
	
	// TODO Constructor needed?

	@Override
	public int compareTo(BaseEntity o) {
		int rtn = 0;
		if (this.identity == o.identity) {
			rtn = 0;
		}
		
		if (this.identity > o.identity) {
			rtn = 1;
		}
		
		if (this.identity < o.identity) {
			rtn = -1;
		}
		
		return rtn;
	}

	public long getIdentity() {
		return identity;
	}

	public int getVersion() {
		return version;
	}

	public long getCreationTimeStamp() {
		return creationTimeStamp;
	}
	
}
