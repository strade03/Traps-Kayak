package com.traps.trapsapp.core;

import java.util.ArrayList;



import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.traps.trapsapp.R;


public class BiblistAdapter extends BaseAdapter {
 
	private Activity activity;
	private int layoutId;
	private ArrayList<Bib> bibList;
	
	public BiblistAdapter(Activity activity, int layoutId, ArrayList<Bib> bibList) {
		this.activity = activity;
		this.layoutId = layoutId;
		this.bibList = bibList;
	}
	
	@Override
	public int getCount() {
		return bibList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return arg0;

		
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if (view == null) {
			view = LayoutInflater.from(activity).inflate(layoutId, parent, false);
		}
		Bib bib = bibList.get(position);
		
		TextView bibnumber = (TextView) view.findViewById(R.id.bibnumberView);
		bibnumber.setText("   "+bib.getStringBibnumber()+"   ");
		TextView penaltyString = (TextView) view.findViewById(R.id.penaltyView);
		penaltyString.setText(bib.getPenaltyString());
		//penaltyString.setText("000000000000000000000000000000");
		String summary = "  "+bib.getTimeStr();
		if (!bib.allPenaltyEmpty()) summary += " + "+bib.getPenalty();
		TextView summaryView = (TextView) view.findViewById(R.id.summaryView);
		summaryView.setText(summary);
		ImageView lock = (ImageView)view.findViewById(R.id.lockView);
		ImageView check = (ImageView)view.findViewById(R.id.checkboxView);
		if (bib.isChecked()) check.setVisibility(View.VISIBLE);
		else check.setVisibility(View.INVISIBLE);
		if (bib.isLocked()) lock.setVisibility(View.VISIBLE);
		else lock.setVisibility(View.INVISIBLE);
		TextView chronoView = (TextView) view.findViewById(R.id.chronoView);
		if (bib.getChrono(0)>0 || bib.getChrono(1)>0) {
			chronoView.setText(bib.getChronoStr(0)+" | "+bib.getChronoStr(1));
			chronoView.setVisibility(View.VISIBLE);
		}
		else chronoView.setVisibility(View.GONE);
		
		
		return view;
	}

}
