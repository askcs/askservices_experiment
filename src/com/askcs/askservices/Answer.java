package com.askcs.askservices;

import java.io.Serializable;
import java.util.UUID;

@SuppressWarnings("serial")
public class Answer implements Serializable{

	public Answer() {}
	
	public Answer(String answer_text, String callback) {
		this.answer_id = UUID.randomUUID().toString();
		this.answer_text = answer_text;
		this.callback = callback;
	}
	
	public Answer(String answer_id, String answer_text, String callback) {
		setAnswer_id(answer_id);
		setAnswer_text(answer_text);
		setCallback(callback);
	}
	
	public String getAnswer_id() {
		return answer_id;
	}
	public void setAnswer_id(String answer_id) {
		this.answer_id = answer_id;
	}
	public String getAnswer_text() {
		return answer_text;
	}
	public void setAnswer_text(String answer_text) {
		this.answer_text = answer_text;
	}
	public String getCallback() {
		return callback;
	}
	public void setCallback(String callback) {
		this.callback = callback;
	}
	
	private String answer_id="";
	private String answer_text="";
	private String callback="";
}
