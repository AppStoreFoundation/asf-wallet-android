package com.asfoundation.wallet.apps.repository;

import com.asfoundation.wallet.apps.Application;
import com.asfoundation.wallet.apps.ApplicationsApi;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.List;

public class NetworkAppsRepository implements Repository {
  private final ApplicationsApi appsApi;

  public NetworkAppsRepository(ApplicationsApi appsApi) {
    this.appsApi = appsApi;
  }

  @Override public Single<List<Application>> getApps() {
    return appsApi.getApplications()
        .map(this::map);
  }

  private List<Application> map(
      com.asfoundation.wallet.apps.repository.webservice.data.Application applications) {
    ArrayList<Application> apps = new ArrayList<>();
    for (com.asfoundation.wallet.apps.repository.webservice.data.List application :
        applications.getDatalist()
        .getList()) {
      apps.add(new Application(application.getName(), application.getUname(), application.getStats()
          .getPrating()
          .getAvg(), application.getIcon(), application.getGraphic(), application.getPackage()));
    }
    return apps;
  }
}
