package org.springframework.bsu;

import java.io.Serializable;
import java.security.PublicKey;

public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
    private PublicKey key;
    private String text;
    
    public Message(String text, PublicKey key) {
    	this.text = text;
    	this.setKey(key);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

	public PublicKey getKey() {
		return key;
	}

	public void setKey(PublicKey key) {
		this.key = key;
	}
	
	@Override
    public String toString() {
        return "key: " + key.toString() + "; text" + text.toString();
    }
}

