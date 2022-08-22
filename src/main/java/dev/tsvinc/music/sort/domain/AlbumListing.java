package dev.tsvinc.music.sort.domain;

import java.util.Objects;
import java.util.Set;

@SuppressWarnings("unused")
public class AlbumListing {
  private Set<String> albums;
  private Set<String> liveAlbums;

  public AlbumListing(Set<String> albums, Set<String> liveAlbums) {
    this.albums = albums;
    this.liveAlbums = liveAlbums;
  }

  public AlbumListing() {}

  public static AlbumListingBuilder builder() {
    return new AlbumListingBuilder();
  }

  public Set<String> getAlbums() {
    return this.albums;
  }

  public Set<String> getLiveAlbums() {
    return this.liveAlbums;
  }

  public void setAlbums(Set<String> albums) {
    this.albums = albums;
  }

  public void setLiveAlbums(Set<String> liveAlbums) {
    this.liveAlbums = liveAlbums;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof AlbumListing)) return false;
    final AlbumListing other = (AlbumListing) o;
    if (!other.canEqual(this)) return false;
    final Object o1Albums = this.getAlbums();
    final Object o2Albums = other.getAlbums();
    if (!Objects.equals(o1Albums, o2Albums)) return false;
    final Object o1LiveAlbums = this.getLiveAlbums();
    final Object o2LiveAlbums = other.getLiveAlbums();
    return Objects.equals(o1LiveAlbums, o2LiveAlbums);
  }

  protected boolean canEqual(final Object other) {
    return other instanceof AlbumListing;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object oAlbums = this.getAlbums();
    result = result * PRIME + (oAlbums == null ? 43 : oAlbums.hashCode());
    final Object oLiveAlbums = this.getLiveAlbums();
    result = result * PRIME + (oLiveAlbums == null ? 43 : oLiveAlbums.hashCode());
    return result;
  }

  public String toString() {
    return "AlbumListing(albums=" + this.getAlbums() + ", liveAlbums=" + this.getLiveAlbums() + ")";
  }

  public static class AlbumListingBuilder {
    private Set<String> albums;
    private Set<String> liveAlbums;

    AlbumListingBuilder() {}

    public AlbumListing.AlbumListingBuilder albums(Set<String> albums) {
      this.albums = albums;
      return this;
    }

    public AlbumListing.AlbumListingBuilder liveAlbums(Set<String> liveAlbums) {
      this.liveAlbums = liveAlbums;
      return this;
    }

    public AlbumListing build() {
      return new AlbumListing(albums, liveAlbums);
    }

    public String toString() {
      return "AlbumListing.AlbumListingBuilder(albums="
          + this.albums
          + ", liveAlbums="
          + this.liveAlbums
          + ")";
    }
  }
}
