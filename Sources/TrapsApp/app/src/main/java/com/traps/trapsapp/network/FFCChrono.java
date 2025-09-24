package com.traps.trapsapp.network;


public class FFCChrono extends FFCPacket {

	int chrono;


	public FFCChrono(short bibnumber, int chrono, int runId) {
		this.bibnumber = bibnumber;
		this.chrono = chrono;
		this.runId = runId;
	}



	public String toString() {
		return "bibnumber="+bibnumber+" | chrono="+chrono;
	}
	

	
	public boolean isValid() {
		if (bibnumber <= 0) return false;
		if (chrono < 0) return false;
		if (runId<1) return false;
		return true;

	}

	
	public byte[] getByteArray() {

		if (chrono < 1) return null;
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
		data[9] = 'A';
		data[10] = 0;
		byte[] num4 = get4Bytes(chrono);
		data[11] = num4[0];
		data[12] = num4[1];
		data[13] = num4[2];
		data[14] = num4[3];
		data[15] = 0;
		data[16] = 0;
		data[17] = 0;
		data[18] = 0;

		return data;
	}

}
