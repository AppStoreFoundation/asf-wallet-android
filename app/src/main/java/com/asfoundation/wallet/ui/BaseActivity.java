package com.asfoundation.wallet.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import android.text.SpannableString;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.asf.wallet.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public abstract class BaseActivity extends AppCompatActivity {

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Window window = getWindow();

    // clear FLAG_TRANSLUCENT_STATUS flag:
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

    // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

    // finally change the color
    window.setStatusBarColor(ContextCompat.getColor(this,R.color.statusBarColor));
  }

  protected Toolbar toolbar() {
    Toolbar toolbar = findViewById(R.id.toolbar);
    if (toolbar != null) {
      setSupportActionBar(toolbar);
      toolbar.setTitle(getTitle());
    }
    enableDisplayHomeAsUp();
    return toolbar;
  }

  protected void setTitle(String title) {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle(title);
    }
  }

  protected void setSubtitle(String subtitle) {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setSubtitle(subtitle);
    }
  }

  protected void setCollapsingTitle(SpannableString title) {
    CollapsingToolbarLayout collapsing = findViewById(R.id.toolbar_layout);
    if (collapsing != null) {
      collapsing.setTitle(title);
    }
  }

  protected void enableDisplayHomeAsUp() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  protected void disableDisplayHomeAsUp() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(false);
    }
  }

  protected void hideToolbar() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }
  }

  protected void showToolbar() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.show();
    }
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        break;
    }
    return true;
  }
}
