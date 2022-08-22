package dev.tsvinc.music.sort.util;

import dev.tsvinc.music.sort.domain.ChecksumVerificationResult;
import org.pmw.tinylog.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.SneakyThrows;

// @Builder
// @Data
// @AllArgsConstructor
// @NoArgsConstructor
public class SfvNio2 {

  public static final int IO_BUFFER_LENGTH = 8192;
  private List<ChecksumVerificationResult> results;

  public List<ChecksumVerificationResult> getResults() {
    return results;
  }

  public void checkCrcForFolderAsync(Path path) {
    CompletableFuture.supplyAsync(() -> path)
        .thenAccept(
            s -> {
              try {
                performCheck(path);
              } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
              }
            });
  }

  public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
    final var path = Paths.get("/home/tsv/test/02-edith_piaf-legende.mp3");

    final var crc = new CRC32();
    crc.update(Files.readAllBytes(path));
    String expected = "4cafbad7";
    Logger.info("{} << >> {}", Long.toHexString(crc.getValue()), expected);
    //    performCheck(path);
    //    02-edith_piaf-legende.mp3 4cafbad7
  }

  private static boolean checkFileCrc(Path path, String expected) {
    CRC32 crc = null;
    try {
      crc = new CRC32();
      crc.update(Files.readAllBytes(path));
    } catch (IOException e) {
      Logger.error("Got error during crc calculation, {}", e.getMessage(), e);
    }
    return Long.toHexString(crc.getValue()).equalsIgnoreCase(expected);
  }

  //  @SneakyThrows
  private static void performCheck(Path path) throws IOException, NoSuchAlgorithmException {
    byte[] b = Files.readAllBytes(path);
    byte[] hash = MessageDigest.getInstance("CRC32").digest(b);
    String expected = "4cafbad7";
    String actual = printHexBinary(hash);
    Logger.info("{} << >> {}", actual, expected);
    System.out.println(expected.equalsIgnoreCase(actual) ? "MATCH" : "NO MATCH");
  }

  private static void checkCrc(final CRC32 crc, final Path file) {
    try (final var cin =
        new CheckedInputStream(new BufferedInputStream(Files.newInputStream(file)), crc)) {
      final var total = Files.size(file);
      var track = 0L;
      int read;
      var lastPercent = 0;
      final var buffer = new byte[64];

      while (-1 != (read = cin.read(buffer))) {
        track += read;
        final var percent = (int) ((((double) track) / ((double) total)) * 100.0d);
        if (percent != lastPercent) {
          Logger.info("{}%", percent);
          lastPercent = percent;
        }
      }
    } catch (final IOException e) {
      Logger.error("{}", e.getMessage(), e);
    }
  }

  private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

  public static String printHexBinary(byte[] data) {
    StringBuilder r = new StringBuilder(data.length * 2);
    for (byte b : data) {
      r.append(hexCode[(b >> 4) & 0xF]);
      r.append(hexCode[(b & 0xF)]);
    }
    return r.toString();
  }

  public static byte[] MD5(byte[] bytes) {
    return messageDigest(bytes, "MD5");
  }

  public static byte[] SHA1(byte[] bytes) {
    return messageDigest(bytes, "SHA1");
  }

  public static byte[] SHA256(byte[] bytes) {
    return messageDigest(bytes, "SHA256");
  }

  public static byte[] MD5(File file) {
    return messageDigest(file, "MD5");
  }

  public static byte[] SHA1(File file) {
    return messageDigest(file, "SHA1");
  }

  //  public static byte[] SHA256(File file) {
  //    return messageDigest(file, "SHA256");
  //  }
  //
  //  public static byte[] MD5(BufferedImage image) {
  //    return messageDigest(ImageManufacture.bytes(image), "MD5");
  //  }
  //
  //  public static byte[] SHA1(BufferedImage image) {
  //    return messageDigest(ImageManufacture.bytes(image), "SHA1");
  //  }
  //
  //  public static byte[] SHA256(BufferedImage image) {
  //    return messageDigest(ImageManufacture.bytes(image), "SHA256");
  //  }

  public static byte[] messageDigest(byte[] bytes, String algorithm) {
    try {
      MessageDigest md = MessageDigest.getInstance(algorithm);
      byte[] digest = md.digest(bytes);
      return digest;
    } catch (Exception e) {
      Logger.debug(e.toString());
      return null;
    }
  }

  public static byte[] messageDigest(File file, String algorithm) {
    try {
      MessageDigest md = MessageDigest.getInstance(algorithm);
      try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
        byte[] buf = new byte[IO_BUFFER_LENGTH];
        int len;
        while ((len = in.read(buf)) != -1) {
          md.update(buf, 0, len);
        }
      }
      byte[] digest = md.digest();
      return digest;
    } catch (Exception e) {
      Logger.debug(e.toString());
      return null;
    }
  }
}
