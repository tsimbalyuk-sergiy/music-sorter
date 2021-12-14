package dev.tsvinc.music.sort.infrastructure.domain;

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
}
