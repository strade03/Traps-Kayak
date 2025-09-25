package com.traps.trapsapp;

import java.net.InetSocketAddress;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.traps.trapsapp.core.CompetFFCKHelper;
import com.traps.trapsapp.core.IConnectedResult;
import com.traps.trapsapp.core.IPAddressHelper;
import com.traps.trapsapp.core.Utility;

public class CompetFFCKConnectActivity extends AppCompatActivity implements OnClickListener, IConnectedResult {
 
	private Button connectButton;
	private EditText portField;
	private RadioButton radioField1;
	private RadioButton radioField2;
	
	private CheckBox forwardPenalty;
	private CheckBox forwardChrono;
	 
	private IPAddressHelper ipAddressHelper;
	
	private final static String KEY_ADDRESS0 = "address0";
	private final static String KEY_ADDRESS1 = "address1";
	private final static String KEY_ADDRESS2 = "address2";
	private final static String KEY_ADDRESS3 = "address3";
	private final static String KEY_PORT = "port";
	private final static String KEY_RUN1 = "run";
	private final static String KEY_FORWARD_PENALTY = "forwardpenalty";
	private final static String KEY_FORWARD_CHRONO = "forwardchrono";

	private SharedPreferences settings;
	
	private Context context;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.competffck_connect_layout);
        context = this;
        settings = getSharedPreferences("SETTINGS_COMPETFFCK", MODE_PRIVATE);
    	
        connectButton = (Button)findViewById(R.id.buttonConnect);
        connectButton.setOnClickListener(this);
        
        final EditText addressField0 = (EditText)findViewById(R.id.ffanoeAddress1);
        addressField0.setText(settings.getString(KEY_ADDRESS0, "192"));
        
        final EditText addressField1 = (EditText)findViewById(R.id.competffckAddress2);
        addressField1.setText(settings.getString(KEY_ADDRESS1, "168"));

        final EditText addressField2 = (EditText)findViewById(R.id.competffckAddress3);
        addressField2.setText(settings.getString(KEY_ADDRESS2, "1"));

        final EditText addressField3 = (EditText)findViewById(R.id.competffckAddress4);
        addressField3.setText(settings.getString(KEY_ADDRESS3, "100"));
        
        forwardPenalty = (CheckBox)findViewById(R.id.forwardPenalty);
        forwardPenalty.setChecked(settings.getBoolean(KEY_FORWARD_PENALTY, true));
        
        forwardChrono = (CheckBox)findViewById(R.id.forwardChrono);
        forwardChrono.setChecked(settings.getBoolean(KEY_FORWARD_CHRONO, false));

        ipAddressHelper = new IPAddressHelper(addressField0, addressField1, addressField2, addressField3);
        
        portField = (EditText)findViewById(R.id.editPortCompetFFCK);
        portField.setText(Integer.toString(settings.getInt(KEY_PORT, 7012)));
        radioField1 = (RadioButton)findViewById(R.id.radioRun1);
        radioField2 = (RadioButton)findViewById(R.id.radioRun2);
        radioField1.setChecked(settings.getBoolean(KEY_RUN1, true));
        radioField2.setChecked(!settings.getBoolean(KEY_RUN1, true));
        
        Button deleteButton = (Button)findViewById(R.id.buttonDeleteCompetFFCKIP);
        deleteButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				
				new AlertDialog.Builder(context)
				.setTitle("Vider l'adresse IP ?")
				.setPositiveButton("OK", 
						new DialogInterface.OnClickListener() {
		    	      		public void onClick(DialogInterface dialog, int which) {
		    	      			ipAddressHelper.clear(); 
		    	      			addressField0.requestFocus();
		    	      		} 		
		    	      	}
				)
				.setNegativeButton("Annuler", null)
				.create()
				.show();
		    	
			}
		});
        
	}

	public void onClick(View v) {
		
		String address = ipAddressHelper.toString();
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(KEY_ADDRESS0, ipAddressHelper.getEdit(0));
		editor.putString(KEY_ADDRESS1, ipAddressHelper.getEdit(1));
		editor.putString(KEY_ADDRESS2, ipAddressHelper.getEdit(2));
		editor.putString(KEY_ADDRESS3, ipAddressHelper.getEdit(3));
		
		editor.putBoolean(KEY_FORWARD_CHRONO, forwardChrono.isChecked());
		editor.putBoolean(KEY_FORWARD_PENALTY, forwardPenalty.isChecked());
		
		int port = Integer.parseInt(portField.getText().toString());
		if (port>0) editor.putInt(KEY_PORT, port);
		
		boolean run1 = true;
		if (!radioField1.isChecked()) run1 = false;
		editor.putBoolean(KEY_RUN1, run1);
		Log.i("CompetFFCK", "address="+address);
		Log.i("CompetFFCK", "port="+port);
		Log.i("CompetFFCK", "runId="+run1);		
		editor.commit();
		
		// now try to connect
		int runId = 1;
		if (!run1) runId = 2;
		// context, address, port, runId, callback (connectedResult);
		CompetFFCKHelper.getInstance().connect(this, new InetSocketAddress(address, port), runId, this);
		
	}

	public void connectedResult(int value) {
		if (value==0) {
			Toast.makeText(this, "Connecte a CompteFFCK", Toast.LENGTH_LONG).show();
			Intent output = new Intent();
			output.putExtra("forwardPenalty", forwardPenalty.isChecked());
			output.putExtra("forwardChrono", forwardChrono.isChecked());
			setResult(RESULT_OK, output);
			finish();
		}
		else if (value==-1) {
			Utility.alert(this, "Erreur de connexion", "Impossible de se connecter a CompetFFCK");
		}
		
		
	}


	
	
}
