package com.asfoundation.wallet.tokenswap;

import com.asf.appcoins.sdk.contractproxy.AppCoinsAddressProxySdk;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.repository.EthereumNetworkRepositoryType;
import com.asfoundation.wallet.repository.PasswordStore;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import com.asfoundation.wallet.repository.Web3jProvider;
import com.asfoundation.wallet.service.AccountKeystoreService;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import org.web3j.protocol.core.DefaultBlockParameterName;

public class SwapTransactionFactory {

  private Web3jProvider web3jProvider;
  private EthereumNetworkRepositoryType networkRepositoryType;
  private AppCoinsAddressProxySdk adsContractAddressProvider;
  private WalletRepositoryType walletRepositoryType;
  private PasswordStore passwordStore;
  private AccountKeystoreService accountKeystoreService;

  public SwapTransactionFactory(Web3jProvider web3jProvider,
      EthereumNetworkRepositoryType networkRepositoryType,
      AppCoinsAddressProxySdk adsContractAddressProvider, WalletRepositoryType walletRepositoryType,
      PasswordStore passwordStore, AccountKeystoreService accountKeystoreService) {

    this.web3jProvider = web3jProvider;
    this.networkRepositoryType = networkRepositoryType;
    this.adsContractAddressProvider = adsContractAddressProvider;
    this.walletRepositoryType = walletRepositoryType;
    this.passwordStore = passwordStore;
    this.accountKeystoreService = accountKeystoreService;
  }

  public Single<byte[]> createTransaction(SwapProof swapProof) {
    return Single.just(this.networkRepositoryType.getDefaultNetwork())
        .subscribeOn(Schedulers.io())
        .flatMap(defaultNetworkInfo -> adsContractAddressProvider.getAdsAddress(
            swapProof.getChainId()) // <---- estou aqui a ir buscar o ads address
            .observeOn(Schedulers.io())
            .doOnSubscribe(disposable -> setNetwork(swapProof.getChainId()))
            .flatMap(adsAddress -> this.walletRepositoryType.getDefaultWallet()
                .flatMap(wallet -> passwordStore.getPassword(wallet)
                    .flatMap(
                        password -> accountKeystoreService.signTransaction(wallet.address, password,
                            swapProof.getToAddress(), swapProof.getAmount(),
                            swapProof.getGasPrice(), swapProof.getGasLimit(),
                            getNonce(wallet.address), swapProof.getData(),
                            swapProof.getChainId()))))
            .doAfterTerminate(
                () -> networkRepositoryType.setDefaultNetworkInfo(defaultNetworkInfo)));
  }

  public void setNetwork(int chainId) {
    for (NetworkInfo networkInfo : networkRepositoryType.getAvailableNetworkList()) {
      if (chainId == networkInfo.chainId) {
        networkRepositoryType.setDefaultNetworkInfo(networkInfo);
      }
    }
  }

  private long getNonce(String address) throws IOException {
    return web3jProvider.getDefault()
        .ethGetTransactionCount(address, DefaultBlockParameterName.LATEST)
        .send()
        .getTransactionCount()
        .longValue();
  }
}
