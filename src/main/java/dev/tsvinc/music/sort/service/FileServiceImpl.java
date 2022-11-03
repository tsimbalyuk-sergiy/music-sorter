package dev.tsvinc.music.sort.service;

import dev.tsvinc.music.sort.domain.AppProperties;
import dev.tsvinc.music.sort.domain.ListingWithFormat;
import io.vavr.control.Try;
import me.tongfei.progressbar.DelegatingProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.tinylog.Logger;
import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.tsvinc.music.sort.util.Constants.CHECKSUM;
import static dev.tsvinc.music.sort.util.Constants.FLAC;
import static dev.tsvinc.music.sort.util.Constants.FLAC_FORMAT;
import static dev.tsvinc.music.sort.util.Constants.MP3;
import static dev.tsvinc.music.sort.util.Constants.MP_3_FORMAT;
import static dev.tsvinc.music.sort.util.Constants.UNKNOWN;
import static dev.tsvinc.music.sort.util.Predicates.IS_MUSIC_FILE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.tinylog.Logger.error;
import static org.tinylog.Logger.info;

public class FileServiceImpl implements FileService {

    @Inject
    private PropertiesService propertiesService;

    @Inject
    private CleanUpService cleanUpService;

    @Inject
    private AudioFileService audioFileService;

    private static boolean isNotLiveRelease(final String folderName, final AppProperties properties) {
        return properties.liveReleasesPatterns().parallelStream().noneMatch(folderName::contains);
    }

    private static ProgressBar buildProgressBar(final Set<String> folderList) {
        return new ProgressBarBuilder()
                .setInitialMax(folderList.size())
                .setStyle(ProgressBarStyle.ASCII)
                .setTaskName("Working ...")
                .showSpeed()
                .continuousUpdate()
                .setConsumer(new DelegatingProgressBarConsumer(Logger::info))
                .build();
    }

