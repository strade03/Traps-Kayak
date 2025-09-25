package com.traps.trapsapp.core;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

public class WIFIActivatingTask extends AsyncTask<Void, Void, Boolean> {

	private Context context;
	private ProgressDialog progressDialog;
	
	public WIFIActivatingTask(Context context) {
		this.context = context;
	}
	
	
	@Override 
	protected void onPreExecute() {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(true);
		progressDialog = new ProgressDialog(context, ProgressDialog.STYLE_SPINNER);
		progressDialog.setTitle("Activation WIFI");
		progressDialog.setMessage("Connexion à un réseau WIFI..."); 
		progressDialog.setCancelable(true);
		progressDialog.show();

	}

	
	@Override
	protected Boolean doInBackground(Void... params) {
		
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		
		int loop=0;
		// loop for 30 seconds (15 times)
		while (loop<16) {
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			if (wifiInfo!=null) { 
				//System.out.println("IPAddress="+wifiInfo.getIpAddress());
				if (wifiInfo.getIpAddress()!=0) return true;
			}
			loop++;
			try {
				Thread.sleep(2000, 0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;

		
	}

	@Override
	protected void onPostExecute(Boolean value) {
	
		progressDialog.dismiss();
		
	}

}
