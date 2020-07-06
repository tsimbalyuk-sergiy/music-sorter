package dev.tsvinc.music.sort.domain;

public class Metadata {

  private String genre;
  private String format;
  private String artist;
  private int year;
  private int audioFilesCount;
  private boolean invalid;

  public Metadata(String genre, String format, String artist, int audioFilesCount, int year, boolean invalid) {
    this.genre = genre;
    this.format = format;
    this.artist = artist;
    this.year = year;
    this.audioFilesCount = audioFilesCount;
    this.invalid = invalid;

  }

  public Metadata(final String genre, final String format) {
    this.genre = genre;
    this.format = format;
  }

  public Metadata() {
  }

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

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public int getAudioFilesCount() {
    return audioFilesCount;
  }

  public void setAudioFilesCount(int audioFilesCount) {
    this.audioFilesCount = audioFilesCount;
  }

  public static class GenreWithFormatBuilder {

    private String genre;
    private String format;
    private String artist;
    private int year;
    private int audioFilesCount;
    private boolean invalid;

    GenreWithFormatBuilder() {
    }

    public Metadata.GenreWithFormatBuilder genre(final String genre) {
      this.genre = genre;
      return this;
    }

    public Metadata.GenreWithFormatBuilder format(final String format) {
      this.format = format;
      return this;
    }

    public Metadata.GenreWithFormatBuilder artist(final String artist) {
      this.artist = artist;
      return this;
    }

    public Metadata.GenreWithFormatBuilder year(final int year) {
      this.year = year;
      return this;
    }

    public Metadata.GenreWithFormatBuilder audioFilesCount(final int audioFilesCount) {
      this.audioFilesCount = audioFilesCount;
      return this;
    }

    public Metadata.GenreWithFormatBuilder invalid(final boolean invalid) {
      this.invalid = invalid;
      return this;
    }

    public Metadata build() {
      return new Metadata(genre, format, artist, year, audioFilesCount, invalid);
    }
  }
}
