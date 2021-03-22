package com.asfoundation.wallet;

import androidx.fragment.app.Fragment;
import androidx.multidex.MultiDexApplication;

import com.asfoundation.wallet.di.DaggerAppComponent;
import com.asfoundation.wallet.interact.AddTokenInteract;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.poa.ProofOfAttentionService;
import com.asfoundation.wallet.repository.EthereumNetworkRepositoryType;
import com.asfoundation.wallet.repository.WalletNotFoundException;
import com.asfoundation.wallet.ui.iab.AppcoinsOperationsDataSaver;
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import io.realm.Realm;

public class App extends MultiDexApplication implements HasAndroidInjector {

    private static final String TAG = App.class.getSimpleName();
    @Inject
    EthereumNetworkRepositoryType ethereumNetworkRepository;
    @Inject
    AddTokenInteract addTokenInteract;
    @Inject
    DefaultTokenProvider defaultTokenProvider;
    @Inject
    ProofOfAttentionService proofOfAttentionService;
    @Inject
    InAppPurchaseInteractor inAppPurchaseInteractor;
    @Inject
    AppcoinsOperationsDataSaver appcoinsOperationsDataSaver;
    @Inject
    DispatchingAndroidInjector<Object> dispatchingFragmentInjector;


    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        DaggerAppComponent.builder()
                .application(this)
                .build()
                .inject(this);
//    setupRxJava();

//    Fabric.with(this, new Crashlytics.Builder().core(
//        new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG)
//            .build())
//        .build());

        inAppPurchaseInteractor.start();
        proofOfAttentionService.start();
        appcoinsOperationsDataSaver.start();
        ethereumNetworkRepository.addOnChangeDefaultNetwork(
                networkInfo -> defaultTokenProvider.getDefaultToken()
                        .flatMapCompletable(
                                defaultToken -> addTokenInteract.add(defaultToken.address, defaultToken.symbol,
                                        defaultToken.decimals))
                        .doOnError(throwable -> {
                            if (!(throwable instanceof WalletNotFoundException)) {
                                throwable.printStackTrace();
                            }
                        })
                        .retry()
                        .subscribe());
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return dispatchingFragmentInjector;
    }

//  private void setupRxJava() {
//    RxJavaPlugins.setErrorHandler(throwable -> {
//      if (throwable instanceof UndeliverableException) {
//        Crashlytics crashlytics = Crashlytics.getInstance();
//        if (crashlytics != null && crashlytics.getFabric()
//            .isDebuggable()) {
//          Crashlytics.logException(throwable);
//        } else {
//          throwable.printStackTrace();
//        }
//      } else {
//        throw new RuntimeException(throwable);
//      }
//    });
//  }
}
