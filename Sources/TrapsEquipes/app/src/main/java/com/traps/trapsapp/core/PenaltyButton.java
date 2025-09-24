package com.traps.trapsapp.core;


public class PenaltyButton  {

	private int gateIndex;
	private int buttonIndex; //0-2
	private int value; //0, 2, 50
	 
	public PenaltyButton(int gateIndex, int buttonIndex, int value) {
		this.gateIndex = gateIndex;
		this.value = value;
		this.buttonIndex = buttonIndex;
	}
 
	public int getGateIndex() {
		return gateIndex; 
	}

	public int getValue() {
		return value;
	}

	public int getButtonIndex() {
		return buttonIndex;
	}


	
 
}
