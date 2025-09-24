package com.traps.trapsapp.network;


import org.json.JSONException;
import org.json.JSONObject;

import android.util.SparseIntArray;

public class TRAPSPenalty extends TRAPSPacket {
	
	protected SparseIntArray penaltyArray = null;
	
	public TRAPSPenalty(int bibnumber, SparseIntArray penaltyArray) {
		this.bibnumber = bibnumber;
		this.penaltyArray = penaltyArray;
	}
	
	public boolean isValid() {
		if (penaltyArray==null) return false;
		return true;
	}
	
	public JSONObject getJsonObject() {

		if (penaltyArray==null) return null;
		JSONObject json = new JSONObject();
		try {
			json.put("command", 1);
			json.put("bib", bibnumber);
			JSONObject penaltyList = new JSONObject();
			for (int index=0; index<penaltyArray.size(); index++) {
				penaltyList.put(""+(1+penaltyArray.keyAt(index)), penaltyArray.valueAt(index));
			}
			json.put("penaltyList", penaltyList);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return json;
	}
	
}
