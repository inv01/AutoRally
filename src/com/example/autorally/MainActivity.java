package com.example.autorally;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;

import java.util.Date;
import jxl.*;
import jxl.write.*; 
import jxl.write.Number;
import jxl.write.biff.RowsExceededException;

import com.example.autorally.AutoRallyDBHelper.DbEn;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends FragmentActivity
        implements DetailsFragment.OnTaskChangedListener {
    private CarListFragment taskList;
    private AutoRallyDBHelper mDbHelper;
    private SQLiteDatabase db;
    private SharedPreferences app_preferences;
    private SharedPreferences.Editor editor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        taskList = new CarListFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, taskList).commit();
        } else {
            taskList = (CarListFragment) getSupportFragmentManager().
                    findFragmentById(R.id.container);
        }
        app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = app_preferences.edit();
        editor.putInt("car_number", 0);
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDbHelper = new AutoRallyDBHelper(this);
        if (mDbHelper.getRallyId() != 0) return;
        
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.alert_request));
        alertDialogBuilder
            .setMessage(getResources().getString(R.string.alert_question_event))
            .setCancelable(false)
            .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    showEventDetails();
                }
              })
              .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    
    private Menu menu;
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void showEventDetails(){
        Intent intent = new Intent();
        intent.setClass(this, RallyActivity.class);
        startActivity(intent);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_event) {
            showEventDetails();
            return true;
        }
        if (id == R.id.action_not_filmed) {
            String msg = showNotFilmed();
            Toast.makeText(this, 
                    getResources().getString(R.string.cars_left) + " " + msg, 
                    Toast.LENGTH_LONG).show();
            return true;
        }
        if (id == R.id.action_filter) {
            setUserCarNumber();
            return true;
        }
        if (id == R.id.action_export) {
            exportAll(false);
        }
        if (id == R.id.action_export_all) {
            exportAll(true);
        }
        if (id == R.id.action_clear) {
            db = mDbHelper.getWritableDatabase();
            //db.execSQL("DELETE FROM " + DbEn.TABLE_TAUTOLINK + ";");
            db.execSQL("DELETE FROM " + DbEn.TABLE_TAUTO + 
                    " WHERE " + DbEn.CN_RALLY_ID + 
                    "=" + mDbHelper.getRallyId() + ";");
            db.close();
            onTaskChanged();
            return true;
        }
        if (id == R.id.action_clear_all) {
            db = mDbHelper.getWritableDatabase();
            mDbHelper.recreate(db);
            this.editor.putInt("rally", 0);
            this.editor.putInt("car_number", 0);
            this.editor.commit();
            db.close();
            
            onOptionsItemSelected(menu.findItem(
                    R.id.action_event));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void setUserCarNumber(){
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.prompts, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
            this);
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
 
        final EditText userInput = (EditText) promptsView
            .findViewById(R.id.editTextDialogUserInput);
 
        // set dialog message
        alertDialogBuilder
          .setCancelable(false)
          .setPositiveButton("OK",
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog,int id) {
                  String s = userInput.getText().toString();
                  int i = 0;
                  if (s != null && !s.isEmpty()) i = Integer.parseInt(s); 
                  editor.putInt("car_number", i);
                  editor.commit();
                  onTaskChanged();
              }
            })
          .setNegativeButton("Cancel",
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog,int id) {
                  editor.putInt("car_number", 0);
                  editor.commit();
                  onTaskChanged();
                  dialog.cancel();
              }
            });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    
    private boolean exportAll(boolean isExpAll){
        String e_state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(e_state)) {
            Toast.makeText(this, "SD card not available.", 
                    Toast.LENGTH_SHORT).show();
            return true;
        }
        new DbAndFilesExport(this, isExpAll).execute();
        return true;
    }
    
    public String showNotFilmed(){
        int id = mDbHelper.getRallyId();
        if (id < 1) return "Please create an event.";
        db = mDbHelper.getReadableDatabase();
        RallyEvent re = mDbHelper.getEventById(db, id);
        int col = re.getCars();
        Cursor c = db.rawQuery("SELECT distinct " + DbEn.CN_NUMBER + 
                " FROM " + DbEn.TABLE_TAUTO + 
                " WHERE " + DbEn.CN_RALLY_ID + "=" + id, null);
        if (c.getCount() == 0) return "No cars filmed yet.";
        
        //String s = "1,2,3,4,5,77,32";
        
        TreeSet<Integer> ta = new TreeSet<Integer>();
        for (int i=1;i<=col;i++)ta.add(i);
        /*StringTokenizer st = new StringTokenizer(s,",;_ -.*+/");
        if(st.countTokens() > 0){
            while (st.hasMoreTokens()) {
                String scn = st.nextToken();
                int i = Integer.parseInt(scn);
                ta.remove(i);
            }*/
            while (c.moveToNext()) {
                int i = c.getInt(c.getColumnIndex(DbEn.CN_NUMBER));
                ta.remove(i);
            }
        c.close();
        db.close();
        String res = "";
        int first = -1; int next = -1;
        int f = -1;
        Iterator<Integer> itr = ta.iterator();
        while (itr.hasNext()){
            first = next;
            next = itr.next().intValue();
            if (next - first > 1) {
                if (f != first){
                    res += ((f < 1) ? "" : "-") + 
                            first + ", " + next;
                } else {
                    res += ((res.isEmpty()) ? "": ", ") + next;
                  }
                f = next;
            }
            if(!itr.hasNext()) res += "-" + next;
        }
        return res;
        //}
    }

    @Override
    public void onTaskChanged() {
        taskList.setAdapter(this);
    }
    
    private class DbAndFilesExport extends AsyncTask<Void, Integer, String> {

        private WritableWorkbook workbook;
        
        private void createExcel() throws IOException, WriteException, RowsExceededException{
            File file_printable = new File(
                    Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            + "/" + reportXLSname);
            workbook = Workbook.createWorkbook(file_printable);
        }
       
       private WritableSheet getInitedSheet(int sheet_i, 
               String sheetName) throws RowsExceededException, WriteException{
           WritableSheet sheet = workbook.createSheet(sheetName, sheet_i);
           //"Date;Location, event name, cars:#;car#;car rating;interview;int rating;loc latitude;loc longitude;team#" + "\r\n");
           sheet.addCell(new Label(0, 0, "Date"));
           sheet.addCell(new Label(1, 0, "Location"));
           sheet.addCell(new Label(2, 0, "Event name"));
           sheet.addCell(new Label(3, 0, "Attending cars"));
           sheet.addCell(new Label(4, 0, "Car Number"));
           sheet.addCell(new Label(5, 0, "Car rating"));
           sheet.addCell(new Label(6, 0, "Interview"));
           sheet.addCell(new Label(7, 0, "Int rating"));
           sheet.addCell(new Label(8, 0, "loc.latitude"));
           sheet.addCell(new Label(9, 0, "loc.longitude"));
           sheet.addCell(new Label(10, 0, "Camera team"));
           for(int i=0; i < 3; i++) {
               CellView cell = sheet.getColumnView(i);
               cell.setAutosize(true);
               sheet.setColumnView(i, cell);
           }
           return sheet;
       }
      @Override
      protected String doInBackground(Void... params) {
          String result = "";
          File download = Environment
                  .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
          /*String destPath = getFilesDir().getPath();
          destPath = destPath.substring(0, destPath.lastIndexOf("/"))
                  + "/databases";
          File fileInDb = new File(destPath + "/"
                  + AutoRallyDBHelper.DATABASE_NAME);
          File fileExtDb = new File(download + "/" + AutoRallyDBHelper.DATABASE_NAME);
          try {
              download.mkdirs();
              copy(fileInDb, fileExtDb);
              Intent intent = new Intent(
                      Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
              intent.setData(Uri.fromFile(fileExtDb));
              sendBroadcast(intent);
              //result = "db";
          } catch (IOException ex) {
              Log.e(ex.getLocalizedMessage(), getResources().getString(R.string.db_not_exp));
          }*/
          db = mDbHelper.getReadableDatabase();
          String where = "";
          if (!isExportAll){
              where = " where r." + 
                      DbEn._ID + "=" + mDbHelper.getRallyId();
          }
          String sql = "Select r." + DbEn._ID + ", a." + DbEn.CN_DATE + 
                  //", r." + DbEn.CN_LOC + "||', '||r." +DbEn.CN_EVENT + "||', cars:'||r." +DbEn.CN_CARS + " " + DbEn.CN_LOC +
                  ", r." + DbEn.CN_LOC + ", r." +DbEn.CN_EVENT + ", r." +DbEn.CN_CARS + 
                  
                  ", a." + DbEn.CN_NUMBER + 
                  ", a." + DbEn.CN_RATING + 
                  ", ifnull(a." + DbEn.CN_NOTE + ",'') " + DbEn.CN_NOTE + 
                  ", a." + DbEn.CN_INT_RATING +
                  ", a." + DbEn.CN_LLAT +
                  ", a." + DbEn.CN_LLNG + 
                  ", r." + DbEn.CN_TEAM +
                  ", a." + DbEn.CN_PIC +
                  " from " + DbEn.TABLE_TAUTO + 
                  " a inner join " + DbEn.TABLE_TRALLY + 
                  " r on a." + DbEn.CN_RALLY_ID + " = r." + DbEn._ID + 
                  where +
                  " order by r." + DbEn._ID + ", a." + DbEn.CN_DATE + ", a." + DbEn.CN_RATING;
          
          Cursor c = db.rawQuery(sql, null);
          /*try {
              File file_printable = new File(
                      Environment
                              .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                              + "/" + reportname);
              FileOutputStream outputStream = new FileOutputStream(
                      file_printable);
              OutputStreamWriter out = new OutputStreamWriter(outputStream);
              BufferedWriter bwriter = new BufferedWriter(out);
              SimpleDateFormat sf = new SimpleDateFormat("EEE, dd/MM/yyyy HH:mm", Locale.getDefault());
              bwriter.write("Date;Location, event name, cars:#;car#;car rating;interview;int rating;loc latitude;loc longitude;team#" + "\r\n");
              while (c.moveToNext()) {
                  String ui_date = sf.format(c.getLong(c.getColumnIndex(DbEn.CN_DATE)));
                  String s = ui_date + ";" + 
                      c.getString(c.getColumnIndex(DbEn.CN_LOC)) + ";" + 
                      c.getInt(c.getColumnIndex(DbEn.CN_NUMBER)) + ";" + 
                      c.getInt(c.getColumnIndex(DbEn.CN_RATING)) + ";" + 
                      c.getString(c.getColumnIndex(DbEn.CN_NOTE)) + ";" + 
                      c.getInt(c.getColumnIndex(DbEn.CN_INT_RATING)) + ";" + 
                      c.getDouble(c.getColumnIndex(DbEn.CN_LLAT)) + ";" + 
                      c.getDouble(c.getColumnIndex(DbEn.CN_LLNG)) + ";" + 
                      c.getInt(c.getColumnIndex(DbEn.CN_TEAM)) + "\r\n";
                  bwriter.write(s);
              }
              bwriter.close();
              c.close();
              File f = new File(
                      Environment
                       .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                         + "/" + reportname);
              Uri contentUri = Uri.fromFile(f);
              Intent mediaScanIntent = new Intent(
                      Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
              sendBroadcast(mediaScanIntent);
              if (result.equals("db")) {
                  result = "db_txt";
              } else {
                  result = "txt";
              }
          } catch (Exception e) {
              Log.e(e.getLocalizedMessage(),
                      getResources().getString(R.string.print_not_exp));
          }*/
          if(c.getCount() > 0)
          try{
              p.setMax(c.getCount());
              createExcel();
              int sheet_i = 0;
              int row_i = 0;
              int id = 0;
              int row_in_event = 0;
              WritableSheet sheet = null;
              while (c.moveToNext()) {
                  p.setProgress(row_i++);
                //"Date;Location, event name, cars:#;car#;car rating;interview;int rating;loc latitude;loc longitude;team#" + "\r\n");
                  int rally_id = c.getInt(c.getColumnIndex(DbEn._ID));
                  String event = c.getString(c.getColumnIndex(DbEn.CN_EVENT));
                  if (id != rally_id){
                      row_in_event = 0;
                      id = rally_id;
                      sheet = getInitedSheet(sheet_i, event);
                      sheet_i++;
                  }
                  row_in_event++;
                  Date now = new Date(c.getLong(c.getColumnIndex(DbEn.CN_DATE))); 
                  DateFormat customDateFormat = new DateFormat ("dd MMM yyyy hh:mm"); 
                  WritableCellFormat dateFormat = new WritableCellFormat (customDateFormat); 
                  DateTime dateCell = new DateTime(0, row_in_event, now, dateFormat); 
                  sheet.addCell(dateCell);
                  sheet.addCell(new Label(1, row_in_event, c.getString(c.getColumnIndex(DbEn.CN_LOC))));
                  sheet.addCell(new Label( 2, row_in_event, event));
                  sheet.addCell(new Number(3, row_in_event, c.getInt(c.getColumnIndex(DbEn.CN_CARS))));
                  sheet.addCell(new Number(4, row_in_event, c.getInt(c.getColumnIndex(DbEn.CN_NUMBER))));
                  sheet.addCell(new Number(5, row_in_event, c.getInt(c.getColumnIndex(DbEn.CN_RATING))));
                  sheet.addCell(new Label(6, row_in_event, c.getString(c.getColumnIndex(DbEn.CN_NOTE))));
                  sheet.addCell(new Number(7, row_in_event, c.getInt(c.getColumnIndex(DbEn.CN_INT_RATING))));
                  sheet.addCell(new Number(8, row_in_event, c.getDouble(c.getColumnIndex(DbEn.CN_LLAT))));
                  sheet.addCell(new Number(9, row_in_event, c.getDouble(c.getColumnIndex(DbEn.CN_LLNG))));
                  sheet.addCell(new Number(10, row_in_event, c.getInt(c.getColumnIndex(DbEn.CN_TEAM))));
                  byte[] bmp = c.getBlob(c.getColumnIndex(DbEn.CN_PIC));
                  if (bmp != null){
                      WritableImage imgobj = new WritableImage(12, row_in_event,
                              3, 8, bmp);
                      row_in_event+=7;
                      sheet.addImage(imgobj);
                  }
              }
              c.close();
              
              db.close();
              workbook.write(); 
              workbook.close();
              //File fileExcel = new File(getFilesDir().getPath() + "/" + reportXLSname);
              File fileExcelInDownloads = new File(download + "/" + reportXLSname);
              try {
                  download.mkdirs();
                  Intent intent = new Intent(
                          Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                  intent.setData(Uri.fromFile(fileExcelInDownloads));
                  sendBroadcast(intent);
              } catch (Exception ex) {
                  Log.e(ex.getLocalizedMessage(), 
                          getResources().getString(R.string.print_not_exp));
                  ex.printStackTrace();
              }
              result = "xls";
          } catch (Exception e) {
              Log.e(e.getLocalizedMessage(),
                      getResources().getString(R.string.print_not_exp));
              e.printStackTrace();
          }
          return result;
      }

      private Context mContext;
      private boolean isExportAll;
      //private String reportname = "RallyReport.txt";
      private String reportXLSname = "RallyReport.xls";
      private ProgressDialog p;
      
      public DbAndFilesExport(Context context, boolean isExportAll) {
          mContext = context;
          this.isExportAll = isExportAll;
          this.p=new ProgressDialog(context);
      }

      @Override
      protected void onPreExecute() {
          Log.i("DbAndFilesExport", "onPreExecute()");
          super.onPreExecute();
          p.setMessage(getResources().getString(R.string.exporting));
          p.setIndeterminate(false);
          p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
          p.setCancelable(false);
          p.show();
      }

      @Override
      protected void onPostExecute(String result) {
          p.dismiss();
          if (result.equals("xls"))
              Toast.makeText(mContext, 
                      reportXLSname + " " + 
                              getResources().getString(R.string.saved_in_downloads), 
                      Toast.LENGTH_SHORT).show();
      }

      @Override
      protected void onProgressUpdate(Integer... values) {
          super.onProgressUpdate(values);
          Log.i("DbAndFilesExport",
                  "onProgressUpdate(): " + String.valueOf(values[0]));
      }
    }
    
}
