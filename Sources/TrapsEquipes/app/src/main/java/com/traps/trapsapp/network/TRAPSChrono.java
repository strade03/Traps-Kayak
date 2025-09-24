package com.traps.trapsapp.network;


import org.json.JSONException;
import org.json.JSONObject;

import android.util.SparseIntArray;

public class TRAPSChrono extends TRAPSPacket {
	
	protected int type;  // 0 = start, 1 = finish
	protected long chrono;
	
	public TRAPSChrono(int bibnumber, int type, long chrono) {
		this.bibnumber = bibnumber;
		this.type = type;
		this.chrono = chrono;
	}
	
	public boolean isValid() {
		return true;
	}
	
	
	public int getBibnumber() {
		return bibnumber;
	}

	public JSONObject getJsonObject() {

		JSONObject json = new JSONObject();
		try {
			json.put("command", type+2);
			json.put("bib", bibnumber);
			json.put("time", chrono);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return json;
	}
	
}
