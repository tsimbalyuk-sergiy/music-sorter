package dev.tsvinc.music.sort.service;

import static org.tinylog.Logger.error;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.vavr.control.Try;

public class CleanUpServiceImpl implements CleanUpService {
    private final PropertiesService propertiesService;

    public CleanUpServiceImpl(PropertiesService propertiesService) {
        this.propertiesService = propertiesService;
    }

    private static boolean isEmptyDirectory(final Path path) {
        return Try.withResources(() -> Files.list(path))
                .of(stream -> stream.findAny().isEmpty())
                .onFailure(e -> error("[ERROR] Error checking if directory '{}' is empty: {}", path, e.getMessage()))
                .getOrElse(false);
    }

    public void cleanUpParentDirectory() {
        final var listOfEmptyDirectories = new AtomicReference<>(CleanUpServiceImpl.getListOfEmptyDirectories(
                Paths.get(this.propertiesService.getProperties().sourceFolder())));
        while (!listOfEmptyDirectories.get().isEmpty()) {
            Try.of(() -> {
                        CleanUpServiceImpl.deleteEachEmptyDirectory(listOfEmptyDirectories);
                        listOfEmptyDirectories.set(CleanUpServiceImpl.getListOfEmptyDirectories(
                                Paths.get(this.propertiesService.getProperties().sourceFolder())));
                        return null;
                    })
                    .onFailure(this::logErrorWhileWalkingDirectory);
        }
    }

    private void logErrorWhileWalkingDirectory(final Throwable e) {
        error(
                "[ERROR] Error scanning source directory '{}': {}",
                this.propertiesService.getProperties().sourceFolder(),
                e.getMessage(),
                e);
    }

    private static void deleteEachEmptyDirectory(final AtomicReference<List<Path>> listOfEmptyDirectories) {
        listOfEmptyDirectories.get().forEach(CleanUpServiceImpl::safelyDeleteDirectory);
    }

    private static void safelyDeleteDirectory(final Path dir) {
        try {
            Files.delete(dir);
        } catch (final IOException ioe) {
            error("[ERROR] Failed to delete empty directory '{}': {}", dir, ioe.getMessage(), ioe);
        }
    }

    private static List<Path> getListOfEmptyDirectories(final Path directory) {
        return Try.withResources(() -> Files.walk(directory, Integer.MAX_VALUE))
                .of(stream -> stream.filter(Files::isDirectory)
                        .filter(path -> !path.equals(directory))
                        .filter(CleanUpServiceImpl::isEmptyDirectory)
                        .toList())
                .onFailure(e ->
                        error("[ERROR] Error scanning directory '{}' for cleanup: {}", directory, e.getMessage(), e))
                .getOrElse(Collections.emptyList());
    }
}
