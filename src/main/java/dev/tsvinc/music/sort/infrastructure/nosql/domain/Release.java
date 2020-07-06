package dev.tsvinc.music.sort.infrastructure.nosql.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Indices({
    @Index(value = "release_name", type = IndexType.Unique),
    @Index(value = "checksum_valid", type = IndexType.NonUnique)
})
public class Release {

  @Id
  private NitriteId id;
  private String releaseName;
  private String fullPath;
  private long releaseSize;
  private Format format;
  private int numberOfAudioFiles;
  private boolean hasNfo;
  private boolean hasChecksum;
  private boolean checksumValid;
  private String artist;
  private String albumArtist;
  private String genre;
  private int year;
}
