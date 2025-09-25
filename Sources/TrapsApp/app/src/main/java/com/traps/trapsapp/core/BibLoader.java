package com.traps.trapsapp.core;

import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.util.Log;

public abstract class BibLoader {
	
	private Context context;
	private boolean wifiWasActivated = false;
	public BibLoader(Context context) {
		this.context = context;
	}
	
	public void load() {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager.isWifiEnabled()) {
			wifiWasActivated = true;
			syncUDP();
		}
		else new AlertDialog.Builder(context)
		.setTitle("Activer le WIFI ?")
		.setMessage("Pour charger la liste des dossards, vous devez activer le WIFI. Activer ?")
		.setNegativeButton("Non", new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				processResult(false);
				
			}
		})
		.setPositiveButton("Oui", new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				turnOnWIFI();
				
			}
		})
		.create()
		.show();
	}
	
	private void turnOnWIFI() {
		(new WIFIActivatingTask(context) {
			@Override
			protected void onPostExecute(Boolean value) {
				super.onPostExecute(value);
				if (value) syncUDP();
					
			}
		}).execute();
	}
	
	private void turnOffWIFI() {
	
		final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager.isWifiEnabled()) {
			new AlertDialog.Builder(context)
			.setTitle("Désactiver le WIFI ?")
			.setMessage("Voulez-vous maintenant désactiver le WIFI ?")
			.setNegativeButton("Non", new OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					processResult(true);
					
				}
			})
			.setPositiveButton("Oui", new OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					wifiManager.setWifiEnabled(false);
					processResult(true);
				}
			})
			.create()
			.show();	
		}
		else processResult(true);
	}
	
	private void syncUDP() {
		
		// first check TRAPSManager is on the network
		(new UDPSyncTask(context) {
			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				// TRAPSManager not found
				if (!result) {
					Utility.alert(context, "TRAPSManager introuvable",
							"Vérifiez que ce terminal est connecté en WIFI sur le même réseau que TRAPSManager. Vérifiez que TRAPSManager est démarré.");
					return;
				}
				// we found TRAPSManager, now load the bibs.
				connectSocket();
			}
		}).execute();
	}
	
	private void connectSocket() {
		// first open the socket
		SharedPreferences pref = context.getSharedPreferences("TRAPS_PREF",0);
		final int port = pref.getInt("port", 8080);
		final String address = pref.getString("address", "UNKNOWN");
		
		(new SocketConnectorTask(context, "Connexion "+address+":"+port) {
			@Override
			protected void onPostExecute(Socket socket) {
				super.onPostExecute(socket);
				// if user aborted the connection, do nothing.
				if (socket==null) {
					Log.e("TRAPSManager", "User aborted the connection");
					return;
				}
				// if not connected, then it is a time out
				if (!socket.isConnected()) {
					Log.e("TRAPSManager", "socket connection time out");
					Utility.alert(context, "Erreur connexion", "Impossible de se connecter à TRAPSManager ("+address+":"+port+")");
					return;
				}
				// we got the socket, now, use it !
				downloadBibs(socket);
			}
		}).execute(new InetSocketAddress(address, port));
	}
	
	private void downloadBibs(Socket socket) {
	
		(new BibListTask(context) {
			@Override
			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);
				if (result>0) {
					Log.i("LoaderTask", "Number of bibs received: "+result);
					if (wifiWasActivated) processResult(true);
					else turnOffWIFI();
				}
				else {
					
					Utility.alert(context, "Erreur", "Erreur pendant le chargement");
					processResult(false);
				}
			}
		}).execute(socket);
	}
	
	public abstract void processResult(boolean success);

}
