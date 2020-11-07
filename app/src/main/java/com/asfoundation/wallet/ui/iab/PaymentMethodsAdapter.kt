package com.asfoundation.wallet.ui.iab

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.jakewharton.rxrelay2.PublishRelay


class PaymentMethodsAdapter(
    private var paymentMethods: List<PaymentMethod>,
    private var paymentMethodId: String,
    private var paymentMethodClick: PublishRelay<Int>) :
    RecyclerView.Adapter<PaymentMethodsViewHolder>() {
  private var selectedItem = -1

  init {
    paymentMethods.forEachIndexed { index, paymentMethod ->
      if (paymentMethod.id == paymentMethodId) selectedItem = index
    }
  }

  fun getSelectedItem() = selectedItem

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentMethodsViewHolder {
    return PaymentMethodsViewHolder(LayoutInflater.from(parent.context)
        .inflate(R.layout.item_payment_method, parent, false))
  }

  override fun getItemCount() = paymentMethods.size

  override fun onBindViewHolder(holder: PaymentMethodsViewHolder, position: Int) {
    holder.bind(paymentMethods[position], selectedItem == position, View.OnClickListener {
      selectedItem = position
      paymentMethodClick.accept(position)
      notifyDataSetChanged()
    })
  }

}