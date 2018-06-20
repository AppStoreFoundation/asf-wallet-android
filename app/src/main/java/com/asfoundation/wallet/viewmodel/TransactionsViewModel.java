package com.asfoundation.wallet.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.format.DateUtils;
import com.asfoundation.wallet.C;
import com.asfoundation.wallet.entity.ErrorEnvelope;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.interact.FetchTransactionsInteract;
import com.asfoundation.wallet.interact.FindDefaultNetworkInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.interact.GetDefaultWalletBalance;
import com.asfoundation.wallet.router.AirdropRouter;
import com.asfoundation.wallet.router.ExternalBrowserRouter;
import com.asfoundation.wallet.router.ManageWalletsRouter;
import com.asfoundation.wallet.router.MyAddressRouter;
import com.asfoundation.wallet.router.MyTokensRouter;
import com.asfoundation.wallet.router.SendRouter;
import com.asfoundation.wallet.router.SettingsRouter;
import com.asfoundation.wallet.router.TransactionDetailRouter;
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.transactions.TransactionsMapper;
import com.asfoundation.wallet.ui.AppcoinsApps;
import com.asfoundation.wallet.ui.appcoins.applications.AppcoinsApplication;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import java.util.Map;

public class TransactionsViewModel extends BaseViewModel {
  private static final long GET_BALANCE_INTERVAL = 10 * DateUtils.SECOND_IN_MILLIS;
  private static final long FETCH_TRANSACTIONS_INTERVAL = 12 * DateUtils.SECOND_IN_MILLIS;
  private final MutableLiveData<NetworkInfo> defaultNetwork = new MutableLiveData<>();
  private final MutableLiveData<Wallet> defaultWallet = new MutableLiveData<>();
  private final MutableLiveData<List<Transaction>> transactions = new MutableLiveData<>();
  private final MutableLiveData<List<AppcoinsApplication>> appcoinsApplications =
      new MutableLiveData<>();
  private final MutableLiveData<Map<String, String>> defaultWalletBalance = new MutableLiveData<>();
  private final FindDefaultNetworkInteract findDefaultNetworkInteract;
  private final FindDefaultWalletInteract findDefaultWalletInteract;
  private final FetchTransactionsInteract fetchTransactionsInteract;
  private final ManageWalletsRouter manageWalletsRouter;
  private final SettingsRouter settingsRouter;
  private final SendRouter sendRouter;
  private final TransactionDetailRouter transactionDetailRouter;
  private final MyAddressRouter myAddressRouter;
  private final MyTokensRouter myTokensRouter;
  private final ExternalBrowserRouter externalBrowserRouter;
  private final CompositeDisposable disposables;
  private final DefaultTokenProvider defaultTokenProvider;
  private final GetDefaultWalletBalance getDefaultWalletBalance;
  private final TransactionsMapper transactionsMapper;
  private final AirdropRouter airdropRouter;
  private final AppcoinsApps applications;
  private Handler handler = new Handler();
  private final Runnable startFetchTransactionsTask = () -> this.fetchTransactions(false);
  private final Runnable startGetBalanceTask = this::getBalance;

  TransactionsViewModel(FindDefaultNetworkInteract findDefaultNetworkInteract,
      FindDefaultWalletInteract findDefaultWalletInteract,
      FetchTransactionsInteract fetchTransactionsInteract, ManageWalletsRouter manageWalletsRouter,
      SettingsRouter settingsRouter, SendRouter sendRouter,
      TransactionDetailRouter transactionDetailRouter, MyAddressRouter myAddressRouter,
      MyTokensRouter myTokensRouter, ExternalBrowserRouter externalBrowserRouter,
      DefaultTokenProvider defaultTokenProvider, GetDefaultWalletBalance getDefaultWalletBalance,
      TransactionsMapper transactionsMapper, AirdropRouter airdropRouter,
      AppcoinsApps applications) {
    this.findDefaultNetworkInteract = findDefaultNetworkInteract;
    this.findDefaultWalletInteract = findDefaultWalletInteract;
    this.fetchTransactionsInteract = fetchTransactionsInteract;
    this.manageWalletsRouter = manageWalletsRouter;
    this.settingsRouter = settingsRouter;
    this.sendRouter = sendRouter;
    this.transactionDetailRouter = transactionDetailRouter;
    this.myAddressRouter = myAddressRouter;
    this.myTokensRouter = myTokensRouter;
    this.externalBrowserRouter = externalBrowserRouter;
    this.defaultTokenProvider = defaultTokenProvider;
    this.getDefaultWalletBalance = getDefaultWalletBalance;
    this.transactionsMapper = transactionsMapper;
    this.airdropRouter = airdropRouter;
    this.applications = applications;
    this.disposables = new CompositeDisposable();
  }

