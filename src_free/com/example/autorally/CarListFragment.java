package com.example.autorally;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
//import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CarListFragment extends ListFragment {
  boolean mDualPane;
  int mCurCheckPosition = 0;
  private SparseArray<String> list_values;
  private Context mContext;
  private EventsArrayAdapter adapter;

  private static class EventsArrayAdapter extends ArrayAdapter<Object> {
      private final Context context;
      private final SparseArray<String> values;

      public EventsArrayAdapter(Context context, SparseArray<String> values) {
        super(context, android.R.layout.simple_list_item_1, new String[values.size()]);
        this.context = context;
        this.values = values;
      }
      /*
      public void clear(){
          this.values = new SparseArray<String>();
          values.put(0, "ADD NEW SHOT");
          notifyDataSetChanged();
      }
      
      public void addAll(SparseArray<String> values){
          this.values = values;
          notifyDataSetChanged();
      }*/
      
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
      AutoRallyDBHelper mDbHelper = new AutoRallyDBHelper(mContext);
      SQLiteDatabase db = mDbHelper.getReadableDatabase();
      list_values = mDbHelper.getAllCars(db);
      //if (adapter == null) {
          adapter = new EventsArrayAdapter(mContext, list_values);
          setListAdapter(adapter);
          //adapter.notifyDataSetChanged();
      /*} else {
          adapter.clear();
          adapter.addAll(list_values);
          adapter.notifyDataSetChanged();
          this.getListView().refreshDrawableState();
      }*/
      db.close();
  }

  public void onConfigurationChanged(Configuration newConfig){
      super.onConfigurationChanged(newConfig);
      mContext = getActivity();
  }
  
  public void setAdapter(Context context){
      if (mContext == null) mContext = context;
      setAdapter();
  }
  public void onStart(){
      super.onStart();
      setAdapter();
  }
  
  @Override
  public void onActivityCreated(Bundle savedState) {
      super.onActivityCreated(savedState);
      mContext = getActivity();
      
      View detailsFrame = getActivity().findViewById(R.id.details);
      mDualPane = detailsFrame != null
              && detailsFrame.getVisibility() == View.VISIBLE;

      if (savedState != null) {
          mCurCheckPosition = savedState.getInt("curChoice", 0);
      }
      /*
      if (mDualPane) {
          setAdapter();
          getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
          showDetails(mCurCheckPosition);
      }*/
  }
  
  @Override
  public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      outState.putInt("curChoice", mCurCheckPosition);
  }

  @Override
  public void onListItemClick(ListView l, View v, int pos, long id) {
      if (new AutoRallyDBHelper(getActivity()).getRallyId() == 0){
          Toast.makeText(getActivity(), getActivity().
                  getResources().getString(R.string.event_empty),
                  Toast.LENGTH_LONG).show();
          return;
      }
      showDetails(pos);
  }

  /**
   * Helper function to show the details of a selected item, either by
   * displaying a fragment in-place in the current UI, or starting a
   * whole new activity in which it is displayed.
   */
  void showDetails(int index) {
      mCurCheckPosition = index;

      /*if (mDualPane) {
          getListView().setItemChecked(index, true);

          DetailsFragment details = (DetailsFragment)
                  getFragmentManager().findFragmentById(R.id.details);
          if (details == null || details.getSelectedCarID() != index) {
              details = DetailsFragment.newInstance(list_values.keyAt(index));

              FragmentTransaction ft
                      = getFragmentManager().beginTransaction();
              ft.replace(R.id.details, details);
              ft.setTransition(
                      FragmentTransaction.TRANSIT_FRAGMENT_FADE);
              ft.commit();
          }

      } else {*/
          Intent intent = new Intent();
          intent.setClass(mContext, DetailsActivity.class);
          intent.putExtra("index", list_values.keyAt(index));
          startActivity(intent);
      //}
  }
}
