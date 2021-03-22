package com.asfoundation.wallet.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.asf.wallet.R;
import com.asfoundation.wallet.ui.widget.OnImportKeystoreListener;
import com.google.android.material.textfield.TextInputEditText;

public class ImportKeystoreFragment extends Fragment implements View.OnClickListener {

  private static final OnImportKeystoreListener dummyOnImportKeystoreListener = (k, p) -> {
  };

  private TextInputEditText keystore;
  private TextInputEditText password;
  @NonNull
  private OnImportKeystoreListener onImportKeystoreListener =
      dummyOnImportKeystoreListener;

  public static ImportKeystoreFragment create() {
    return new ImportKeystoreFragment();
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return LayoutInflater.from(getContext())
        .inflate(R.layout.fragment_import_keystore, container, false);
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    keystore = view.findViewById(R.id.keystore);
    password = view.findViewById(R.id.password);
    view.findViewById(R.id.import_action)
        .setOnClickListener(this);
  }

  @Override public void onClick(View view) {
    this.keystore.setError(null);
    String keystore = this.keystore.getText()
        .toString();
    String password = this.password.getText()
        .toString();
    if (TextUtils.isEmpty(keystore)) {
      this.keystore.setError(getString(R.string.error_field_required));
    } else {
      onImportKeystoreListener.onKeystore(keystore, password);
    }
  }

  public void setOnImportKeystoreListener(
      @Nullable OnImportKeystoreListener onImportKeystoreListener) {
    this.onImportKeystoreListener =
        onImportKeystoreListener == null ? dummyOnImportKeystoreListener : onImportKeystoreListener;
  }
}
