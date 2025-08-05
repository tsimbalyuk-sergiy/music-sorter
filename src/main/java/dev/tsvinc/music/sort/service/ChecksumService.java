package dev.tsvinc.music.sort.service;

import java.io.File;

public interface ChecksumService {
    boolean validateDirectory(File directory);

    boolean hasChecksumFiles(File directory);
}
