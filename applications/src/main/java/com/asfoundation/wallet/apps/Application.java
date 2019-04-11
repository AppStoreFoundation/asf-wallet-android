package com.asfoundation.wallet.apps;

public class Application {
  private final String name;
  private final String uniqueName;
  private final double rating;
  private final String iconUrl;
  private final String featuredGraphic;
  private final String packageName;

  public Application(String name, String uniqueName, double rating, String iconUrl,
      String featuredGraphic,
      String packageName) {
    this.name = name;
    this.uniqueName = uniqueName;
    this.rating = rating;
    this.iconUrl = iconUrl;
    this.featuredGraphic = featuredGraphic;
    this.packageName = packageName;
  }

  public String getName() {
    return name;
  }

  @Override public int hashCode() {
    int result;
    long temp;
    result = name.hashCode();
    temp = Double.doubleToLongBits(rating);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + iconUrl.hashCode();
    result = 31 * result + featuredGraphic.hashCode();
    result = 31 * result + packageName.hashCode();
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Application)) return false;

    Application app = (Application) o;

    if (Double.compare(app.rating, rating) != 0) return false;
    if (!name.equals(app.name)) return false;
    if (!iconUrl.equals(app.iconUrl)) return false;
    if (!featuredGraphic.equals(app.featuredGraphic)) return false;
    return packageName.equals(app.packageName);
  }

  @Override public String toString() {
    return "App{" + "name='" + name + '\'' + '}';
  }

  public double getRating() {
    return rating;
  }

  public String getIconUrl() {
    return iconUrl;
  }

  public String getFeaturedGraphic() {
    return featuredGraphic;
  }

  public String getPackageName() {
    return packageName;
  }
}
