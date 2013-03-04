package com.askcs.askservices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import com.almende.eve.rpc.jsonrpc.jackson.JOM;

@SuppressWarnings("serial")
public class Question implements Serializable {

	public Question() {}
	
	public Question(String requester, Set<String> responders, String priority, String timeout, String text, String type, String state) {
		
		this.uuid = UUID.randomUUID().toString();
		this.requester = requester;
		this.responder = responders;
		this.priority = priority;
		this.timeout = timeout;
		
		this.question_text = text;
		this.timeout = timeout;
		this.type = type;
		this.state = state;
	}
	
	public Question(String questionUUID, String requester, Set<String> responders, String priority, String timeout, String text, String type, String state, ArrayList<Answer> answer) {
		
		this.uuid = questionUUID;
		this.requester = requester;
		this.responder = responders;
		this.priority = priority;
		this.timeout = timeout;
		
		this.question_text = text;
		this.timeout = timeout;
		this.type = type;
		this.state = state;
		this.answers = answer;
	}
		
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getQuestion_text() {
		return question_text;
	}
	public void setQuestion_text(String question_text) {
		this.question_text = question_text;
	}
	public String getRequester() {
		return requester;
	}
	public void setRequester(String requester) {
		this.requester = requester;
	}
	public Set<String> getResponder() {
		return responder;
	}

	public void setResponder(Set<String> responder) {
		this.responder = responder;
	}

	public String getPriority() {
		return priority;
	}
	public void setPriority(String priority) {
		this.priority = priority;
	}
	public String getTimeout() {
		return timeout;
	}
	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public ArrayList<Answer> getAnswers() {
		return answers;
	}
	public void setAnswers(ArrayList<Answer> answers) {
		this.answers = answers;
	}
	public String getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(String creationTime) {
		this.creationTime = creationTime;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public String getModuleId() {
		return moduleId;
	}
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}
	public String getAgent() {
		return agent;
	}
	public void setAgent(String agent) {
		this.agent = agent;
	}
	public String getBox() {
		return box;
	}
	public void setBox(String box) {
		this.box = box;
	}
	
	public void addAnswer(Answer answer) {
		if(answers==null)
			answers = new ArrayList<Answer>();
		
		answers.add(answer);
	}
	
	public String toJSON() throws Exception {
		return JOM.getInstance().writeValueAsString(this);
	}

	private String uuid = null;
	private String subject = null;
	private String question_text = null;
	private String requester = null;
	private Set<String> responder = null;
	private String priority = null;
	
	private String timeout = null;
	private String type = null;
	private String state = null;
	private ArrayList<Answer> answers = null;
	
	private String creationTime = null;
	private String module = null;
	private String moduleId = null;
	
	private String agent = null;
	private String box = null;
}

