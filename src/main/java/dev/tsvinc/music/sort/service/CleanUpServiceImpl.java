package dev.tsvinc.music.sort.service;

import static org.tinylog.Logger.error;

import com.google.inject.Inject;
import io.vavr.control.Try;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class CleanUpServiceImpl implements CleanUpService {
    @Inject
    PropertiesService propertiesService;

    private static boolean isEmptyDirectory(final Path path) {
        return Try.withResources(() -> Files.list(path))
                .of(stream -> stream.findAny().isEmpty())
                .onFailure(e -> error("Error listing directory: {}, {}", path, e.getMessage()))
                .getOrElse(false);
    }

    public void cleanUpParentDirectory() {
        final var emptyDirsList = new AtomicReference<>(CleanUpServiceImpl.getListOfEmptyDirectories(
                Paths.get(this.propertiesService.getProperties().sourceFolder())));

        while (!emptyDirsList.get().isEmpty()) {
            runOrLogError(emptyDirsList);
        }
    }

    private void runOrLogError(final AtomicReference<List<Path>> emptyDirsList) {
        Try.run(() -> {
                    CleanUpServiceImpl.deleteEachEmptyDirectory(emptyDirsList);
                    emptyDirsList.set(CleanUpServiceImpl.getListOfEmptyDirectories(
                            Paths.get(this.propertiesService.getProperties().sourceFolder())));
                })
                .onFailure(this::logErrorWhileWalkingDirectory);
    }

    private void logErrorWhileWalkingDirectory(final Throwable e) {
        error(
                "Error while walking directory: {}, {}",
                this.propertiesService.getProperties().sourceFolder(),
                e.getMessage(),
                e);
    }

    private static void deleteEachEmptyDirectory(final AtomicReference<? extends List<Path>> emptyDirsList) {
        emptyDirsList.get().forEach(CleanUpServiceImpl::safelyDeleteDirectory);
    }

    private static void safelyDeleteDirectory(final Path dir) {
        try {
            Files.delete(dir);
        } catch (final IOException ioe) {
            error("Error deleting folder: {} {}\n{}", dir, ioe.getMessage(), ioe);
        }
    }

    private static List<Path> getListOfEmptyDirectories(final Path directory) {
        return Try.withResources(() -> Files.walk(directory, Integer.MAX_VALUE))
                .of(stream -> stream.filter(Files::isDirectory)
                        .filter(path -> !path.equals(directory))
                        .filter(CleanUpServiceImpl::isEmptyDirectory)
                        .toList())
                .onFailure(e -> error("Error walking directory: {}, {}", directory, e.getMessage(), e))
                .getOrElse(Collections.emptyList());
    }
}
