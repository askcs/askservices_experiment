package com.askcs.askservices.timeline;

public class Slot {

	int id;
	long start,end;
	String value;
	
	Slot(int i,long s,long e,String v){id=i;start=s;end=e;value=v;}
	
	public long getStart(){return start;}
	public long getEnd(){return end;}
	public String getValue(){return value;}
	public int getIDX(){return id;}
		
	void setIDX(int i){id=i;}
	public void setValue(String s){value=s;}
}
