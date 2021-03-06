package de.sb.broker.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.persistence.annotations.CacheIndex;

@Entity
@Table(name="Document", schema="_s0545840__brokerDB")
@PrimaryKeyJoinColumn(name = "documentIdentity")	
@DiscriminatorValue("Document")
public class Document extends BaseEntity {
	
	@Column(name = "type", updatable=false, nullable=false, insertable=true)
	@Size(min = 1, max = 50)
	private String type;

	@Lob()
	@Column(name = "content", updatable=false, nullable=false, insertable=true)
	private byte[] content;

	@Column(name = "hash", updatable=false, nullable=false, insertable=true)
	@CacheIndex(updateable = false)
	@Size(min = 32, max = 32)
	private byte[] hash;
	
	protected Document() {
		this(null, null, null);
	}

	public Document(String type, byte[] content, byte[] hash) {
		this.type = type;
		this.content = content;
		this.hash = hash;	
	}

	/* GETTER */
	
	public String getType() {
		return this.type;
	}

	public byte[] getContent() {
		return this.content;
	}

	public byte[] getHash() {
		return this.hash;
	}
	
	/* SETTER */
	
	public void setType(String mimetype) {
		this.type = mimetype;
	}	
}
