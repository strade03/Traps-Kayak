package com.traps.trapsapp.network;


public class FFCPenalty extends FFCPacket {

	byte penalty;
	byte gateIndex;


	public FFCPenalty(short bibnumber, byte gateIndex, byte penalty, int runId) {
		this.bibnumber = bibnumber;
		this.penalty = penalty;
		this.gateIndex = gateIndex;
		this.runId = runId;
	}

	

	public String toString() {
		return "bibnumber="+bibnumber+" | gateIndex="+gateIndex+" | penalty="+penalty;
	}
	

	
	
	public boolean isValid() {
		if (bibnumber <= 0) return false;
		if (gateIndex < 0) return false;
		if ((penalty != 0) && (penalty != 2) && (penalty != 50)) return false;
		if (runId<1) return false;
		return true;

	}



	/**
	 * Returns an array of bytes to be sent to FFCanoe
	 * 
	 * @return
	 */
	public byte[] getByteArray() {

		if (gateIndex < 0) return null;
		byte[] data = new byte[19];
		data[0] = 0; // header
		data[1] = 0; // header
		data[2] = 0; // end of machine name
		data[3] = 0; // raceId LSB
		data[4] = 0; // raceId MSB
		byte[] num = get2Bytes((short) bibnumber);
		data[5] = num[0]; // bib number LSB
		data[6] = num[1]; // bib number MSB
		num = get2Bytes((short) runId);
		data[7] = num[0]; // run id LSB
		data[8] = num[1]; // run id MSB
		data[9] = 'P';
		data[10] = 0;
		data[11] = penalty;
		data[12] = 0;
		data[13] = 0;
		data[14] = 0;
		data[15] = gateIndex;
		data[16] = 0;
		data[17] = 0;
		data[18] = 0;

		return data;
	}



}
