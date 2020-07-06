package dev.tsvinc.music.sort.infrastructure.sqlike.domain;

import java.util.Objects;
import java.util.StringJoiner;

@SuppressWarnings("unused")
public class GenreWithFormat {
  private String genre;
  private String format;
  private boolean invalid;

  public GenreWithFormat(final String genre, final String format) {
    this.genre = genre;
    this.format = format;
  }

  public GenreWithFormat(String genre, String format, boolean invalid) {
    this.genre = genre;
    this.format = format;
    this.invalid = invalid;
  }

  public GenreWithFormat() {
  }

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

  public boolean isInvalid() {
    return invalid;
  }

  public void setInvalid(boolean invalid) {
    this.invalid = invalid;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", GenreWithFormat.class.getSimpleName() + "[", "]")
        .add("genre='" + genre + "'")
        .add("format='" + format + "'")
        .add("invalid='" + invalid + "'")
        .toString();
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
        && Objects.equals(format, that.format);
  }

  @Override
  public int hashCode() {
    return Objects.hash(genre, format, invalid);
  }

  public static class GenreWithFormatBuilder {

    private String genre;
    private String format;
    private boolean invalid;

    GenreWithFormatBuilder() {
    }

    public GenreWithFormat.GenreWithFormatBuilder genre(final String genre) {
      this.genre = genre;
      return this;
    }

    public GenreWithFormat.GenreWithFormatBuilder format(final String format) {
      this.format = format;
      return this;
    }

    public GenreWithFormat.GenreWithFormatBuilder invalid(final boolean invalid) {
      this.invalid = invalid;
      return this;
    }

    public GenreWithFormat build() {
      return new GenreWithFormat(genre, format, invalid);
    }

    @Override
    public String toString() {
      return new StringJoiner(", ", GenreWithFormatBuilder.class.getSimpleName() + "[", "]")
          .add("genre='" + genre + "'")
          .add("format='" + format + "'")
          .add("invalid='" + invalid + "'")
          .toString();
    }
  }
}
