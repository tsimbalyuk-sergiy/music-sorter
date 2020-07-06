package dev.tsvinc.music.sort.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AppProperties {
  private String sourceFolder;
  private String targetFolder;
  private boolean sortByArtist;
  private boolean skipLiveReleases;
  private List<String> liveReleasesPatterns;
}
