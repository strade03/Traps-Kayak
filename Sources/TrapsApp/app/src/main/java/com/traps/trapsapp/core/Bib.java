package com.traps.trapsapp.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import android.util.Log;
import android.util.SparseIntArray;


public class Bib {
 
	public static final int CHRONO_START = 0;
	public static final int CHRONO_FINISH = 0;

	private int bibnumber;
	private int index;
	private int[] pen = new int[SystemParam.GATE_COUNT];
	private boolean locked = false;
	private long start = 0;
	private long finish = 0;
	private boolean checked = false;
	private static SimpleDateFormat dateFormatter1 = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
	private static SimpleDateFormat dateFormatter2 = new SimpleDateFormat("mm:ss.SSS", Locale.US);
	
	public Bib(int bibnumber, int index) {
		this.bibnumber = bibnumber;
		this.index = index;
		locked = false;
		start = 0;
		finish = 0;
		resetPenalties();
	}
	
	public long getChrono(int whichone) {
		if (whichone==CHRONO_START) return start;
		return finish;
	}
	
	public String getChronoStr(int whichone) {
		long chrono = getChrono(whichone);
		if (chrono<1) return "";
		return dateFormatter1.format(new Date(chrono));
	}
	
	public void setChrono(int type, long chrono) {
		if (type==CHRONO_START) setStart(chrono);
		else setFinish(chrono);
	}
	
	public String getTimeStr() {
		long mytime = getTime();
		if (mytime<1) return "";
		return dateFormatter2.format(new Date(finish-start));
		
	}
	
	public int getIndex() {
		return index;
	}
	
	public void resetPenalties() {
		for (int i=0; i<SystemParam.GATE_COUNT; i++) {
			pen[i] = -1;
		}
		start = 0;
		finish = 0;
	}

	
	
	public int getBibnumber() {
		return bibnumber;
	}
	
	public String getStringBibnumber() {
		return Utility.digit3(bibnumber);
	}

	// return penalty at specific gateIndex
	public int getPenalty(int gateIndex) {
		if ((gateIndex<0) || (gateIndex>=SystemParam.GATE_COUNT)) return -1;
		return pen[gateIndex];
	}
	
	//return total of penalty
	public int getPenalty() {
		int sum = 0;
		for (int i=0; i<SystemParam.GATE_COUNT; i++)
			if (pen[i]>-1) sum += pen[i];
		return sum;
	}

	public String getPenaltyString() {
		StringBuffer sb = new StringBuffer(SystemParam.GATE_COUNT);
		for (int i=0; i<SystemParam.GATE_COUNT; i++) {
			switch (pen[i]) {
				case 0: sb.append('0'); break;
				case 2: sb.append('2'); break;
				case 50: sb.append('5'); break;
				default: sb.append('-');
			
			}
		}
			
		return sb.toString();
	}

	public void setPenaltyMap(SparseIntArray values) {
		for (int index=0; index<values.size(); index++) {
			int gateIndex = values.keyAt(index);
			int value = values.valueAt(index);
			Log.i("setPenaltyMap", "gateId="+gateIndex+" value="+value);
			setPen(gateIndex, value);
		}
	}
	
	/**
	 * Retuns the penalty values associated to the gates passed as parameters. Retuns
	 * all the penalties if null is passed as parameter
	 * @param gateSet
	 * @return
	 */
	public SparseIntArray getPenaltyMap(Set<Integer> gateSet) {
		SparseIntArray values = new SparseIntArray();
		if (gateSet==null) 
			for (int index=0; index<SystemParam.GATE_COUNT; index++)
				values.put(index, pen[index]);
		else
			for (Integer gateIndex : gateSet) {
				if ((gateIndex>=0) && (gateIndex<pen.length)) {
					values.put(gateIndex, pen[gateIndex]);
				}
			}
		return values;
	}
	
	/**
	 * returns only the gates with penalties >-1
	 * @param gateIndex
	 * @param value
	 */
	public SparseIntArray getPenaltyMap() {
		SparseIntArray values = new SparseIntArray();
		for (int index=0; index<SystemParam.GATE_COUNT; index++)
			if (pen[index]>-1) values.put(index, pen[index]);
		return values;
	}
	// gateId always starts at 1
	public void setPen(int gateIndex, int value) {
		if ((gateIndex<0) || (gateIndex>=SystemParam.GATE_COUNT)) return;
		if (!SystemParam.isPenaltyValid(value)) return;
		pen[gateIndex] = value;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	
	public long getStart() {
		return start;
	}
	
	public void setFinish(long chrono) {
		this.finish = chrono;
	}
	
	public long getFinish() {
		return finish;
	}
	
	public void setStart(long chrono) {
		this.start = chrono;
	}

	
	public boolean allPenaltyEmpty() {
		for (int i=0; i<SystemParam.GATE_COUNT; i++) 
			if (pen[i]>-1) return false;
		return true;
	}
	
	public String penaltyToSMSString() {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<SystemParam.GATE_COUNT; i++) 
			if (pen[i]>-1) sb.append(" "+(i+1)+":"+pen[i]);
			
		// the space between the bib number and the rest is included in the rest
		return "@"+System.currentTimeMillis()+" "+bibnumber+" penalty"+sb.toString();
	}
	
	public String chronoToSMSString(int chronoType) {
		if (chronoType==CHRONO_START) return "@"+System.currentTimeMillis()+" "+bibnumber+" start "+start;
		else return "@"+System.currentTimeMillis()+" "+bibnumber+" finish "+finish;
	}
	
	// return 0 if start or finish not set
	public long getTime() {
		if (start<1 || finish<1) return 0;
		return finish-start;
		
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer(Utility.digit3(bibnumber)+" : "+getPenaltyString());
		if (start>0 || finish>0) {
			buffer.append(" / ");
			if (start>0) buffer.append(dateFormatter1.format(new Date(start)));
			buffer.append(" - ");
			if (finish>0) buffer.append(dateFormatter1.format(new Date(finish)));
			long mytime = getTime();
			if (mytime>0) {
				buffer.append(" = ");
				
				buffer.append(dateFormatter2.format(new Date(finish-start)));
			}
			
		} 
		if (locked) return "¤¤¤ "+buffer.toString();
		return buffer.toString();
	}


	
	/**
	 * Returns true if all the penalties in the map are the same as for this bib
	 * @param values
	 * @return
	 */
	public boolean hasSamePenalties(SparseIntArray values) {
		for (int index=0; index<values.size(); index++) {
			int gateIndex = values.keyAt(index);
			if ((gateIndex<0) || (gateIndex>=pen.length)) return false;
			if (pen[gateIndex]!=values.valueAt(index)) return false;
		}
		return true;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	
	
	
}
