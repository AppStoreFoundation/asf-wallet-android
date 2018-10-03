package com.asfoundation.wallet.apps.repository.webservice.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Rating {

  @SerializedName("avg") @Expose private float avg;
  @SerializedName("total") @Expose private float total;

  public float getAvg() {
    return avg;
  }

  public void setAvg(int avg) {
    this.avg = avg;
  }

  public float getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }
}
