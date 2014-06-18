package com.example.autorally;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.media.ExifInterface;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class DetailsFragment extends Fragment 
              implements GooglePlayServicesClient.ConnectionCallbacks,
                   GooglePlayServicesClient.OnConnectionFailedListener,
                   LocationListener{
  private View view;
  private EditText note, edtCarNumber;
  private TextView date_text, carNumber;
  private AutoRallyDBHelper mDbHelper;
  private SQLiteDatabase db;
  private RallyCar rc;
  private RatingBar rbRating;
  private ImageButton btnInterview;
  private ImageView imgPlace;
  
  private LocationClient mLocationClient;
  //private Location mCurrentLocation;
  //Define an object that holds accuracy and frequency parameters
  private LocationRequest mLocationRequest;
  private boolean isWithInterview;
  //private SharedPreferences mPrefs;
  //private Editor mEditor;
  
  private final static int
  CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
//Milliseconds per second
  private static final int MILLISECONDS_PER_SECOND = 1000;
  // Update frequency in seconds
  public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
  // Update frequency in milliseconds
  private static final long UPDATE_INTERVAL =
          MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
  // The fastest update frequency, in seconds
  private static final int FASTEST_INTERVAL_IN_SECONDS = 3;
  // A fast frequency ceiling in milliseconds
  private static final long FASTEST_INTERVAL =
          MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
  
  OnTaskChangedListener mCallback;

  // Container Activity must implement this interface
  public interface OnTaskChangedListener {
      public void onTaskChanged();
  }
  
  public static DetailsFragment newInstance(int index) {
      DetailsFragment f = new DetailsFragment();
      Bundle args = new Bundle();
      args.putInt("index", index);
      f.setArguments(args);
      return f;
  }
  
/*
  public static DetailsFragment newInstance(int index, boolean isdual) {
      DetailsFragment f = new DetailsFragment();
      Bundle args = new Bundle();
      args.putInt("index", index);
      //args.putBoolean("isdual", isdual);
      f.setArguments(args);
      return f;
  }*/
  @Override
  public void onAttach(Activity activity) {
      super.onAttach(activity);
      
      // This makes sure that the container activity has implemented
      // the callback interface. If not, it throws an exception
      try {
          mCallback = (OnTaskChangedListener) activity;
      } catch (ClassCastException e) {
          throw new ClassCastException(activity.toString()
                  + " must implement OnTaskChangedListener");
      }
  }
  public int getSelectedCarID() {
      return getArguments().getInt("index", 0);
  }
  public boolean getIsDual() {
      int currentOrientation = getResources().getConfiguration().orientation;
      if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
         // Landscape
          return true;
      }
      else {
         // Portrait
          return false;
      }
      //return getArguments().getBoolean("isdual", false);
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater,
          ViewGroup container, Bundle savedInstanceState) {
      if (container == null) {
          return null;
      }
      
      mDbHelper = new AutoRallyDBHelper(getActivity());
      db = mDbHelper.getWritableDatabase();
      rc = mDbHelper.getCarById(db, getSelectedCarID());
      
      view = inflater.inflate(R.layout.details_fragment, container, false);
      
      date_text = (TextView) view.findViewById(R.id.tvDate);
      rbRating = (RatingBar) view.findViewById(R.id.rbRating);
      note = (EditText) view.findViewById(R.id.edtNote);
      if(getIsDual()){
        edtCarNumber = (EditText) view.findViewById(R.id.edtCarNumber);
      } else {
          carNumber = (TextView) view.findViewById(R.id.tvCarNumber);
          setDigitsListeners();
      }
      imgPlace = (ImageView) view.findViewById(R.id.imgPlace);
      ImageButton btnPicture = (ImageButton) view.findViewById(R.id.btnPicture);
      btnPicture.setOnClickListener(new OnClickListener(){
        @Override
        public void onClick(View v) {
            onPictureClick();
        }
      });
      btnInterview = (ImageButton) view.findViewById(R.id.btnInterview);
      btnInterview.setOnClickListener(new OnClickListener(){
        @Override
        public void onClick(View v) {
            isWithInterview = !isWithInterview;
            showIfInterview(isWithInterview);
        }
      });
        ImageButton btnSave = (ImageButton) view.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveCarClick();
            }
        });
        ImageButton btnRemoveCar = (ImageButton) view
                .findViewById(R.id.btnDelCar);
        btnRemoveCar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onRemoveCarClick();
            }
        });
      initViews(rc);
      
      ImageButton btnNext = (ImageButton) view.findViewById(R.id.btnNext);
      btnNext.setOnClickListener(new OnClickListener(){
        @Override
        public void onClick(View v) {
          onNextClick();
        }
      });
      db.close();
      return view;
  }
  public void setDigitsListeners(){
      TextView btnRemoveDig = (TextView) view.findViewById(R.id.btnDelDig);
      if (btnRemoveDig == null) return;
      btnRemoveDig.setOnClickListener(new OnClickListener(){
          @Override
          public void onClick(View v) {
            onRemoveDigitClick();
          }
      });
      
      LinearLayout digitsContainer = (LinearLayout) view.findViewById(R.id.digits_container);
      for (int i = 0; i < digitsContainer.getChildCount(); i++ ){
          LinearLayout line_i = (LinearLayout) digitsContainer.getChildAt(i);
          for (int j = 0; j < line_i.getChildCount(); j++){
              TextView btn = (TextView) line_i.getChildAt(j);
              btn.setOnClickListener(new OnClickListener(){
                  @Override
                  public void onClick(View v) {
                    onEnterDigitClick(v);
                  }
                });
              if(i == digitsContainer.getChildCount() - 1) break;
          }
      }
  }
  
  static final int REQUEST_IMAGE_CAPTURE = 1;
  public void onPictureClick(){
      PackageManager pm = getActivity().getPackageManager();
      if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)){
          Toast.makeText(getActivity(), 
                  getActivity().getResources().
                      getString(R.string.alert_nocamera), 
                  Toast.LENGTH_SHORT).show();
          return;
      }
      Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      if (takePictureIntent.resolveActivity(pm) != null) {
          File download = Environment
                  .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
          File image = new File(download + "/shotPlace.jpg");
          takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                  Uri.fromFile(image));
          startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
      }
  }
  
  public File getTempFile(Context context, String url) {
      File file = null;
      try {
          String fileName = Uri.parse(url).getLastPathSegment();
          file = File.createTempFile(fileName, null, context.getCacheDir());
      } catch (IOException e) {
          Log.e("No file created : ", e.getMessage());
      }
      return file;
  }
  
  public void onEnterDigitClick(View v){
      String text = carNumber.getText().toString();
      String digit = ((TextView) v).getText().toString();
      carNumber.setText(text + digit);
  }
  public void onRemoveDigitClick(){
      String text = (getIsDual()) ? edtCarNumber.getText().toString() : 
          carNumber.getText().toString();
      if (text != null && !text.isEmpty())
      carNumber.setText(text.substring(0, text.length() - 1));
    }
  public void onSaveCarClick(){
      if (mDbHelper.getRallyId() == 0){
          Toast.makeText(getActivity(), getActivity().
                  getResources().getString(R.string.event_empty),
                  Toast.LENGTH_LONG).show();
          return;
      }
      String s = (getIsDual()) ? edtCarNumber.getText().toString() : carNumber.getText().toString();
      if (s.length() == 0){
          Toast.makeText(getActivity(), getActivity().
                  getResources().getString(R.string.car_number_empty),
                  Toast.LENGTH_LONG).show();
          return;
      }
      db = mDbHelper.getWritableDatabase();
      int car_number = Integer.parseInt(s);
      rc.setCarNumber(car_number);
      rc.setNote(note.getText().toString());
      rc.setCarRating((int) rbRating.getRating());
      if (isWithInterview || 
              (rc.getNote() != null && 
              !rc.getNote().isEmpty())){
          rc.setIntRating((int) rbRating.getRating());
      } else {
          rc.setIntRating(0);
      }
      rc.setEvent(mDbHelper.getEventById(db, mDbHelper.getRallyId()));
      rc = mDbHelper.saveCar(db, rc);
      Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.changes_saved),
              Toast.LENGTH_SHORT).show();
      db.close();
      mCallback.onTaskChanged();
  }
  public void onRemoveCarClick(){
      db = mDbHelper.getWritableDatabase();
      mDbHelper.onRemoveRecord(db, rc.getId());
      rc = new RallyCar();
      rc.setEvent(mDbHelper.getEventById(db, mDbHelper.getRallyId()));
      initViews(rc);
      if (mLocationClient.isConnected()) {
          mLocationClient.requestLocationUpdates(mLocationRequest, this);
      }
      Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.task_deleted),
              Toast.LENGTH_SHORT).show();
      db.close();
      mCallback.onTaskChanged();
  }
  public void onNextClick(){
      onSaveCarClick();
      rc = new RallyCar();
      db = mDbHelper.getWritableDatabase();
      rc.setEvent(mDbHelper.getEventById(db, mDbHelper.getRallyId()));
      initViews(rc);
      if (mLocationClient.isConnected()) {
          mLocationClient.requestLocationUpdates(mLocationRequest, this);
      }
      db.close();
  }
  
  public void clearViews(){
      date_text.setText("");
      note.setText("");
      if (getIsDual()){
          edtCarNumber.setText("");
      } else {
          carNumber.setText("");
      }
      imgPlace.setImageBitmap(null);
      rbRating.setRating(3);
      isWithInterview = false;
  }
  
  public void initViews(RallyCar p){
    clearViews();
    if (p.getId() > 0) {
        if(p.getDate() != null) {
            SimpleDateFormat sf = 
                    new SimpleDateFormat("dd/MM/yyyy HH:mm", 
                            Locale.getDefault());
            date_text.setText(sf.format(p.getDate()));
        }
        note.setText(p.getNote());
        if(getIsDual()){
            edtCarNumber.setText("" + p.getCarNumber());
        } else {
            carNumber.setText("" + p.getCarNumber());
        }
        if (p.getPic() != null){
            byte[] bmp = p.getPic();
            Bitmap bMap = BitmapFactory.decodeByteArray(bmp, 0, bmp.length);
            imgPlace.setImageBitmap(bMap);
        }
        rbRating.setRating(p.getCarRating());
        isWithInterview = (p.getIntRating() > 0 ||
                (p.getNote() != null && 
                !p.getNote().isEmpty())) ? true : false;
    }
    showIfInterview(isWithInterview);
  }
  
  public void showIfInterview(boolean isWithInterview){
      if (isWithInterview){
          this.btnInterview.setBackgroundResource(R.drawable.ic_interview);
      } else {
          this.btnInterview.setBackgroundResource(R.drawable.ic_1);
      }
  }
  
  @Override
  public void onActivityResult(int reqCode, int resultCode, Intent data) {
      super.onActivityResult(reqCode, resultCode, data);
      if(resultCode == Activity.RESULT_CANCELED) return;
      if (reqCode == CONNECTION_FAILURE_RESOLUTION_REQUEST && 
              resultCode == Activity.RESULT_OK) {
          if (servicesConnected()){
            Toast.makeText(
                getActivity().getBaseContext(),
                "Location service is available now", Toast.LENGTH_LONG).show();
          }
          return;
      }
      if (reqCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
          //original solution
          /*  if (data == null) return;
          Bundle extras = data.getExtras();
          Bitmap imageBitmap = (Bitmap) extras.get("data");
          imgPlace.setImageBitmap(imageBitmap);
          
          ByteArrayOutputStream stream = new ByteArrayOutputStream();
          imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
          byte[] byteArray = stream.toByteArray();
          rc.setPic(byteArray);
          onSaveCarClick();*/
          
          String filename = "shotPlace.jpg";
          File filepath = Environment
                  .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
          
          File pictureFile = new File(filepath+"/"+filename);
          ExifInterface exif = null;
          try {
              exif = new ExifInterface(pictureFile.getPath());
          } catch (IOException e) {
              Log.e("onActivityResult could not create ExifInterface:",
                      e.getMessage());
          }
          if (exif == null) return;
          byte[] imageData=exif.getThumbnail();
          Bitmap  imageBitmap= BitmapFactory.decodeByteArray(imageData,0,imageData.length);
          imgPlace.setImageBitmap(imageBitmap);
          ByteArrayOutputStream stream = new ByteArrayOutputStream();
          imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
          byte[] byteArray = stream.toByteArray();
          rc.setPic(byteArray);
          onSaveCarClick();
          pictureFile.delete();
      }
  }
  

  /*
  private static boolean isMockLocationSet(Context context) { 
      if (Settings.Secure.getString(context.getContentResolver(), 
              Settings.Secure.ALLOW_MOCK_LOCATION).contentEquals("1")) { 
          return true;  
      } 
      else { 
          return false;
      } 
  }
  
  private void onRefreshLocationClick(){
      if (servicesConnected()){
          //if (isMockLocationSet(getActivity().getBaseContext()))
          //    mLocationClient.setMockMode(true);
          mLocationClient.requestLocationUpdates(mLocationRequest, this);
          mCurrentLocation = mLocationClient.getLastLocation();
          if (Build.VERSION.SDK_INT >=
              Build.VERSION_CODES.GINGERBREAD
                          &&
              Geocoder.isPresent()) {
            (new GetAddressTask(getActivity().getBaseContext())).execute(mCurrentLocation);
          }
          mLocationClient.removeLocationUpdates(this);
      }
  }*/
  
  private boolean servicesConnected() {
      // Check that Google Play services is available
      int resultCode =
              GooglePlayServicesUtil.
                      isGooglePlayServicesAvailable(getActivity());
      if (ConnectionResult.SUCCESS == resultCode) {
          // In debug mode, log the status
          Log.d("Location Updates",
                  "Google Play services is available.");
          return true;
      // Google Play services was not available for some reason
      } else {
          showErrorDialog(resultCode);
          return false;
      }
  }
    private void showErrorDialog(int errorCode){
      Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
          errorCode,
              getActivity(),
              CONNECTION_FAILURE_RESOLUTION_REQUEST);

      // If Google Play services can provide an error dialog
      if (errorDialog != null) {
          ErrorDialogFragment errorFragment =
                  new ErrorDialogFragment();
          errorFragment.setDialog(errorDialog);
          errorFragment.show(((FragmentActivity) getActivity()).getSupportFragmentManager(),
                  "Location Updates");
      }
    }
  //Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
    /*
  private class GetAddressTask extends AsyncTask<Location, Void, String> {
      Context mContext;
      public GetAddressTask(Context context) {
          super();
          mContext = context;
      }
    @Override
    protected void onPostExecute(String address) {
        Toast.makeText(getActivity(), address, Toast.LENGTH_SHORT).show();
    }
    @Override
    protected String doInBackground(Location... params) {
      Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
      // Get the current location from the input parameter list
      Location loc = params[0];
      // Create a list to contain the result address
      List<Address> addresses = null;
      if(loc != null)
      try {
          rc.setLocLat(loc.getLatitude());
          rc.setLocLong(loc.getLongitude());
          addresses = geocoder.getFromLocation(loc.getLatitude(),
                  loc.getLongitude(), 1);
      } catch (IOException e1) {
      Log.e("LocationSampleActivity",
              "IO Exception in getFromLocation()");
      //e1.printStackTrace();
      return ("IO Exception trying to get address");
      } catch (IllegalArgumentException e2) {
          // Error message to post in the log
          String errorString = "Illegal arguments " +
                  Double.toString(loc.getLatitude()) + " , " +
                  Double.toString(loc.getLongitude()) + " passed to address service";
          Log.e("LocationSampleActivity", errorString);
          //e2.printStackTrace();
          return errorString;
      }
      // If the reverse geocode returned an address
      if (addresses != null && addresses.size() > 0) {
          // Get the first address
          Address address = addresses.get(0);
          //Format the first line of address (if available),
          //city, and country name.
           
          String addressText = String.format(
                  "%s, %s, %s",
                  // If there's a street address, add it
                  address.getMaxAddressLineIndex() > 0 ?
                          address.getAddressLine(0) : "",
                  // Locality is usually a city
                  address.getLocality(),
                  // The country of the address
                  address.getCountryName());
          // Return the text
          return addressText;
      } else {
          return "No address found";
      }
    }
    }*/
  
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
      /*
       * Google Play services can resolve some errors it detects.
       * If the error has a resolution, try sending an Intent to
       * start a Google Play services activity that can resolve
       * error.
       */
      if (connectionResult.hasResolution()) {
          try {
              // Start an Activity that tries to resolve the error
              connectionResult.startResolutionForResult(
                      getActivity(),
                      CONNECTION_FAILURE_RESOLUTION_REQUEST);
              /*
               * Thrown if Google Play services canceled the original
               * PendingIntent
               */
          } catch (IntentSender.SendIntentException e) {
              // Log the error
              e.printStackTrace();
          }
      } else {
          /*
           * If no resolution is available, display a dialog to the
           * user with the error.
           */
          showErrorDialog(connectionResult.getErrorCode());
      }
    }
    
    @Override
    public void onConnected(Bundle arg0) {
      Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT).show();
      if (mDbHelper == null) 
          mDbHelper = new AutoRallyDBHelper(getActivity());
      if (db == null) db = mDbHelper.getWritableDatabase();
      if (rc == null) rc = mDbHelper.getCarById(db, getSelectedCarID());
      if (db.isOpen()) db.close();
      if (rc.getId() == 0) {
          mLocationClient.requestLocationUpdates(mLocationRequest, this);
      }
    }

    @Override
    public void onDisconnected() {
      Toast.makeText(getActivity(), "Disconnected. Please re-connect.",
              Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocationClient = new LocationClient(getActivity().getBaseContext(),
            this, this);
     // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 30 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 5 second
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
    }

    /*
     * Called when the Activity becomes visible.
     */
    @Override
    public void onStart() {
        super.onStart();
        mLocationClient.connect();
    }
    
    @Override
    public void onStop() {
        if (mLocationClient.isConnected()) {
            mLocationClient.removeLocationUpdates(this);
        }
        mLocationClient.disconnect();
        super.onStop();
    }
  //Define the callback method that receives location updates
    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        //mCurrentLocation = location;
        rc.setLocLat(location.getLatitude());
        rc.setLocLong(location.getLongitude());
        String msg = "Updated Location: " +
                location.getLatitude() + "," +
                location.getLongitude();
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }
}
