package com.traps.trapsapp;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import android.content.pm.PackageManager;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;

import com.traps.trapsapp.core.Bib;
import com.traps.trapsapp.core.SystemParam;
import com.traps.trapsapp.core.TrapsDB;
import com.traps.trapsapp.core.Utility;
import com.traps.trapsapp.network.TRAPSChrono;
import com.traps.trapsapp.network.TRAPSManagerThread;

public class ChronoActivity extends AppCompatActivity {

	private static final boolean SMS_ACTIVATED = true;
	private static SimpleDateFormat dateFormatter1 = new SimpleDateFormat("HH:mm:ss", Locale.US);
		 
	private int bibIndex = 0;
	private int chronoType = 0;  // 0=start, 1=finish
	private boolean lock = false;

	private boolean smsEnabled = false;
	private boolean transferEnabled = false;
	private SmsManager smsManager = SmsManager.getDefault();
	// destination address
	private String dAddress;
	private TRAPSManagerThread trapsManager;
	private SharedPreferences settings;
	
	private ArrayList<Bib> bibList;
	private Spinner spinner;
	private TrapsDB db;
	private TextView chronoTextView;
	private TextView runningTextView;
	private ImageButton lockButton;
	private Button lapButton;
	private Handler handler = new Handler();
	private Runnable runnable;
	private boolean autodetect = true;
	private InetSocketAddress lanAddress;

