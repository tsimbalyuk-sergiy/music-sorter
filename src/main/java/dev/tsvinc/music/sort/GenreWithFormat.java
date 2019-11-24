package dev.tsvinc.music.sort;

import java.util.Objects;
import java.util.StringJoiner;

@SuppressWarnings("unused")
public class GenreWithFormat {
  private String genre;
  private String format;

  public GenreWithFormat(final String genre, final String format) {
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

  public void setGenre(final String genre) {
    this.genre = genre;
  }

  public void setFormat(final String format) {
    this.format = format;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", GenreWithFormat.class.getSimpleName() + "[", "]")
        .add("genre='" + genre + "'")
        .add("format='" + format + "'")
        .toString();
  }

  @Override
  public boolean equals(final Object o) {
    if (Objects.equals(o, this)) {
      return true;
    }
    if (!(o instanceof GenreWithFormat)) {
      return false;
    }
    final var other = (GenreWithFormat) o;
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

  @Override
  public int hashCode() {
    final var PRIME = 59;
    var result = 1;
    final Object oGenre = this.getGenre();
    result = result * PRIME + (null == oGenre ? 43 : oGenre.hashCode());
    final Object oFormat = this.getFormat();
    result = result * PRIME + (null == oFormat ? 43 : oFormat.hashCode());
    return result;
  }

  public static class GenreWithFormatBuilder {

    private String genre;
    private String format;

    GenreWithFormatBuilder() {}

    public GenreWithFormat.GenreWithFormatBuilder genre(final String genre) {
      this.genre = genre;
      return this;
    }

    public GenreWithFormat.GenreWithFormatBuilder format(final String format) {
      this.format = format;
      return this;
    }

    public GenreWithFormat build() {
      return new GenreWithFormat(genre, format);
    }

    @Override
    public String toString() {
      return new StringJoiner(", ", GenreWithFormatBuilder.class.getSimpleName() + "[", "]")
          .add("genre='" + genre + "'")
          .add("format='" + format + "'")
          .toString();
    }
  }
}
