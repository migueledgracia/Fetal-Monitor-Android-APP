package DatabaseHandler;

import java.util.ArrayList;
import java.util.List;

import com.example.fetalmonitor.model.Patient;


import android.R.string;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper 
{
	
	// All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "FetalMonitor";
 
    // Contacts table name
    private static final String TABLE_REPORTS = "Patients";
 
    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
 
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) 
    {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_REPORTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"+ KEY_NAME + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPORTS);
 
        // Create tables again
        onCreate(db);
    }
    
    // Adding new contact
    public void addUnSubmitedReport(String name) {
    	
    	SQLiteDatabase db = this.getWritableDatabase();
   	 
        ContentValues values = new ContentValues();
        
        values.put(KEY_NAME, name); // Name

        // Inserting Row
        db.insert(TABLE_REPORTS, null, values);
        db.close(); // Closing database connection
    }
     
    // Getting single contact
    public Patient getPatients(int id) 
    {
    	SQLiteDatabase db = this.getReadableDatabase();
    	
        Cursor cursor = db.query(TABLE_REPORTS, new String[] { KEY_ID,
                KEY_NAME}, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        
        if (cursor != null)
            cursor.moveToFirst();
     
        Patient p = new Patient (Integer.parseInt(cursor.getString(0)),cursor.getString(1));
        
        return p;
    }
     
    // Getting All Contacts
    public List<Patient> getAllContacts() {
    	
    	List<Patient> patientstList = new ArrayList<Patient>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_REPORTS;
     
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
     
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Patient p = new Patient();
                
                p.setID(Integer.parseInt(cursor.getString(0)));
                p.setName(cursor.getString(1));
                
                // Adding contact to list
                patientstList.add(p);
                
            } while (cursor.moveToNext());
        }
     
        // return patients list
        return patientstList;
    }
     
    // Getting patients Count
    public int getUnSubmitedReport() {
    	
    	String countQuery = "SELECT  * FROM " + TABLE_REPORTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
 
        // return count
        return cursor.getCount();
    }
    
    // Updating single patient
    public int updateUnSubmitedReport(int id, String name) 
    {
    	SQLiteDatabase db = this.getWritableDatabase();
    	 
        ContentValues values = new ContentValues();

        values.put(KEY_NAME, name);
     
        // updating row
        return db.update(TABLE_REPORTS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
    }
     
    // Deleting single contact
    public void deleteUnSubmitedReport(int id) {
    	
    	SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_REPORTS, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
        db.close();
    }

}
