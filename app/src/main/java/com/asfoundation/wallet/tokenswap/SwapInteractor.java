package com.asfoundation.wallet.tokenswap;

import java.math.BigInteger;
import org.web3j.utils.Convert;

public class SwapInteractor {
  private final SwapProofWriter swapBlockchainWriter;
  private final SwapDataMapper swapDataMapper;
  private SwapProofFactory swapProofFactory;

  public SwapInteractor(SwapProofWriter swapBlockchainWriter, SwapDataMapper swapDataMapper) {
    this.swapBlockchainWriter = swapBlockchainWriter;
    this.swapDataMapper = swapDataMapper;
    this.swapProofFactory = new SwapProofFactory();
  }

  public BigInteger getRates(String toAddress, String srcToken, String destToken,
      String tokenAmount) {
    SwapProof swapProof = swapProofFactory.createDefaultSwapProof();
    swapProof.setToAddress(toAddress);
    swapProof.setSrcToken(srcToken);
    swapProof.setDestToken(destToken);
    swapProof.setTokenAmount(Convert.toWei(tokenAmount, Convert.Unit.ETHER));

    BigInteger rateWei = swapBlockchainWriter.writeGetterSwapProof(swapProof);
    return rateWei;
  }

  public void swapEtherToToken(String srcToken, String destToken, String amount, String ToAddress,
      ResponseListener listener) {
    SwapProof swapProof = swapProofFactory.createDefaultSwapProof();

    swapProof.setSrcToken(srcToken);
    swapProof.setDestToken(destToken);
    swapProof.setAmount(Convert.toWei(amount, Convert.Unit.ETHER));
    swapProof.setToAddress(ToAddress);
    swapProof.setData(swapDataMapper.getDataSwapEtherToToken(swapProof));

    swapBlockchainWriter.setListener(listener);
    swapBlockchainWriter.writeSwapProof(swapProof);
  }
}
