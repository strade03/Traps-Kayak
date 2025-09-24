package com.traps.trapsapp;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;

import com.traps.trapsapp.core.IPAddressHelper;
import com.traps.trapsapp.core.Utility;
import com.traps.trapsapp.core.WIFIActivatingTask;

public class TerminalConfigActivity extends AppCompatActivity { // Removed OnClickListener here as we set listeners individually

	public final static String KEY_PORT = "port";
	public final static String KEY_IP_ADDRESS = "lanAddress";
	public final static String KEY_SMS_ADDRESS = "smsAddress";
	public final static String KEY_SMS_ENABLED = "smsEnabled";
	public final static String KEY_AUTODETECT = "autodetect";
	public final static String KEY_TRANSFER_ENABLED = "transferEnabled";
	
	public final static String MODE_NOTRANSFER = "modeNoTransfer";
	public final static String MODE_SMS = "modeSMS";
	public final static String MODE_LAN = "modeLAN";
	public final static String MODE = "mode";

    // New key for storing penalty layout mode
    public final static String KEY_PENALTY_LAYOUT_MODE = "penaltyLayoutMode";
    public final static String LAYOUT_MODE_SLALOM = "slalom";
    public final static String LAYOUT_MODE_KCROSS = "kcross";
	

	private final static String KEY_ADDRESS0 = "address0";
	private final static String KEY_ADDRESS1 = "address1";
	private final static String KEY_ADDRESS2 = "address2";
	private final static String KEY_ADDRESS3 = "address3";
	
	// private Button okButton; // This button is commented out in layout
	private Button kCrossButton;
	private Button slalomButton;

	private EditText portField;
	private EditText smsAddress;
	
	private boolean smsEnabled = true;
	private boolean transferEnabled = false;
	private boolean autoDetect = true;
	
	private ViewGroup layoutLAN;
	private ViewGroup layoutSMS;
	private RadioButton radioLAN;
	private RadioButton radioSMS;
	private CheckBox checkbox;
	private CheckBox autoDetectCheckbox;
	private boolean chronoConfig;
	
