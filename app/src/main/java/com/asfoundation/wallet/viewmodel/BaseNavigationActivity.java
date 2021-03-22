package com.asfoundation.wallet.viewmodel;

import android.view.MenuItem;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;

import com.asf.wallet.R;
import com.asfoundation.wallet.ui.BaseActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BaseNavigationActivity extends BaseActivity
    implements BottomNavigationView.OnNavigationItemSelectedListener {

  private BottomNavigationView navigation;

  protected void initBottomNavigation() {
    navigation = findViewById(R.id.bottom_navigation);
    navigation.setOnNavigationItemSelectedListener(this);
  }

  protected void setBottomMenu(@MenuRes int menuRes) {
    navigation.getMenu()
        .clear();
    navigation.inflateMenu(menuRes);
  }

  @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    return false;
  }
}
