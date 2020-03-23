package dev.tsvinc.music.sort.infrastructure.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@DatabaseTable(tableName = "releases")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Release {
  
  @DatabaseField(generatedId = true)
  private long id;

  @DatabaseField(canBeNull = false, columnName = "release_name")
  private String releaseName;

  @DatabaseField(canBeNull = false, columnName = "full_path")
  private String fullPath;

  @DatabaseField(canBeNull = false, columnName = "genre")
  private Genre genre;

  @DatabaseField(canBeNull = false, columnName = "size")
  private long releaseSize;

  @DatabaseField(canBeNull = false, columnName = "format")
  private Format format;

  @DatabaseField(canBeNull = false, columnName = "audio_files")
  private int numberOfAudioFiles;

  @DatabaseField(canBeNull = false, columnName = "has_nfo")
  private boolean hasNfo;

  @DatabaseField(canBeNull = false, columnName = "has_checksum")
  private boolean hasChecksum;

  @DatabaseField(canBeNull = false, columnName = "checksum_valid")
  private boolean checksumValid;

  @DatabaseField(canBeNull = false, foreign = true, foreignAutoCreate = true)
  private AlbumDetails albumDetails;
}