    private static void moveDirectory(final String sourceDirectory, final File source, final File destination) {
        Try.withResources(() -> Files.newDirectoryStream(Paths.get(sourceDirectory)))
                .of(directoryStream -> {
                    directoryStream.forEach(src ->
                            FileServiceImpl.move(src, destination.toPath().resolve(src.getFileName())));
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

    private static List<String> listFiles(final String folderName, final String glob) {
        final List<String> resultList = new ArrayList<>(100);
        /*
        TODO
        Files.list(Paths.get(folderName)).parallel()
                .filter(IS_MUSIC_FILE)
                .filter(path -> path.toString().endsWith(glob))
                .forEach(path -> resultList.add(path.toString()));*/
        try (final var stream = Files.newDirectoryStream(Paths.get(folderName), glob)) {
            stream.forEach(o -> resultList.add(folderName + File.separator + o.getFileName()));
        } catch (final IOException e) {
            error("Error listing directory: {} with a filter: {}, {}", folderName, glob, e.getMessage(), e);
        }
        return resultList;
    }

    private boolean tryWithProgressBar(final Set<String> folderList, final AppProperties properties) {
        return Try.withResources(() -> FileServiceImpl.buildProgressBar(folderList))
                .of(progressBar -> {
                    folderList.forEach(release -> {
                        this.moveRelease(release, properties);
                        progressBar.step();
                    });
                    return true;
                })
                .onSuccess(status -> info("finished moving releases: {}", status))
                .onFailure(throwable -> error("had errors moving releases: {}", throwable.getMessage(), throwable))
                .get();
    }

    private static void verifyCheckSum(final File source) {
        final var paths = Try.withResources(() -> Files.list(source.toPath()))
                .of(pathStream -> pathStream.parallel().toList())
                .get();

        final var sfv =
                paths.parallelStream().filter(path -> path.endsWith("sfv")).findFirst();

        final var strings = Try.of(() -> Files.readAllLines(sfv.orElseThrow(FileNotFoundException::new)))
                .onFailure(t -> error("Error reading lines: {}, {}", sfv.toString(), t.getMessage(), t))
                .get();
        final var collect = strings.parallelStream()
                .map(s -> s.split(" "))
                .collect(Collectors.toMap(res -> res[0], res -> 1 < res.length ? res[1] : ""));

        for (final var path : paths) {
            // TODO calc crc, get by key from map, compare calculated crc and crc from sfv file
        }
    }

    private void moveRelease(final String sourceDirectory, final AppProperties properties) {
        final var source = new File(sourceDirectory);
        final var metadata = this.audioFileService.getMetadata(sourceDirectory);
        //        var containsCheckSum = false;
        //        if (FileServiceImpl.hasCheckSum(source).containsKey(CHECKSUM)) {
        //            containsCheckSum = true;
        //            FileServiceImpl.verifyCheckSum(source); // TODO
        //        }

        /*private String releaseName;
        private long releaseSize;
        private boolean hasNfo;
        private boolean hasChecksum;
        private boolean checksumValid;*/

        /*Release.builder()
        .artist(metadata.getArtist())
        .releaseName(source.getName())
        .hasChecksum(containsCheckSum)
        .build();*/

        final var outWithFormat = properties.targetFolder() + File.separator + metadata.format();
        final var outWithFormatDir = new File(outWithFormat);
        if (!outWithFormatDir.exists()) {
            FileServiceImpl.createDirectory(outWithFormatDir);
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
            FileServiceImpl.createDirectory(genreDir);
        }
        if (!destination.exists()) {
            FileServiceImpl.createDirectory(destination);
        }
        FileServiceImpl.moveDirectory(sourceDirectory, source, destination);
    }

    private static Map<?, ?> hasCheckSum(final File source) {
        final var sfv = Try.withResources(() -> Files.list(source.toPath()))
                .of(pathStream -> pathStream
                        .filter(path -> path.getFileName().endsWith("sfv"))
                        .toList())
                .onFailure(throwable -> info("No Checksum file for {}", source.toString()))
                .getOrElse(Collections.emptyList());
        if (sfv.isEmpty()) {
            return Collections.emptyMap();
        } else {
            return Map.of(CHECKSUM, sfv.get(0));
        }
    }

    private Set<String> listAlbums() {
        return Try.withResources(() -> Files.walk(
                        Paths.get(this.propertiesService.getProperties().sourceFolder()), Integer.MAX_VALUE))
                .of(stream -> {
                    final var dirs = stream.parallel().filter(IS_MUSIC_FILE).collect(Collectors.toSet()).stream()
                            .map(o -> o.getParent().toString())
                            .collect(Collectors.toSet());
                    if (this.propertiesService.getProperties().skipLiveReleases()) {
                        return dirs.stream()
                                .filter(folderName -> FileServiceImpl.isNotLiveRelease(
                                        folderName, this.propertiesService.getProperties()))
                                .collect(Collectors.toSet());
                    } else {
                        return dirs;
                    }
                })
                .onFailure(e -> error(
                        "Error while walking directory: {}, {}",
                        this.propertiesService.getProperties().sourceFolder(),
                        e.getMessage(),
                        e))
                .getOrElse(new HashSet<>(0));
    }

    @Override
    public void processDirectories() {
        final var folderList = this.listAlbums();
        if (folderList.isEmpty()) {
            info("No data to work with. Exiting.");
            System.exit(0);
        }

        info("folders list created. size: {}", folderList.size());
        final var result = this.tryWithProgressBar(folderList, this.propertiesService.getProperties());
        if (result) {
            info("Finished. Cleaning empty directories ...");
            this.cleanUpService.cleanUpParentDirectory();
            System.exit(0);
        }
    }

    /*need*/
    @Override
    public ListingWithFormat createFileListForEachDir(final String folderName) {
        /*creating filter for musical files*/
        final var resultMp3 = FileServiceImpl.listFiles(folderName, MP3);
        final var resultFlac = FileServiceImpl.listFiles(folderName, FLAC);
        if (!resultMp3.isEmpty()) {
            return new ListingWithFormat(MP_3_FORMAT, resultMp3);
        } else if (!resultFlac.isEmpty()) {
            return new ListingWithFormat(FLAC_FORMAT, resultFlac);
        } else {
            return new ListingWithFormat(UNKNOWN, Collections.emptyList());
        }
    }
}
