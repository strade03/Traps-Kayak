package com.traps.trapsapp.core;

import java.net.InetSocketAddress;
import java.net.Socket;



import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.traps.trapsapp.network.FFCanoeThread;

public class FFCanoeHelper {
	
	
	private FFCanoeThread ffcClient;
	private static FFCanoeHelper instance;

	
	protected FFCanoeHelper() {	}
		
	public static FFCanoeHelper getInstance() {
		if (instance==null) instance = new FFCanoeHelper();
		return instance;
	}
	  
	
	public boolean isActive() {
		if ((ffcClient!=null) && (ffcClient.isConnected())) return true;
		return false;
	}
	
	
	public boolean addPenalty(int bibnumber, int gateName, int value) {
		if (!isActive()) {
			Log.e("TRAPS", "Trying to send penalty but FFCClient not connected. Ignore");
			return false;
		}
		Log.i("FFCanoeHelper", "Adding to WIFI queue penalty "+value+" for bib number "+bibnumber+" and gate "+gateName);
		ffcClient.addPenalty(bibnumber, gateName, value);
		return true;
	}
	
	public boolean addChrono(int bibnumber, int chrono) {
		if (!isActive()) {
			Log.e("TRAPS", "Trying to send chrono but FFCClient not connected. Ignore");
			return false;
		}
		Log.i("FFCanoeHelper", "Adding to WIFI queue chrono "+chrono+" for bib number "+bibnumber);
		ffcClient.addChrono(bibnumber, chrono);
		return true;
	}
	
	public void connect(Context context, InetSocketAddress address, final int runId, final IConnectedResult cb) {
		
		disconnect(context);
		Log.i("FFCanoehelper", "Trying to connect to FFCanoe");
		(new SocketConnectorTask(context, "Connexion FFCanoe") {
			
			@Override
			protected void onPostExecute(Socket socket) {
				// dismiss progress bar
				super.onPostExecute(socket);
				if (socket!=null) {
					if (socket.isConnected()) {
				
						try {
							ffcClient = new FFCanoeThread(socket, runId);
							cb.connectedResult(0);
							return;
						} catch (Exception e) {}
					
					} else {
						cb.connectedResult(-1);
						return;
					}
				}
				cb.connectedResult(-2);
				return;
				
			}
		}).execute(address);
	
	}
	

	
	public void disconnect(Context context) {
		
		if (isActive()) {
			ffcClient.disconnect();
			Toast.makeText(context, "Déconnexion FFCanoe", Toast.LENGTH_SHORT).show();
		}		
			
		 
	}



}
