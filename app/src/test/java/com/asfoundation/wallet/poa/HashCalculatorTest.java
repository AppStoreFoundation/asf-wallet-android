package com.asfoundation.wallet.poa;

import java.security.NoSuchAlgorithmException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HashCalculatorTest {

  private HashCalculator hashCalculator;

  @Before public void before() {
    hashCalculator = new HashCalculator(5, new Calculator());
  }

  @Test public void calculateNonce() throws NoSuchAlgorithmException {
    NonceData nonceData = new NonceData(135852, "package_name_");
    Assert.assertEquals(1, hashCalculator.
        calculateNonce(nonceData));
  }
}
