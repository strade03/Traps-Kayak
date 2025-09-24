package com.traps.trapsapp.core;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.SparseIntArray;

public class TrapsDB {
	
	/**************************************************/
	private static final int DATABASE_VERSION = 22;
	/**************************************************/
	
	private static final String DATABASE_NAME = "trapsck.db";
	private static final String TABLE_NAME_PENALTIES = "penalties";
	private static final String TABLE_NAME_CONF = "configuration";
	// destination address for the sms (phone number of the receiver)
	private static final String TABLE_NAME_GATE = "gates";
	private static final String TABLE_NAME_VERSION = "version";
	private static final String TABLE_NAME_AUTHORIZE = "authorize";
	
	// destination address
	
	private static final String COLUMN_NAME_PHONE = "phone";	
	private static final String COLUMN_NAME_VERSION = "dbversion";	
	private static final String COLUMN_NAME_BIB = "bib";
	private static final String COLUMN_NAME_INDEX = "row";
	private static final String COLUMN_NAME_START = "start";
	private static final String COLUMN_NAME_FINISH = "finish";
	private static final String COLUMN_NAME_GATEINDEX = "gateindex";
	private static final String COLUMN_NAME_LOCK = "lock";
	
	
	private static final String COLUMN_NAME_ASSIGN_GATE = "assigngate";
	
	public final static int MAX_GATE_COUNT = 25;
	public final static int MAX_GATE_TERMINAL_COUNT = 5;
	
	private static TrapsDB instance = null;
	
	private DatabaseHelper dbHelper;

	// return hard coded version
	public static int getHCVersion() {
		
		return DATABASE_VERSION;
	}

	static class DatabaseHelper extends SQLiteOpenHelper {

	       DatabaseHelper(Context context) {

	           // calls the super constructor, requesting the default cursor factory.
	           super(context, DATABASE_NAME, null, DATABASE_VERSION);
	       }

		@Override
		public void onCreate(SQLiteDatabase db) {
			
			// bib table
			StringBuffer sql = new StringBuffer("CREATE TABLE IF NOT EXISTS " + TABLE_NAME_PENALTIES + " ("
					+ COLUMN_NAME_INDEX + " INTEGER PRIMARY KEY,"
					+ COLUMN_NAME_BIB + " INTEGER,");
	               
			for (int index=0; index<MAX_GATE_COUNT; index++) {
				sql.append(COLUMN_NAME_GATEINDEX + index+" INTEGER,");
				
			}			
			sql.append(COLUMN_NAME_LOCK + " INTEGER, ");
			sql.append(COLUMN_NAME_START + " INTEGER, ");
			sql.append(COLUMN_NAME_FINISH + " INTEGER);");
			Log.i("SQL", sql.toString());
			db.execSQL(sql.toString());
			
			// gate table
			String request = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME_GATE+" ("+
					COLUMN_NAME_INDEX+" INTEGER PRIMARY KEY,"+
					COLUMN_NAME_ASSIGN_GATE+" INTEGER);";
			Log.i("SQL", request);
			db.execSQL(request);
			
	
					
			ContentValues map = new ContentValues(2);
			for (int i=0; i<3; i++) {
				map.put(COLUMN_NAME_INDEX, i);
				map.put(COLUMN_NAME_ASSIGN_GATE, i);
				db.insert(TABLE_NAME_GATE, null, map);
			}
			
			
			
			// version db
			db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_NAME_VERSION+" ("+
					COLUMN_NAME_INDEX+" INTEGER PRIMARY KEY,"+
					COLUMN_NAME_VERSION + " INTEGER);");
						
			map = new ContentValues(2);
			map.put(COLUMN_NAME_INDEX, 0);
			map.put(COLUMN_NAME_VERSION, DATABASE_VERSION);
			db.insert(TABLE_NAME_VERSION, null, map);
			
			// authorize
			db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_NAME_AUTHORIZE+" ("+
						COLUMN_NAME_INDEX+" INTEGER PRIMARY KEY,"+
						COLUMN_NAME_PHONE + " CHAR(50));");
									
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				
			dropTables(db);

