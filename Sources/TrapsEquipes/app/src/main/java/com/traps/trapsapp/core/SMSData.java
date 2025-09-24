package com.traps.trapsapp.core;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import android.util.SparseIntArray;



public class SMSData {

	public static final int UNDEFINED_TYPE = -1;
	public static final int PENALTY_TYPE = 0;
	public static final int START_TYPE = 1;
	public static final int FINISH_TYPE = 2;
	
	
	private int bibnumber;
	private long timestamp;
	private long start = 0;
	private long finish = 0;
	private SparseIntArray map = new SparseIntArray();
	private int msgType = UNDEFINED_TYPE;
	
	public SMSData(String msg) throws ParseException {
		
		String[] elementArray = msg.split("\\ ");
		if (elementArray.length<4) throw new ParseException("Not enough info in the SMS",0);
		
		try {
			timestamp = Long.parseLong(elementArray[0].substring(1));
		} catch (NumberFormatException e) {
			throw new ParseException("Cannot parse timestamp: "+elementArray[0], 0);
		}
		
		try {
			bibnumber = Integer.parseInt(elementArray[1]);
		} catch (NumberFormatException e) {
			throw new ParseException("Cannot parse bib number: "+elementArray[1], 0);
		}
		
		// get chrono
		if ("start".equals(elementArray[2])) {
			start = Long.parseLong(elementArray[3]);
			msgType = START_TYPE;
			return;
		}
		
		if ("finish".equals(elementArray[2])) {
			finish = Long.parseLong(elementArray[3]);
			msgType = FINISH_TYPE;
			return;
		}
		
		if (!"penalty".equals(elementArray[2])) throw new ParseException("No key word found", 0);
		
		msgType = PENALTY_TYPE;
		// get penalties
		for (int index=3; index<elementArray.length; index++) {
			String[] tab = elementArray[index].split("\\:");
			if (tab.length!=2) throw new ParseException("Exactly one colon separating gate and penalty is needed",index);
			try {
				int gateIndex = Integer.parseInt(tab[0])-1;
				int penalty = Integer.parseInt(tab[1]);
				if ((gateIndex<0) || (gateIndex>=SystemParam.GATE_COUNT)) throw new ParseException("Wrong gate id:"+gateIndex, index);
				if (!SystemParam.isPenaltyValid(penalty)) throw new ParseException("Wrong penalty value:"+penalty, index);
				map.put(gateIndex, penalty);
				
				
			} catch (NumberFormatException e) {
				throw new ParseException("Cannot parse gate:penalty: "+elementArray[index], index);
			}
		}
		
	}

	public int getMsgType() {
		return msgType;
	}
	
	public long getStart() {
		return start;
	}
	
	public long getFinish() {
		return finish;
	}
	

	public int getBibnumber() {
		return bibnumber;
	}

	public SparseIntArray getMap() {
		return map;
	}
	
	
	public long getTimestamp() {
		return timestamp;
	}


	/*public static void main(String[] args) {
		
		try {
			SMSData data = new SMSData(args[0]);
			System.out.println("bibnumber="+data.getBibnumber());
			System.out.println("timestamp="+data.getTimestamp());
			SparseIntArray map = data.getMap();
			for (int index=0; index<map.size(); index++) {
				System.out.println("Gate "+map.keyAt(index)+" : "+map.valueAt(index));
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	*/
	
}

