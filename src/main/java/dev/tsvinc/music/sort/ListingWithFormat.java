package dev.tsvinc.music.sort;

import java.util.List;
import java.util.Objects;

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
    return this.fileList;
  }

  public void setFormat(final String format) {
    this.format = format;
  }

  public void setFileList(final List<String> fileList) {
    this.fileList = fileList;
  }

  public String toString() {
    return "ListingWithFormat(format="
        + this.getFormat()
        + ", fileList="
        + this.getFileList()
        + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ListingWithFormat)) {
      return false;
    }
    final ListingWithFormat other = (ListingWithFormat) o;
    if (!other.canEqual(this)) {
      return false;
    }
    final Object o1Format = this.getFormat();
    final Object o2Format = other.getFormat();
    if (!Objects.equals(o1Format, o2Format)) {
      return false;
    }
    final Object o1FileList = this.getFileList();
    final Object o2FileList = other.getFileList();
    return Objects.equals(o1FileList, o2FileList);
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ListingWithFormat;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object oFormat = this.getFormat();
    result = result * PRIME + (oFormat == null ? 43 : oFormat.hashCode());
    final Object oFileList = this.getFileList();
    result = result * PRIME + (oFileList == null ? 43 : oFileList.hashCode());
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

    public String toString() {
      return "ListingWithFormat.ListingWithFormatBuilder(format="
          + this.format
          + ", fileList="
          + this.fileList
          + ")";
    }
  }
}
