package com.traps.trapsapp.core;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class IPAddressHelper {
	
	EditText[] edit;
	
	public IPAddressHelper(EditText... edit) {
		this.edit = edit;
		for (int i=0; i<edit.length-1; i++) 
			edit[i].addTextChangedListener(new IPTextWatcher(edit[i+1])); 
	}
	 
	public void setAddress(String address) {
		String[] tab = address.split(".");
		for (int i=0; i<tab.length; i++) 
			if (i<edit.length) edit[i].setText(tab[i]);
		
		
	}
	
	public void clear() {
		for (int i=0; i<edit.length; i++) edit[i].setText("");
	}
	
	public String getEdit(int i) {
		if (i<edit.length) return edit[i].getText().toString();
		return null;
		
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer(edit[0].getText());
		for (int i=1; i<edit.length; i++) sb.append("."+edit[i].getText());
		return sb.toString();
	}


	private class IPTextWatcher implements TextWatcher {

		private EditText nextEdit;
		
		public IPTextWatcher(EditText nextEdit) {
			this.nextEdit = nextEdit;
		}
		
		public void afterTextChanged(Editable arg0) {
			//Log.d("IPHelper", "Text has changed");
			if (arg0.length()<3) return;
			nextEdit.requestFocus();
			
		}

		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// TODO Auto-generated method stub
			
		}

		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// TODO Auto-generated method stub
			
		}
		
	}
	

}
