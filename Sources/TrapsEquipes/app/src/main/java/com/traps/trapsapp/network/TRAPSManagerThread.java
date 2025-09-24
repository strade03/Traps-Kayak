package com.traps.trapsapp.network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class TRAPSManagerThread extends Thread {

	private LinkedBlockingQueue<TRAPSPacket> outputQ = new LinkedBlockingQueue<TRAPSPacket>();
	private InetSocketAddress manualAddress = null;
	private Context context;
	
	public TRAPSManagerThread() {
		
	}
	
	public void setAddress(Context context, InetSocketAddress manualAddress) {
		this.context = context;
		this.manualAddress = manualAddress;
	}
	
	public void clearQueue() {
		outputQ.clear();
	}
	
	public void stopThread() {
		clearQueue();
		// adding a void packet will make the thread stop.
		outputQ.add(new TRAPSPacket());
	}
	
	public void addPacket(TRAPSPacket packet) {
		outputQ.add(packet);
	}
	
	public void run() {
		
		Log.i("TRAPSManager", "Starting thread...");
		while (true) {
			try {
				TRAPSPacket packet = outputQ.take();
				// if packet is not valid, then empty queue
				if (!packet.isValid()) {
					outputQ.clear();
					Log.i("TRAPSManager", "Stopping thread !");
					break;
				}
				try {
					InetSocketAddress address = manualAddress;
					if (address==null) {
						SharedPreferences pref = context.getSharedPreferences("TRAPS_PREF",0);
						int port = pref.getInt("port", 8080);
						String addr = pref.getString("address", "192.168.1.100");
						address = new InetSocketAddress(addr, port);
					}
					Log.i("TRAPSManager", "Trying to send penalties for bib "+packet.getBibnumber()+" to "+address.getHostName()+":"+address.getPort());
					Socket socket = new Socket(); 
					socket.setSoTimeout(10000);
					socket.connect(address, 6000);  // wait for 5 sec max
					
					DataOutputStream output = new DataOutputStream(socket.getOutputStream());
			        output.writeBytes(packet.getJsonObject().toString());
			        output.writeByte(4);  // EOT
			        output.flush(); 
						
			        // now read answer
						
					BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					StringBuilder builder = new StringBuilder();
				    String line;
				    while ((line = input.readLine())!=null) {
				    	if (line=="EOT") break;
				    	builder.append(line);
				    }
				    
				    int response = -1;
				    JSONObject jsonObject = new JSONObject(builder.toString());
				    System.out.println(jsonObject.toString(4));
				    response = jsonObject.getInt("response");
					socket.close();
					if (response!=0) {
						Log.e("TRAPSManager", "Server did not respond 0. Put it back in the queue");
						outputQ.put(packet);
						sleep(5000); // wait for 5 sec before reading Q again
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("TRAPSManager", "Error while sending over network. Put it back in the queue");
					outputQ.put(packet);
					sleep(5000); // wait for 5 sec before reading Q again
				}
				
				
				
			} catch (InterruptedException e) {
				
				Log.e("TRAPSManager", "INTERRUPTED!");
			}
			
			
		}
		Log.i("TRAPSManager", "Reaching end of run method");
	}
	

}
