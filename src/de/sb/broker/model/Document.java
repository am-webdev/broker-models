package de.sb.broker.model;

public class Document extends BaseEntity {
	private String type;
	private byte[] content;
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
		return type;
	}

	public byte[] getContent() {
		return content;
	}

	public byte[] getHash() {
		return hash;
	}
	
}
