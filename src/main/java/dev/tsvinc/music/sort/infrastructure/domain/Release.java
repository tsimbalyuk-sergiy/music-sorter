package dev.tsvinc.music.sort.infrastructure.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Indices({
  @Index(value = "release_name", type = IndexType.Unique),
  @Index(value = "checksum_valid", type = IndexType.NonUnique)
})
public class Release {

  @Id private NitriteId id;
  private Format format;
  private String genre;
  private String artist;
  private int year;
  private int numberOfAudioFiles;

  private String releaseName;
  private long releaseSize;
  private boolean hasNfo;
  private boolean hasChecksum;
  private boolean checksumValid;

  public Release(
      final NitriteId id,
      final Format format,
      final String genre,
      final String artist,
      final int year,
      final int numberOfAudioFiles,
      final String releaseName,
      final long releaseSize,
      final boolean hasNfo,
      final boolean hasChecksum,
      final boolean checksumValid) {
    this.id = id;
    this.format = format;
    this.genre = genre;
    this.artist = artist;
    this.year = year;
    this.numberOfAudioFiles = numberOfAudioFiles;
    this.releaseName = releaseName;
    this.releaseSize = releaseSize;
    this.hasNfo = hasNfo;
    this.hasChecksum = hasChecksum;
    this.checksumValid = checksumValid;
  }

  public Release() {}

  public static ReleaseBuilder builder() {
    return new ReleaseBuilder();
  }

  public NitriteId getId() {
    return this.id;
  }

  public void setId(final NitriteId id) {
    this.id = id;
  }

  public Format getFormat() {
    return this.format;
  }

  public void setFormat(final Format format) {
    this.format = format;
  }

  public String getGenre() {
    return this.genre;
  }

  public void setGenre(final String genre) {
    this.genre = genre;
  }

  public String getArtist() {
    return this.artist;
  }

  public void setArtist(final String artist) {
    this.artist = artist;
  }

  public int getYear() {
    return this.year;
  }

  public void setYear(final int year) {
    this.year = year;
  }

  public int getNumberOfAudioFiles() {
    return this.numberOfAudioFiles;
  }

  public void setNumberOfAudioFiles(final int numberOfAudioFiles) {
    this.numberOfAudioFiles = numberOfAudioFiles;
  }

  public String getReleaseName() {
    return this.releaseName;
  }

  public void setReleaseName(final String releaseName) {
    this.releaseName = releaseName;
  }

  public long getReleaseSize() {
    return this.releaseSize;
  }

  public void setReleaseSize(final long releaseSize) {
    this.releaseSize = releaseSize;
  }

  public boolean isHasNfo() {
    return this.hasNfo;
  }

  public void setHasNfo(final boolean hasNfo) {
    this.hasNfo = hasNfo;
  }

  public boolean isHasChecksum() {
    return this.hasChecksum;
  }

  public void setHasChecksum(final boolean hasChecksum) {
    this.hasChecksum = hasChecksum;
  }

  public boolean isChecksumValid() {
    return this.checksumValid;
  }

