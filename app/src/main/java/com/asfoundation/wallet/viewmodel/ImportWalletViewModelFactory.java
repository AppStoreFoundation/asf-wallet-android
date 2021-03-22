package com.asfoundation.wallet.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.asfoundation.wallet.interact.ImportWalletInteract;

public class ImportWalletViewModelFactory implements ViewModelProvider.Factory {

  private final ImportWalletInteract importWalletInteract;

  public ImportWalletViewModelFactory(ImportWalletInteract importWalletInteract) {
    this.importWalletInteract = importWalletInteract;
  }

  @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new ImportWalletViewModel(importWalletInteract);
  }
}
