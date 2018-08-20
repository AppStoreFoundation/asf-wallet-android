package com.asfoundation.wallet.tokenswap;

import android.annotation.SuppressLint;
import com.asfoundation.wallet.repository.TransactionException;
import com.asfoundation.wallet.repository.Web3jProvider;
import io.reactivex.Single;
import io.reactivex.annotations.Nullable;
import io.reactivex.functions.Consumer;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

import static org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction;

public class SwapBlockchainWriter implements SwapProofWriter {

  private final Web3jProvider web3jProvider;
  private final SwapTransactionFactory swapTransactionFactory;
  private SwapDataMapper swapDataMapper;
  private ResponseListener resL;

  public SwapBlockchainWriter(Web3jProvider web3jProvider,
      SwapTransactionFactory swapTransactionFactory, SwapDataMapper swapDataMapper) {
    this.web3jProvider = web3jProvider;
    this.swapTransactionFactory = swapTransactionFactory;
    this.swapDataMapper = swapDataMapper;
  }

  @SuppressLint("CheckResult") @Override public void writeSwapProof(SwapProof swapProof) {
    swapTransactionFactory.createTransaction(swapProof)
        .flatMap(this::sendTransaction)
        .subscribe(new Consumer<Object>() {
          @Override public void accept(Object result) {
            resL.onResponse(result.toString());
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(Throwable error) {
            resL.onError(error);
          }
        });
  }

  @Override public void setListener(@Nullable ResponseListener listener) {
    this.resL = listener;
  }

  //writes Transactions that are only query from the blockchain
  @Override public BigInteger writeGetterSwapProof(SwapProof swapProof) {
    String from = swapProof.getFromAddress();
    String to = swapProof.getToAddress();
    Function getRates = swapDataMapper.getDataExpectedRate(swapProof);
    String encodedFunction = FunctionEncoder.encode(getRates);
    Transaction ethCallTransaction = createEthCallTransaction(from, to, encodedFunction);
    try {
      Future<EthCall> rawResponse = web3jProvider.get(swapProof.getChainId())
          .ethCall(ethCallTransaction, DefaultBlockParameterName.LATEST)
          .sendAsync();
      while (!rawResponse.isDone()) {
      }
      if (!rawResponse.get()
          .hasError()) {
        List<Type> response = FunctionReturnDecoder.decode(rawResponse.get()
            .getValue(), getRates.getOutputParameters());
        return ((Uint) response.get(0)).getValue();
      } else {
        throw new RuntimeException(mapErrorToMessage(rawResponse.get()
            .getError()));
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    return BigInteger.ZERO;
  }

  private Single<String> sendTransaction(byte[] transaction) {
    return Single.fromCallable(() -> {
      EthSendTransaction sentTransaction = web3jProvider.getDefault()
          .ethSendRawTransaction(Numeric.toHexString(transaction))
          .send();
      if (sentTransaction.hasError()) {
        throw new TransactionException(sentTransaction.getError()
            .getCode(), sentTransaction.getError()
            .getMessage(), sentTransaction.getError()
            .getData());
      }
      return sentTransaction.getResult();
    });
  }

  private String mapErrorToMessage(Response.Error error) {
    return "Code: "
        + error.getCode()
        + "\nmessage: "
        + error.getMessage()
        + "\nData: "
        + error.getData();
  }
}
