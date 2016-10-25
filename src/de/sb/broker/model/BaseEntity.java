package de.sb.broker.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;

@Entity
@Table(name="BaseEntity", schema="_s0545840__brokerDB")
public class BaseEntity implements Comparable<BaseEntity> {

	/*   Attributes   */
	/* ************** */

	@Column(name = "identity", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long identity;
	
	@Column(name = "version")
	private int version;
	
	@Column(name = "creationTimestamp")
	private long creationTimeStamp;

	/*   Methods   */
	/* *********** */
	
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
