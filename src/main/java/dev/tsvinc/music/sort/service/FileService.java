package dev.tsvinc.music.sort.service;

import static dev.tsvinc.music.sort.util.Constants.FLAC_FORMAT;
import static dev.tsvinc.music.sort.util.Constants.MP3_FORMAT;
import static dev.tsvinc.music.sort.util.Constants.UNKNOWN;
import static dev.tsvinc.music.sort.util.Predicates.IS_MUSIC_FILE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.tinylog.Logger.error;
import static org.tinylog.Logger.info;

import dev.tsvinc.music.sort.domain.AppProperties;
import dev.tsvinc.music.sort.domain.ListingWithFormat;
import io.vavr.control.Try;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileService {

    private static FileService fileServiceInstance;
    private FileService() {}

    private static boolean isNotLiveRelease(final String folderName, final AppProperties properties) {
        return properties.liveReleasesPatterns().parallelStream().noneMatch(folderName::contains);
    }

    private static void moveDirectory(final String sourceDirectory, final File source, final File destination) {
        Try.withResources(() -> Files.newDirectoryStream(Paths.get(sourceDirectory)))
                .of(directoryStream -> {
                    directoryStream.forEach(
                            src -> FileService.move(src, destination.toPath().resolve(src.getFileName())));
                    Files.deleteIfExists(source.toPath());
                    return null;
                })
                .onFailure(throwable ->
                        error("Failed to move directory: {}, {}", sourceDirectory, throwable.getMessage(), throwable));
    }

    private static void move(final Path src, final Path dest) {
        Try.of(() -> Files.move(src, dest, REPLACE_EXISTING))
                .onFailure(throwable ->
                        error("Failed to move file {} to {}, {}", src, dest, throwable.getMessage(), throwable));
    }

    private static void createDirectory(final File destination) {
        Try.of(() -> Files.createDirectories(destination.toPath()))
                .onFailure(e -> error("Failed to create a directory: {}", e.getMessage(), e));
    }

    public static FileService getInstance() {
        if (fileServiceInstance == null) {
            fileServiceInstance = new FileService();
        }
        return fileServiceInstance;
    }

    public List<String> listFiles(final String folderName, final String glob) {
        try (Stream<Path> list = Files.walk(Paths.get(folderName))) {
            return list.parallel()
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(glob))
                    .map(path -> folderName + File.separator + path.getFileName())
                    .toList();
        } catch (final IOException e) {
            error("Error listing directory: {} with a filter: {}, {}", folderName, glob, e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    private boolean moveReleases(final Set<String> folderList, final AppProperties properties) {
        return Try.of(() -> {
                    var folderListSize = new AtomicLong(folderList.size());
                    folderList.parallelStream().forEach(release -> {
                        this.moveRelease(release, properties);
                        info("[folders left to process: {}]\tmoved: {}", folderListSize.decrementAndGet(), release);
                    });
                    return true;
                })
                .onSuccess(status -> info("finished moving releases: {}", status))
                .onFailure(throwable -> error("had errors moving releases: {}", throwable.getMessage(), throwable))
                .get();
    }

    private void moveRelease(final String sourceDirectory, final AppProperties properties) {
        final var source = new File(sourceDirectory);
        final var metadata = AudioFileService.getInstance().getMetadata(sourceDirectory);

        final var outWithFormat = properties.targetFolder() + File.separator + metadata.format();
        final var outWithFormatDir = new File(outWithFormat);
        if (!outWithFormatDir.exists()) {
            FileService.createDirectory(outWithFormatDir);
        }
        final String outWithGenreAndFormat;
        if (properties.sortByArtist()) {
            outWithGenreAndFormat = Path.of(
                            outWithFormat, metadata.genre(), metadata.artist().trim())
                    .toString();
        } else if (!metadata.genre().isBlank()) {
            outWithGenreAndFormat =
                    Path.of(outWithFormat, metadata.genre().strip()).toString();
        } else {
            outWithGenreAndFormat = Path.of(outWithFormat, UNKNOWN).toString();
        }
        final var genreDir = new File(outWithGenreAndFormat);
        final var finalDestination = outWithGenreAndFormat + File.separator;
        final var destination = new File(finalDestination + source.getName());
        if (!genreDir.exists()) {
            FileService.createDirectory(genreDir);
        }
        if (!destination.exists()) {
            FileService.createDirectory(destination);
        }
        FileService.moveDirectory(sourceDirectory, source, destination);
    }

    private Set<String> listAlbums() {
        return Try.withResources(() -> Files.walk(
                        Paths.get(PropertiesService.getInstance().getProperties().sourceFolder()), Integer.MAX_VALUE))
                .of(stream -> {
                    final var dirs = stream.parallel().filter(IS_MUSIC_FILE).collect(Collectors.toSet()).stream()
                            .parallel()
                            .map(o -> o.getParent().toString())
                            .collect(Collectors.toSet());
                    if (PropertiesService.getInstance().getProperties().skipLiveReleases()) {
                        return dirs.stream()
                                .parallel()
                                .filter(folderName -> FileService.isNotLiveRelease(
                                        folderName, PropertiesService.getInstance().getProperties()))
                                .collect(Collectors.toSet());
                    } else {
                        return dirs;
                    }
                })
                .onFailure(e -> error(
                        "Error while walking directory: {}, {}",
                        PropertiesService.getInstance().getProperties().sourceFolder(),
                        e.getMessage(),
                        e))
                .getOrElse(new HashSet<>(0));
    }

    public void processDirectories() {
        final var folderList = this.listAlbums();
        if (folderList.isEmpty()) {
            info("No data to work with. Exiting.");
            System.exit(0);
        }

        info("folders list created. size: {}", folderList.size());
        final var result = this.moveReleases(folderList, PropertiesService.getInstance().getProperties());
        if (result) {
            info("Finished. Cleaning empty directories ...");
            CleanUpService.getInstance().cleanUpParentDirectory();
            System.exit(0);
        }
    }

    public ListingWithFormat createFileListForEachDir(final String folderName) {
        /*creating filter for musical files*/
        final var resultMp3 = listFiles(folderName, MP3_FORMAT);
        final var resultFlac = listFiles(folderName, FLAC_FORMAT);
        if (!resultMp3.isEmpty()) {
            return new ListingWithFormat(MP3_FORMAT, resultMp3);
        } else if (!resultFlac.isEmpty()) {
            return new ListingWithFormat(FLAC_FORMAT, resultFlac);
        } else {
            return new ListingWithFormat(UNKNOWN, Collections.emptyList());
        }
    }
}
