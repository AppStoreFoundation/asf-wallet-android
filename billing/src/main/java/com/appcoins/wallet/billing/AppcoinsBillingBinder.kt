package com.appcoins.wallet.billing

import android.content.pm.PackageManager
import android.os.Binder
import android.os.Bundle
import android.os.Parcel
import android.os.RemoteException
import com.appcoins.billing.AppcoinsBilling
import com.appcoins.wallet.billing.repository.BillingSupportedType
import com.appcoins.wallet.billing.repository.entity.Purchase
import com.appcoins.wallet.billing.mappers.ExternalBillingSerializer
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.ArrayList

internal class AppcoinsBillingBinder(private val supportedApiVersion: Int,
                                     private val billingMessagesMapper: BillingMessagesMapper,
                                     private var packageManager: PackageManager,
                                     private val billingFactory: BillingFactory,
                                     private val serializer: ExternalBillingSerializer) :
    AppcoinsBilling.Stub() {
  companion object {
    internal const val RESULT_OK = 0 // success
    internal const val RESULT_USER_CANCELED = 1 // user pressed back or canceled a dialog
    internal const val RESULT_SERVICE_UNAVAILABLE = 2 // The network connection is down
    internal const val RESULT_BILLING_UNAVAILABLE =
        3 // this billing API version is not supported for the type requested
    internal const val RESULT_ITEM_UNAVAILABLE = 4 // requested SKU is not available for purchase
    internal const val RESULT_DEVELOPER_ERROR = 5 // invalid arguments provided to the API
    internal const val RESULT_ERROR = 6 // Fatal error during the API action
    internal const val RESULT_ITEM_ALREADY_OWNED =
        7 // Failure to purchase since item is already owned
    internal const val RESULT_ITEM_NOT_OWNED = 8 // Failure to consume since item is not owned

    internal const val RESPONSE_CODE = "RESPONSE_CODE"
    internal const val INAPP_PURCHASE_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST"
    internal const val INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST"
    internal const val INAPP_DATA_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST"


    internal const val ITEM_TYPE_INAPP = "inapp"
    internal const val ITEM_TYPE_SUBS = "subs"
    internal const val DETAILS_LIST = "DETAILS_LIST"
    internal const val ITEM_ID_LIST = "ITEM_ID_LIST"

  }

  private lateinit var billing: Billing
  private lateinit var merchantName: String

  @Throws(RemoteException::class)
  override fun onTransact(code: Int, data: Parcel, reply: Parcel, flags: Int): Boolean {
    merchantName = packageManager.getPackagesForUid(Binder.getCallingUid())!![0]
    billing = billingFactory.getBilling(merchantName)
    return super.onTransact(code, data, reply, flags)
  }

  override fun isBillingSupported(apiVersion: Int, packageName: String?, type: String?): Int {
    if (apiVersion != supportedApiVersion || packageName == null || packageName.isBlank() || type == null || type.isBlank()) {
      return RESULT_BILLING_UNAVAILABLE
    }
    return when (type) {
      ITEM_TYPE_INAPP -> {
        billing.isInAppSupported()
      }
      ITEM_TYPE_SUBS -> {
        billing.isSubsSupported()
      }
      else -> Single.just(Billing.BillingSupportType.UNKNOWN_ERROR)
    }.subscribeOn(Schedulers.io())
        .map { supported -> billingMessagesMapper.mapSupported(supported) }
        .blockingGet()
  }

  override fun getSkuDetails(apiVersion: Int, packageName: String?, type: String?,
                             skusBundle: Bundle?): Bundle {
    val result = Bundle()

    if (skusBundle == null || !skusBundle.containsKey(
            ITEM_ID_LIST) || apiVersion != supportedApiVersion || type == null || type.isBlank()) {
      result.putInt(RESPONSE_CODE, RESULT_DEVELOPER_ERROR)
      return result
    }

    val skus = skusBundle.getStringArrayList(ITEM_ID_LIST)

    if (skus == null || skus.size <= 0) {
      result.putInt(RESPONSE_CODE, RESULT_DEVELOPER_ERROR)
      return result
    }

    return try {
      val serializedProducts: List<String> = billing.getProducts(skus, type)
          .flatMap { Single.just(serializer.serializeProducts(it)) }.subscribeOn(Schedulers.io())
          .blockingGet()
      billingMessagesMapper.mapSkuDetails(serializedProducts)
    } catch (exception: Exception) {
      exception.printStackTrace()
      billingMessagesMapper.mapSkuDetailsError(exception)
    }
  }

  override fun getBuyIntent(apiVersion: Int, packageName: String?, sku: String?, type: String?,
                            developerPayload: String?): Bundle {
    TODO(
        "not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getPurchases(apiVersion: Int, packageName: String?, type: String?,
                            continuationToken: String?): Bundle {
    val result = Bundle()

    if (apiVersion != supportedApiVersion) {
      result.putInt(RESPONSE_CODE, RESULT_DEVELOPER_ERROR)
      return result
    }

    val dataList = ArrayList<String>()
    val signatureList = ArrayList<String>()
    val skuList = ArrayList<String>()

    if (type == ITEM_TYPE_INAPP) {
      try {
        val purchases =
            billing.getPurchases(BillingSupportedType.INAPP)
                .blockingGet()

        purchases.forEach { purchase: Purchase ->
          dataList.add(serializer.serializeSignatureData(purchase))
          signatureList.add(purchase.signature.value)
          skuList.add(purchase.product.name)
        }
      } catch (exception: Exception) {
        return billingMessagesMapper.mapPurchasesError(exception)
      }

    }

    result.putStringArrayList(INAPP_PURCHASE_DATA_LIST, dataList)
    result.putStringArrayList(INAPP_PURCHASE_ITEM_LIST, skuList)
    result.putStringArrayList(INAPP_DATA_SIGNATURE_LIST, signatureList)
    result.putInt(RESPONSE_CODE, RESULT_OK)
    return result
  }

  override fun consumePurchase(apiVersion: Int, packageName: String?, purchaseToken: String?): Int {
    TODO(
        "not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}