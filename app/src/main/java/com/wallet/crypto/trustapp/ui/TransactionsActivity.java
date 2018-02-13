package com.wallet.crypto.trustapp.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.wallet.crypto.trustapp.R;
import com.wallet.crypto.trustapp.entity.ErrorEnvelope;
import com.wallet.crypto.trustapp.entity.NetworkInfo;
import com.wallet.crypto.trustapp.entity.Token;
import com.wallet.crypto.trustapp.entity.Transaction;
import com.wallet.crypto.trustapp.entity.Wallet;
import com.wallet.crypto.trustapp.ui.widget.adapter.TransactionsAdapter;
import com.wallet.crypto.trustapp.util.RootUtil;
import com.wallet.crypto.trustapp.viewmodel.BaseNavigationActivity;
import com.wallet.crypto.trustapp.viewmodel.TransactionsViewModel;
import com.wallet.crypto.trustapp.viewmodel.TransactionsViewModelFactory;
import com.wallet.crypto.trustapp.widget.DepositView;
import com.wallet.crypto.trustapp.widget.EmptyTransactionsView;
import com.wallet.crypto.trustapp.widget.SystemView;
import dagger.android.AndroidInjection;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.inject.Inject;

import static com.wallet.crypto.trustapp.C.ETHEREUM_NETWORK_NAME;
import static com.wallet.crypto.trustapp.C.ErrorCode.EMPTY_COLLECTION;

public class TransactionsActivity extends BaseNavigationActivity implements View.OnClickListener {

  @Inject TransactionsViewModelFactory transactionsViewModelFactory;
  private TransactionsViewModel viewModel;