	private IPAddressHelper ipAddressHelper;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.transfer_layout);
        
        chronoConfig = getIntent().getExtras().getBoolean("chrono", false);
      
        if (!chronoConfig) setTitle("Configuration envoi pénalités");
        else setTitle("Configuration envoi chronos");
        
        
        SharedPreferences settings = getSharedPreferences("SETTINGS_TRANSFER", MODE_PRIVATE);
        transferEnabled = settings.getBoolean(KEY_TRANSFER_ENABLED, false);
        smsEnabled = settings.getBoolean(KEY_SMS_ENABLED, true);
        autoDetect = settings.getBoolean(KEY_AUTODETECT, true);
        
        portField = (EditText)findViewById(R.id.editPortTRAPSManager);
        portField.setText(Integer.toString(settings.getInt(KEY_PORT, 8080)));
        
        final EditText addressField0 = (EditText)findViewById(R.id.transferAddress1);
        addressField0.setText(settings.getString(KEY_ADDRESS0, "192"));
        final EditText addressField1 = (EditText)findViewById(R.id.transferAddress2);
        addressField1.setText(settings.getString(KEY_ADDRESS1, "168"));
        final EditText addressField2 = (EditText)findViewById(R.id.transferAddress3);
        addressField2.setText(settings.getString(KEY_ADDRESS2, "1"));
        final EditText addressField3 = (EditText)findViewById(R.id.transferAddress4);
        addressField3.setText(settings.getString(KEY_ADDRESS3, "100"));
        ipAddressHelper = new IPAddressHelper(addressField0, addressField1, addressField2, addressField3);
        
        smsAddress = (EditText)findViewById(R.id.transferAddressParam);
        smsAddress.setText(settings.getString(KEY_SMS_ADDRESS, ""));
        layoutLAN = (ViewGroup)findViewById(R.id.transferLayoutLAN);
        layoutSMS = (ViewGroup)findViewById(R.id.transferLayoutSMS);
        radioLAN = (RadioButton)findViewById(R.id.transferRadioButton1);
        radioSMS = (RadioButton)findViewById(R.id.transferRadioButton2);
        checkbox = (CheckBox)findViewById(R.id.transferCheckBox);
        if (!chronoConfig) checkbox.setText("Envoyer les pénalités");
        else checkbox.setText("Envoyer les chronos");
        autoDetectCheckbox = (CheckBox)findViewById(R.id.detectCheckBox);
        
        // okButton = (Button)findViewById(R.id.transferOKButton); // Commented out
        // okButton.setOnClickListener(this);
        
        kCrossButton = (Button) findViewById(R.id.kCrossButton);
        slalomButton = (Button) findViewById(R.id.slalomButton);

        kCrossButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettingsAndProceed(LAYOUT_MODE_KCROSS);
            }
        });

        slalomButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettingsAndProceed(LAYOUT_MODE_SLALOM);
            }
        });
        
        processCheckChanged();
         
        checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				transferEnabled = arg1;
				processCheckChanged();
					
			}
		});
        
        
        radioLAN.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				smsEnabled = !arg1;
				processCheckChanged();
			}
				
		});
        
        radioSMS.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				smsEnabled = arg1;
				processCheckChanged();
			}
				
		});
        
        autoDetectCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				autoDetect = isChecked;
				processCheckChanged();
				
			}
		});
		
	}

	private void processCheckChanged() {
        if (!transferEnabled) {
        	checkbox.setChecked(false);
        	radioLAN.setEnabled(false);
        	radioSMS.setEnabled(false);
        	radioSMS.setChecked(false);
        	radioLAN.setChecked(false);
        	autoDetectCheckbox.setVisibility(View.GONE);
        	layoutLAN.setVisibility(View.GONE);
        	layoutSMS.setVisibility(View.GONE);
        	
        } else {
        	checkbox.setChecked(true);
        	radioLAN.setEnabled(true);
        	radioSMS.setEnabled(true);
        	if (smsEnabled) {
        		radioSMS.setChecked(true);
        		radioLAN.setChecked(false);
        		layoutLAN.setVisibility(View.GONE);
            	layoutSMS.setVisibility(View.VISIBLE);
            	autoDetectCheckbox.setVisibility(View.GONE);
            	
        	}
        	else {
        		radioSMS.setChecked(false);
        		radioLAN.setChecked(true);
        		autoDetectCheckbox.setVisibility(View.VISIBLE);
        		if (autoDetect) {
        			layoutLAN.setVisibility(View.GONE);
        			autoDetectCheckbox.setChecked(true);
        		}
        		else {
        			layoutLAN.setVisibility(View.VISIBLE);
        			autoDetectCheckbox.setChecked(false);
        		}
        		
            	layoutSMS.setVisibility(View.GONE);
        	}
        }
	}
	
	private void saveSettingsAndProceed(String penaltyLayoutMode) {
		
		int port = 0;
		//check first
		if (transferEnabled && smsEnabled) {
			String smsDestination = smsAddress.getText().toString().trim();
			if (smsDestination.equals("")) {
				Utility.alert(this, "Mauvais numéro SMS", "Vous devez entrer un numéro de téléphone");
				return;
			}	
		}
		else if (transferEnabled) {  // LAN is enabled
			port = Integer.parseInt(portField.getText().toString());
		}
		SharedPreferences pref = getSharedPreferences("SETTINGS_TRANSFER",MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		
        // Save the chosen penalty layout mode
        if (!chronoConfig) { // Only save for penalty mode, not chrono mode
             editor.putString(KEY_PENALTY_LAYOUT_MODE, penaltyLayoutMode);
        }

		if (transferEnabled) {
			editor.putBoolean(KEY_TRANSFER_ENABLED, true);
			if (smsEnabled) {
				editor.putBoolean(KEY_SMS_ENABLED, true);
				editor.putString(KEY_SMS_ADDRESS, smsAddress.getText().toString());
			}
			else {
				editor.putBoolean(KEY_SMS_ENABLED, false);
				editor.putBoolean(KEY_AUTODETECT, autoDetect);
				editor.putString(KEY_ADDRESS0, ipAddressHelper.getEdit(0));
				editor.putString(KEY_ADDRESS1, ipAddressHelper.getEdit(1));
				editor.putString(KEY_ADDRESS2, ipAddressHelper.getEdit(2));
				editor.putString(KEY_ADDRESS3, ipAddressHelper.getEdit(3));
				editor.putString(KEY_IP_ADDRESS, ipAddressHelper.toString());
				if (port>0) editor.putInt(KEY_PORT, port);
			}
			
		}
		else {
			editor.putBoolean(KEY_TRANSFER_ENABLED, false);
			
		}
			
		editor.commit();
	
		Intent data = new Intent();
		if (transferEnabled) {
			if (smsEnabled) data.putExtra(MODE, MODE_SMS);
			else data.putExtra(MODE, MODE_LAN);
		}
		else data.putExtra(MODE, MODE_NOTRANSFER);
		
        // If it's for penalty configuration, pass the chosen mode back.
        // This is useful if BiblistActivity needs it, though PenaltyActivity will read from SharedPreferences.
        if (!chronoConfig) {
            data.putExtra(KEY_PENALTY_LAYOUT_MODE, penaltyLayoutMode);
        }

		setResult(RESULT_OK, data);
		
		final Context thisContext = this;
		if (transferEnabled && !smsEnabled) {
			WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			if (wifiManager.isWifiEnabled()) finish();
			else new AlertDialog.Builder(this)
				.setTitle("Activer le WIFI ?")
				.setMessage("Pour envoyer les pénalités, vous devez activer le WIFI. Activer ?")
				.setNegativeButton("Non", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						finish();
						
					}
				}) 
				.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
			
					public void onClick(DialogInterface dialog, int which) {
						(new WIFIActivatingTask(thisContext) {
							@Override
							protected void onPostExecute(Boolean value) {
								super.onPostExecute(value);
								finish();
									
							}
						}).execute();
				
					}
				})
				.create()
				.show();
		} else finish();
		
	}
}