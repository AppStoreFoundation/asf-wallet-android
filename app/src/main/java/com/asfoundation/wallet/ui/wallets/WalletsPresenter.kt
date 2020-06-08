package com.asfoundation.wallet.ui.wallets

import com.asfoundation.wallet.logging.Logger
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class WalletsPresenter(private val view: WalletsView,
                       private val walletsInteract: WalletsInteract,
                       private val logger: Logger,
                       private val disposables: CompositeDisposable,
                       private val viewScheduler: Scheduler,
                       private val networkScheduler: Scheduler) {
  fun present() {
    retrieveViewInformation()
    handleActiveWalletCardClick()
    handleOtherWalletCardClick()
    handleCreateNewWalletClick()
    handleRestoreWalletClick()
    handleBottomSheetHeaderClick()
  }

  private fun handleBottomSheetHeaderClick() {
    disposables.add(view.onBottomSheetHeaderClicked()
        .doOnNext { view.changeBottomSheetState() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleRestoreWalletClick() {
    disposables.add(view.restoreWalletClicked()
        .observeOn(viewScheduler)
        .doOnNext { view.navigateToRestoreView() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleCreateNewWalletClick() {
    disposables.add(view.createNewWalletClicked()
        .doOnNext { view.showCreatingAnimation() }
        .observeOn(networkScheduler)
        .flatMapCompletable {
          walletsInteract.createWallet()
              .observeOn(viewScheduler)
              .andThen { view.showWalletCreatedAnimation() }
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleOtherWalletCardClick() {
    disposables.add(view.otherWalletCardClicked()
        .observeOn(viewScheduler)
        .doOnNext { view.navigateToWalletDetailView(it, false) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleActiveWalletCardClick() {
    disposables.add(view.activeWalletCardClicked()
        .observeOn(viewScheduler)
        .doOnNext { view.navigateToWalletDetailView(it, true) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun retrieveViewInformation() {
    disposables.add(walletsInteract.retrieveWalletsModel()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { view.setupUi(it.totalWallets, it.totalBalance, it.walletsBalance) }
        .subscribe({}, { logger.log("WalletsPresenter", it) }))
  }

  fun stop() = disposables.clear()
}
