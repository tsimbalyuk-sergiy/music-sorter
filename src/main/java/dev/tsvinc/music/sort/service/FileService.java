package dev.tsvinc.music.sort.service;

import dev.tsvinc.music.sort.domain.ListingWithFormat;

public interface FileService {
  void processDirectories();

  ListingWithFormat createFileListForEachDir(final String folderName);
}
