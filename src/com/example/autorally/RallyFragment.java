package com.example.autorally;

import com.example.autorally.AutoRallyDBHelper.DbEn;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RallyFragment extends Fragment{
  private View view;
  private EditText edtEvent, edtTeam, edtLocation, edtCars;
  private ListView listview;
  private AutoRallyDBHelper mDbHelper;
  private RallyEvent re;
  
  @Override
  public View onCreateView(LayoutInflater inflater,
          ViewGroup container, Bundle savedInstanceState) {
      if (container == null) {
          return null;
      }
      mDbHelper = new AutoRallyDBHelper(getActivity());
      SQLiteDatabase db = mDbHelper.getWritableDatabase();
      re = mDbHelper.getEventById(db, mDbHelper.getRallyId());
      db.close();
      view = inflater.inflate(R.layout.rally_fragment, container, false);
      edtEvent = (EditText) view.findViewById(R.id.edtEvent);
      edtTeam = (EditText) view.findViewById(R.id.edtTeam);
      edtLocation = (EditText) view.findViewById(R.id.edtLocation);
      edtCars = (EditText) view.findViewById(R.id.edtCars);
      listview = (ListView) view.findViewById(R.id.listview);
      listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
      listview.setSelection(mDbHelper.getRallyId());
      listview.setOnItemClickListener(new OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            initEvent(id);
        }
      });
      setAdapter();
      initViews(re);
      ImageButton btnSave = (ImageButton) view.findViewById(R.id.btnSave);
      btnSave.setOnClickListener(new OnClickListener(){
        @Override
        public void onClick(View v) {
          onSaveEventClick();
        }
      });
      ImageButton btnDel = (ImageButton) view.findViewById(R.id.btnDelete);
      btnDel.setOnClickListener(new OnClickListener(){
        @Override
        public void onClick(View v) {
          onDeleteEventClick();
        }
      });
      return view;
  }
  
  public void initEvent(long id){
      SQLiteDatabase db = mDbHelper.getReadableDatabase();
      re = mDbHelper.getEventById(db, (int)id);
      db.close();
      initViews(re);
  }
  private void onDeleteEventClick(){
      SQLiteDatabase db = mDbHelper.getWritableDatabase();
      db.execSQL("DELETE FROM " + DbEn.TABLE_TAUTO + 
              " WHERE " + DbEn.CN_RALLY_ID + "=" + re.getId());
      db.execSQL("DELETE FROM " + DbEn.TABLE_TRALLY + 
              " WHERE " + DbEn._ID + "=" + re.getId());
      db.close();
      setAdapter();
      re = mDbHelper.getEventById(db, 0);
      initViews(re);
      Toast.makeText(getActivity(), getActivity().
              getResources().getString(R.string.task_deleted),
              Toast.LENGTH_SHORT).show();
  }
  
  private void onSaveEventClick(){
      String s = edtEvent.getText().toString();
      if (s == null || s.isEmpty()){
          Toast.makeText(getActivity(), getActivity().
                  getResources().getString(R.string.eventname_empty),
                  Toast.LENGTH_LONG).show();
          return;
      }
      String s1 = edtTeam.getText().toString();
      if (s1 == null || s1.isEmpty()){
          Toast.makeText(getActivity(), getActivity().
                  getResources().getString(R.string.teamnumber_empty),
                  Toast.LENGTH_LONG).show();
          return;
      }
      String s2 = edtLocation.getText().toString();
      if (s2 == null || s2.isEmpty()){
          Toast.makeText(getActivity(), getActivity().
                  getResources().getString(R.string.locname_empty),
                  Toast.LENGTH_LONG).show();
          return;
      }
      String s3 = edtCars.getText().toString();
      if (s3 == null || s3.isEmpty()){
          Toast.makeText(getActivity(), getActivity().
                  getResources().getString(R.string.carnum_empty),
                  Toast.LENGTH_LONG).show();
          return;
      }
      re.setEvent(s);
      re.setTeam(Integer.parseInt(s1));
      re.setLocationName(s2);
      re.setCars(Integer.parseInt(s3));
      SQLiteDatabase db = mDbHelper.getWritableDatabase();
      re = mDbHelper.saveEvent(db, re);
      db.close();
      setAdapter();
      Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.changes_saved),
              Toast.LENGTH_SHORT).show();
  }
  
  public void onPause(){
      super.onPause();
      SharedPreferences app_preferences = 
              PreferenceManager.getDefaultSharedPreferences(getActivity());
      SharedPreferences.Editor editor = app_preferences.edit();
      editor.putInt("rally", re.getId());
      editor.commit();
  }
  
  public void clearViews(){
      edtEvent.setText("");
      edtTeam.setText("");
      edtLocation.setText("");
      edtCars.setText("");
  }
  
  public void initViews(RallyEvent p){
    clearViews();
    if (p.getId() > 0) {
        edtEvent.setText(p.getEvent());
        edtTeam.setText("" + p.getTeam());
        edtLocation.setText(p.getLocationName());
        edtCars.setText("" + p.getCars());
    }
  }
  
  private static class RallyArrayAdapter extends ArrayAdapter<Object> {
      private final Context context;
      private final SparseArray<String> values;

      public RallyArrayAdapter(Context context, SparseArray<String> values) {
        super(context, android.R.layout.simple_list_item_1, new String[values.size()]);
        this.context = context;
        this.values = values;
      }
      
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        TextView textView = (TextView) rowView.findViewById(android.R.id.text1);
        textView.setText(values.get(values.keyAt(position)));
        return rowView;
      }
    } 
  
  public void setAdapter(){
      AutoRallyDBHelper mDbHelper = new AutoRallyDBHelper(getActivity());
      SQLiteDatabase db = mDbHelper.getReadableDatabase();
      SparseArray<String> list_values = mDbHelper.getAllEvents(db);
      RallyArrayAdapter adapter = new RallyArrayAdapter(getActivity(), list_values);
      listview.setAdapter(adapter);
      db.close();
  }

}
