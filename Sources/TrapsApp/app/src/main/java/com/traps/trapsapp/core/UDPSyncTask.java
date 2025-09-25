package com.traps.trapsapp.core;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.traps.trapsapp.network.UDPListener;

public class UDPSyncTask extends AsyncTask<Void, Integer, Boolean> {

	
	private Context context;
	private ProgressDialog progressDialog;
	
	public UDPSyncTask(Context context) {
		this.context = context;
		

	}
	
	@Override 
	protected void onPreExecute() {
		
		progressDialog = new ProgressDialog(context, ProgressDialog.STYLE_SPINNER);
		progressDialog.setTitle("Recherche TRAPSManager");
		progressDialog.setMessage("Recherche de TRAPSManager sur le réseau..."); 
		progressDialog.setCancelable(false);
		progressDialog.show();
	}

	
	@Override
	protected Boolean doInBackground(Void... params) {
		return UDPListener.getInstance().waitforUpdate();
		
	}

	@Override
	protected void onPostExecute(Boolean value) {
	
		progressDialog.dismiss();
		
	}


}
