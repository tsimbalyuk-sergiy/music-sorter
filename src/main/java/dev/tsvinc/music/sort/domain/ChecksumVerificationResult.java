package dev.tsvinc.music.sort.domain;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class ChecksumVerificationResult<T> {
  private List<T> failed;
  private List<T> ok;
  private List<T> notFound;

  public ChecksumVerificationResult(
      final List<T> failed, final List<T> ok, final List<T> notFound) {
    this.failed = failed;
    this.ok = ok;
    this.notFound = notFound;
  }

  public ChecksumVerificationResult() {}

  public static <T> ChecksumVerificationResultBuilder<T> builder() {
    return new ChecksumVerificationResultBuilder<>();
  }

  public List<T> getFailed() {
    return this.failed;
  }

  public void setFailed(final List<T> failed) {
    this.failed = failed;
  }

  public List<T> getOk() {
    return this.ok;
  }

  public void setOk(final List<T> ok) {
    this.ok = ok;
  }

  public List<T> getNotFound() {
    return this.notFound;
  }

  public void setNotFound(final List<T> notFound) {
    this.notFound = notFound;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ChecksumVerificationResult)) return false;
    final ChecksumVerificationResult<?> other = (ChecksumVerificationResult<?>) o;
    if (!other.canEqual(this)) return false;
    final Object o1Failed = this.getFailed();
    final Object o2Failed = other.getFailed();
    if (!Objects.equals(o1Failed, o2Failed)) return false;
    final Object o1Ok = this.getOk();
    final Object o2Ok = other.getOk();
    if (!Objects.equals(o1Ok, o2Ok)) return false;
    final Object o1NotFound = this.getNotFound();
    final Object o2NotFound = other.getNotFound();
    return Objects.equals(o1NotFound, o2NotFound);
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ChecksumVerificationResult;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object oFailed = this.getFailed();
    result = result * PRIME + (oFailed == null ? 43 : oFailed.hashCode());
    final Object oOk = this.getOk();
    result = result * PRIME + (oOk == null ? 43 : oOk.hashCode());
    final Object oNotFound = this.getNotFound();
    result = result * PRIME + (oNotFound == null ? 43 : oNotFound.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ChecksumVerificationResult.class.getSimpleName() + "[", "]")
        .add("failed=" + this.failed)
        .add("ok=" + this.ok)
        .add("notFound=" + this.notFound)
        .toString();
  }

  public static class ChecksumVerificationResultBuilder<T> {
    private List<T> failed;
    private List<T> ok;
    private List<T> notFound;

    ChecksumVerificationResultBuilder() {}

    public ChecksumVerificationResult.ChecksumVerificationResultBuilder<T> failed(
        final List<T> failed) {
      this.failed = failed;
      return this;
    }

    public ChecksumVerificationResult.ChecksumVerificationResultBuilder<T> ok(final List<T> ok) {
      this.ok = ok;
      return this;
    }

    public ChecksumVerificationResult.ChecksumVerificationResultBuilder<T> notFound(
        final List<T> notFound) {
      this.notFound = notFound;
      return this;
    }

    public ChecksumVerificationResult<T> build() {
      return new ChecksumVerificationResult<>(this.failed, this.ok, this.notFound);
    }

    @Override
    public String toString() {
      return new StringJoiner(
              ", ", ChecksumVerificationResultBuilder.class.getSimpleName() + "[", "]")
          .add("failed=" + this.failed)
          .add("ok=" + this.ok)
          .add("notFound=" + this.notFound)
          .toString();
    }
  }
}