  public void setChecksumValid(final boolean checksumValid) {
    this.checksumValid = checksumValid;
  }

  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Release)) {
      return false;
    }
    final Release other = (Release) o;
    if (!other.canEqual(this)) {
      return false;
    }
    final Object this$id = this.getId();
    final Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) {
      return false;
    }
    final Object this$format = this.getFormat();
    final Object other$format = other.getFormat();
    if (this$format == null ? other$format != null : !this$format.equals(other$format)) {
      return false;
    }
    final Object this$genre = this.getGenre();
    final Object other$genre = other.getGenre();
    if (this$genre == null ? other$genre != null : !this$genre.equals(other$genre)) {
      return false;
    }
    final Object this$artist = this.getArtist();
    final Object other$artist = other.getArtist();
    if (this$artist == null ? other$artist != null : !this$artist.equals(other$artist)) {
      return false;
    }
    if (this.getYear() != other.getYear()) {
      return false;
    }
    if (this.getNumberOfAudioFiles() != other.getNumberOfAudioFiles()) {
      return false;
    }
    final Object this$releaseName = this.getReleaseName();
    final Object other$releaseName = other.getReleaseName();
    if (this$releaseName == null
        ? other$releaseName != null
        : !this$releaseName.equals(other$releaseName)) {
      return false;
    }
    if (this.getReleaseSize() != other.getReleaseSize()) {
      return false;
    }
    if (this.isHasNfo() != other.isHasNfo()) {
      return false;
    }
    if (this.isHasChecksum() != other.isHasChecksum()) {
      return false;
    }
    return this.isChecksumValid() == other.isChecksumValid();
  }

  protected boolean canEqual(final Object other) {
    return other instanceof Release;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final Object $format = this.getFormat();
    result = result * PRIME + ($format == null ? 43 : $format.hashCode());
    final Object $genre = this.getGenre();
    result = result * PRIME + ($genre == null ? 43 : $genre.hashCode());
    final Object $artist = this.getArtist();
    result = result * PRIME + ($artist == null ? 43 : $artist.hashCode());
    result = result * PRIME + this.getYear();
    result = result * PRIME + this.getNumberOfAudioFiles();
    final Object $releaseName = this.getReleaseName();
    result = result * PRIME + ($releaseName == null ? 43 : $releaseName.hashCode());
    final long $releaseSize = this.getReleaseSize();
    result = result * PRIME + (int) ($releaseSize >>> 32 ^ $releaseSize);
    result = result * PRIME + (this.isHasNfo() ? 79 : 97);
    result = result * PRIME + (this.isHasChecksum() ? 79 : 97);
    result = result * PRIME + (this.isChecksumValid() ? 79 : 97);
    return result;
  }

  public String toString() {
    return "Release(id="
        + this.getId()
        + ", format="
        + this.getFormat()
        + ", genre="
        + this.getGenre()
        + ", artist="
        + this.getArtist()
        + ", year="
        + this.getYear()
        + ", numberOfAudioFiles="
        + this.getNumberOfAudioFiles()
        + ", releaseName="
        + this.getReleaseName()
        + ", releaseSize="
        + this.getReleaseSize()
        + ", hasNfo="
        + this.isHasNfo()
        + ", hasChecksum="
        + this.isHasChecksum()
        + ", checksumValid="
        + this.isChecksumValid()
        + ")";
  }

  public static class ReleaseBuilder {

    private NitriteId id;
    private Format format;
    private String genre;
    private String artist;
    private int year;
    private int numberOfAudioFiles;
    private String releaseName;
    private long releaseSize;
    private boolean hasNfo;
    private boolean hasChecksum;
    private boolean checksumValid;

    ReleaseBuilder() {}

    public Release.ReleaseBuilder id(final NitriteId id) {
      this.id = id;
      return this;
    }

    public Release.ReleaseBuilder format(final Format format) {
      this.format = format;
      return this;
    }

    public Release.ReleaseBuilder genre(final String genre) {
      this.genre = genre;
      return this;
    }

    public Release.ReleaseBuilder artist(final String artist) {
      this.artist = artist;
      return this;
    }

    public Release.ReleaseBuilder year(final int year) {
      this.year = year;
      return this;
    }

    public Release.ReleaseBuilder numberOfAudioFiles(final int numberOfAudioFiles) {
      this.numberOfAudioFiles = numberOfAudioFiles;
      return this;
    }

    public Release.ReleaseBuilder releaseName(final String releaseName) {
      this.releaseName = releaseName;
      return this;
    }

    public Release.ReleaseBuilder releaseSize(final long releaseSize) {
      this.releaseSize = releaseSize;
      return this;
    }

    public Release.ReleaseBuilder hasNfo(final boolean hasNfo) {
      this.hasNfo = hasNfo;
      return this;
    }

    public Release.ReleaseBuilder hasChecksum(final boolean hasChecksum) {
      this.hasChecksum = hasChecksum;
      return this;
    }

    public Release.ReleaseBuilder checksumValid(final boolean checksumValid) {
      this.checksumValid = checksumValid;
      return this;
    }

    public Release build() {
      return new Release(
          this.id,
          this.format,
          this.genre,
          this.artist,
          this.year,
          this.numberOfAudioFiles,
          this.releaseName,
          this.releaseSize,
          this.hasNfo,
          this.hasChecksum,
          this.checksumValid);
    }

    public String toString() {
      return "Release.ReleaseBuilder(id="
          + this.id
          + ", format="
          + this.format
          + ", genre="
          + this.genre
          + ", artist="
          + this.artist
          + ", year="
          + this.year
          + ", numberOfAudioFiles="
          + this.numberOfAudioFiles
          + ", releaseName="
          + this.releaseName
          + ", releaseSize="
          + this.releaseSize
          + ", hasNfo="
          + this.hasNfo
          + ", hasChecksum="
          + this.hasChecksum
          + ", checksumValid="
          + this.checksumValid
          + ")";
    }
  }
}
