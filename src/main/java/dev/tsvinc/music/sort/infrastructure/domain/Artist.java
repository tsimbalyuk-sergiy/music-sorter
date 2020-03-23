package dev.tsvinc.music.sort.infrastructure.domain;

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
  private String name;
}
