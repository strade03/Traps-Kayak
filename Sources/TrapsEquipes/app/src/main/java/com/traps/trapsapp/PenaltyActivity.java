package com.traps.trapsequipes;

import java.net.InetSocketAddress;
import java.util.ArrayList;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.traps.trapsequipes.core.Bib;
import com.traps.trapsequipes.core.PenaltyPad;
import com.traps.trapsequipes.core.TrapsDB;
import com.traps.trapsequipes.core.Utility;
import com.traps.trapsequipes.network.TRAPSManagerThread;
import com.traps.trapsequipes.network.TRAPSPenalty;

public class PenaltyActivity extends AppCompatActivity implements DialogInterface.OnClickListener {

	private static final boolean SMS_ACTIVATED = true;
 
	private PenaltyPad penPad;
	private TrapsDB db;
	private ArrayList<Bib> bibList;
	private int bibIndex = 0;
	private int changeIndex = 0;
	private Spinner spinner;
	private SharedPreferences settings;

	private boolean smsEnabled = false;
	private boolean transferEnabled = false;
	private boolean autodetect = true;
	private SmsManager smsManager = SmsManager.getDefault();
	// destination address
	private String dAddress;
	private InetSocketAddress lanAddress;
	private TRAPSManagerThread trapsManager;

	private AlertDialog sendPenaltyConfirmDialog;

