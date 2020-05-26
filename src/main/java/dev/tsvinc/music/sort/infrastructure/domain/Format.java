package dev.tsvinc.music.sort.infrastructure.domain;

public enum Format {
  FLAC("flac"),
  MP3("mp3");
  private String value;

  Format(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
