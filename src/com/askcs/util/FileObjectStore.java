package com.askcs.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FileObjectStore {

	private static String path = "objects/"; //TODO: load from config?
	private static ObjectMapper om = null;
	
	public FileObjectStore() {
		om = ParallelInit.getObjectMapper();
		// TODO: check if path exists
	}
	
	public <T> T load(Class<T> type, String id) {
		
		try {
			String json = "";
			FileReader stream = new FileReader(path+type.getName().toLowerCase()+"_"+id);
			BufferedReader in = new BufferedReader(stream);
		
			String line;
			while( (line = in.readLine() ) != null )
            {
                json += line;
            }
            in.close();

			
			return om.convertValue(json, type);
			
		} catch(Exception e){
		}
		
		return null;
	}
	
	public void store(String id, Object object) {
		try {
			String json = om.writeValueAsString(object);
			
			// TODO: Check if file already exists
			FileWriter stream = new FileWriter(path+object.getClass().getName().toLowerCase()+"_"+id,false);
			
			BufferedWriter out = new BufferedWriter(stream);
			out.write(json);
			out.close();
			
		} catch(Exception ex){
		}
	}
	
	public void update(String id, Object object) {
		
		// TODO: implement
	}
	
	public void delete(String id, String type) {
		File file = new File(path+type+"_"+id);
		file.delete();
	}
}
