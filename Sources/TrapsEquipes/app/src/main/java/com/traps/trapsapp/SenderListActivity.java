package com.traps.trapsequipes;

import java.util.ArrayList;


import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.traps.trapsequipes.core.SimpleInputDialog;
import com.traps.trapsequipes.core.TrapsDB;

public class SenderListActivity extends ListActivity implements DialogInterface.OnClickListener {

	private TrapsDB db;

	private ArrayAdapter<String> adapter;
	private ArrayList<String> numberList = new ArrayList<String>();
	private SimpleInputDialog addSenderDialog;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        db = TrapsDB.getInstance();
        
        createListFromDB(); 
        
        addSenderDialog = new SimpleInputDialog(this);
		addSenderDialog.setTitle(R.string.SenderListActivity_addDAddress);
		addSenderDialog.setButton(DialogInterface.BUTTON_POSITIVE, getTString(R.string.OK), this);
		addSenderDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getTString(R.string.cancel), this);

        
	}
	
	private String getTString(int id) {
		return getResources().getString(id);
	}
	 
	
	public void createListFromDB() {
		
		numberList = db.getAuthorize();
		ListView listView = getListView();
		String[] numberArray;
		
		if (numberList.isEmpty()) {
			numberArray = new String[1];
			numberArray[0] = getTString(R.string.SenderListActivity_no_device_specified);
			adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, numberArray);	        
		    setListAdapter(adapter);  
	    	listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
		}
		else {
			numberArray = new String[numberList.size()];
			for (int index = 0; index<numberList.size(); index++) {
				numberArray[index] = numberList.get(index);
			}
			adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, numberArray);	        
		    setListAdapter(adapter);  
	   	 	listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			
		}

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.senderlist_menu, menu);
		return true;
	}
		

	private void addSender() {
		
		addSenderDialog.setValue("");
		addSenderDialog.show();
	}
		
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		if (id == R.id.add_sender) {
			addSender();
			return true;
		} else if (id == R.id.delete_sender) {
			deleteSender();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	 
	 
	private void deleteSender() {
		
		ListView listView = getListView();
		ArrayList<String> newList = new ArrayList<String>();
		SparseBooleanArray array = listView.getCheckedItemPositions();
		for (int i=0; i<numberList.size(); i++) 
			if (!array.get(i)) newList.add(numberList.get(i)); 
		db.setAuthorize(newList);
		createListFromDB();

	}
	 
	public void onBackPressed() {
		finish();
	}


	public void onClick(DialogInterface dialog, int which) {
		
		if (
			(dialog==addSenderDialog) 
			&& (which==DialogInterface.BUTTON_POSITIVE)
		) {
			String value = addSenderDialog.getValue();
			if (value.trim().length()==0) return;
			numberList.add(addSenderDialog.getValue());
			db.setAuthorize(numberList);
			createListFromDB();

		}
		getWindow().setSoftInputMode(
			WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
		);
	}
	 
	
}
