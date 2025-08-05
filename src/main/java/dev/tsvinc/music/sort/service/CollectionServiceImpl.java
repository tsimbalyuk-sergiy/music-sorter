package dev.tsvinc.music.sort.service;

import static org.tinylog.Logger.error;
import static org.tinylog.Logger.info;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import dev.tsvinc.music.sort.domain.AppProperties;

import io.vavr.control.Try;

public class CollectionServiceImpl {

    private final PropertiesServiceImpl propertiesService;
    private final AudioFileServiceImpl audioFileService;
    private final boolean isWindows;

    public CollectionServiceImpl(PropertiesServiceImpl propertiesService, AudioFileServiceImpl audioFileService) {
        this.propertiesService = propertiesService;
        this.audioFileService = audioFileService;
        this.isWindows = System.getProperty("os.name").toLowerCase().contains("win");
    }

    public void createCollection() {
        final var properties = propertiesService.getProperties();
        final var sourcePath = Paths.get(properties.sourceFolder());
        final var targetPath = Paths.get(properties.targetFolder());

        info("[*] Creating collection structure in: {}", targetPath);
        info("[*] Using {} linking strategy", isWindows ? "hard link" : "symbolic link");

        Try.of(() -> {
                    createCollectionDirectories(targetPath);
                    processSourceDirectories(sourcePath, targetPath, properties);
                    return null;
                })
                .onFailure(e -> error("[ERROR] Collection creation failed: {}", e.getMessage(), e));
    }

    private void createCollectionDirectories(Path targetPath) throws IOException {
        Files.createDirectories(targetPath.resolve("by-year"));
        Files.createDirectories(targetPath.resolve("by-genre"));
        Files.createDirectories(targetPath.resolve("by-artist"));
        Files.createDirectories(targetPath.resolve("by-format"));
        info("[*] Created collection directory structure");
    }

    private void processSourceDirectories(Path sourcePath, Path targetPath, AppProperties properties)
            throws IOException {
        try (var directories = Files.walk(sourcePath, 2)) {
            directories
                    .filter(Files::isDirectory)
                    .filter(path -> !path.equals(sourcePath))
                    .forEach(albumPath -> processAlbumDirectory(albumPath, targetPath, properties));
        }
    }

    private void processAlbumDirectory(Path albumPath, Path targetPath, AppProperties properties) {
        Try.of(() -> {
                    final var metadata = audioFileService.getMetadata(albumPath.toString());

                    if (metadata.isValid()) {
                        final var albumName = albumPath.getFileName().toString();

                        createLinkInCategory(
                                albumPath,
                                targetPath.resolve("by-year").resolve(String.valueOf(metadata.year())),
                                albumName);
                        createLinkInCategory(
                                albumPath,
                                targetPath.resolve("by-genre").resolve(sanitizeDirectoryName(metadata.genre())),
                                albumName);
                        createLinkInCategory(
                                albumPath,
                                targetPath.resolve("by-artist").resolve(sanitizeDirectoryName(metadata.artist())),
                                albumName);
                        createLinkInCategory(
                                albumPath,
                                targetPath
                                        .resolve("by-format")
                                        .resolve(metadata.format().toUpperCase()),
                                albumName);

                        info(
                                "[*] Linked album: {} -> by-year/{}, by-genre/{}, by-artist/{}, by-format/{}",
                                albumName,
                                metadata.year(),
                                metadata.genre(),
                                metadata.artist(),
                                metadata.format().toUpperCase());
                    }
                    return null;
                })
                .onFailure(e ->
                        error("[ERROR] Failed to process album '{}': {}", albumPath.getFileName(), e.getMessage()));
    }

    private void createLinkInCategory(Path sourcePath, Path categoryPath, String albumName) throws IOException {
        Files.createDirectories(categoryPath);
        final var linkPath = categoryPath.resolve(albumName);

        if (Files.exists(linkPath)) {
            return; // Link already exists
        }

        Try.of(() -> {
                    if (isWindows) {
                        // Use hard links on Windows - only works for files, not directories
                        // For directories, we'll create the directory and link individual files
                        createDirectoryWithHardLinks(sourcePath, linkPath);
                    } else {
                        // Use symbolic links on Unix systems
                        Files.createSymbolicLink(linkPath, sourcePath);
                    }
                    return null;
                })
                .recover(throwable -> {
                    // Fallback: copy directory if linking fails
                    info("[WARN] Linking failed for '{}', falling back to copy", albumName);
                    return copyDirectory(sourcePath, linkPath);
                });
    }

    private void createDirectoryWithHardLinks(Path sourceDir, Path targetDir) throws IOException {
        Files.createDirectories(targetDir);

        try (var files = Files.walk(sourceDir)) {
            files.filter(Files::isRegularFile).forEach(sourceFile -> {
                Try.of(() -> {
                            final var relativePath = sourceDir.relativize(sourceFile);
                            final var targetFile = targetDir.resolve(relativePath);
                            Files.createDirectories(targetFile.getParent());
                            Files.createLink(targetFile, sourceFile);
                            return null;
                        })
                        .onFailure(e ->
                                error("[ERROR] Failed to create hard link for '{}': {}", sourceFile, e.getMessage()));
            });
        }
    }

    private Void copyDirectory(Path source, Path target) {
        Try.of(() -> {
                    try (var files = Files.walk(source)) {
                        files.forEach(sourceFile -> {
                            Try.of(() -> {
                                        final var relativePath = source.relativize(sourceFile);
                                        final var targetFile = target.resolve(relativePath);

                                        if (Files.isDirectory(sourceFile)) {
                                            Files.createDirectories(targetFile);
                                        } else {
                                            Files.createDirectories(targetFile.getParent());
                                            Files.copy(sourceFile, targetFile);
                                        }
                                        return null;
                                    })
                                    .onFailure(
                                            e -> error("[ERROR] Failed to copy '{}': {}", sourceFile, e.getMessage()));
                        });
                    }
                    return null;
                })
                .onFailure(e -> error("[ERROR] Directory copy failed: {}", e.getMessage()));
        return null;
    }

    private String sanitizeDirectoryName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Unknown";
        }
        return name.replaceAll("[<>:\"/\\\\|?*]", "_").trim();
    }
}
