package dev.tsvinc.music.sort;

import static org.tinylog.Logger.info;

import dev.tsvinc.music.sort.service.AudioFileServiceImpl;
import dev.tsvinc.music.sort.service.ChecksumServiceImpl;
import dev.tsvinc.music.sort.service.CleanUpServiceImpl;
import dev.tsvinc.music.sort.service.CollectionServiceImpl;
import dev.tsvinc.music.sort.service.FileService;
import dev.tsvinc.music.sort.service.FileServiceImpl;
import dev.tsvinc.music.sort.service.PropertiesServiceImpl;

public class App {

    public static void main(final String[] args) {
        org.tinylog.jul.JulTinylogBridge.activate();

        final boolean collectionMode = hasCollectionFlag(args);

        if (collectionMode) {
            info("[*] Music Sorter v0.0.7 - Collection Mode");
            info("[*] Creating organized collection with links (no file moving)");
        } else {
            info("[*] Music Sorter v0.0.7 - Standard Mode");
            info("[*] Organizing your music collection by format and genre");
        }

        final var propertiesService = new PropertiesServiceImpl();
        final var cleanUpService = new CleanUpServiceImpl(propertiesService);

        final FileService fileService = createFileService(propertiesService, cleanUpService);

        if (collectionMode) {
            final var audioFileService = new AudioFileServiceImpl(fileService);
            final var collectionService = new CollectionServiceImpl(propertiesService, audioFileService);
            collectionService.createCollection();
        } else {
            fileService.processDirectories();
        }

        info("[SUCCESS] Music Sorter completed successfully!");
    }

    private static boolean hasCollectionFlag(final String[] args) {
        for (final String arg : args) {
            if ("--collection".equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private static FileService createFileService(
            PropertiesServiceImpl propertiesService, CleanUpServiceImpl cleanUpService) {
        final var checksumService = new ChecksumServiceImpl();
        final var fileService = new FileServiceImpl(propertiesService, cleanUpService, null, checksumService);
        final var audioFileService = new AudioFileServiceImpl(fileService);

        return new FileServiceImpl(propertiesService, cleanUpService, audioFileService, checksumService);
    }
}
