package com.traps.trapsapp.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class UDPListener extends Thread {
	
	private static UDPListener instance = null;
	
	private DatagramSocket  datagramSocket = null;
	private Context context;
	private long timestamp = 0;
	private Semaphore semaphore;
	
	public UDPListener(Context context) {
		this.context = context;
		SharedPreferences pref = context.getSharedPreferences("TRAPS_PREF",0);
		int port = pref.getInt("udpport", 5432);
		try {
			datagramSocket = new DatagramSocket(port);
			start();
			instance = this;
		} catch (SocketException e) {
			Log.i("UDP", "It looks like the UDP listener is already started");
		}
		semaphore = new Semaphore(0);
	}
	
	public static UDPListener getInstance() {
		return instance;
	}
	
	public boolean waitforUpdate() {
		boolean updated = false;
		try {
			updated = semaphore.tryAcquire(6, TimeUnit.SECONDS);
		} catch (InterruptedException e) {}
		return updated;
	}
	
	public void run() {
		
		if (datagramSocket==null) return;
		
		Log.i("UDPListener","Starting UDP server...");
		byte[] data = new byte[1024];
		
		while(true) {
			try {
				DatagramPacket packet = new DatagramPacket(data,data.length);
				datagramSocket.receive(packet);
				String dataString = new String(packet.getData(),0,packet.getLength());
				//Log.d("UDP", "got packet:"+dataString);
		        String[] array = dataString.split(",");
		        if (array.length<3) {
		        	Log.e("UDP", "Data packet corrupted");
		        	return;
		        }
		        long newTimestamp = Long.parseLong(array[0]);
		        // if no change, just return
		        if (newTimestamp>timestamp) {
			        timestamp = newTimestamp;
			        String address = array[1];
			        int port = Integer.parseInt(array[2]);
			        Log.i("UDP", "Address="+address+":"+port);
			        SharedPreferences pref = context.getSharedPreferences("TRAPS_PREF",0);
					SharedPreferences.Editor editor = pref.edit();
					editor.putString("address", address);
					editor.putInt("port", port);
					editor.commit();
		        }
		        // release thread waiting for update
		        if (semaphore.hasQueuedThreads()) semaphore.release();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			
			}
           
        }
	}
	
}
