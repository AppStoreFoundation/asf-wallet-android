package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.transactions.TransactionsMapper;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import java.util.List;

public class OffChainTransactions {
  private final OffChainTransactionsRepository repository;
  private final TransactionsMapper mapper;
  private final FindDefaultWalletInteract defaultWalletInteract;
  private Scheduler scheduler;

  public OffChainTransactions(OffChainTransactionsRepository repository, TransactionsMapper mapper,
      FindDefaultWalletInteract defaultWalletInteract, Scheduler scheduler) {
    this.repository = repository;
    this.mapper = mapper;
    this.defaultWalletInteract = defaultWalletInteract;
    this.scheduler = scheduler;
  }

  public Single<List<Transaction>> getTransactions() {
    return defaultWalletInteract.find()
        .observeOn(scheduler)
        .flatMap(wallet -> repository.getTransactions(wallet.address))
        .flatMap(channelHistoryResponse -> mapper.map(channelHistoryResponse.getResult()));
  }
}
