package dev.tsvinc.music.sort.service;

import dev.tsvinc.music.sort.domain.GenreWithFormat;

public interface AudioFileService {
  GenreWithFormat getMetadata(String sourceDirectory);
}
