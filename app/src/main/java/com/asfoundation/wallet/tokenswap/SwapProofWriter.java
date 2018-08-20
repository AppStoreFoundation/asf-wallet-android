package com.asfoundation.wallet.tokenswap;

import java.math.BigInteger;

public interface SwapProofWriter {

  void writeSwapProof(SwapProof swapProof);

  void setListener(ResponseListener resL);

  BigInteger writeGetterSwapProof(SwapProof swapProof);
}