	           // Recreates the database with a new version
	           onCreate(db);
			
		}
		
		private void dropTables(SQLiteDatabase db) {
			db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME_PENALTIES);
			db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME_CONF);
			db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME_GATE);
			db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME_VERSION);
		}
		
		private void clearBibs(SQLiteDatabase db) {
			db.execSQL("DELETE FROM "+TABLE_NAME_PENALTIES);
		}
				
		public void reset(SQLiteDatabase db) {
			dropTables(db);
			onCreate(db);
		}
	       
	       
	}

	public static void init(Context context) {
		if (instance==null) instance = new TrapsDB(context);
		//instance.reset();
		int version = instance.getVersion();
		if (version!=DATABASE_VERSION) {
			Log.i("TrapsDB", "There is an older version of the DB here. Drop all and create again");
			instance.reset();
		}
		
		
	}
	
	public static TrapsDB getInstance() {
		if (instance==null) {
			Log.e("TrapsDB", "DB must be initialized with a context first");
			return null;
		}
		return instance;
	}
	
	private TrapsDB(Context context) {
	
		dbHelper = new DatabaseHelper(context);
		instance = this;
		
	}

	
	public void reset() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		dbHelper.reset(db);
		db.close();
	}
	
	// truncate table
	public void clearBibs() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		dbHelper.clearBibs(db);
		db.close();
	}
	
	public void createBibList(ArrayList<Bib> bibList, IProgress target) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (Bib bib: bibList) list.add(bib.getBibnumber());
		createBibListInteger(list, target);
		
	}
	
	public void createBibListInteger(ArrayList<Integer> bibList, IProgress target) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues map = new ContentValues(27);
		map.put(COLUMN_NAME_LOCK, 0);
		map.put(COLUMN_NAME_START, 0);
		map.put(COLUMN_NAME_FINISH, 0);
		for (int index=0; index<MAX_GATE_COUNT; index++) {
			map.put(COLUMN_NAME_GATEINDEX+(index),-1);
		}
		int index = 0;
		for (Integer bibnumber: bibList) {
			map.put(COLUMN_NAME_INDEX, index++);
			map.put(COLUMN_NAME_BIB, bibnumber);
			//Log.i("TrapsDB", "Insert row with bib="+bibnumber);
			db.insert(TABLE_NAME_PENALTIES, null, map);
			if (target!=null) target.setProgress(index);
		}
		
		db.close();
	}
	
	public ArrayList<Bib> getBibList() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME_PENALTIES, null, null, null, null, null, null);
		
		ArrayList<Bib> bibList = new ArrayList<Bib>();
		int rank = 0;
		while (cursor.moveToNext()) {
			
			//	int bibnumber = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_BIB));
			int bibnumber = 0; // Déclaration avant toute assignation

			int columnIndex = cursor.getColumnIndex(COLUMN_NAME_BIB);
			if (columnIndex != -1) {
				bibnumber = cursor.getInt(columnIndex);
			} else {
				Log.e("TrapsDB", "La colonne " + COLUMN_NAME_BIB + " est introuvable dans le curseur !");
			}

			//Log.i("TrapsDB", "Read row with bib="+bibnumber);
			Bib bib = new Bib(bibnumber, rank++);
			for (int index=0; index<SystemParam.GATE_COUNT; index++) {
			//	int penalty = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_GATEINDEX+(index)));
				int gateIndexColumn = cursor.getColumnIndex(COLUMN_NAME_GATEINDEX + index);
				int penalty = 0; // Valeur par défaut

				if (gateIndexColumn != -1) {
					penalty = cursor.getInt(gateIndexColumn);
				} else {
					Log.e("TrapsDB", "La colonne " + (COLUMN_NAME_GATEINDEX + index) + " est introuvable dans le curseur !");
				}


				bib.setPen(index, penalty);
			}
			//	int locked = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_LOCK));
			int lockColumnIndex = cursor.getColumnIndex(COLUMN_NAME_LOCK);
			int locked = 0; // Valeur par défaut

			if (lockColumnIndex != -1) {
				locked = cursor.getInt(lockColumnIndex);
			} else {
				Log.e("TrapsDB", "La colonne " + COLUMN_NAME_LOCK + " est introuvable dans le curseur !");
			}

			boolean lock = false;
			if (locked==1) lock = true;
			bib.setLocked(lock);
			//	long start = cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_START));
			//	long finish = cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_FINISH));
			int startColumnIndex = cursor.getColumnIndex(COLUMN_NAME_START);
			long start = 0L; // Valeur par défaut

			if (startColumnIndex != -1) {
				start = cursor.getLong(startColumnIndex);
			} else {
				Log.e("TrapsDB", "La colonne " + COLUMN_NAME_START + " est introuvable dans le curseur !");
			}

			int finishColumnIndex = cursor.getColumnIndex(COLUMN_NAME_FINISH);
			long finish = 0L; // Valeur par défaut

			if (finishColumnIndex != -1) {
				finish = cursor.getLong(finishColumnIndex);
			} else {
				Log.e("TrapsDB", "La colonne " + COLUMN_NAME_FINISH + " est introuvable dans le curseur !");
			}

			bib.setStart(start);
			bib.setFinish(finish);
			bibList.add(bib);	
		}
		cursor.close();
		db.close();
		return bibList;
	}
	
	public boolean[] getGateSelection() {
		boolean[] tab = new boolean[MAX_GATE_COUNT];
		for (int i=0; i<MAX_GATE_COUNT; i++) tab[i] = false;
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME_GATE, null, null, null, null, null, null);
		while (cursor.moveToNext()) {
			// int gateIndex = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ASSIGN_GATE));
			int gateIndexColumn = cursor.getColumnIndex(COLUMN_NAME_ASSIGN_GATE);
			int gateIndex = 0; // Valeur par défaut

			if (gateIndexColumn != -1) {
				gateIndex = cursor.getInt(gateIndexColumn);
			} else {
				Log.e("TrapsDB", "La colonne " + COLUMN_NAME_ASSIGN_GATE + " est introuvable dans le curseur !");
			}

			if ((gateIndex>-1) && (gateIndex<MAX_GATE_COUNT))	tab[gateIndex] = true;
		}
		db.close();
		return tab;
	}
	
	// the integer stored in the db is the index of the gate (starts at 0)
	public void setGateSelection(boolean tab[]) {
		ContentValues map = new ContentValues(1);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM "+TABLE_NAME_GATE);
		int index = 0;
		for (int i=0; i<tab.length; i++) {
			if (tab[i]) {
				map.put(COLUMN_NAME_ASSIGN_GATE, i);
				map.put(COLUMN_NAME_INDEX, index++);
				db.insert(TABLE_NAME_GATE, null, map);

			}
		
		}
		db.close();
		
	}
	
	

	private int getVersion() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME_VERSION, null, null, null, null, null, null);
		cursor.moveToNext(); 
		//	int value =  cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_VERSION));
		int columnIndex = cursor.getColumnIndex(COLUMN_NAME_VERSION);
		int value = 0; // Valeur par défaut

		if (columnIndex != -1) {
			value = cursor.getInt(columnIndex);
		} else {
			Log.e("TrapsDB", "La colonne " + COLUMN_NAME_VERSION + " est introuvable dans le curseur !");
		}
		db.close();
		return value;
	}
	
	
	public ArrayList<String> getAuthorize() {
		ArrayList<String> list = new ArrayList<String>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME_AUTHORIZE, null, null, null, null, null, null);
		while (cursor.moveToNext()) { 
			//	String value =  cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PHONE));
			int columnIndex = cursor.getColumnIndex(COLUMN_NAME_PHONE);
			String value = ""; // Valeur par défaut

			if (columnIndex != -1) {
				value = cursor.getString(columnIndex);
			} else {
				Log.e("TrapsDB", "La colonne " + COLUMN_NAME_PHONE + " est introuvable dans le curseur !");
			}

			list.add(value);
		}
		db.close();
		return list;
	}
	
	
	public void setAuthorize(ArrayList<String> list) {
		
		SQLiteDatabase db = dbHelper.getWritableDatabase();		
		db.execSQL("DELETE FROM "+TABLE_NAME_AUTHORIZE);
		if (list.size()>0) {
			ContentValues map = new ContentValues(list.size());
			int index=0;
			for (String string: list) {
				map.put(COLUMN_NAME_INDEX, index++);
				map.put(COLUMN_NAME_PHONE, string);
				db.insert(TABLE_NAME_AUTHORIZE, null, map);
			}
			
		
		}
		db.close();
	}
	

	
	public void updateBibPenalty(int bibnumber, SparseIntArray map) {
		ContentValues contentValues = new ContentValues(map.size());
		for (int index=0; index<map.size(); index++) {
			int gateIndex = map.keyAt(index);
			int value = map.valueAt(index);
			if ((gateIndex>-1) && (gateIndex<MAX_GATE_COUNT)) 
				contentValues.put(COLUMN_NAME_GATEINDEX+gateIndex, value);
			Log.i("gateIndex", ""+gateIndex);
			Log.i("value", ""+value);
			
			
		}
		Log.i("TrapsDB", "Updating SQLite base");
		Log.i("bibnumber", ""+bibnumber);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		// update penalties where bibnumber is correct and where timestamp is lower
		int row = db.update(TABLE_NAME_PENALTIES, contentValues, COLUMN_NAME_BIB+"="+bibnumber, null);
		db.close();
		
	}
	
	public void updateBibLock(int bibnumber, boolean lock) {
		ContentValues contentValues = new ContentValues(1);
		int value = 0;
		if (lock) value = 1;
		contentValues.put(COLUMN_NAME_LOCK, value);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.update(TABLE_NAME_PENALTIES, contentValues, COLUMN_NAME_BIB+"="+bibnumber, null);
		
		db.close();
		
	}
	
	public void updateBibChrono(int chronoType, int bibnumber, long chrono) {
		ContentValues contentValues = new ContentValues(1);
		if (chronoType==Bib.CHRONO_START) contentValues.put(COLUMN_NAME_START, chrono);
		else contentValues.put(COLUMN_NAME_FINISH, chrono);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int row = db.update(TABLE_NAME_PENALTIES, contentValues, COLUMN_NAME_BIB+"="+bibnumber, null);
		System.out.println("Updated rows: "+ row);
		db.close();
		
	}
	
	public void deleteBib(ArrayList<Integer> bibnumberList) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		for (Integer bibnumber: bibnumberList)
			db.delete(TABLE_NAME_PENALTIES, COLUMN_NAME_BIB+"="+bibnumber, null);
		db.close();
	}
}
