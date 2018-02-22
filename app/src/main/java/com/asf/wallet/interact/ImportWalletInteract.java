package com.asf.wallet.interact;

import com.asf.wallet.entity.Wallet;
import com.asf.wallet.interact.rx.operator.Operators;
import com.asf.wallet.repository.PasswordStore;
import com.asf.wallet.repository.WalletRepositoryType;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class ImportWalletInteract {

  private final WalletRepositoryType walletRepository;
  private final PasswordStore passwordStore;

  public ImportWalletInteract(WalletRepositoryType walletRepository, PasswordStore passwordStore) {
    this.walletRepository = walletRepository;
    this.passwordStore = passwordStore;
  }

  public Single<Wallet> importKeystore(String keystore, String password) {
    return passwordStore.generatePassword()
        .flatMap(
            newPassword -> walletRepository.importKeystoreToWallet(keystore, password, newPassword)
                .compose(Operators.savePassword(passwordStore, walletRepository, newPassword)))
        .observeOn(AndroidSchedulers.mainThread());
  }

  public Single<Wallet> importPrivateKey(String privateKey) {
    return passwordStore.generatePassword()
        .flatMap(newPassword -> walletRepository.importPrivateKeyToWallet(privateKey, newPassword)
            .compose(Operators.savePassword(passwordStore, walletRepository, newPassword)))
        .observeOn(AndroidSchedulers.mainThread());
  }
}