  @Override protected void onCleared() {
    super.onCleared();

    if (!disposables.isDisposed()) {
      disposables.dispose();
    }
    handler.removeCallbacks(startFetchTransactionsTask);
    handler.removeCallbacks(startGetBalanceTask);
  }

  public LiveData<NetworkInfo> defaultNetwork() {
    return defaultNetwork;
  }

  public LiveData<Wallet> defaultWallet() {
    return defaultWallet;
  }

  public LiveData<List<Transaction>> transactions() {
    return transactions;
  }

  public MutableLiveData<Map<String, String>> defaultWalletBalance() {
    return defaultWalletBalance;
  }

  public void prepare() {
    progress.postValue(true);
    disposables.add(findDefaultNetworkInteract.find()
        .subscribe(this::onDefaultNetwork, this::onError));
  }

  public void fetchTransactions(boolean shouldShowProgress) {
    handler.removeCallbacks(startFetchTransactionsTask);
    progress.postValue(shouldShowProgress);
    /*For specific address use: new Wallet("0x60f7a1cbc59470b74b1df20b133700ec381f15d3")*/
    Observable<List<Transaction>> fetch = fetchTransactionsInteract.fetch(defaultWallet.getValue())
        .flatMapSingle(rawTransactions -> transactionsMapper.map(rawTransactions))
        .observeOn(AndroidSchedulers.mainThread());
    disposables.add(
        fetch.subscribe(this::onTransactions, this::onError, this::onTransactionsFetchCompleted));
    if (shouldShowProgress) {
      disposables.add(applications.getApps()
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(appcoinsApplications::postValue));
    }
  }

  private void getBalance() {
    disposables.add(getDefaultWalletBalance.get(defaultWallet.getValue())
        .subscribe(values -> {
          defaultWalletBalance.postValue(values);
          handler.removeCallbacks(startGetBalanceTask);
          handler.postDelayed(startGetBalanceTask, GET_BALANCE_INTERVAL);
        }, throwable -> throwable.printStackTrace()));
  }

  private void onDefaultNetwork(NetworkInfo networkInfo) {
    defaultNetwork.postValue(networkInfo);
    disposables.add(findDefaultWalletInteract.find()
        .subscribe(this::onDefaultWallet, this::onError));
  }

  private void onDefaultWallet(Wallet wallet) {
    defaultWallet.setValue(wallet);
    getBalance();
    fetchTransactions(true);
  }

  private void onTransactions(List<Transaction> transactions) {
    this.transactions.setValue(transactions);
    Boolean last = progress.getValue();
    if (transactions != null && transactions.size() > 0 && last != null && last) {
      progress.postValue(true);
    }
  }

  private void onTransactionsFetchCompleted() {
    progress.postValue(false);
    List<Transaction> transactions = this.transactions.getValue();
    if (transactions == null || transactions.size() == 0) {
      error.postValue(new ErrorEnvelope(C.ErrorCode.EMPTY_COLLECTION, "empty collection"));
    }
    handler.postDelayed(startFetchTransactionsTask, FETCH_TRANSACTIONS_INTERVAL);
  }

  public void showWallets(Context context) {
    manageWalletsRouter.open(context, false);
  }

  public void showSettings(Context context) {
    settingsRouter.open(context);
  }

  public void showSend(Context context) {
    defaultTokenProvider.getDefaultToken()
        .doOnSuccess(defaultToken -> sendRouter.open(context, defaultToken))
        .subscribe();
  }

  public void showDetails(Context context, Transaction transaction) {
    transactionDetailRouter.open(context, transaction);
  }

  public void showMyAddress(Context context) {
    myAddressRouter.open(context, defaultWallet.getValue());
  }

  public void showTokens(Context context) {
    myTokensRouter.open(context, defaultWallet.getValue());
  }

  public void pause() {
    handler.removeCallbacks(startFetchTransactionsTask);
    handler.removeCallbacks(startGetBalanceTask);
  }

  public void openDeposit(Context context, Uri uri) {
    externalBrowserRouter.open(context, uri);
  }

  public void showAirDrop(Context context) {
    airdropRouter.open(context);
  }

  public void onLearnMoreClick(Context context, Uri uri) {
    openDeposit(context, uri);
  }

  public LiveData<List<AppcoinsApplication>> Applications() {
    return appcoinsApplications;
  }

  public void onAppClick(AppcoinsApplication appcoinsApplication, Context context) {
    externalBrowserRouter.open(context,
        Uri.parse("https://www.appstorefoundation.org/offer-wall#spendAppCoinsList"));
  }
}
