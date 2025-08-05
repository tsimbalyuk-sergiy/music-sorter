package dev.tsvinc.music.sort.service;

import static org.tinylog.Logger.debug;
import static org.tinylog.Logger.error;
import static org.tinylog.Logger.warn;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

import io.vavr.control.Try;

public class ChecksumServiceImpl implements ChecksumService {

    @Override
    public boolean validateDirectory(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            return false;
        }

        var sfvValid = validateSfvFiles(directory);
        var md5Valid = validateMd5Files(directory);

        return sfvValid && md5Valid;
    }

    @Override
    public boolean hasChecksumFiles(File directory) {
        return Try.withResources(() -> Files.list(directory.toPath()))
                .of(pathStream -> pathStream.anyMatch(path -> {
                    String fileName = path.getFileName().toString().toLowerCase();
                    return fileName.endsWith(".sfv") || fileName.endsWith(".md5");
                }))
                .getOrElse(false);
    }

    private boolean validateSfvFiles(File directory) {
        List<Path> sfvFiles = findFilesByExtension(directory, ".sfv");
        if (sfvFiles.isEmpty()) {
            debug("No SFV files found in {}", directory.getAbsolutePath());
            return true;
        }

        for (Path sfvFile : sfvFiles) {
            if (!validateSfvFile(sfvFile)) {
                return false;
            }
        }
        return true;
    }

    private boolean validateMd5Files(File directory) {
        List<Path> md5Files = findFilesByExtension(directory, ".md5");
        if (md5Files.isEmpty()) {
            debug("No MD5 files found in {}", directory.getAbsolutePath());
            return true;
        }

        for (Path md5File : md5Files) {
            if (!validateMd5File(md5File)) {
                return false;
            }
        }
        return true;
    }

    private List<Path> findFilesByExtension(File directory, String extension) {
        return Try.withResources(() -> Files.list(directory.toPath()))
                .of(pathStream -> pathStream
                        .filter(path ->
                                path.getFileName().toString().toLowerCase().endsWith(extension))
                        .collect(Collectors.toList()))
                .getOrElse(Collections.emptyList());
    }

    private boolean validateSfvFile(Path sfvFile) {
        debug("Validating SFV file: {}", sfvFile.getFileName());

        Map<String, String> expectedChecksums = parseSfvFile(sfvFile);
        if (expectedChecksums.isEmpty()) {
            warn("Failed to parse SFV file: {}", sfvFile.getFileName());
            return false;
        }

        File directory = sfvFile.getParent().toFile();
        for (Map.Entry<String, String> entry : expectedChecksums.entrySet()) {
            String fileName = entry.getKey();
            String expectedCrc = entry.getValue().toUpperCase();

            File file = new File(directory, fileName);
            if (!file.exists()) {
                error("File referenced in SFV not found: {}", fileName);
                return false;
            }

            String actualCrc = calculateCrc32(file);
            if (!expectedCrc.equals(actualCrc)) {
                error("CRC32 mismatch for {}: expected {}, got {}", fileName, expectedCrc, actualCrc);
                return false;
            }
            debug("CRC32 validated for {}: {}", fileName, actualCrc);
        }
        return true;
    }

    private boolean validateMd5File(Path md5File) {
        debug("Validating MD5 file: {}", md5File.getFileName());

        Map<String, String> expectedChecksums = parseMd5File(md5File);
        if (expectedChecksums.isEmpty()) {
            warn("Failed to parse MD5 file: {}", md5File.getFileName());
            return false;
        }

        File directory = md5File.getParent().toFile();
        for (Map.Entry<String, String> entry : expectedChecksums.entrySet()) {
            String fileName = entry.getKey();
            String expectedMd5 = entry.getValue().toLowerCase();

            File file = new File(directory, fileName);
            if (!file.exists()) {
                error("File referenced in MD5 not found: {}", fileName);
                return false;
            }

            String actualMd5 = calculateMd5(file);
            if (!expectedMd5.equals(actualMd5)) {
                error("MD5 mismatch for {}: expected {}, got {}", fileName, expectedMd5, actualMd5);
                return false;
            }
            debug("MD5 validated for {}: {}", fileName, actualMd5);
        }
        return true;
    }

    private Map<String, String> parseSfvFile(Path sfvFile) {
        return Try.of(() -> Files.readAllLines(sfvFile))
                .map(lines -> lines.stream()
                        .filter(line -> !line.trim().isEmpty() && !line.startsWith(";"))
                        .map(line -> line.split("\\s+", 2))
                        .filter(parts -> parts.length == 2)
                        .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1])))
                .onFailure(e -> error("Error parsing SFV file {}: {}", sfvFile.getFileName(), e.getMessage()))
                .getOrElse(Collections.emptyMap());
    }

    private Map<String, String> parseMd5File(Path md5File) {
        return Try.of(() -> Files.readAllLines(md5File))
                .map(lines -> lines.stream()
                        .filter(line -> !line.trim().isEmpty())
                        .map(line -> {
                            String[] parts = line.split("\\s+", 2);
                            if (parts.length == 2) {
                                String hash = parts[0];
                                String filename = parts[1].startsWith("*") ? parts[1].substring(1) : parts[1];
                                return new String[] {filename, hash};
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1])))
                .onFailure(e -> error("Error parsing MD5 file {}: {}", md5File.getFileName(), e.getMessage()))
                .getOrElse(Collections.emptyMap());
    }

    private String calculateCrc32(File file) {
        CRC32 crc32 = new CRC32();

        return Try.withResources(() -> new FileInputStream(file))
                .of(inputStream -> {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        crc32.update(buffer, 0, bytesRead);
                    }
                    return String.format("%08X", crc32.getValue());
                })
                .onFailure(e -> error("Error calculating CRC32 for {}: {}", file.getName(), e.getMessage()))
                .getOrElse("");
    }

    private String calculateMd5(File file) {
        return Try.of(() -> MessageDigest.getInstance("MD5"))
                .flatMap(md5 -> Try.withResources(() -> new FileInputStream(file))
                        .of(inputStream -> {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                md5.update(buffer, 0, bytesRead);
                            }
                            byte[] digest = md5.digest();
                            StringBuilder hexString = new StringBuilder();
                            for (byte b : digest) {
                                String hex = Integer.toHexString(0xff & b);
                                if (hex.length() == 1) {
                                    hexString.append('0');
                                }
                                hexString.append(hex);
                            }
                            return hexString.toString();
                        }))
                .onFailure(e -> error("Error calculating MD5 for {}: {}", file.getName(), e.getMessage()))
                .getOrElse("");
    }
}
