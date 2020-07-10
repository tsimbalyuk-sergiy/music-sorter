package dev.tsvinc.music.sort.domain;

import java.util.Objects;

public class DbProperties {
  private String dbLocation;
  private String dbUsername;
  private String dbPassword;

  public DbProperties(String dbLocation, String dbUsername, String dbPassword) {
    this.dbLocation = dbLocation;
    this.dbUsername = dbUsername;
    this.dbPassword = dbPassword;
  }

  public DbProperties() {}

  public static DbPropertiesBuilder builder() {
    return new DbPropertiesBuilder();
  }

  public String getDbLocation() {
    return this.dbLocation;
  }

  public void setDbLocation(String dbLocation) {
    this.dbLocation = dbLocation;
  }

  public String getDbUsername() {
    return this.dbUsername;
  }

  public void setDbUsername(String dbUsername) {
    this.dbUsername = dbUsername;
  }

  public String getDbPassword() {
    return this.dbPassword;
  }

  public void setDbPassword(String dbPassword) {
    this.dbPassword = dbPassword;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DbProperties that = (DbProperties) o;
    return Objects.equals(dbLocation, that.dbLocation)
        && Objects.equals(dbUsername, that.dbUsername)
        && Objects.equals(dbPassword, that.dbPassword);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dbLocation, dbUsername, dbPassword);
  }

  @Override
  public String toString() {
    return "DbProperties{"
        + "dbLocation='"
        + dbLocation
        + '\''
        + ", dbUsername='"
        + '\''
        + ", dbPassword='"
        + '\''
        + '}';
  }

  public static class DbPropertiesBuilder {

    private String dbLocation;
    private String dbUsername;
    private String dbPassword;

    DbPropertiesBuilder() {}

    public DbProperties.DbPropertiesBuilder dbLocation(String dbLocation) {
      this.dbLocation = dbLocation;
      return this;
    }

    public DbProperties.DbPropertiesBuilder dbUsername(String dbUsername) {
      this.dbUsername = dbUsername;
      return this;
    }

    public DbProperties.DbPropertiesBuilder dbPassword(String dbPassword) {
      this.dbPassword = dbPassword;
      return this;
    }

    public DbProperties build() {
      return new DbProperties(dbLocation, dbUsername, dbPassword);
    }
  }
}
