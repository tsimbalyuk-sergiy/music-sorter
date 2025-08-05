package dev.tsvinc.music.sort.service;

import static dev.tsvinc.music.sort.util.Constants.FLAC;
import static dev.tsvinc.music.sort.util.Constants.FLAC_FORMAT;
import static dev.tsvinc.music.sort.util.Constants.MP3;
import static dev.tsvinc.music.sort.util.Constants.MP_3_FORMAT;
import static dev.tsvinc.music.sort.util.Constants.UNKNOWN;
import static dev.tsvinc.music.sort.util.Predicates.IS_MUSIC_FILE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.tinylog.Logger.error;
import static org.tinylog.Logger.info;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import dev.tsvinc.music.sort.domain.AppProperties;
import dev.tsvinc.music.sort.domain.ListingWithFormat;
import dev.tsvinc.music.sort.domain.Metadata;

import io.vavr.control.Try;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

public class FileServiceImpl implements FileService {

    public static final String UNDEFINED = "UNDEFINED";
    private static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 30;

    private final ExecutorService processingExecutor = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);

    private final PropertiesService propertiesService;
    private final CleanUpService cleanUpService;
    private final AudioFileService audioFileService;
    private final ChecksumService checksumService;

    public FileServiceImpl(
            PropertiesService propertiesService,
            CleanUpService cleanUpService,
            AudioFileService audioFileService,
            ChecksumService checksumService) {
        this.propertiesService = propertiesService;
        this.cleanUpService = cleanUpService;
        this.audioFileService = audioFileService;
        this.checksumService = checksumService;
    }

    private static boolean isNotLiveRelease(final String folderName, final AppProperties properties) {
        return properties.liveReleasesPatterns().forAll(pattern -> !folderName.contains(pattern));
    }

    private static ProgressBar buildProgressBar(final Set<String> folderList) {
        return new ProgressBarBuilder()
                .setInitialMax(folderList.size())
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BLOCK)
                .setTaskName("Processing albums")
                .setUnit(" albums", 1)
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
                .onFailure(throwable -> error(
                        "Failed to move album directory '{}': {}", sourceDirectory, throwable.getMessage(), throwable));
    }

    private static void move(final Path src, final Path destination) {
        Try.of(() -> Files.move(src, destination, REPLACE_EXISTING))
                .onFailure(throwable -> error(
                        "Failed to move file '{}' to '{}': {}",
                        src.getFileName(),
                        destination.getFileName(),
                        throwable.getMessage(),
                        throwable));
    }

    private static void createDirectory(final File destination) {
        Try.of(() -> Files.createDirectories(destination.toPath()))
                .onFailure(e -> error("Failed to create directory '{}': {}", destination.getPath(), e.getMessage(), e));
    }

    private static List<String> listFiles(final String folderName, final String glob) {
        final List<String> resultList = new ArrayList<>(100);
        try (final var stream = Files.newDirectoryStream(Paths.get(folderName), glob)) {
            stream.forEach(o -> resultList.add(folderName + File.separator + o.getFileName()));
        } catch (final IOException e) {
            error("Error scanning directory '{}' for {} files: {}", folderName, glob, e.getMessage(), e);
        }
        return resultList;
    }

    private boolean tryWithProgressBar(final Set<String> folderList, final AppProperties properties) {
        return Try.withResources(() -> FileServiceImpl.buildProgressBar(folderList))
                .of(progressBar -> {
                    final var processedCount = new AtomicInteger(0);
                    final var completableFutures = folderList.stream()
                            .map(release -> CompletableFuture.supplyAsync(
                                            () -> this.moveReleaseAsync(release, properties), processingExecutor)
                                    .thenRun(() -> {
                                        progressBar.step();
                                        processedCount.incrementAndGet();
                                    })
                                    .exceptionally(throwable -> {
                                        error(
                                                "[ERROR] Failed to process album '{}': {}",
                                                release,
                                                throwable.getMessage(),
                                                throwable);
                                        progressBar.step();
                                        return null;
                                    }))
                            .toArray(CompletableFuture[]::new);

                    CompletableFuture.allOf(completableFutures).join();
                    return processedCount.get() == folderList.size();
                })
                .onSuccess(allSuccessful -> {
                    if (allSuccessful) {
                        info("[OK] Successfully processed all {} albums", folderList.size());
                    } else {
                        info("[PARTIAL] Some albums failed to process. Check logs for details.");
                    }
                })
                .onFailure(throwable ->
                        error("[ERROR] Critical error during album processing: {}", throwable.getMessage(), throwable))
                .get();
    }

    private boolean moveReleaseAsync(final String sourceDirectory, final AppProperties properties) {
        final var source = new File(sourceDirectory);
        final var metadata = this.audioFileService.getMetadata(sourceDirectory);

        if (properties.checksumValidationEnabled() && this.checksumService.hasChecksumFiles(source)) {
            info("Validating checksums in {}", source.getName());
            if (!this.checksumService.validateDirectory(source)) {
                error("Checksum validation failed for {}. Skipping directory.", source.getName());
                return false;
            }
            info("Checksum validation passed for {}", source.getName());
        }
        final var outWithFormat = properties.targetFolder() + File.separator + metadata.format();
        final var outWithFormatDir = new File(outWithFormat);
        if (!outWithFormatDir.exists()) {
            FileServiceImpl.createDirectory(outWithFormatDir);
        }
        final String destinationPath = createDestinationPath(properties, outWithFormat, metadata);
        final var genreDir = new File(destinationPath);
        final var finalDestination = destinationPath + File.separator;
        final var destination = new File(finalDestination + source.getName());
        if (!genreDir.exists()) {
            FileServiceImpl.createDirectory(genreDir);
        }
        if (!destination.exists()) {
            FileServiceImpl.createDirectory(destination);
        }
        FileServiceImpl.moveDirectory(sourceDirectory, source, destination);
        return true;
    }

    private static String createDestinationPath(AppProperties properties, String outWithFormat, Metadata metadata) {
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
        return outWithGenreAndFormat;
    }

    private Set<String> listAlbums() {
        return Try.withResources(() -> Files.walk(
                        Paths.get(this.propertiesService.getProperties().sourceFolder()), Integer.MAX_VALUE))
                .of(stream -> {
                    final var dirs = stream.filter(IS_MUSIC_FILE).collect(Collectors.toSet()).stream()
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
        try {
            final var folderList = this.listAlbums();
            if (folderList.isEmpty()) {
                info("[INFO] No music albums found in source directory. Please check your configuration.");
                return;
            }

            info("[*] Found {} music albums to process", folderList.size());
            final var result = this.tryWithProgressBar(folderList, this.propertiesService.getProperties());
            if (result) {
                info("[CLEANUP] Processing complete. Cleaning up empty directories...");
                this.cleanUpService.cleanUpParentDirectory();
            }
        } finally {
            shutdownExecutor();
        }
    }

    private void shutdownExecutor() {
        processingExecutor.shutdown();
        try {
            if (!processingExecutor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                info("[WARN] Thread pool did not terminate gracefully, forcing shutdown...");
                processingExecutor.shutdownNow();
                if (!processingExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    error("[ERROR] Thread pool did not terminate after forced shutdown");
                }
            }
        } catch (InterruptedException e) {
            processingExecutor.shutdownNow();
            Thread.currentThread().interrupt();
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
