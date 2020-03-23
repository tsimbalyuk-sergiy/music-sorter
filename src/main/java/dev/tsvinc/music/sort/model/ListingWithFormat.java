package dev.tsvinc.music.sort.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@SuppressWarnings("unused")
public class ListingWithFormat {
  private String format;
  private List<String> fileList;

  public ListingWithFormat() {}

  public ListingWithFormat(final String format, final List<String> fileList) {
    this.format = format;
    this.fileList = fileList;
  }

  public ListingWithFormat(final List<String> fileList) {
    this.fileList = fileList;
  }

  public static ListingWithFormatBuilder builder() {
    return new ListingWithFormatBuilder();
  }

  public String getFormat() {
    return this.format;
  }

  public List<String> getFileList() {
    return !fileList.isEmpty() ? List.copyOf(this.fileList) : new ArrayList<>();
  }

  public void setFormat(final String format) {
    this.format = format;
  }

  public void setFileList(final List<String> fileList) {
    this.fileList = fileList;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ListingWithFormat.class.getSimpleName() + "[", "]")
        .add("format='" + format + "'")
        .add("fileList=" + fileList)
        .toString();
  }

  @Override
  public boolean equals(final Object o) {
    if (Objects.equals(o, this)) {
      return true;
    }
    if (!(o instanceof ListingWithFormat)) {
      return false;
    }
    final var other = (ListingWithFormat) o;
    if (!other.canEqual(this)) {
      return false;
    }
    final var o1Format = this.getFormat();
    final var o2Format = other.getFormat();
    if (!Objects.equals(o1Format, o2Format)) {
      return false;
    }
    final var o1FileList = this.getFileList();
    final var o2FileList = other.getFileList();
    return Objects.equals(o1FileList, o2FileList);
  }

  protected boolean canEqual(final ListingWithFormat other) {
    return null != other;
  }

  @Override
  public int hashCode() {
    final var PRIME = 59;
    var result = 1;
    final var oFormat = this.getFormat();
    result = result * PRIME + (null == oFormat ? 43 : oFormat.hashCode());
    final var oFileList = this.getFileList();
    result = result * PRIME + (null == oFileList ? 43 : oFileList.hashCode());
    return result;
  }

  public static class ListingWithFormatBuilder {

    private String format;
    private List<String> fileList;

    ListingWithFormatBuilder() {}

    public ListingWithFormat.ListingWithFormatBuilder format(final String format) {
      this.format = format;
      return this;
    }

    public ListingWithFormat.ListingWithFormatBuilder fileList(final List<String> fileList) {
      this.fileList = fileList;
      return this;
    }

    public ListingWithFormat build() {
      return new ListingWithFormat(format, fileList);
    }

    @Override
    public String toString() {
      return new StringJoiner(", ", ListingWithFormatBuilder.class.getSimpleName() + "[", "]")
          .add("format='" + format + "'")
          .add("fileList=" + fileList)
          .toString();
    }
  }
}
