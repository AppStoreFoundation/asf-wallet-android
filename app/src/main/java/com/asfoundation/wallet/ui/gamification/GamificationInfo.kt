package com.asfoundation.wallet.ui.gamification

import com.appcoins.wallet.gamification.repository.Levels
import java.math.BigDecimal

data class GamificationInfo(val currentLevel: Int, val totalSpend: BigDecimal,
                            val nextLevelAmount: BigDecimal,
                            val levels: List<Levels.Level>,
                            val status: Status) {

  constructor(status: Status) : this(0, BigDecimal.ZERO, BigDecimal.ZERO, emptyList(), status)

}
