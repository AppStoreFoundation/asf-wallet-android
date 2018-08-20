package com.asfoundation.wallet.tokenswap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import com.asf.wallet.R;
import dagger.android.AndroidInjection;
import javax.inject.Inject;

public class SwapEtherToTokenActivity extends AppCompatActivity implements SwapView {
  @Inject SwapInteractor swapInteractor;
  private EditText etSrcAmount;
  private ResponseListener<String> showTxHash;
  private SwapPresenter presenter;
  private EditText amountFromView;
  private EditText amountToView;

  public static Intent newIntent(Context context) {
    return new Intent(context, SwapEtherToTokenActivity.class);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_swap_ether_to_token);
    AndroidInjection.inject(this);
    presenter = new SwapPresenter(this, swapInteractor);

    etSrcAmount = findViewById(R.id.etAmountFrom);
    amountFromView = findViewById(R.id.etAmountFrom);
    amountToView = findViewById(R.id.etAmountTo);

    findViewById(R.id.bSwap).setOnClickListener(v -> clickedSwap());
    findViewById(R.id.imgRates).setOnClickListener(v -> clickedGetRates());
    showTxHash = new ResponseListener<String>() {
      @Override public void onResponse(String txHash) {
        runOnUiThread(new Runnable() {
          @Override public void run() {

            Log.d("swapLog2", "hey");
          }
        });
      }

      @Override public void onError(Throwable error) {
        error.printStackTrace();
      }
    };
  }

  @Override public void clickedGetRates() {
    String toAddress = getString(R.string.RopstenKyberNetworkProxy);
    String srcToken = getString(R.string.RopstenEther);
    String destToken = getString(R.string.RopstenAppCoins);
    String tokenAmount = etSrcAmount.getText()
        .toString();

    presenter.getRates(toAddress, srcToken, destToken, tokenAmount);
  }

  @Override public void showRates(String rates) {
    amountToView.setText(rates);
  }

  @Override public void clickedSwap() {
    String srcToken = getString(R.string.RopstenEther);
    String destToken = getString(R.string.RopstenAppCoins);
    String amount = amountFromView.getText()
        .toString();
    String toAddress = getString(R.string.RopstenKyberNetworkProxy);

    presenter.swapEtherToToken(srcToken, destToken, amount, toAddress, showTxHash);
  }
}




