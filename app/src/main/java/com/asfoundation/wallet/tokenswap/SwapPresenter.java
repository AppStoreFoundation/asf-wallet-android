package com.asfoundation.wallet.tokenswap;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.web3j.utils.Convert;

public class SwapPresenter {

  private SwapView view;
  private SwapInteractor swapInteractor;

  public SwapPresenter(SwapView view, SwapInteractor swapInteractor) {
    this.view = view;
    this.swapInteractor = swapInteractor;
  }

  public void swapEtherToToken(String srcToken, String destToken, String amount, String ToAddress,
      ResponseListener listener) {
    swapInteractor.swapEtherToToken(srcToken, destToken, amount, ToAddress, listener);
    // ToDo: get confirmation of transaction and handle Ux
  }

  public void getRates(String toAddress, String srcToken, String destToken, String tokenAmount) {
    // fetch rates 1:1
    BigInteger rateWei = swapInteractor.getRates(toAddress, srcToken, destToken, tokenAmount);
    // Convert to UI/UX
    Float amount = Float.parseFloat(tokenAmount);
    Float swapValueWei = rateWei.floatValue() * amount;
    String swapValueWeiStr = swapValueWei.toString();
    BigDecimal swapValueEth = Convert.fromWei(swapValueWeiStr, Convert.Unit.ETHER);
    String swapValueEthStr = swapValueEth.toString();
    view.showRates(swapValueEthStr);
  }
}
