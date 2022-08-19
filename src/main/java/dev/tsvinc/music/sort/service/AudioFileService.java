package dev.tsvinc.music.sort.service;

import dev.tsvinc.music.sort.domain.Metadata;

public interface AudioFileService {

    Metadata getMetadata(String sourceDirectory);
}
