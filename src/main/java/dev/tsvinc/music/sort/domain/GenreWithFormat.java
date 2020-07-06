package dev.tsvinc.music.sort.domain;

import java.util.Objects;

@SuppressWarnings("unused")
public class GenreWithFormat {

  private String genre;
  private String format;
  private String artist;
  private boolean invalid;

  public GenreWithFormat(String genre, String format, String artist, boolean invalid) {
    this.genre = genre;
    this.format = format;
    this.artist = artist;
    this.invalid = invalid;
  }

  public GenreWithFormat(final String genre, final String format) {
    this.genre = genre;
    this.format = format;
  }

  public GenreWithFormat() {}

  public static GenreWithFormatBuilder builder() {
    return new GenreWithFormatBuilder();
  }

  public String getGenre() {
    return genre;
  }

  public void setGenre(String genre) {
    this.genre = genre;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public String getArtist() {
    return artist;
  }

  public void setArtist(String artist) {
    this.artist = artist;
  }

  public boolean isInvalid() {
    return invalid;
  }

  public void setInvalid(boolean invalid) {
    this.invalid = invalid;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GenreWithFormat that = (GenreWithFormat) o;
    return invalid == that.invalid
        && Objects.equals(genre, that.genre)
        && Objects.equals(format, that.format)
        && Objects.equals(artist, that.artist);
  }

  @Override
  public int hashCode() {
    return Objects.hash(genre, format, artist, invalid);
  }

  @Override
  public String toString() {
    return "GenreWithFormat{"
        + "genre='"
        + genre
        + '\''
        + ", format='"
        + format
        + '\''
        + ", artist='"
        + artist
        + '\''
        + ", invalid="
        + invalid
        + '}';
  }

  public static class GenreWithFormatBuilder {

    private String genre;
    private String format;
    private String artist;
    private boolean invalid;

    GenreWithFormatBuilder() {}

    public GenreWithFormat.GenreWithFormatBuilder genre(final String genre) {
      this.genre = genre;
      return this;
    }

    public GenreWithFormat.GenreWithFormatBuilder format(final String format) {
      this.format = format;
      return this;
    }

    public GenreWithFormat.GenreWithFormatBuilder artist(final String artist) {
      this.artist = artist;
      return this;
    }

    public GenreWithFormat.GenreWithFormatBuilder invalid(final boolean invalid) {
      this.invalid = invalid;
      return this;
    }

    public GenreWithFormat build() {
      return new GenreWithFormat(genre, format, artist, invalid);
    }
  }
}
