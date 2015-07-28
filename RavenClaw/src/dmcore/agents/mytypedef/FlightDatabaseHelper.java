package dmcore.agents.mytypedef;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.example.ravenclaw.R;

import dmcore.agents.coreagents.DMCore;
import dmcore.outputs.MyOutput;

import utils.Const;
import utils.SplitReturnType;
import utils.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FlightDatabaseHelper extends SQLiteOpenHelper{
	
	private static final int DATABASE_VERSION = 1;
	private static final String FLIGHT_TABLE_NAME = "flight_table";
	private static final String FLIGHT_TABLE_CREATE=
			"CREATE TABLE "+FLIGHT_TABLE_NAME+" ("
			+"FlightID integer primary key,"+
			"startDate TEXT,startLoc TEXT,endLoc TEXT,Price integer);";
			
	
	public FlightDatabaseHelper(Context context) {
		super(context, FLIGHT_TABLE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(FLIGHT_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS "+FLIGHT_TABLE_NAME);
        onCreate(db);
	}
	
	// Initialize the database, we write some fake flight information
	public void WriteIntoDatabase(){
		SQLiteDatabase db = this.getWritableDatabase();
		// Access the flightinfo.txt in folder /res/raw 
		InputStream inputStream = MyOutput.getAppContext().
				getResources().openRawResource(R.raw.flightinfo);
		InputStreamReader inputStreamReader = null;
		try {
			inputStreamReader = new InputStreamReader(inputStream, "gbk");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		BufferedReader reader = new BufferedReader(inputStreamReader);
		String line="";
		try {
			while ((line = reader.readLine()) != null) {
				
				// Parse a piece of data to a content
				SplitReturnType srt = Utils.SplitOnFirst(line, ",");
				String sFlightID = srt.FirstPart;
				String sRest = srt.SecondPart;
				int id = Integer.parseInt(sFlightID);
				srt = Utils.SplitOnFirst(sRest, ",");
				String sDate = srt.FirstPart;
				sRest = srt.SecondPart;
				srt = Utils.SplitOnFirst(sRest, ",");
				String sstartLoc = srt.FirstPart;
				sRest = srt.SecondPart;
				srt = Utils.SplitOnFirst(sRest, ",");
				String sendLoc = srt.FirstPart;
				sRest = srt.SecondPart;
				int price = Integer.parseInt(sRest);
				
				// define a new content
				ContentValues content = new ContentValues();
				
				// construct the content
				content.put("FlightID", id);
				content.put("startDate", sDate);
				content.put("startLoc", sstartLoc);
				content.put("endLoc", sendLoc);
				content.put("Price", price);
				
				// insert into the database
				db.insert(FLIGHT_TABLE_NAME,null,content);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		db.close();
	}
	
	public String QueryItem(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(FLIGHT_TABLE_NAME,
				null, null, null, null, null, null);
		int FlightIDIndex = cursor.getColumnIndex("FlightID");
		int startDateIndex = cursor.getColumnIndex("startDate");
		int startLocIndex = cursor.getColumnIndex("startLoc");
		int endLocIndex = cursor.getColumnIndex("endLoc");
		int PriceIndex = cursor.getColumnIndex("Price");
		String sResult = "";
		for(cursor.moveToFirst();!(cursor.isAfterLast());cursor.moveToNext()){ 
			sResult += cursor.getInt(FlightIDIndex)+"\t";
			sResult += cursor.getString(startDateIndex)+"\t";
			sResult += cursor.getString(startLocIndex)+"\t";
			sResult += cursor.getString(endLocIndex)+"\t";
			sResult += cursor.getInt(PriceIndex)+"\n";
		}
		cursor.close();
		db.close();
		return sResult;
				
	}
	
	// L:Match the user's query from database
	public String MatchQuery(String sQuery){
		String sResult = "";
		
		// Parse sQuery from string to <key,value> map
		HashMap<String,String> querymap = new HashMap<String,String>();
		String sFirst = "";
		String sRest = sQuery;
		SplitReturnType srt = null;
		while(!sRest.equals("")){
			srt = Utils.SplitOnFirst(sRest,",");
			sFirst = srt.FirstPart;
			sRest = srt.SecondPart;
			srt = Utils.SplitOnFirst(sFirst, ":");
			String key = srt.FirstPart.trim();
			String value= srt.SecondPart.trim();
			querymap.put(key, value);
		}
		// Get the readable database to query items
		SQLiteDatabase db = DMCore.fdhDatabaseHelper.getReadableDatabase();
		Cursor cursor = db.query(FLIGHT_TABLE_NAME,
				null, null, null, null, null, null);
		for(cursor.moveToFirst();!(cursor.isAfterLast());
				cursor.moveToNext()){
			boolean bFlag = true;
			Iterator<Map.Entry<String,String>> iterator =
					querymap.entrySet().iterator();
			Map.Entry<String, String> iPtr = null;
			while (iterator.hasNext()){
				iPtr = iterator.next();
				int keyIndex = cursor.getColumnIndex(iPtr.getKey());
				String keyValue = cursor.getString(keyIndex);
				if (!keyValue.contains(iPtr.getValue())){
					// not match, break and go on next cursor
					bFlag = false;
					break;
				}
			}
			if (bFlag){
				// bFlag = true means this cursor matches all keys in map
				int FlightIDIndex = cursor.getColumnIndex("FlightID");
				int DateIndex = cursor.getColumnIndex("startDate");
				int startLIndex = cursor.getColumnIndex("startLoc");
				int endLocIndex = cursor.getColumnIndex("endLoc");
				int PriceIndex = cursor.getColumnIndex("Price");
				String ID = cursor.getString(FlightIDIndex);
				String Date = cursor.getString(DateIndex);
				String startL=cursor.getString(startLIndex);
				String endLoc=cursor.getString(endLocIndex);
				String Price = cursor.getString(PriceIndex);
				
				sResult = "符合要求的航班是："+Date+"从"+startL
						+"飞往"+endLoc+"的"+ID+"次航班，票价"
						+Price+"元。";
				Log.d(Const.FLIGHT_DATABASE,"Match query successfully");
				cursor.close();
				db.close();
				return sResult;
			}
		}
		cursor.close();
		db.close();
		sResult = "没有查询到该航班信息";
		return sResult;
	}
	
	public void DeleteDatabase(){
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(FLIGHT_TABLE_NAME, null, null);
	}

}
