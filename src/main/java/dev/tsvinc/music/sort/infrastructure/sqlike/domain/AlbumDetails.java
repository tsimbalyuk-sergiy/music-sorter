package dev.tsvinc.music.sort.infrastructure.sqlike.domain;

import com.j256.ormlite.field.DataType;
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

  @DatabaseField(canBeNull = false, columnName = "artist", dataType = DataType.STRING)
  private String artist;

  @DatabaseField(canBeNull = false, columnName = "album_artist", dataType = DataType.STRING)
  private String albumArtist;

  @DatabaseField(canBeNull = false, columnName = "genre", dataType = DataType.STRING)
  private String genre;

  @DatabaseField(columnName = "year")
  private int year;
}
