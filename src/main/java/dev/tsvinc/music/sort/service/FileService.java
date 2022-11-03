package dev.tsvinc.music.sort.service;

import dev.tsvinc.music.sort.domain.ListingWithFormat;

import java.util.List;

public interface FileService {

    void processDirectories();

    ListingWithFormat createFileListForEachDir(final String folderName);

    List<String> listFiles(final String folderName, final String glob);
}
