package com.traps.trapsapp.network;


import org.json.JSONObject;

public class TRAPSPacket {
	
	protected int bibnumber = 0;
		
	public boolean isValid() {
		return false;
	}
	
	
	public int getBibnumber() {
		return bibnumber;
	}

	public JSONObject getJsonObject() {
		return null;
	}
	
}
