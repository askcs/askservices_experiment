package com.askcs.askservices.agents;

import java.util.ArrayList;

import com.almende.eve.agent.Agent;
import com.almende.eve.agent.annotation.Name;

import com.askcs.askservices.timeline.Slot;
import com.askcs.askservices.timeline.TimelineOnce;
import com.askcs.askservices.timeline.TimelineWeekly;

public class StateAgent extends Agent{
	//@Override
	public String getDescription() {
		return "Manage some time aspects..";
	}
	@Override
	public String getVersion() {
		return null;
	}
	
	//dummy unit
	public String ping()
	{
		return "pong";
	}
	
	// storage getter/setter
	private TimelineOnce getTimeline()
	{
		return TimelineOnce.load_from_agentStore( this.getContext() );
	}
	private void putTimeline(TimelineOnce tl)
	{
		if( tl == null )return;
		tl.save_in_agentStore(  this.getContext() );
	}
	
	private TimelineWeekly getTimelineRecurring()
	{
		return TimelineWeekly.load_from_agentStore( this.getContext() );
	}
	private void putTimelineRecurring(TimelineWeekly tlr)
	{
		if( tlr == null )return;
		tlr.save_in_agentStore(  this.getContext() );
	}
	
	//--------------------------------------
	
	public boolean slots_put(
			@Name("start_millis")long startTime,
			@Name("end_millis")long endTime,
			@Name("description")String desc,
			@Name("occurence")String occurence)
	{

		if( desc != null && desc.length() == 0 )desc = null;	// remove this line?
		
//System.out.println("put: "+ startTime +" "+ endTime +" "+ desc+ " "+ occurence );

		if( occurence.equalsIgnoreCase("WEEKLY") )
		{
			TimelineWeekly tlr = this.getTimelineRecurring();
			tlr.insertSlot(startTime,endTime, desc );
			this.putTimelineRecurring(tlr);
			return true;
		}
		
		TimelineOnce tl = this.getTimeline();
		tl.insertSlot(startTime,endTime, desc );
		this.putTimeline(tl);
		return true;
	}
	
	public ArrayList<Slot> slots_get(
			@Name("start_millis")long startTime,
			@Name("end_millis")long endTime,
			@Name("occurence")String occurence )
	{
		if( startTime == 0 )startTime = System.currentTimeMillis();
		if( endTime   == 0 )endTime =startTime +1;
		
		if( startTime > endTime )return null;
		
//System.out.println("get: "+ startTime +" "+ endTime +" "+ occurence );
		
		if( occurence.equalsIgnoreCase("ALL") )
		{
			//internal forward
			return slots_get_combined( startTime,endTime);
		}
		
		if( occurence.equalsIgnoreCase("WEEKLY") )
		{
			TimelineWeekly tlr = this.getTimelineRecurring();
			return tlr.getSlots(false,  startTime,endTime );
		}
		
		TimelineOnce tl = this.getTimeline();
		return tl.getSlots(false,  startTime,endTime );
	}
	
	
	public ArrayList<Slot> slots_get_combined(
		@Name("start_millis")long startTime,
		@Name("end_millis")long endTime )
	{

		//make timeline with recurring slots
		TimelineOnce tl = this.getTimelineRecurring().getAsTimeline(startTime,endTime);
		
		//make timeline for normal slots
		TimelineOnce tl2 = this.getTimeline();
		tl2.events_clip(startTime,endTime);

		//mix
		tl.combine( tl2, "" );

		//post process values..
		ArrayList<Slot> slots = tl.getSlots(false,  startTime,endTime );
		for(int i=slots.size()-1;i>=0;i--)
		{
			String[] values = slots.get(i).getValue().split(";");
			if( values.length <= 0 )
			{
				slots.remove(i);
				continue;
			}
		
			String val = "{";
			if( values.length == 1 )
				val += "\"weekly\":\""+ values[0] +"\"";	
			else
			if( values[0] == null || values[0].length() == 0 )
				val += "\"event\":\""+ values[1] +"\"";
			else
			{
				val += "\"weekly\":\""+ values[0] +"\"";
				val += ",\"event\":\""+ values[1] +"\"";
			}	
			val += "}";
			slots.get(i).setValue( val );
		}
		return slots;
	}
	
}