	private SoundPool soundPool;
	public int sndHighPitch;
	public int sndMidPitch;
	public int sndLowPitch;
	public int sndOKPitch;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// if permanent menu key, remove title 
		if(Build.VERSION.SDK_INT <= 10 || (Build.VERSION.SDK_INT >= 14 &&    
                ViewConfiguration.get(this).hasPermanentMenuKey())) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		} 
		
		setContentView(R.layout.terminal2);
		getWindow().setFlags(          
				WindowManager.LayoutParams.FLAG_FULLSCREEN,  
				WindowManager.LayoutParams.FLAG_FULLSCREEN);   
	
		db = TrapsDB.getInstance();

		penPad = new PenaltyPad(this);
		bibList = db.getBibList();

		settings = getSharedPreferences("SETTINGS_TRANSFER", MODE_PRIVATE);
		dAddress = settings.getString(TerminalConfigActivity.KEY_SMS_ADDRESS,
				"");
		Log.i("DAddress", dAddress);
		smsEnabled = settings.getBoolean(
				TerminalConfigActivity.KEY_SMS_ENABLED, false);
		transferEnabled = settings.getBoolean(
				TerminalConfigActivity.KEY_TRANSFER_ENABLED, false);
		autodetect = settings.getBoolean(TerminalConfigActivity.KEY_AUTODETECT,
				true);
		lanAddress = new InetSocketAddress(settings.getString(
				TerminalConfigActivity.KEY_IP_ADDRESS, ""), settings.getInt(
				TerminalConfigActivity.KEY_PORT, 8080));

		Log.i("smsEnabled", smsEnabled ? "true" : "false");
		Log.i("transferEnabled", transferEnabled ? "true" : "false");

		String title = "PENALITÉS ";
		if (smsEnabled && transferEnabled) title += "SMS";
		else if (transferEnabled) title += "WIFI";
		setTitle(title);
		
		spinner = (Spinner) findViewById(R.id.spinner1);
		int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
		Log.i("Screen height", "" + screenHeight);
		//bibText.setTextSize(TypedValue.COMPLEX_UNIT_PX, screenHeight / 7);

		ImageButton prevButton = (ImageButton) findViewById(R.id.previous_button);
		prevButton.setMaxHeight(1);
		prevButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				prevBib();

			}
		});
		ImageButton nextButton = (ImageButton) findViewById(R.id.next_button);
		nextButton.setMaxHeight(1);
		nextButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				nextBib();

			}
		});

		String stringArray[] = new String[bibList.size()];
		for (int index = 0; index < stringArray.length; index++) {
			stringArray[index] = bibList.get(index).getStringBibnumber();
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.spinnerlayout, stringArray);
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
  
		
		paintBib();

		// init sound
		soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		sndHighPitch = soundPool.load(this, R.raw.hz600ms100, 1);
		sndMidPitch = soundPool.load(this, R.raw.hz300ms100, 1);
		sndLowPitch = soundPool.load(this, R.raw.hz150ms100, 1);
		sndOKPitch = soundPool.load(this, R.raw.sndok300ms, 1);

		// init TRAPSManager
		trapsManager = new TRAPSManagerThread();
		if (transferEnabled && !smsEnabled) {
			trapsManager.start();
			if (autodetect)
				trapsManager.setAddress(this, null);
			else
				trapsManager.setAddress(this, lanAddress);
		}

	}

	public void play(int id) {
		soundPool.play(id, 1, 1, 0, 0, 1);
	}

	private String getTString(int id) {
		return getResources().getString(id);
	}

	private void sendPenalties(Bib bib) {


		// SMS
		if (smsEnabled) {

			if (!SMS_ACTIVATED) {
				Log.e("SMS", "SMS IS HARDCODED AS DISABLED !");
				return;
			}

			if (dAddress == "") {
				Utility.alert(this, "Erreur",
						"Impossible d'envoyer les penalites: numero destinataire SMS incorrect");
				return;
			}

			String text = bib.penaltyToSMSString();
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

			// LAN
		} else {
			Log.i("Terminal", "Adding penalties for bib " + bib.getBibnumber());
			trapsManager.addPacket(new TRAPSPenalty(bib.getBibnumber(), bib
					.getPenaltyMap()));

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
	private boolean changeBib(int index) {
		// first check if the penalties have changed

		SparseIntArray map = penPad.getPenaltyMap();
		changeIndex = index;
		if (bibList.get(bibIndex).hasSamePenalties(map)) {

			bibIndex = index;
			//bibText.setText(bibList.get(bibIndex).getStringBibnumber());
			paintBib();
			return true;
		} else {
			// show dialog box only if penalty are partially filled
			// count the number of penalties set and compare with the total
			// number of gate. Display popup if count>0 and count<number of
			// gatges
			int count = map.size(); 

			int totalVisibleRows = penPad.getActiveJudgmentRowCount();

			// Si le terminal est configuré pour n'afficher aucune porte, on ne fait rien.
			if (totalVisibleRows == 0) {
				commitPenalties();
				paintBib();
				return true;
			}
				
			// if partially filled or there were not empty before
			if (count > 0 && count < totalVisibleRows) {
				sendPenaltyConfirmDialog = new AlertDialog.Builder(this)
						.setTitle("Pénalités incomplètes")
						.setMessage(
								"Des pénalités ne sont pas remplies. Envoyer quand même ?")
						.setPositiveButton(getTString(R.string.OK), this)
						.setNegativeButton(getTString(R.string.cancel), this)
						.create();
				sendPenaltyConfirmDialog.show();
			} else if (!bibList.get(bibIndex).allPenaltyEmpty()) {
				sendPenaltyConfirmDialog = new AlertDialog.Builder(this)
						.setTitle("Pénalités modifiées")
						.setMessage(
								"Êtes-vous sûrs de vouloir envoyer les modifications ?")
						.setPositiveButton(getTString(R.string.OK), this)
						.setNegativeButton(getTString(R.string.cancel), this)
						.create();
				sendPenaltyConfirmDialog.show();
			} else {
				commitPenalties();
				paintBib();
			}

			play(sndHighPitch);
			return false;
		}
	}

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
		spinner.setSelection(bibIndex);
		penPad.setPenaltyMap(bib.getPenaltyMap(null));

	}

	public void closeTerminal() {
		trapsManager.stopThread();
		finish();

	}

	// back key is disabled
	public void onBackPressed() {
	};

	private void setGateNumbers() {
		penPad.getDialogGateSelection(this).show();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		getMenuInflater().inflate(R.menu.terminal_menu, menu);
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		if (id == R.id.setgates) {
			setGateNumbers();
			return true;
		} else if (id == R.id.exitterm) {
			closeTerminal();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void commitPenalties() {
		Bib bib = bibList.get(bibIndex);
		SparseIntArray penaltyMap = penPad.getPenaltyMap();
		bib.setPenaltyMap(penaltyMap);
		// store in db
		db.updateBibPenalty(bib.getBibnumber(), penaltyMap);
		if (transferEnabled) {
			// HERE: send penalties
			
				sendPenalties(bib);
				play(sndOKPitch);
			

		}

		bibIndex = changeIndex;
		//bibText.setText(bib.getStringBibnumber());
	}

	public void onClick(DialogInterface dialog, int which) {
		Log.i("TerminalActivity", "onClick()");

		if (dialog == sendPenaltyConfirmDialog) {

			if (which == DialogInterface.BUTTON_POSITIVE) {

				commitPenalties();

			}
			paintBib();
		}
	}

}
