package com.asfoundation.wallet.promotions

import android.graphics.drawable.Drawable
import androidx.annotation.StringRes
import java.math.BigDecimal

open class Promotion(val id: String)

open class PerkPromotion(id: String, val startDate: Long?, val endDate: Long) : Promotion(id)

class TitleItem(
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
    val isGamificationTitle: Boolean,
    val bonus: String = "0.0",
    id: String = ""
) : Promotion(id)

class DefaultItem(
    id: String,
    val description: String,
    val icon: String?,
    startDate: Long?,
    endDate: Long
) : PerkPromotion(id, startDate, endDate)

class FutureItem(
    id: String,
    val description: String,
    val icon: String?,
    startDate: Long?,
    endDate: Long
) : PerkPromotion(id, startDate, endDate)

class ProgressItem(
    id: String,
    val description: String,
    val icon: String?,
    startDate: Long?,
    endDate: Long,
    val current: BigDecimal,
    val objective: BigDecimal?
) : PerkPromotion(id, startDate, endDate)

class GamificationItem(
    id: String,
    val planet: Drawable?,
    val level: Int,
    val levelColor: Int,
    val title: String,
    val toNextLevelAmount: BigDecimal?,
    var bonus: Double,
    val links: MutableList<GamificationLinkItem>
) : Promotion(id)

class ReferralItem(
    id: String,
    val bonus: BigDecimal,
    val currency: String,
    val link: String
) : Promotion(id)

class GamificationLinkItem(
    id: String,
    val description: String,
    val icon: String?,
    startDate: Long?,
    endDate: Long
) : PerkPromotion(id, startDate, endDate)