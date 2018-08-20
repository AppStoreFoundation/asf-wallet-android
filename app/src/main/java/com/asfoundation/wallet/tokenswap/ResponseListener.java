package com.asfoundation.wallet.tokenswap;

public interface ResponseListener<T> {
  void onResponse(T t);

  void onError(Throwable error);
}
