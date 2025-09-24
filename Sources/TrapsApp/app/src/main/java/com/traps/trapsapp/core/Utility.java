package com.traps.trapsapp.core;


import android.app.AlertDialog;
import android.content.Context;

import com.traps.trapsapp.R;


public class Utility {

	public static String digit3(int value) {
		 if (value>99) return ""+value;
		 if (value>9) return "0"+value;
		 return "00"+value;
	 }
	
	public static String digit2(int value) {
		 if (value>9) return ""+value;
		 return "0"+value;
	 }
	
//	public static String toChronoStr(int value) {
//		int min = value / 60000;
//		int sec = (value % 60000) / 1000;
//		int sec100 = (value - min * 60000 - sec * 1000) / 10;
//		return min + digit2(sec) + digit2(sec100);
//	}
//	
//	public static int toChronoInt(String str) {
//		if (str.length()==0) return 0;
//		if (str.length()>6) str = str.substring(str.length()-6, str.length());
//		if (str.length()<5) return Integer.parseInt(str)*10;
//		String minStr = str.substring(0, str.length()-4);
//		String secStr = str.substring(str.length()-4, str.length());
//		return Integer.parseInt(minStr)*60000+Integer.parseInt(secStr)*10;
//	}
//	
//	public static String toChronoParse(String str) {
//		if (str.length()>6) str = str.substring(str.length()-6, str.length());
//		String value = "";
//		for (int i=str.length()-1; i>-1; i--) {
//			if (value.length()==2) value = "." + value;
//			else if (value.length()==5) value = ":" + value;
//			value = str.charAt(i)+value;
//		}
//		return value;
//	}
	
	public static void alert(Context context, int title, int message) {
		new AlertDialog.Builder(context)
		.setTitle(title)
		.setMessage(context.getResources().getString(message))
		.setNeutralButton(context.getResources().getString(R.string.OK),null)
		.create()
		.show();
	}
	
	public static void alert(Context context, String title, String message) {
		new AlertDialog.Builder(context)
		.setTitle(title)
		.setMessage(message)
		.setNeutralButton("OK",null)
		.create()
		.show();
	}

}
