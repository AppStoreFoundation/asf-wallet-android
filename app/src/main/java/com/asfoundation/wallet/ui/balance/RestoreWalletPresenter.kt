package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.interact.RestoreWalletInteractor
import com.asfoundation.wallet.interact.WalletModel
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.util.RestoreError
import com.asfoundation.wallet.util.RestoreErrorType
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

class RestoreWalletPresenter(private val view: RestoreWalletView,
                             private val activityView: RestoreWalletActivityView,
                             private val disposable: CompositeDisposable,
                             private val restoreWalletInteractor: RestoreWalletInteractor,
                             private val walletsEventSender: WalletsEventSender,
                             private val logger: Logger,
                             private val viewScheduler: Scheduler,
                             private val computationScheduler: Scheduler) {

  fun present() {
    handleRestoreFromString()
    handleRestoreFromFile()
    handleFileChosen()
    handleOnPermissionsGiven()
  }

  private fun handleOnPermissionsGiven() {
    disposable.add(activityView.onPermissionsGiven()
        .doOnNext { activityView.launchFileIntent(restoreWalletInteractor.getPath()) }
        .subscribe())
  }

  private fun handleFileChosen() {
    disposable.add(activityView.onFileChosen()
        .doOnNext { activityView.showWalletRestoreAnimation() }
        .flatMapSingle { restoreWalletInteractor.readFile(it) }
        .observeOn(computationScheduler)
        .flatMapSingle { fetchWalletModel(it) }
        .observeOn(viewScheduler)
        .doOnNext { handleWalletModel(it) }
        .subscribe({}, {
          logger.log("RestoreWalletPresenter", it)
          activityView.hideAnimation()
          view.showError(RestoreErrorType.INVALID_KEYSTORE)
        })
    )
  }

  private fun handleRestoreFromFile() {
    disposable.add(view.restoreFromFileClick()
        .doOnNext { activityView.askForReadPermissions() }
        .doOnNext {
          walletsEventSender.sendWalletImportRestoreEvent(WalletsAnalytics.ACTION_IMPORT_FROM_FILE,
              WalletsAnalytics.STATUS_SUCCESS)
        }
        .doOnError { t ->
          walletsEventSender.sendWalletImportRestoreEvent(WalletsAnalytics.ACTION_IMPORT_FROM_FILE,
              WalletsAnalytics.STATUS_FAIL, t.message)
        }
        .subscribe())
  }

  private fun handleRestoreFromString() {
    disposable.add(view.restoreFromStringClick()
        .doOnNext {
          activityView.hideKeyboard()
          activityView.showWalletRestoreAnimation()
        }
        .observeOn(computationScheduler)
        .flatMapSingle { fetchWalletModel(it) }
        .observeOn(viewScheduler)
        .doOnNext { handleWalletModel(it) }
        .doOnError { t ->
          walletsEventSender.sendWalletImportRestoreEvent(WalletsAnalytics.ACTION_IMPORT,
              WalletsAnalytics.STATUS_FAIL, t.message)
        }
        .subscribe())
  }

  private fun setDefaultWallet(address: String) {
    disposable.add(restoreWalletInteractor.setDefaultWallet(address)
        .doOnComplete { activityView.showWalletRestoredAnimation() }
        .subscribe())
  }

  private fun handleWalletModel(walletModel: WalletModel) {
    if (walletModel.error.hasError) {
      activityView.hideAnimation()
      if (walletModel.error.type == RestoreErrorType.INVALID_PASS) {
        view.navigateToPasswordView(walletModel.keystore)
        walletsEventSender.sendWalletImportRestoreEvent(WalletsAnalytics.ACTION_IMPORT,
            WalletsAnalytics.STATUS_SUCCESS)
      } else {
        view.showError(walletModel.error.type)
        walletsEventSender.sendWalletImportRestoreEvent(WalletsAnalytics.ACTION_IMPORT,
            WalletsAnalytics.STATUS_FAIL, walletModel.error.type.toString())
      }
    } else {
      setDefaultWallet(walletModel.address)
      walletsEventSender.sendWalletImportRestoreEvent(WalletsAnalytics.ACTION_IMPORT,
          WalletsAnalytics.STATUS_SUCCESS)
    }
  }

  private fun fetchWalletModel(key: String): Single<WalletModel> {
    return if (restoreWalletInteractor.isKeystore(key)) restoreWalletInteractor.restoreKeystore(key)
    else {
      if (key.length == 64) restoreWalletInteractor.restorePrivateKey(key)
      else Single.just(WalletModel(RestoreError(RestoreErrorType.INVALID_PRIVATE_KEY)))
    }
  }

  fun stop() {
    disposable.clear()
  }

}
