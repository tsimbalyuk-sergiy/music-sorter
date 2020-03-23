package dev.tsvinc.music.sort.infrastructure.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@DatabaseTable(tableName = "album_details")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AlbumDetails {
  @DatabaseField(generatedId = true)
  private long id;

  @DatabaseField(canBeNull = false, columnName = "artist")
  private Artist artist;

  @DatabaseField(canBeNull = false, columnName = "album_artist")
  private Artist albumArtist;

  @DatabaseField(canBeNull = false, columnName = "genre")
  private Genre genre;

  @DatabaseField(columnName = "year")
  private int year;

  @DatabaseField(columnName = "format")
  private Format format;
}
