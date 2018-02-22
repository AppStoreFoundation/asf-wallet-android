package com.asf.wallet.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.asf.wallet.R;
import com.asf.wallet.entity.ErrorEnvelope;
import com.asf.wallet.entity.Token;
import com.asf.wallet.ui.widget.adapter.ChangeTokenCollectionAdapter;
import com.asf.wallet.viewmodel.TokenChangeCollectionViewModel;
import com.asf.wallet.viewmodel.TokenChangeCollectionViewModelFactory;
import com.asf.wallet.widget.SystemView;
import dagger.android.AndroidInjection;
import javax.inject.Inject;

import static com.asf.wallet.C.ErrorCode.EMPTY_COLLECTION;
import static com.asf.wallet.C.Key.WALLET;

public class TokenChangeCollectionActivity extends BaseActivity implements View.OnClickListener {

  @Inject protected TokenChangeCollectionViewModelFactory viewModelFactory;
  private TokenChangeCollectionViewModel viewModel;

  private ChangeTokenCollectionAdapter adapter;
  private SystemView systemView;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {

    AndroidInjection.inject(this);

    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_tokens);

    toolbar();

    adapter = new ChangeTokenCollectionAdapter(this::onTokenClick, this::onTokenDeleteClick);
    RecyclerView list = findViewById(R.id.list);
    systemView = findViewById(R.id.system_view);
    SwipeRefreshLayout refreshLayout = findViewById(R.id.refresh_layout);

    list.setLayoutManager(new LinearLayoutManager(this));
    list.setAdapter(adapter);

    systemView.attachRecyclerView(list);
    systemView.attachSwipeRefreshLayout(refreshLayout);

    viewModel = ViewModelProviders.of(this, viewModelFactory)
        .get(TokenChangeCollectionViewModel.class);

    viewModel.progress()
        .observe(this, systemView::showProgress);
    viewModel.error()
        .observe(this, this::onError);
    viewModel.tokens()
        .observe(this, this::onTokens);
    viewModel.wallet()
        .setValue(getIntent().getParcelableExtra(WALLET));

    refreshLayout.setOnRefreshListener(viewModel::fetchTokens);
  }

  private void onTokenClick(View view, Token token) {
    viewModel.setEnabled(token);
  }

  private void onTokenDeleteClick(View view, Token token) {
    viewModel.deleteToken(token);
  }

  @Override protected void onResume() {
    super.onResume();

    viewModel.prepare();
  }

  private void onTokens(Token[] tokens) {
    adapter.setTokens(tokens);
  }

  private void onError(ErrorEnvelope errorEnvelope) {
    if (errorEnvelope.code == EMPTY_COLLECTION) {
      systemView.showEmpty(getString(R.string.no_tokens));
    } else {
      systemView.showError(getString(R.string.error_fail_load_tokens), this);
    }
  }

  @Override public void onClick(View view) {
    switch (view.getId()) {
      case R.id.try_again: {
        viewModel.fetchTokens();
      }
      break;
    }
  }
}
