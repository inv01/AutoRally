package com.example.autorally;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class RallyActivity extends FragmentActivity implements DetailsFragment.OnTaskChangedListener{
  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (savedInstanceState == null) {
              getSupportFragmentManager().beginTransaction().add(
                  android.R.id.content, new RallyFragment()).commit();
      }
  }

@Override
public void onTaskChanged() {}
}
