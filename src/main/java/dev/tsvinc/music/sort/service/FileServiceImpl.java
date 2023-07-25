package dev.tsvinc.music.sort.service;

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

import com.google.inject.Inject;
import dev.tsvinc.music.sort.domain.AppProperties;
import dev.tsvinc.music.sort.domain.ListingWithFormat;
import io.vavr.control.Try;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

public class FileServiceImpl implements FileService {

    public static final String UNDEFINED = "UNDEFINED";
    private static final Pattern SPACES = java.util.regex.Pattern.compile("\\s+");
    private static final Pattern SFV_ENTRY_PATTERN = Pattern.compile("^\\s*([^;#].+\\S)\\s+(0x)?([\\dA-Fa-f]{1,8})$");

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
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BLOCK)
                .setTaskName("Working ...")
                .showSpeed()
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
        try (final var stream = Files.newDirectoryStream(Paths.get(folderName), glob)) {
            stream.forEach(o -> resultList.add(folderName + File.separator + o.getFileName()));
        } catch (final IOException e) {
            error("Error listing directory: {} with a filter: {}, {}", folderName, FLAC, e.getMessage(), e);
        }
        return resultList;
    }

    private boolean tryWithProgressBar(final Set<String> folderList, final AppProperties properties) {
        return Try.withResources(() -> FileServiceImpl.buildProgressBar(folderList))
                .of(progressBar -> {
                    folderList.forEach(release -> {
                        moveRelease(release, properties);
                        progressBar.step();
                    });
                    return true;
                })
                .onSuccess(status -> info("finished moving releases: {}", status))
                .onFailure(throwable -> error("had errors moving releases: {}", throwable.getMessage(), throwable))
                .get();
    }

    private void moveRelease(final String sourceDirectory, final AppProperties properties) {
        final var source = new File(sourceDirectory);
        final var metadata = audioFileService.getMetadata(sourceDirectory);
        var containsCheckSum = false;
        if (FileServiceImpl.hasCheckSum(source).containsKey(CHECKSUM)) {
            containsCheckSum = true;
            FileServiceImpl.verifyCheckSum(source); // TODO
        }

        /*
         * private String releaseName;
         * private long releaseSize;
         * private boolean hasNfo;
         * private boolean hasChecksum;
         * private boolean checksumValid;
         */

        /*
         * Release.builder()
         * .artist(metadata.getArtist())
         * .releaseName(source.getName())
         * .hasChecksum(containsCheckSum)
         * .build();
         */

        final var outWithFormat = properties.targetFolder() + File.separator + metadata.format();
        final var outWithFormatDir = new File(outWithFormat);
        if (!outWithFormatDir.exists()) {
            FileServiceImpl.createDirectory(outWithFormatDir);
        }
        final String outWithGenreAndFormat;
        if (properties.sortByArtist()) {
            outWithGenreAndFormat = Path.of(
                            outWithFormat,
                            null != metadata.genre() ? metadata.genre() : UNDEFINED,
                            null != metadata.artist() ? metadata.artist().trim() : UNDEFINED)
                    .toString();
        } else if (metadata.genre() != null && !metadata.genre().isBlank()) {
            outWithGenreAndFormat =
                    Path.of(outWithFormat, metadata.genre().strip()).toString();
        } else {
            outWithGenreAndFormat = Path.of(outWithFormat, UNKNOWN).toString();
        }
        final var genreDir = new File(outWithGenreAndFormat);
        if (!genreDir.exists()) {
            FileServiceImpl.createDirectory(genreDir);
        }
        final var finalDestination = outWithGenreAndFormat + File.separator;
        final var destination = new File(finalDestination + source.getName());
        if (!destination.exists()) {
            FileServiceImpl.createDirectory(destination);
        }
        FileServiceImpl.moveDirectory(sourceDirectory, source, destination);
    }

    public static void verifyCheckSum(final File source) {
        final var paths = Try.withResources(() -> Files.list(source.toPath()))
                .of(Stream::toList)
                .get();

        final var sfvFile = paths.parallelStream()
                .filter(path -> path.toFile().getName().endsWith("sfv"))
                .findFirst();

        final var sfvEntries = Try.of(() ->
                        Files.readAllLines(sfvFile.orElseThrow(FileNotFoundException::new), StandardCharsets.UTF_8))
                .onFailure(t -> error("Error reading lines: {}, {}", sfvFile.toString(), t.getMessage(), t))
                .get()
                .parallelStream()
                .filter(line -> SFV_ENTRY_PATTERN.matcher(line).matches())
                .map(SPACES::split)
                .collect(Collectors.toMap(res -> res[0], res -> 1 < res.length ? res[1] : ""));
        final CRC32 crc = new CRC32();
        sfvEntries.entrySet().stream()
                .filter(stringStringEntry -> Files.exists(source.toPath().resolve(stringStringEntry.getKey())))
                .filter(stringStringEntry -> Files.isRegularFile(source.toPath().resolve(stringStringEntry.getKey())))
                .forEach(stringStringEntry -> {
                    final Path file = source.toPath().resolve(stringStringEntry.getKey());
                    if (Files.exists(file, LinkOption.NOFOLLOW_LINKS) && Files.isRegularFile(file)) {
                        crc.reset();
                        try (final CheckedInputStream checkedInputStream =
                                new CheckedInputStream(new BufferedInputStream(Files.newInputStream(file)), crc)) {
                            final long fileSize = Files.size(file);
                            long bytesRead = 0L;
                            int numBytesRead;
                            int previousPercent = 0;
                            final byte[] buffer = new byte[64];

                            while ((numBytesRead = checkedInputStream.read(buffer)) != -1) {
                                bytesRead += numBytesRead;
                                final int currentPercent =
                                        (int) ((((double) bytesRead) / ((double) fileSize)) * 100.0d);
                                if (currentPercent != previousPercent) {
                                    previousPercent = currentPercent;
                                }
                            }
                        } catch (final IOException exception) {
                            error("error working with sfv: {}", exception.getMessage(), exception);
                        }

                        final long crcValue = crc.getValue();
                        final long expectedCrcValue = Long.parseLong(stringStringEntry.getValue(), 16);
                        if (crcValue != expectedCrcValue) {
                            error("{} CRC32 mismatch: expected {}, got {}", file, expectedCrcValue, crcValue);
                        } else {
                            info("CRC32 OK: {} {}", file, crcValue);
                        }
                    }
                });
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
        return Try.withResources(() ->
                        Files.walk(Paths.get(propertiesService.getProperties().sourceFolder()), Integer.MAX_VALUE))
                .of(stream -> {
                    final var dirs = stream.filter(IS_MUSIC_FILE).collect(Collectors.toSet()).stream()
                            .map(o -> o.getParent().toString())
                            .collect(Collectors.toSet());
                    if (propertiesService.getProperties().skipLiveReleases()) {
                        return dirs.stream()
                                .filter(folderName ->
                                        FileServiceImpl.isNotLiveRelease(folderName, propertiesService.getProperties()))
                                .collect(Collectors.toSet());
                    } else {
                        return dirs;
                    }
                })
                .onFailure(e -> error(
                        "Error while walking directory: {}, {}",
                        propertiesService.getProperties().sourceFolder(),
                        e.getMessage(),
                        e))
                .getOrElse(new HashSet<>(0));
    }

    @Override
    public void processDirectories() {
        final var folderList = listAlbums();
        if (folderList.isEmpty()) {
            info("No data to work with. Exiting.");
            System.exit(0);
        }

        info("folders list created. size: {}", folderList.size());
        final var result = tryWithProgressBar(folderList, propertiesService.getProperties());
        if (result) {
            info("Finished. Cleaning empty directories ...");
            cleanUpService.cleanUpParentDirectory();
            System.exit(0);
        }
    }

    /* need */
    @Override
    public ListingWithFormat createFileListForEachDir(final String folderName) {
        /* creating filter for musical files */
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
