package de.sb.broker.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.xml.bind.annotation.*;

@Entity
@Table(name="BaseEntity", schema="_s0545840__brokerDB")
@Inheritance(strategy = InheritanceType.JOINED)
@PrimaryKeyJoinColumn(name = "identity")
@DiscriminatorValue("BaseEntity")			
@DiscriminatorColumn(name = "discriminator", discriminatorType = DiscriminatorType.STRING)  
@XmlAccessorType(XmlAccessType.NONE) // NONE -> explicit annotation for all elements
@XmlType // XML types -> WSDL, WADL
@XmlSeeAlso({Auction.class, Person.class}) // Referencing subclasses
public abstract class BaseEntity implements Comparable<BaseEntity> {

	/*   Attributes   */
	/* ************** */
	@Id
	@Column(name = "identity", updatable=false, nullable=false, insertable=true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long identity;
	
	@Column(name = "version", updatable=true, nullable=false, insertable=true)
	@Version
	private int version;
	
	@Column(name = "creationTimestamp", updatable=false, nullable=false, insertable=true)
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
