package com.traps.trapsapp.core;


import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.traps.trapsapp.R;

public class SimpleInputDialog extends AlertDialog {

	private EditText editText;
	
	public SimpleInputDialog(Context context) {
		super(context);	       
		
		LayoutInflater factory = LayoutInflater.from(context);
		final View dialogView = factory.inflate(R.layout.digitfield_layout, null);
		
		setView(dialogView);
		editText = (EditText)dialogView.findViewById(R.id.digit_editText);          
	}
	
	public void setValue(String value) {
		editText.setText(value);
	}

	public String getValue() {
		return editText.getText().toString();
	}

}
