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

// pour la gestion droits SMS
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;

import com.traps.trapsapp.core.CompetFFCKHelper;
import com.traps.trapsapp.core.IConnectedResult;
import com.traps.trapsapp.core.IPAddressHelper;
import com.traps.trapsapp.core.Utility;

public class CompetFFCKConnectActivity extends AppCompatActivity implements OnClickListener, IConnectedResult {
 
	private Button connectButton;
	private EditText portField;
	
	private CheckBox forwardPenalty;
	private CheckBox forwardChrono;
	 
	private IPAddressHelper ipAddressHelper;
	
	private final static String KEY_ADDRESS0 = "address0";
	private final static String KEY_ADDRESS1 = "address1";
	private final static String KEY_ADDRESS2 = "address2";
	private final static String KEY_ADDRESS3 = "address3";
	private final static String KEY_PORT = "port";
	private final static String KEY_FORWARD_PENALTY = "forwardpenalty";
	private final static String KEY_FORWARD_CHRONO = "forwardchrono";

	private static final int REQUEST_SMS_PERMISSION = 1001;

	private SharedPreferences settings;
	
	private Context context;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.competffck_connect_layout);
		// Vérifie les permissions SMS dès l'ouverture
		if (!checkAndRequestSmsPermission()) {
			// On ne bloque pas, mais on affiche un toast pour informer
			Toast.makeText(this, "Les permissions SMS sont nécessaires pour recevoir les données des autres terminaux.", Toast.LENGTH_LONG).show();
		}

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

	private boolean checkAndRequestSmsPermission() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
				!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.SEND_SMS},
					REQUEST_SMS_PERMISSION);
			return false;
		}
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_SMS_PERMISSION) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Toast.makeText(this, "Permission SMS accordée.", Toast.LENGTH_SHORT).show();
			} else {
				if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
					Toast.makeText(this, "Sans permission SMS, vous ne recevrez pas les pénalités des autres terminaux.", Toast.LENGTH_LONG).show();
				} else {
					// L'utilisateur a coché "Ne plus demander"
					Toast.makeText(this, "Permission SMS bloquée. Réactivez-la dans les paramètres de l'appli.", Toast.LENGTH_LONG).show();
				}
			}
		}
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
		int runId = 1;
		Log.i("CompetFFCK", "address="+address);
		Log.i("CompetFFCK", "port="+port);	
		editor.commit();
		
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
