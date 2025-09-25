package com.traps.trapsapp;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.telephony.SmsMessage;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.traps.trapsapp.core.Bib;
import com.traps.trapsapp.core.BibLoader;
import com.traps.trapsapp.core.BiblistAdapter;
import com.traps.trapsapp.core.CreateListTask;
import com.traps.trapsapp.core.FFCanoeHelper;
import com.traps.trapsapp.core.ResetPenaltyTask;
import com.traps.trapsapp.core.SMSData;
import com.traps.trapsapp.core.SimpleInputDialog;
import com.traps.trapsapp.core.SystemParam;
import com.traps.trapsapp.core.TrapsDB;
import com.traps.trapsapp.core.Utility;
import com.traps.trapsapp.network.UDPListener;

import java.net.NetworkInterface;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BiblistActivity extends AppCompatActivity implements OnClickListener, OnItemClickListener {

    private static final int FFCANOE_CONNECT_ACTIVITY = 0;
    private static final int TERMINAL_ACTIVITY = 2;
    private static final int CONFIG_TERMINAL_ACTIVITY = 3;
    private static final int CONFIG_CHRONO_ACTIVITY = 4;
    private static final int CHRONO_ACTIVITY = 5;

    private BiblistAdapter listAdapter;
    private ArrayList<Bib> bibList = new ArrayList<Bib>();
    private ArrayList<Integer> checkedList = new ArrayList<Integer>();
    private ListView listView;

    private TrapsDB db;

    private static BiblistActivity instance = null;
    private Context biblistContext;
    private int minScreenSize;

    private SimpleInputDialog createBibListDialog;

    private FFCanoeHelper ffcanoeHelper;

    // used to check the validity of SMS for each sender
    private Map<String, SparseArray<Long>> sequenceMap = new HashMap<String, SparseArray<Long>>();

    private BroadcastReceiver receiver;

    private SoundPool soundPool;
    public int sndHighPitch;
    public int sndLowPitch;

    private int chronoType = 0;
    private boolean warningTrialVersion = true;
    private boolean forwardPenalty = true;
    private boolean forwardChrono = false;

    private String getTString(int id) {
        return getResources().getString(id);
    }

    public static BiblistActivity getInstance() {
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        String myid = "AA"
                + Build.BOARD.length() % 10 + Build.BRAND.length() % 10
                + Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10
                + Build.DISPLAY.length() % 10 + Build.HOST.length() % 10
                + Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10
                + Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10
                + Build.TAGS.length() % 10 + Build.TYPE.length() % 10
                + Build.USER.length() % 10; // 13 digits

        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi!=null) {
            WifiInfo info = wifi.getConnectionInfo();
            if (info!=null) {
                String macaddress = info.getMacAddress();
                if (macaddress.equals("02:00:00:00:00:00")) macaddress = getMacAddr();
                if (!macaddress.equals("02:00:00:00:00:00")) myid = macaddress;
            }
            if (myid!=null) Log.i("ID",myid);

        }
        System.out.println(myid);
        Log.i("unique code", myid);

        BiblistActivity.instance = this;
        biblistContext = this;
        setContentView(R.layout.biblist_activity);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        TrapsDB.init(this);
        db = TrapsDB.getInstance();

        SharedPreferences settings1 = getSharedPreferences("SETTINGS_TIMESHIFT",
                MODE_PRIVATE);
        SystemParam.timeshift = settings1.getLong("timeshift", 0);

        SharedPreferences settings = getSharedPreferences("SETTINGS_LICENSE",
                MODE_PRIVATE);

        listView = (ListView)findViewById(R.id.mainlist);
        listView.setDrawSelectorOnTop(true);
        listView.setOnItemClickListener(this);
        listView.setFastScrollEnabled(true);

        ffcanoeHelper = FFCanoeHelper.getInstance();
        initReceiver();
        // init sound
        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        sndHighPitch = soundPool.load(this, R.raw.hz600ms100, 1);
        sndLowPitch = soundPool.load(this, R.raw.hz150ms100, 1);

        Display display = getWindowManager().getDefaultDisplay();
        Log.i("SCREEN", "Screen height ="+display.getHeight());
        Log.i("SCREEN", "Screen y ="+display.getWidth());

        minScreenSize = display.getHeight();
        if (display.getWidth()<minScreenSize) minScreenSize = display.getWidth();

        Log.d("UDP","Trying to start UPD listener");
        new UDPListener(this); // this is to be a singleton (acces it via UDPListener.getInstance())
        reloadBibsFromDB();

    }

    private static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }

    public void play(int id) {
        soundPool.play(id, 1, 1, 0, 0, 1);
    }

    private void initReceiver() {

        IntentFilter filter = new IntentFilter();
        filter.setPriority(999);
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle pudsBundle = intent.getExtras();
                Object[] pdus = (Object[]) pudsBundle.get("pdus");
                SmsMessage message = SmsMessage.createFromPdu((byte[]) pdus[0]);
                Log.i("SMS", message.getMessageBody());

                try {
                    processSMS(message.getOriginatingAddress(),
                            message.getMessageBody());
                    abortBroadcast();
                } catch (ParseException e) {
                    Log.e("SMS", e.getMessage());
                }

            }

        };

        registerReceiver(receiver, filter);
        Log.i("Receiver", "Registered");

    }

    private void prepareMenu(Menu menu) {
        menu.clear();
        buildCheckedList();
        if (checkedList.size() == 0) {
            getMenuInflater().inflate(R.menu.biblist_menu, menu);
            MenuItem item = menu.findItem(R.id.connect);
            if (ffcanoeHelper.isActive()) {
                item.setTitle(getTString(R.string.BiblistActivity_disconnect_ffcanoe));
            } else {
                item.setTitle(getTString(R.string.BiblistActivity_connect_ffcanoe));
            }
        } else {
            getMenuInflater().inflate(R.menu.selection_menu, menu);

        }
    }

    private void buildCheckedList() {
        checkedList.clear();
        int i = 0;
        for (Bib bib: bibList) {
            if (bib.isChecked()) checkedList.add(i);
            i++;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        prepareMenu(menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        prepareMenu(menu);
        return true;
    }

    // we only take the last 9 digits
    private boolean validCaller(String callerId) {
        if (callerId.length() == 0)
            return false;
        return true;
    }

    private Bib getBibByNumber(int bibnumber) {
        for (Bib bib : bibList)
            if (bib.getBibnumber() == bibnumber)
                return bib;
        return null;
    }

    public void processSMS(String callerId, String msg) throws ParseException {

        Log.d("Message", msg);

        if (!validCaller(callerId)) {
            Log.d("BibListActivity", "Caller ID is NOT VALID");
            throw new ParseException("Invalid caller Id", 0);

        }

        SMSData data = new SMSData(msg);
        // get the bib
        Bib bib = getBibByNumber(data.getBibnumber());
        if (bib == null)
            throw new ParseException("Bib number not found:"
                    + data.getBibnumber(), 0);
        if (bib.isLocked())
            throw new ParseException("Bib is locked: " + data.getBibnumber(), 0);

        // look if sequence is OK
        SparseArray<Long> callerIdArray = sequenceMap.get(callerId);
        if (callerIdArray == null) {
            callerIdArray = new SparseArray<Long>();
            sequenceMap.put(callerId, callerIdArray);
        } else {
            Long lastTime = callerIdArray.get(bib.getBibnumber());
            if ((lastTime != null) && (lastTime > data.getTimestamp()))
                throw new ParseException("Out of date message: "
                        + data.getBibnumber(), 0);

        }

        callerIdArray.put(bib.getBibnumber(), data.getTimestamp());
        play(sndHighPitch);

        // update db
        switch (data.getMsgType()) {
            case SMSData.PENALTY_TYPE : {
                db.updateBibPenalty(data.getBibnumber(), data.getMap());
                bib.setPenaltyMap(data.getMap());
                // send over network
                if (ffcanoeHelper.isActive()) sendBibPenalty(bib);
                break;
            }
            case SMSData.START_TYPE : {
                long start = data.getStart();
                db.updateBibChrono(0, data.getBibnumber(), start);
                bib.setChrono(0, start);
                if (ffcanoeHelper.isActive()) sendBibChrono(bib);
                break;
            }
            case SMSData.FINISH_TYPE : {
                long finish = data.getFinish();
                db.updateBibChrono(1, data.getBibnumber(), finish);
                bib.setChrono(1, finish);
                if (ffcanoeHelper.isActive()) sendBibChrono(bib);
                break;

            }
        }
        listAdapter.notifyDataSetChanged();



    }


    private void openPenalty() {
        if (bibList.size() == 0) {
            Utility.alert(this, R.string.BiblistActivity_terminal,
                    R.string.BiblistActivity_create_or_load_bib_first);
            return;
        }
        // Disconnect from FFCanoe if connected
        ffcanoeHelper.disconnect(this);
        Log.i("BibListActivity", "Open terminal");

        Intent intent = new Intent(this, TerminalConfigActivity.class);
        intent.putExtra("chrono", false);
        startActivityForResult(intent, CONFIG_TERMINAL_ACTIVITY);

    }

    private void openChrono() {
        if (bibList.size() == 0) {
            Utility.alert(this, R.string.BiblistActivity_terminal,
                    R.string.BiblistActivity_create_or_load_bib_first);
            return;
        }

        Intent intent = new Intent(this, TerminalConfigActivity.class);
        intent.putExtra("chrono", true);
        startActivityForResult(intent, CONFIG_CHRONO_ACTIVITY);

    }

    // clear or create db
    private void clearPenalties() {

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog
                .setTitle(getTString(R.string.BiblistActivity_delete_penalty_title));
        alertDialog
                .setMessage(getTString(R.string.BiblistActivity_delete_penalty_are_you_sure));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                getTString(R.string.OK), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        new ResetPenaltyTask(biblistContext).execute();

                    }
                });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getTString(R.string.cancel), this);
        alertDialog.show();

    }

    // creates a list of x bibs
    private void newList() {
        warningTrialVersion = true;
        createBibListDialog = new SimpleInputDialog(this);
        createBibListDialog.setTitle(getTString(R.string.BiblistActivity_how_many_bibs));
        createBibListDialog.setValue("");
        createBibListDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                getTString(R.string.OK), this);
        createBibListDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getTString(R.string.cancel), this);
        createBibListDialog.show();

    }

    private void authorize() {
        Intent intent = new Intent(this, SenderListActivity.class);
        startActivity(intent);
    }

    private void lockCheckedBibs(boolean lock) {
        for (Integer index : checkedList) {
            Bib bib = bibList.get(index);
            db.updateBibLock(bib.getBibnumber(), lock);
            bib.setLocked(lock);
        }
        uncheckBibs();
        listAdapter.notifyDataSetChanged();

    }

    private void uncheckBibs() {
        for (Bib bib : bibList) bib.setChecked(false);
        listAdapter.notifyDataSetChanged();
    }

    private void sendBibPenalty(Bib bib) {
        if (!forwardPenalty) return;
        SparseIntArray map = bib.getPenaltyMap();
        for (int i = 0; i < map.size(); i++) {
            int gateName = map.keyAt(i) + 1;
            int value = map.valueAt(i);
            if ((gateName > 0)
                    && (gateName <= SystemParam.GATE_COUNT)
                    && (value > -1))
                ffcanoeHelper.addPenalty(bib.getBibnumber(),
                        gateName, value);

        }

    }

    private void sendBibChrono(Bib bib) {
        if (!forwardChrono) return;
        int chrono = (int)bib.getTime();
        if (chrono>0) ffcanoeHelper.addChrono(bib.getBibnumber(), chrono);

    }


    private void sendCheckedBibs() {
        if (ffcanoeHelper.isActive()) {
            for (Integer index : checkedList) {
                sendBibPenalty(bibList.get(index));
                sendBibChrono(bibList.get(index));
            }
            uncheckBibs();
        } else {
            Toast.makeText(
                    this,
                    getTString(R.string.BiblistActivity_not_connected_to_ffcanoe),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBibList() {
        // first check TRAPSManager is on the network and get synchro with its network address
        warningTrialVersion = true;
        (new BibLoader(this) {

            @Override
            public void processResult(boolean success) {
                if (success) {
                    reloadBibsFromDB();
                    SharedPreferences settings = getSharedPreferences("SETTINGS_TIMESHIFT",
                            MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putLong("timeshift", SystemParam.timeshift);
                    editor.commit();
                }

            }

        }).load();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
        Log.i("TRAPS", "requestCode=" + requestCode);
        Log.i("TRAPS", "resultCode=" + resultCode);

        // returning from FFCAnoe connection
        if (requestCode == FFCANOE_CONNECT_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                setTitle(getResources().getString(
                        R.string.BiblistActivity_title)
                        + " | Connecté CompetFFCK");
                forwardChrono = data.getBooleanExtra("forwardChrono", false);
                forwardPenalty = data.getBooleanExtra("forwardPenalty", true);

            } else {
                setTitle(getResources().getString(
                        R.string.BiblistActivity_title));
            }
        }

        else if (requestCode == TERMINAL_ACTIVITY || requestCode == CHRONO_ACTIVITY) {
            setTitle(getResources().getString(R.string.BiblistActivity_title));
            reloadBibsFromDB();
        }
        else if ((requestCode == CONFIG_TERMINAL_ACTIVITY) && (resultCode == RESULT_OK)) {
            Intent intent = new Intent(this, PenaltyActivity.class);
            startActivityForResult(intent, TERMINAL_ACTIVITY);
        }
        else if ((requestCode == CONFIG_CHRONO_ACTIVITY) && (resultCode == RESULT_OK)) {
            Intent intent = new Intent(this, ChronoActivity.class);
            intent.putExtra("chronoType", chronoType);
            startActivityForResult(intent, CHRONO_ACTIVITY);
        }


    }

    private void toggleConnection() {
        if (ffcanoeHelper.isActive()) {
            ffcanoeHelper.disconnect(this);
            setTitle(getResources().getString(R.string.BiblistActivity_title));
            return;
        }
        Intent intent = new Intent(this, FFCanoeConnectActivity.class);
        startActivityForResult(intent, FFCANOE_CONNECT_ACTIVITY);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.terminal) {
            openPenalty();
            return true;
        } else if (id == R.id.chronostart) {
            chronoType = 0;
            openChrono();
            return true;
        } else if (id == R.id.chronofinish) {
            chronoType = 1;
            openChrono();
            return true;
        } else if (id == R.id.connect) {
            toggleConnection();
            return true;
        } else if (id == R.id.newlist) {
            newList();
            return true;
        } else if (id == R.id.load) {
            loadBibList();
            return true;
        } else if (id == R.id.reset) {
            clearPenalties();
            return true;
        } else if (id == R.id.authorize) {
            authorize();
            return true;
        } else if (id == R.id.lock) {
            lockCheckedBibs(true);
            return true;
        } else if (id == R.id.unlock) {
            lockCheckedBibs(false);
            return true;
        } else if (id == R.id.unselectall) {
            uncheckBibs();
            return true;
        } else if (id == R.id.sendagain) {
            sendCheckedBibs();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // this can be done only once when the program starts
    public void reloadBibsFromDB() {

        bibList = db.getBibList();
        if (bibList.isEmpty()) {
            new CreateListTask(biblistContext, 10).execute();
            return;
        }

        Log.i("TRAPS-CK", "Bibs read from DB");

        listAdapter = new BiblistAdapter(this, R.layout.biblistcell, bibList);
        listView.setAdapter(listAdapter);

    }

    public void onResume() {
        super.onResume();

        listView.invalidateViews();

    }

    public void onPause() {
        super.onPause();


    }

    public void onDestroy() {
        instance = null;
        super.onDestroy();
        unregisterReceiver(receiver);

    }

    // back key is disabled
    public void onBackPressed() {};

    public void onClick(DialogInterface dialog, int which) {

        // deal with bib creation dialog
        if ((dialog == createBibListDialog)
                && (which == DialogInterface.BUTTON_POSITIVE)) {
            int bibCount = 0;
            try {
                bibCount = Integer.parseInt(createBibListDialog.getValue());
            } catch (NumberFormatException e) {
                Log.e("BibList", "Error while parsing number of bibs:"
                        + createBibListDialog.getValue());
            }

            if ((bibCount < 1) || (bibCount > 1000)) {
                Utility.alert(this, "Erreur de saisie",
                        "On a dit entre 1 et 1000");
            } else {
                new CreateListTask(biblistContext, bibCount).execute();
            }
        }


    }

    @Override
    public void onItemClick(AdapterView adapterView, View view, int position, long id) {
        // TODO Auto-generated method stub
        Bib bib = bibList.get(position);
        bib.setChecked(!bib.isChecked());
        listAdapter.notifyDataSetChanged();

    }

}
