package com.traps.trapsapp.core;

import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

public class SocketConnectorTask extends AsyncTask<InetSocketAddress, Integer, Socket> {

	private Context context;
	private ProgressDialog progressDialog;
	private boolean stop = false;
	private String title = "";
	
	public SocketConnectorTask(Context context, String title) {
		this.context = context;
		this.title = title;

	}
	
	@Override 
	protected void onPreExecute() {
		
		progressDialog = new ProgressDialog(context, ProgressDialog.STYLE_SPINNER);
		progressDialog.setTitle(title);
		progressDialog.setMessage("Ouverture connexion..."); 
		progressDialog.setCancelable(false);
		progressDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "Annuler", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				stop = true;
				
			}
		}); 
		progressDialog.show();
	}
	
	@Override
	protected Socket doInBackground(InetSocketAddress... params) {
		Log.i("SocketConnector", "doInBackground");
		Socket socket = new Socket();
		try {
			socket.setSoTimeout(10000);
			Log.i("SocketConnector", "Opening socket...");
			socket.connect(params[0], 7000);
			Log.i("SocketConnector", "Socket opened");
		} catch (Exception e) {
			Log.e("SocketConnector", "Cannot open socket (time out ?)");
		}
		if (stop) {
			Log.i("SocketConnector", "Interrupted by user");
			return null;
		}
		
		return socket;
	}
	
	@Override
	protected void onPostExecute(Socket socket) {
		Log.i("SocketConnectorTask", "onPostExecute");
		progressDialog.dismiss();
		
	}


}
