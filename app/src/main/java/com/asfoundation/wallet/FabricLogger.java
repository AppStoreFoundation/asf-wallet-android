package com.asfoundation.wallet;

import android.util.Log;

public class FabricLogger implements Logger {
  @Override public void log(Throwable throwable) {
    throwable.printStackTrace();
//    if (Crashlytics.getInstance() != null) {
//      Crashlytics.logException(throwable);
//    }
    Log.e("FabricLogger", "Fabric Logger", throwable);
  }
}
