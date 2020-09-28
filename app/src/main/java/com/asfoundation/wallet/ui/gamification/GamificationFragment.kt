package com.asfoundation.wallet.ui.gamification

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.appcoins.wallet.gamification.LevelModel
import com.asf.wallet.R
import com.asfoundation.wallet.analytics.gamification.GamificationAnalytics
import com.asfoundation.wallet.ui.widget.MarginItemDecoration
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.bonus_updated_layout.*
import kotlinx.android.synthetic.main.fragment_gamification.*
import java.math.BigDecimal
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class GamificationFragment : BasePageViewFragment(), GamificationView {

  @Inject
  lateinit var interactor: GamificationInteractor

  @Inject
  lateinit var analytics: GamificationAnalytics

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var mapper: GamificationMapper
  private lateinit var presenter: GamificationPresenter
  private lateinit var activityView: GamificationActivityView
  private lateinit var levelsAdapter: LevelsAdapter
  private var uiEventListener: PublishSubject<Boolean>? = null

  override fun onAttach(context: Context) {
    super.onAttach(context)
    require(
        context is GamificationActivityView) { GamificationFragment::class.java.simpleName + " needs to be attached to a " + GamificationActivityView::class.java.simpleName }
    activityView = context
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    uiEventListener = PublishSubject.create()
    presenter =
        GamificationPresenter(this, activityView, interactor, analytics, formatter,
            CompositeDisposable(), AndroidSchedulers.mainThread(), Schedulers.io())
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_gamification, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present(savedInstanceState)
  }

  override fun displayGamificationInfo(currentLevel: Int,
                                       nextLevelAmount: BigDecimal?,
                                       hiddenLevels: List<LevelModel>,
                                       shownLevels: List<LevelModel>,
                                       totalSpend: BigDecimal,
                                       updateDate: Date?) {
    levelsAdapter =
        LevelsAdapter(context!!, hiddenLevels, shownLevels, totalSpend, currentLevel,
            nextLevelAmount, formatter,
            mapper, uiEventListener!!)
    gamification_recycler_view.addItemDecoration(
        MarginItemDecoration(resources.getDimension(R.dimen.gamification_card_margin)
            .toInt()))
    gamification_recycler_view.adapter = levelsAdapter
    handleBonusUpdatedText(updateDate)
  }

  override fun showHeaderInformation(totalSpent: String, bonusEarned: String, symbol: String) {
    bonus_earned.text = getString(R.string.value_fiat, symbol, bonusEarned)
    total_spend.text = getString(R.string.gamification_how_table_a2, totalSpent)

    bonus_earned_skeleton.visibility = View.INVISIBLE
    total_spend_skeleton.visibility = View.INVISIBLE
    bonus_earned.visibility = View.VISIBLE
    total_spend.visibility = View.VISIBLE
  }

  override fun getToggleButtonClick() = uiEventListener!!

  override fun toggleReachedLevels(show: Boolean) {
    levelsAdapter.toggleReachedLevels(show)
    gamification_scroll_view.scrollTo(0, 0)
  }

  private fun handleBonusUpdatedText(updateDate: Date?) {
    if (updateDate != null) {
      val df: DateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
      val date = df.format(updateDate)
      bonus_update_text.text = getString(R.string.pioneer_bonus_updated_body, date)
      bonus_update.visibility = View.VISIBLE
    }
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }
}