  private SystemView systemView;
  private TransactionsAdapter adapter;
  private Dialog dialog;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_transactions);

    toolbar();
    setTitle(getString(R.string.unknown_balance_with_symbol));
    setSubtitle("");
    initBottomNavigation();
    dissableDisplayHomeAsUp();

    adapter = new TransactionsAdapter(this::onTransactionClick);
    SwipeRefreshLayout refreshLayout = findViewById(R.id.refresh_layout);
    systemView = findViewById(R.id.system_view);

    RecyclerView list = findViewById(R.id.list);

    list.setLayoutManager(new LinearLayoutManager(this));
    list.addItemDecoration(new RecyclerView.ItemDecoration() {
      @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
          RecyclerView.State state) {
        int position = list.getChildAdapterPosition(view);
        if (position == 0) {
          outRect.top = (int) getResources().getDimension(R.dimen.big_margin);
        }
      }
    });
    list.setAdapter(adapter);

    systemView.attachRecyclerView(list);
    systemView.attachSwipeRefreshLayout(refreshLayout);

    viewModel = ViewModelProviders.of(this, transactionsViewModelFactory)
        .get(TransactionsViewModel.class);
    viewModel.progress()
        .observe(this, systemView::showProgress);
    viewModel.error()
        .observe(this, this::onError);
    viewModel.defaultNetwork()
        .observe(this, this::onDefaultNetwork);
    viewModel.defaultWalletBalance()
        .observe(this, this::onBalanceChanged);
    viewModel.defaultWallet()
        .observe(this, this::onDefaultWallet);
    viewModel.transactions()
        .observe(this, this::onTransactions);

    refreshLayout.setOnRefreshListener(() -> viewModel.fetchTransactions(true));
  }

  private void onTransactionClick(View view, Transaction transaction) {
    viewModel.showDetails(view.getContext(), transaction);
  }

  @Override protected void onPause() {
    super.onPause();

    if (dialog != null && dialog.isShowing()) {
      dialog.dismiss();
    }
    viewModel.pause();
  }

  @Override protected void onResume() {
    super.onResume();

    setTitle(getString(R.string.unknown_balance_without_symbol));
    setSubtitle("");
    adapter.clear();
    viewModel.prepare();
    checkRoot();
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_settings, menu);

    NetworkInfo networkInfo = viewModel.defaultNetwork()
        .getValue();
    if (networkInfo != null && networkInfo.name.equals(ETHEREUM_NETWORK_NAME)) {
      getMenuInflater().inflate(R.menu.menu_deposit, menu);
    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_settings: {
        viewModel.showSettings(this);
      }
      break;
      case R.id.action_deposit: {
        openExchangeDialog();
      }
      break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void onClick(View view) {
    switch (view.getId()) {
      case R.id.try_again: {
        viewModel.fetchTransactions(true);
      }
      break;
      case R.id.action_buy: {
        openExchangeDialog();
      }
    }
  }

  @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_my_address: {
        viewModel.showMyAddress(this);
        return true;
      }
      case R.id.action_my_tokens: {
        viewModel.showTokens(this);
        return true;
      }
      case R.id.action_send: {
        viewModel.showSend(this);
        return true;
      }
    }
    return false;
  }

  private void onBalanceChanged(Token token) {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar == null) {
      return;
    }
    BigDecimal decimalDivisor = new BigDecimal(Math.pow(10, token.tokenInfo.decimals));
    BigDecimal ethBalance =
        token.tokenInfo.decimals > 0 ? token.balance.divide(decimalDivisor) : token.balance;
    ethBalance = ethBalance.setScale(4, RoundingMode.HALF_UP)
        .stripTrailingZeros();
    String value = ethBalance.compareTo(BigDecimal.ZERO) == 0 ? "0" : ethBalance.toPlainString();
    actionBar.setTitle(value + " " + token.tokenInfo.symbol);

    String converted = ethBalance.compareTo(BigDecimal.ZERO) == 0 ? "\u2014\u2014"
        : ethBalance.multiply(new BigDecimal(token.ticker.price))
            .setScale(2, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString();
    actionBar.setSubtitle("$" + converted);
  }

  private void onTransactions(Transaction[] transaction) {
    adapter.addTransactions(transaction);
    invalidateOptionsMenu();
  }

  private void onDefaultWallet(Wallet wallet) {
    adapter.setDefaultWallet(wallet);
  }

  private void onDefaultNetwork(NetworkInfo networkInfo) {
    adapter.setDefaultNetwork(networkInfo);
    setBottomMenu(R.menu.menu_main_network);
  }

  private void onError(ErrorEnvelope errorEnvelope) {
    if (errorEnvelope.code == EMPTY_COLLECTION || adapter.getItemCount() == 0) {
      EmptyTransactionsView emptyView = new EmptyTransactionsView(this, this);
      emptyView.setNetworkInfo(viewModel.defaultNetwork()
          .getValue());
      systemView.showEmpty(emptyView);
    }/* else {
            systemView.showError(getString(R.string.error_fail_load_transaction), this);
        }*/
  }

  private void checkRoot() {
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
    if (RootUtil.isDeviceRooted() && pref.getBoolean("should_show_root_warning", true)) {
      pref.edit()
          .putBoolean("should_show_root_warning", false)
          .apply();
      new AlertDialog.Builder(this).setTitle(R.string.root_title)
          .setMessage(R.string.root_body)
          .setNegativeButton(R.string.ok, (dialog, which) -> {
          })
          .show();
    }
  }

  private void openExchangeDialog() {
    Wallet wallet = viewModel.defaultWallet()
        .getValue();
    if (wallet == null) {
      Toast.makeText(this, getString(R.string.error_wallet_not_selected), Toast.LENGTH_SHORT)
          .show();
    } else {
      BottomSheetDialog dialog = new BottomSheetDialog(this);
      DepositView view = new DepositView(this, wallet);
      view.setOnDepositClickListener(this::onDepositClick);
      dialog.setContentView(view);
      BottomSheetBehavior behavior = BottomSheetBehavior.from((View) view.getParent());
      dialog.setOnShowListener(d -> behavior.setPeekHeight(view.getHeight()));
      dialog.show();
      this.dialog = dialog;
    }
  }

  private void onDepositClick(View view, Uri uri) {
    viewModel.openDeposit(view.getContext(), uri);
  }
}
