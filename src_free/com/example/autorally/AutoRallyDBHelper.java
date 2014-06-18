package com.example.autorally;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;
import android.util.SparseArray;

public class AutoRallyDBHelper extends SQLiteOpenHelper {
    
    public static abstract class DbEn implements BaseColumns {
        public static final String TABLE_TRALLY = "TRally";
        public static final String CN_LOC = "location_name";
        public static final String CN_EVENT = "event";
        public static final String CN_TEAM = "team";
        public static final String CN_CARS = "cars";
        
        public static final String TABLE_TAUTO = "TAuto";
        public static final String CN_RALLY_ID = "rally_id";
        public static final String CN_DATE = "date_time";
        public static final String CN_NUMBER = "car_number";
        public static final String CN_RATING = "car_rating";
        public static final String CN_LLNG = "loc_longitude";
        public static final String CN_LLAT = "loc_latitude";
        public static final String CN_NOTE = "interview_note";
        public static final String CN_INT_RATING = "interview_rating";
        public static final String CN_PIC = "pic";
        
        public static final String TABLE_TAUTOLINK = "TAutoLink";
        public static final String CN_AUTO1 = "id_auto1";
        public static final String CN_AUTO2 = "id_auto2";
    }
    
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "AutoRally.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String BLOB_TYPE = " BLOB";
    private static final String REAL_TYPE = " REAL";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private Context mContext;
    /*
     CREATE TABLE TRally ( _id INTEGER PRIMARY KEY AUTOINCREMENT,
         location_name TEXT, 
         event TEXT, 
         team INTEGER,
         cars INTEGER)
     */
    private static final String SQL_CREATE_TRALLY =
        "CREATE TABLE " + DbEn.TABLE_TRALLY + " (" +
        DbEn._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
        DbEn.CN_LOC + TEXT_TYPE + COMMA_SEP +
        DbEn.CN_EVENT + TEXT_TYPE + COMMA_SEP +
        DbEn.CN_TEAM + INT_TYPE + COMMA_SEP +
        DbEn.CN_CARS + INT_TYPE + " )";
    /*
     CREATE TABLE TAuto (_id INTEGER PRIMARY KEY AUTOINCREMENT,
        rally_id INTEGER NOT NULL,
        date_time INTEGER,
        car_number INTEGER,
        car_rating INTEGER DEFAULT 3,
        loc_longitude REAL,
        loc_latitude REAL,
        interview_note TEXT,
        interview_rating INTEGER  DEFAULT 3,
        pic BLOB,
        FOREIGN KEY(rally_id) REFERENCES TRally(_id))
     */
    private static final String SQL_CREATE_TAUTO = 
        "CREATE TABLE " + DbEn.TABLE_TAUTO + " (" +
        DbEn._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
        DbEn.CN_RALLY_ID + INT_TYPE + " NOT NULL" + COMMA_SEP +
        DbEn.CN_DATE + TEXT_TYPE + COMMA_SEP +
        DbEn.CN_NUMBER + INT_TYPE + COMMA_SEP +
        DbEn.CN_RATING + INT_TYPE + " DEFAULT 3 " + COMMA_SEP +
        DbEn.CN_LLNG + REAL_TYPE + COMMA_SEP +
        DbEn.CN_LLAT + REAL_TYPE + COMMA_SEP +
        DbEn.CN_NOTE + TEXT_TYPE + COMMA_SEP +
        DbEn.CN_INT_RATING + INT_TYPE + " DEFAULT 0 " + COMMA_SEP +
        DbEn.CN_PIC + BLOB_TYPE  + COMMA_SEP + 
        " FOREIGN KEY(" + DbEn.CN_RALLY_ID + ") REFERENCES " + DbEn.TABLE_TRALLY +"(_id))";
    /*
    CREATE TABLE TAutoLink ( id_auto1 INTEGER NOT NULL,
        id_auto2 INTEGER NOT NULL,
        FOREIGN KEY(id_auto1,id_auto2) REFERENCES TAuto(_id,_id))
    */
    private static final String SQL_CREATE_TAUTO_LINK = 
            "CREATE TABLE " + DbEn.TABLE_TAUTOLINK + " (" +
            DbEn.CN_AUTO1 + INT_TYPE + " NOT NULL" + COMMA_SEP +
            DbEn.CN_AUTO2 + INT_TYPE + " NOT NULL" + COMMA_SEP +
            " FOREIGN KEY(" + DbEn.CN_AUTO1 + COMMA_SEP + DbEn.CN_AUTO2 + ") REFERENCES " +
            DbEn.TABLE_TAUTO + "(_id, _id))";
        
    
    private static final String SQL_DELETE_RALLY = 
        "DROP TABLE IF EXISTS " + DbEn.TABLE_TRALLY + ";";
    private static final String SQL_DELETE_AUTOLINK =
            "DROP TABLE IF EXISTS " + DbEn.TABLE_TAUTOLINK + ";";
    private static final String SQL_DELETE_AUTO =
            "DROP TABLE IF EXISTS " + DbEn.TABLE_TAUTO + ";";

