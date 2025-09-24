package com.traps.trapsapp.core;

import java.util.ArrayList;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.traps.trapsapp.BiblistActivity;

public class CreateListTask extends AsyncTask<Void, Integer, Integer> implements IProgress {

	private Context context;
	private ProgressDialog progress;
	private int bibCount;
	
	
	public CreateListTask(Context context, int bibCount) {
		this.context = context;
		this.bibCount = bibCount;
	}
	
	@Override 
	protected void onPreExecute() {
		progress = new ProgressDialog(context);
		progress.setMessage("Creation des dossards...");
		progress.setIndeterminate(false);
		progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progress.setCancelable(true);
		progress.setMax(bibCount);
		progress.show();
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		progress.setProgress(values[0]);
	}
	
	@Override
	protected Integer doInBackground(Void... params) {
		Log.i("CreateListTask", "doInBackground");
		TrapsDB db = TrapsDB.getInstance();
		db.clearBibs();
		ArrayList<Bib> bibList = new ArrayList<Bib>();
		int n = bibCount;
		if (n>500) n = 500;
		if (n<1) n = 0;
		for (int i=1; i<=n; i++) bibList.add(new Bib(i, i));
		db.createBibList(bibList, this);
		return 0;
	}

	@Override
	protected void onPostExecute(Integer result) {
		progress.dismiss();
		BiblistActivity.getInstance().reloadBibsFromDB();
		
	}

	public void setProgress(int value) {
		if (value%10==0) publishProgress(value);
		
	}
	
}
