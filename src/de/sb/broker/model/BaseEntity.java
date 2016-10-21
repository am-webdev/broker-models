package de.sb.broker.model;

public class BaseEntity implements Comparable<BaseEntity> {

	private long identity;
	private int version;
	private long creationTimeStamp;

	@Override
	public int compareTo(BaseEntity o) {
		return Long.compare(this.identity, o.identity);
	}

	public long getIdentity() {
		return identity;
	}

	public int getVersion() {
		return version;
	}	

	public void setVersion(int version) {
		this.version = version;
	}

	public long getCreationTimeStamp() {
		return creationTimeStamp;
	}
	
}
