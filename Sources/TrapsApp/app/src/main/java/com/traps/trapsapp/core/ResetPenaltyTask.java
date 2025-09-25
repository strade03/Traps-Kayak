package com.traps.trapsapp.core;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.traps.trapsapp.BiblistActivity;

public class ResetPenaltyTask extends AsyncTask<Void, Integer, Integer> implements IProgress {

	private Context context;
	private ProgressDialog progress;
	private int bibCount = 1;
	
	public ResetPenaltyTask(Context context) {
		this.context = context;
		
	}
	
	@Override 
	protected void onPreExecute() {

		progress = new ProgressDialog(context);
		progress.setMessage("Effacer les données ?");
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
	protected Integer doInBackground(Void... params) {
		Log.i("ResetPenaltyTask", "doInBackground");
		TrapsDB db = TrapsDB.getInstance();
		ArrayList<Bib> bibList = db.getBibList();
		bibCount = bibList.size();
		db.clearBibs();
		db.createBibList(bibList, this);
		return 0;
	}

	@Override
	protected void onPostExecute(Integer result) {
		
		
		progress.dismiss();
		BiblistActivity.getInstance().reloadBibsFromDB();
	
	}

	public void setProgress(int value) {
		if (value%10==0) publishProgress((value*100)/bibCount);
		
	}
	
}
