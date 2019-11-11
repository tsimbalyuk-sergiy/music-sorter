package dev.tsvinc.music.sort;

import java.util.Objects;

public class GenreWithFormat {
  private String genre;
  private String format;

  public GenreWithFormat(String genre, String format) {
    this.genre = genre;
    this.format = format;
  }

  public GenreWithFormat() {}

  public static GenreWithFormatBuilder builder() {
    return new GenreWithFormatBuilder();
  }

  public String getGenre() {
    return this.genre;
  }

  public String getFormat() {
    return this.format;
  }

  public void setGenre(String genre) {
    this.genre = genre;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public String toString() {
    return "GenreWithFormat(genre=" + this.getGenre() + ", format=" + this.getFormat() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof GenreWithFormat)) {
      return false;
    }
    final GenreWithFormat other = (GenreWithFormat) o;
    if (!other.canEqual(this)) {
      return false;
    }
    final Object o1Genre = this.getGenre();
    final Object o2Genre = other.getGenre();
    if (!Objects.equals(o1Genre, o2Genre)) {
      return false;
    }
    final Object o1Format = this.getFormat();
    final Object o2Format = other.getFormat();
    return Objects.equals(o1Format, o2Format);
  }

  protected boolean canEqual(final Object other) {
    return other instanceof GenreWithFormat;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object oGenre = this.getGenre();
    result = result * PRIME + (oGenre == null ? 43 : oGenre.hashCode());
    final Object oFormat = this.getFormat();
    result = result * PRIME + (oFormat == null ? 43 : oFormat.hashCode());
    return result;
  }

  public static class GenreWithFormatBuilder {

    private String genre;
    private String format;

    GenreWithFormatBuilder() {}

    public GenreWithFormat.GenreWithFormatBuilder genre(String genre) {
      this.genre = genre;
      return this;
    }

    public GenreWithFormat.GenreWithFormatBuilder format(String format) {
      this.format = format;
      return this;
    }

    public GenreWithFormat build() {
      return new GenreWithFormat(genre, format);
    }

    public String toString() {
      return "GenreWithFormat.GenreWithFormatBuilder(genre="
          + this.genre
          + ", format="
          + this.format
          + ")";
    }
  }
}
