package dev.tsvinc.music.sort.domain;

import java.util.Objects;

public class DbProperties {

    private String dbLocation;
    private String dbUsername;
    private String dbPassword;

    public DbProperties(final String dbLocation, final String dbUsername, final String dbPassword) {
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

    public void setDbLocation(final String dbLocation) {
        this.dbLocation = dbLocation;
    }

    public String getDbUsername() {
        return this.dbUsername;
    }

    public void setDbUsername(final String dbUsername) {
        this.dbUsername = dbUsername;
    }

    public String getDbPassword() {
        return this.dbPassword;
    }

    public void setDbPassword(final String dbPassword) {
        this.dbPassword = dbPassword;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || this.getClass() != o.getClass()) {
            return false;
        }
        final var that = (DbProperties) o;
        return Objects.equals(this.dbLocation, that.dbLocation)
                && Objects.equals(this.dbUsername, that.dbUsername)
                && Objects.equals(this.dbPassword, that.dbPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.dbLocation, this.dbUsername, this.dbPassword);
    }

    @Override
    public String toString() {
        return "DbProperties{"
                + "dbLocation='"
                + this.dbLocation
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

        public DbProperties.DbPropertiesBuilder dbLocation(final String dbLocation) {
            this.dbLocation = dbLocation;
            return this;
        }

        public DbProperties.DbPropertiesBuilder dbUsername(final String dbUsername) {
            this.dbUsername = dbUsername;
            return this;
        }

        public DbProperties.DbPropertiesBuilder dbPassword(final String dbPassword) {
            this.dbPassword = dbPassword;
            return this;
        }

        public DbProperties build() {
            return new DbProperties(this.dbLocation, this.dbUsername, this.dbPassword);
        }
    }
}
