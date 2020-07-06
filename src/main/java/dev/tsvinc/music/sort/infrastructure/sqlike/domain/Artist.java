package dev.tsvinc.music.sort.infrastructure.sqlike.domain;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@DatabaseTable(tableName = "artist")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Artist {
  @DatabaseField(canBeNull = false, dataType = DataType.STRING, columnName = "artist")
  private String name;
}