    public AutoRallyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }
    
    public void recreate(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE_AUTOLINK);
        db.execSQL(SQL_DELETE_AUTO);
        db.execSQL(SQL_DELETE_RALLY);
        db.execSQL(SQL_CREATE_TRALLY);
        db.execSQL(SQL_CREATE_TAUTO);
        db.execSQL(SQL_CREATE_TAUTO_LINK);
    }
    
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TRALLY);
        db.execSQL(SQL_CREATE_TAUTO);
        db.execSQL(SQL_CREATE_TAUTO_LINK);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_AUTOLINK);
        db.execSQL(SQL_DELETE_AUTO);
        db.execSQL(SQL_DELETE_RALLY);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void onRemoveRecord(SQLiteDatabase db, int selectedID){
        Cursor c = db.rawQuery("Select " + DbEn.CN_AUTO2 + 
                " FROM " + DbEn.TABLE_TAUTOLINK +
                " WHERE " + DbEn.CN_AUTO1 + "=" + selectedID, null);
        if (c.getCount() == 0){
            db.execSQL("DELETE FROM " + DbEn.TABLE_TAUTOLINK + 
                   " WHERE " + DbEn.CN_AUTO2 + "=" + selectedID);
        } else {
            db.execSQL("DELETE FROM " + DbEn.TABLE_TAUTOLINK + 
                   " WHERE " + DbEn.CN_AUTO1 + "=" + selectedID);
            while(c.moveToNext()){
                db.execSQL("DELETE FROM " + DbEn.TABLE_TAUTO + 
                        " WHERE " + DbEn._ID + 
                        "=" + c.getInt(c.getColumnIndex(DbEn.CN_AUTO2)));
            }
        }
        c.close();
        db.execSQL("DELETE FROM " + DbEn.TABLE_TAUTO + " WHERE " + DbEn._ID + "=" + selectedID);
    }
    
    public SparseArray<String> getAllCars(SQLiteDatabase db){
        SparseArray<String> sa = new SparseArray<String>();
        sa.put(0, "ADD NEW SHOT");
        SharedPreferences app_preferences = 
                PreferenceManager.getDefaultSharedPreferences(mContext);
        int car_number = app_preferences.getInt("car_number", 0);
        int rally_id = app_preferences.getInt("rally", 0);
        String where = (car_number == 0) ? "" : (" AND " + DbEn.CN_NUMBER + "=" + car_number);
        String select = "SELECT " + DbEn._ID + COMMA_SEP +
                DbEn.CN_DATE + COMMA_SEP + DbEn.CN_NUMBER + COMMA_SEP + DbEn.CN_RATING +
                COMMA_SEP + DbEn.CN_NOTE + COMMA_SEP + DbEn.CN_INT_RATING +
                " FROM " + DbEn.TABLE_TAUTO + 
                " WHERE " + DbEn.CN_RALLY_ID + "=" + rally_id +
                where +
                " order by " + DbEn.CN_DATE + COMMA_SEP + DbEn.CN_RATING;
        Cursor cursor = db.rawQuery(select, null);
        if(cursor.getCount() > 0){
            SimpleDateFormat sf = new SimpleDateFormat("EEE, dd/MM/yyyy HH:mm", Locale.getDefault());
            while (cursor.moveToNext()){
                String ui_date = sf.format(cursor.getLong(cursor.getColumnIndex(DbEn.CN_DATE)));
                String interview = cursor.getString(cursor.getColumnIndex(DbEn.CN_NOTE));
                interview = (((interview == null || interview.isEmpty()) &&
                        (cursor.getInt(cursor.getColumnIndex(DbEn.CN_INT_RATING)) == 0))
                        ? "" : " i");
                String s = ui_date + " Car:" + cursor.getInt(cursor.getColumnIndex(DbEn.CN_NUMBER)) +
                        " R:" + cursor.getInt(cursor.getColumnIndex(DbEn.CN_RATING)) +
                        interview;
                sa.put(cursor.getInt(cursor.getColumnIndex(DbEn._ID)), s);
             }
        }
        cursor.close();
        return sa;
    }
    public SparseArray<String> getAllEvents(SQLiteDatabase db){
        SparseArray<String> sa = new SparseArray<String>();
        sa.put(0, "ADD NEW EVENT");
        String select = "SELECT " + DbEn._ID + COMMA_SEP +
                "ifnull(" + DbEn.CN_EVENT + ",'')" + DbEn.CN_EVENT + COMMA_SEP +
                "ifnull(" + DbEn.CN_LOC + ",'')" + DbEn.CN_LOC + COMMA_SEP +
                DbEn.CN_CARS + COMMA_SEP +
                DbEn.CN_TEAM + 
                " FROM " + DbEn.TABLE_TRALLY + 
                " order by " + DbEn._ID + " desc";
        Cursor cursor = db.rawQuery(select, null);
        if(cursor.getCount() > 0){
            while (cursor.moveToNext()){
                String s = cursor.getString(cursor.getColumnIndex(DbEn.CN_EVENT));
                sa.put(cursor.getInt(cursor.getColumnIndex(DbEn._ID)), 
                        s);
             }
        }
        cursor.close();
        return sa;
    }
    
    public RallyCar getCarById(SQLiteDatabase db, int id){
        String select = "SELECT * FROM " + DbEn.TABLE_TAUTO + " WHERE " + DbEn._ID + " = " + id;
        Cursor c = db.rawQuery(select, null);
        if (c.moveToFirst()){
            Date date_time = new Date(c.getLong(c.getColumnIndex(DbEn.CN_DATE)));
            int car_number = c.getInt(c.getColumnIndex(DbEn.CN_NUMBER));
            int car_rating = c.getInt(c.getColumnIndex(DbEn.CN_RATING));
            double loc_lng = c.getDouble(c.getColumnIndex(DbEn.CN_LLNG));
            double loc_lat = c.getDouble(c.getColumnIndex(DbEn.CN_LLAT));
            String note = c.getString(c.getColumnIndex(DbEn.CN_NOTE));
            int int_rating = c.getInt(c.getColumnIndex(DbEn.CN_INT_RATING));
            byte[] pic = c.getBlob(c.getColumnIndex(DbEn.CN_PIC));
            RallyEvent event = getEventById(db, c.getInt(c.getColumnIndex(DbEn.CN_RALLY_ID)));
            c.close();
            return new RallyCar(id, date_time, car_number, car_rating, loc_lng, loc_lat,
                    pic, int_rating, note, event);
        }
        else {
            c.close();
            RallyCar r = new RallyCar();
            r.setEvent(getEventById(db, getRallyId()));
            return r;
        }
    }
    
    public RallyEvent getEventById(SQLiteDatabase db, int id){
        if(id == -1) return new RallyEvent();
        String select = "SELECT * FROM " + DbEn.TABLE_TRALLY + " WHERE " + DbEn._ID + " = " + id;
        Cursor c = db.rawQuery(select, null);
        if (c.moveToFirst()){
            String event = c.getString(c.getColumnIndex(DbEn.CN_EVENT));
            int team = c.getInt(c.getColumnIndex(DbEn.CN_TEAM));
            int cars = c.getInt(c.getColumnIndex(DbEn.CN_CARS));
            String location_name = c.getString(c.getColumnIndex(DbEn.CN_LOC));
            c.close();
            return new RallyEvent(id, event, team, location_name, cars);
        }
        else {
            c.close();
            return new RallyEvent();
        }
    }
    
    public RallyCar saveCar(SQLiteDatabase db, RallyCar pe){
        ContentValues values = new ContentValues();
        if(pe.getDate() == null) pe.setDate(Calendar.getInstance().getTime());
        values.put(DbEn.CN_DATE, pe.getDate().getTime());
        values.put(DbEn.CN_NUMBER, pe.getCarNumber());
        values.put(DbEn.CN_RATING, pe.getCarRating());
        values.put(DbEn.CN_LLAT, pe.getLocLat());
        values.put(DbEn.CN_LLNG, pe.getLocLong());
        values.put(DbEn.CN_PIC, pe.getPic());
        values.put(DbEn.CN_RALLY_ID, pe.getEvent().getId());
        values.put(DbEn.CN_INT_RATING, pe.getIntRating());
        values.put(DbEn.CN_NOTE, pe.getNote());
        
        if(pe.getId() == 0){
            long newRowId = db.insert(DbEn.TABLE_TAUTO, null, values);
            if (newRowId == -1){Log.e("New row not inserted ", "");
            } else pe.setId((int)newRowId);
        } else {
          String selection = DbEn._ID + " = ?";
          String[] selectionArgs = { String.valueOf(pe.getId())};
          db.update(DbEn.TABLE_TAUTO, values, selection, selectionArgs);
        }
        //saveCarLinks(db, pe);
        return pe;
    }
    
    public RallyEvent saveEvent(SQLiteDatabase db, RallyEvent re){
        ContentValues values = new ContentValues();
        values.put(DbEn.CN_LOC, re.getLocationName());
        values.put(DbEn.CN_TEAM, re.getTeam());
        values.put(DbEn.CN_EVENT, re.getEvent());
        values.put(DbEn.CN_CARS, re.getCars());
        
        if(re.getId() == 0){
            long newRowId = db.insert(DbEn.TABLE_TRALLY, null, values);
            if (newRowId == -1){Log.e("New row not inserted ", "");
            } else re.setId((int)newRowId);
            return re;
        }
        String selection = DbEn._ID + " = ?";
        String[] selectionArgs = { String.valueOf(re.getId())};
        db.update(DbEn.TABLE_TRALLY, values, selection, selectionArgs);
        return re;
    }
    
    public int getRallyId(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sp.getInt("rally", 0);
    }
    
    public HashSet<Integer> getCarIdByNumber(SQLiteDatabase db, RallyCar main_car,  int car_number){
        HashSet<Integer> hs = new HashSet<Integer>();
        //get ids of inserted car timings
        /*if(main_car == null){
            String select = "SELECT "+ DbEn._ID +" FROM " + DbEn.TABLE_TAUTO + 
                    " WHERE " + DbEn.CN_RALLY_ID + " = " + getRallyId() + 
                    " AND " + DbEn.CN_NUMBER + " = " + car_number;
            Cursor c = db.rawQuery(select, null);
            if (c.getCount() > 0){
            while (c.moveToNext()){
                int id = c.getInt(c.getColumnIndex(DbEn._ID));
                if (!hs.contains(id)) hs.add(id);
            }}
            c.close();
        }*/
        //insert new car timing and return id
        if(main_car != null){
            Date date_time = main_car.getDate();
            double loc_lng = main_car.getLocLong();
            double loc_lat = main_car.getLocLat();
            RallyEvent event = main_car.getEvent();
            RallyCar new_car = new RallyCar(0, date_time, car_number, 1, loc_lng, loc_lat,
                    null, 0, "", event);
            new_car = saveCar(db, new_car);
            hs.add(new_car.getId());
         }
        return hs;
    }
    
    public int getCarNumById(SQLiteDatabase db, int id){
        String select = "SELECT "+ DbEn.CN_NUMBER +" FROM " + DbEn.TABLE_TAUTO + 
                " WHERE " + DbEn._ID + " = " + id;
        Cursor c = db.rawQuery(select, null);
        if (c.moveToFirst()){
            int num = c.getInt(c.getColumnIndex(DbEn.CN_NUMBER));
            c.close();
            return num;
        }
        return -1;
    }
    
    public String saveCarLinks(SQLiteDatabase db, RallyCar rc){
        //Delete all carlinks to this car and linked cars in db
        Cursor c = db.rawQuery("Select " + DbEn.CN_AUTO2 + 
                " FROM " + DbEn.TABLE_TAUTOLINK +
                " WHERE " + DbEn.CN_AUTO1 + "=" + rc.getId(), null);
        if (c.getCount() > 0){
            db.execSQL("DELETE FROM " + DbEn.TABLE_TAUTOLINK + 
                   " WHERE " + DbEn.CN_AUTO1 + "=" + rc.getId());
            while(c.moveToNext()){
                db.execSQL("DELETE FROM " + DbEn.TABLE_TAUTO + 
                        " WHERE " + DbEn._ID + 
                        "=" + c.getInt(c.getColumnIndex(DbEn.CN_AUTO2)));
            }
        }
        c.close();
        //end Delete
        
        String bad_cars = "";
        String links = rc.getLinksStr();
        if (links == null || links.isEmpty()) return bad_cars;
        StringTokenizer st = new StringTokenizer(links,",;_ -.*+/");
        if(st.countTokens() > 0){
            while (st.hasMoreTokens()) {
                String scn = st.nextToken();
                int i = 0;
                try {
                    i = Integer.parseInt(scn);
                } catch (NumberFormatException e){
                    bad_cars += " " + scn;
                }
                if (i == 0) continue;
                HashSet<Integer> car_ids = getCarIdByNumber(db, rc, i);
                /*All linked cars should be either existed or already saved with low rating
                 * if (car_ids.isEmpty()) {
                    bad_cars += " " + scn;
                    continue;
                }*/
                Iterator<Integer> itr = car_ids.iterator();
                while (itr.hasNext()){
                    ContentValues values = new ContentValues();
                    values.put(DbEn.CN_AUTO1, rc.getId());
                    values.put(DbEn.CN_AUTO2, itr.next());
                    long newRowId = db.insert(DbEn.TABLE_TAUTOLINK, null, values);
                    if (newRowId == -1){
                        Log.e("New row not inserted ", " car links: " + rc.getCarNumber() + "-" + scn);
                        bad_cars += " " + scn;
                    }
                }
            }
        }
        return bad_cars.trim();
    }
    
    public String getCarLinksById(SQLiteDatabase db, int car_id){
        String links = "";
          String select = "SELECT DISTINCT " + DbEn.CN_NUMBER + " FROM " + DbEn.TABLE_TAUTO + " ta INNER JOIN " + 
              "(SELECT DISTINCT " + DbEn.CN_AUTO2 + 
              " FROM " + DbEn.TABLE_TAUTOLINK + 
                  " WHERE " + DbEn.CN_AUTO1 + " = " + car_id + ") t ON " +
                  DbEn._ID + "=" + DbEn.CN_AUTO2;
          Cursor c = db.rawQuery(select, null);
          if(c.getCount() > 0){
              links = "m";
              while (c.moveToNext()){
                  links += " " + c.getInt(c.getColumnIndex(DbEn.CN_NUMBER));
              }
              c.close();
          }
          if(links == null || links.isEmpty()){
              String search_dep = "Select distinct " + DbEn.CN_NUMBER + " from " + DbEn.TABLE_TAUTO + 
                      " a inner join (Select " + DbEn.CN_AUTO1 + 
                      " car_id from " + DbEn.TABLE_TAUTOLINK + " where " + DbEn.CN_AUTO2 + "=" + car_id +
                      " union Select c1." + DbEn.CN_AUTO2 + " car_id from " + 
                      DbEn.TABLE_TAUTOLINK + " c1 inner join "
                       + DbEn.TABLE_TAUTOLINK + " c2 on c1." + 
                       DbEn.CN_AUTO1 + "= c2." + DbEn.CN_AUTO1 + " and c2." + DbEn.CN_AUTO2 + "=" + car_id +
                      ") c on a." + DbEn._ID + "= c.car_id";
                  Cursor c1 = db.rawQuery(search_dep, null);
                  if(c1.getCount() > 0){
                      links = "d";
                      while (c1.moveToNext()){
                          links += " " + c1.getInt(c1.getColumnIndex(DbEn.CN_NUMBER));
                      }
                      c1.close();
                  }
          }
        return links;
    }
    
    /*public String getCarLinksByNumber(SQLiteDatabase db, int car_number){
        String links = "";
        HashSet<Integer> car_ids = getCarIdByNumber(db, null, car_number);
        if (!car_ids.isEmpty()){
          String str_ids = car_ids.toString().replace('[', '(').replace(']', ')');
          String select = "SELECT DISTINCT " + DbEn.CN_NUMBER + " FROM " + DbEn.TABLE_TAUTO + " ta INNER JOIN " + 
              "(SELECT DISTINCT " + DbEn.CN_AUTO2 + " FROM " + DbEn.TABLE_TAUTOLINK + 
                  " WHERE " + DbEn.CN_AUTO1 + " in " + str_ids + ") t ON " +
                  DbEn._ID + "=" + DbEn.CN_AUTO2;
          Cursor c = db.rawQuery(select, null);
          if(c.getCount() > 0){
              while (c.moveToNext()){
              links += " " + c.getInt(c.getColumnIndex(DbEn.CN_AUTO2));
              }
              c.close();
          }
        }
        return links.trim();
    }
    
    public String getCarAppearByNumber(SQLiteDatabase db, int car_number){
        String links = "";
        int car_id = getCarIdByNumber(db, car_number);
        String select = "SELECT " +DbEn.CN_AUTO1 + " car_id FROM " + DbEn.TABLE_TAUTOLINK + 
                " WHERE " + DbEn.CN_AUTO1 + " = " + car_id + 
                " OR " + DbEn.CN_AUTO2 + " = " + car_id + 
                " UNION " +
                "SELECT " +DbEn.CN_AUTO2 + " car_id FROM " + DbEn.TABLE_TAUTOLINK + 
                " WHERE " + DbEn.CN_AUTO1 + " = " + car_id + 
                " OR " + DbEn.CN_AUTO2 + " = " + car_id;
        Cursor c = db.rawQuery(select, null);
        if(c.getCount() > 0){
            while (c.moveToNext()){
            links += " " + c.getInt(c.getColumnIndex("car_id"));
            }
              c.close();
          }
        return links.trim();
    }*/
}