	private SoundPool soundPool;
	public int sndHighPitch;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// if permanent menu key, remove title 
		if(Build.VERSION.SDK_INT <= 10 || (Build.VERSION.SDK_INT >= 14 &&    
                ViewConfiguration.get(this).hasPermanentMenuKey())) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		}   
		setContentView(R.layout.activity_chrono);
		getWindow().setFlags(          
				WindowManager.LayoutParams.FLAG_FULLSCREEN,  
				WindowManager.LayoutParams.FLAG_FULLSCREEN);  
	
		settings = getSharedPreferences("SETTINGS_TRANSFER", MODE_PRIVATE);
		dAddress = settings.getString(TerminalConfigActivity.KEY_SMS_ADDRESS,
				"");
		
		Log.i("DAddress", dAddress);
		smsEnabled = settings.getBoolean(
				TerminalConfigActivity.KEY_SMS_ENABLED, false);
		transferEnabled = settings.getBoolean(
				TerminalConfigActivity.KEY_TRANSFER_ENABLED, false);
		chronoType = getIntent().getExtras().getInt("chronoType", 0);
				
		autodetect = settings.getBoolean(TerminalConfigActivity.KEY_AUTODETECT,
				true);
		lanAddress = new InetSocketAddress(settings.getString(
				TerminalConfigActivity.KEY_IP_ADDRESS, ""), settings.getInt(
				TerminalConfigActivity.KEY_PORT, 8080));

		Log.i("smsEnabled", smsEnabled ? "true" : "false");
		Log.i("transferEnabled", transferEnabled ? "true" : "false");
		
		String title = "CHRONO ";
		if (smsEnabled && transferEnabled) title += "SMS";
		else if (transferEnabled) title += "WIFI";
		setTitle(title);
		
		
		db = TrapsDB.getInstance();
		bibList = db.getBibList();
		
		String stringArray[] = new String[bibList.size()];
		for (int index = 0; index < stringArray.length; index++) {
			stringArray[index] = bibList.get(index).getStringBibnumber();
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.spinnerlayout, stringArray);
		spinner = (Spinner) findViewById(R.id.chronoSpinner); 
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				changeBib(arg2);
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		}); 
		
		chronoTextView = (TextView)findViewById(R.id.textViewChrono);
		
		ImageButton prevButton = (ImageButton) findViewById(R.id.chrono_previous_button);
		prevButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				prevBib();

			}
		});
		ImageButton nextButton = (ImageButton) findViewById(R.id.chrono_next_button);
		nextButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				nextBib();

			}
		});
	
		lockButton = (ImageButton) findViewById(R.id.lockImageButton);
		lockButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { 
				setLock(!lock);
			}
		});
		
		lapButton = (Button)findViewById(R.id.lapButton);
		if (chronoType==0) lapButton.setText("\nDÉPART\n");
		else lapButton.setText("\nARRIVÉE\n");
		lapButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				lapButtonPressed();
				
			}
		}); 
		
		// init TRAPSManager
		trapsManager = new TRAPSManagerThread();
		if (transferEnabled && !smsEnabled) {
			trapsManager.start();
			if (autodetect)
				trapsManager.setAddress(this, null);
			else
				trapsManager.setAddress(this, lanAddress);
		}
		
		runningTextView = (TextView)findViewById(R.id.textViewTime);
		runnable = new Runnable() {

			    @Override
			    public void run() {
			    	
			    	long currentTime = System.currentTimeMillis()+ SystemParam.timeshift;
			    	Date date = new Date(currentTime);
			    	
			    	runningTextView.setText(" "+dateFormatter1.format(date)+"."+((currentTime/100)%10)+" ");
			    	handler.postDelayed(runnable, 100);
			    }
			  }; 

		handler.postDelayed(runnable, 100);
		// init sound
		soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		sndHighPitch = soundPool.load(this, R.raw.hz600ms100, 1);
		paintBib();
	}

	private void setLock(boolean lock) {
		this.lock = lock;
		if (lock) {
			lapButton.setVisibility(View.INVISIBLE);
			lockButton.setImageResource(R.drawable.chronolock);
		}
		else {
			lapButton.setVisibility(View.VISIBLE); 
			lockButton.setImageResource(R.drawable.chronounlock);
		}
		
	}
	
	public void play(int id) {
		soundPool.play(id, 1, 1, 0, 0, 1);
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1001) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// Permission accordée → relance l’envoi si besoin
				lapButtonPressed(); // ou ce que tu veux
			} else {
				if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
					// L'utilisateur a bloqué définitivement
					Utility.alert(this, "Permission bloquée",
						"L'envoi de SMS est désactivé. Veuillez l'activer dans les paramètres de l'application.");
					Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
					Uri uri = Uri.fromParts("package", getPackageName(), null);
					intent.setData(uri);
					startActivity(intent);
				} else {
					// Permission refusée
					Utility.alert(this, "Permission requise", "L'envoi SMS nécessite la permission d'envoyer des SMS.");
				}
			}
		}
	}

	private void lapButtonPressed() {
		long currentTime = System.currentTimeMillis()+SystemParam.timeshift;
		Bib bib = bibList.get(bibIndex);
		bib.setChrono(chronoType, currentTime);
		// store in db
		db.updateBibChrono(chronoType, bib.getBibnumber(), bib.getChrono(chronoType));
		if (transferEnabled) {
						if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
				!= PackageManager.PERMISSION_GRANTED) {
			// Permission non accordée → la demander
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.SEND_SMS},
					1001); // code de requête
			} else {
			// Permission OK → envoi possible
			sendChrono(bib);
			}
			
		}
    	chronoTextView.setText(bib.getChronoStr(chronoType));
    	chronoTextView.setVisibility(View.VISIBLE);
		setLock(true);
		play(sndHighPitch);
	}
	
	private void sendChrono(Bib bib) {

		// SMS
		if (smsEnabled) {

			String text = bib.chronoToSMSString(chronoType);
	
			if (!SMS_ACTIVATED) {
				Log.e("SMS", "SMS IS HARDCODED AS DISABLED !");
				return;
			}
	
			if (dAddress == "") {
				Utility.alert(this, "Erreur",
						"Impossible d'envoyer les chronos: numero destinataire SMS incorrect") ;
				return;
			}

			if (PhoneNumberUtils.isWellFormedSmsAddress(dAddress)) {
				try {
					Log.i("TerminalActivity", "Sending to " + dAddress + "> "
							+ text);
					smsManager
							.sendTextMessage(dAddress, null, text, null, null);
				} catch (Exception e) {
					Log.e("SMS", e.getMessage());
					Utility.alert(this, "Erreur",
							"Impossible d'envoyer les penalités: numero destinataire SMS incorrect"
									+ dAddress);
				}
			} else
				Utility.alert(this, "Erreur", "Numero SMS incorrect:"
						+ dAddress);
		}
		// LAN
		else {
			Log.i("Terminal", "Adding chronos for bib " + bib.getBibnumber());
			trapsManager.addPacket(new TRAPSChrono(bib.getBibnumber(), chronoType, bib.getChrono(chronoType)));
		}
		

	}

	private void nextBib() {
		Log.i("TerminalActivity", "Next bib");
		int nextIndex = bibIndex + 1;
		if (nextIndex >= bibList.size()) {
			nextIndex = 0;
		}

		changeBib(nextIndex);

	}

	private void prevBib() {
		Log.i("TerminalActivity", "Prev bib");
		int prevIndex = bibIndex - 1;
		if (prevIndex < 0) {
			prevIndex = bibList.size() - 1;
		}

		changeBib(prevIndex);

	}

	/**
	 * Returns true if the change has been done
	 * 
	 * @param index
	 * @return
	 */
	private void changeBib(int index) {
		
		bibIndex = index;
		paintBib();
		
	
	}
	// back key is disabled
	public void onBackPressed() {};


	/**
	 * Paint the current bib (bibIndex is the current index)
	 */
	private void paintBib() {
		if (bibIndex >= bibList.size()) {
			Log.e("TerminalActivity", "bibIndex out of range: " + bibIndex);
		}
		Bib bib = bibList.get(bibIndex);
		if (bib == null) {
			Log.e("TerminalActivity", "bib is null");
		}

		String chronoStr = bib.getChronoStr(chronoType);
		chronoTextView.setText(chronoStr);
		if ("".equals(chronoStr)) chronoTextView.setVisibility(View.INVISIBLE);
		else chronoTextView.setVisibility(View.VISIBLE);
		if (bib.getChrono(chronoType)>0) {
			setLock(true);
		}
		else {
			setLock(false);
		}
		
		spinner.setSelection(bibIndex);
	}

	public void closeTerminal() {
		trapsManager.stopThread();
		finish();

	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		if (id == R.id.exitchrono) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chrono, menu);
		return true;
	}



}
