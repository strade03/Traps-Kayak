package com.traps.trapsapp.core;

import java.util.HashSet;
import java.util.Set;

public class SystemParam {

	private static Set<Integer> validPenaltySet = new HashSet<Integer>();
	public static final int MAX_GATE_PER_TERMINAL = 5;
	public static long timeshift = 0; // difference of time between TRAPSManager and the terminal
	
	static {
		validPenaltySet.add(-1);
		validPenaltySet.add(0);
		validPenaltySet.add(2);
		validPenaltySet.add(50); 
	}	
	
	public static int GATE_COUNT = 25;
		
	public static boolean isPenaltyValid(int value) {
		if (validPenaltySet.contains(value)) return true;
		return false;
	}
		
		
}
