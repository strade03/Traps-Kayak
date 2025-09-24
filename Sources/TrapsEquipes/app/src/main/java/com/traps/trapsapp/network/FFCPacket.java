package com.traps.trapsapp.network;


public class FFCPacket {

	protected int runId = 1;
	protected short bibnumber = 0;

	/**
	 * Returns LSB at index 0 and MSB at index 1
	 * 
	 * @param value
	 * @return
	 */
	protected byte[] get2Bytes(short value) {
		byte[] data = new byte[2];
		data[1] = (byte) (value >> 8);
		data[0] = (byte) (value & 0xFF);
		return data;
	}
	
	protected byte[] get4Bytes(int value) {
		byte[] data = new byte[4];
		data[3] = (byte) (value >> 24);
		data[2] = (byte) (value >> 16);
		data[1] = (byte) (value >> 8);
		data[0] = (byte) (value & 0xFF);
		return data;
	}

	public boolean isValid() {
		
		return false;

	}
	/**
	 * Returns an array of bytes to be sent to FFCanoe
	 * 
	 * @return
	 */
	public byte[] getByteArray() {
		return null;
	}

	public short getBibnumber() {
		return bibnumber;
	}


	public int getRunId() {
		return runId;
	}


}
