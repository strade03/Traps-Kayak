package com.traps.trapsapp.core;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class BibListTask extends AsyncTask<Socket, Integer, Integer> implements IProgress {

	private Context context;
	private ProgressDialog progress;
	private int bibCount;
	
	public BibListTask(Context context) {
		this.context = context;
	}
	 
	 
	@Override 
	protected void onPreExecute() {
		
		progress = new ProgressDialog(context);
		progress.setMessage("Chargement des dossards");
		progress.setIndeterminate(false);
		progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progress.setCancelable(true);
		progress.setMax(100);
		progress.show();
	
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		progress.setProgress(values[0]);
	}
	
	@Override
	protected Integer doInBackground(Socket... params) {
		Log.i("LoaderTask", "doInBackground");
		Socket socket = params[0];
		ArrayList<Bib> bibList = new ArrayList<Bib>();
		try {
			
			// make request
			JSONObject request = new JSONObject();
			request.put("command", 0);
			
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            output.writeBytes(request.toString());
            output.writeByte(4); // EOT
            output.flush(); 
			
            // now read answer
			
			BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			StringBuilder builder = new StringBuilder();
		    String line;
		    while ((line = input.readLine())!=null) {
		    	if (line=="EOT") break;
		    	builder.append(line);
		    }
            

	        try {
				JSONObject jsonObject = new JSONObject(builder.toString());
				long epoch = jsonObject.getLong("epoch");
				SystemParam.timeshift = epoch-System.currentTimeMillis();
				JSONArray jsonArray = jsonObject.getJSONArray("bibList");
				bibCount = jsonArray.length();
				for (int index=0; index<bibCount; index++) {
					int bibnumber = jsonArray.getInt(index);
					bibList.add(new Bib(bibnumber, index));
				}
				
				
			} catch (JSONException e1) {
				e1.printStackTrace();
			
			}
            
			
			socket.close();
		} catch (Exception e) {				
			e.printStackTrace();
		}
		
		if (!bibList.isEmpty()) {
			TrapsDB db = TrapsDB.getInstance();
			db.clearBibs();
			db.createBibList(bibList, this);
		}
		
		return bibList.size();
	}

	@Override
	protected void onPostExecute(Integer result) {
		Log.i("LoaderTask", "onPostExecute");
		progress.dismiss();
				
	}


	public void setProgress(int value) {
		if (value%10==0) publishProgress((value*100)/bibCount);
		
	}


}
