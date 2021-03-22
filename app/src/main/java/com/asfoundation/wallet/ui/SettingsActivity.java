package com.asfoundation.wallet.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.MenuItem;
import com.asf.wallet.R;
import com.asfoundation.wallet.router.TransactionsRouter;
import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;


public class SettingsActivity extends BaseActivity {

  @Inject
  DispatchingAndroidInjector<Fragment> fragmentInjector;

  @Override protected void onCreate(Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);
    toolbar();
    // Display the fragment as the main content.
    getFragmentManager().beginTransaction()
        .replace(R.id.fragment_container, new SettingsFragment())
        .commit();
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home: {
        new TransactionsRouter().open(this, true);
        finish();
        return true;
      }
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void onBackPressed() {
    new TransactionsRouter().open(this, true);
    finish();
  }

}
