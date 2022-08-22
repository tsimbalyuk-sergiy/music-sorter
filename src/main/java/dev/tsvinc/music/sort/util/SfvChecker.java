package dev.tsvinc.music.sort.util;

import dev.tsvinc.music.sort.domain.ChecksumVerificationResult;
import org.pmw.tinylog.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

@SuppressWarnings("unused")
public final class SfvChecker {
  //  private static final Logger log = LoggerFactory.getLogger(SfvChecker.class);

  public static final String SFV_STRING_PATTERN = "^\\s*([^;#].+\\S)\\s+(0x)?([\\dA-Fa-f]{1,8})$";
  public static final LinkOption[] LINK_OPTIONS = new LinkOption[0];

  private SfvChecker() {}

  public <T> List<ChecksumVerificationResult<T>> check(final boolean noFollowLinks, Path sfv)
      throws IOException {
    final var lnkOptions = (noFollowLinks ? LinkOption.values() : LINK_OPTIONS);

    sfv = sfv.toRealPath(lnkOptions);

    final var sfvParentDir = sfv.getParent();
    var lineCount = 1;
    final var crc = new CRC32();

    Logger.info("Reading the contents of {}", sfv);
    final var lines = Files.readAllLines(sfv, StandardCharsets.UTF_8);

    // Loop through the lines of the sfv file.
    // If a line starts with ';', then it is a comment and is ignored.
    for (final var line : lines) {
      lineCount = readSfv(lnkOptions, sfvParentDir, lineCount, crc, line);
    }
    return new ArrayList<>();
  }

  private static int readSfv(
      final LinkOption[] lnkOptions,
      final Path sfvParentDir,
      int lineCount,
      final CRC32 crc,
      final String line) {

    final var filePattern = Pattern.compile(SFV_STRING_PATTERN);
    final var matcher = filePattern.matcher(line);
    // File line, group 1 is the file name, group 3 is the crc32
    if (matcher.matches()) {
      final var filePart = Paths.get(matcher.group(1));
      final var file = sfvParentDir.resolve(filePart);

      if (Files.exists(file, lnkOptions)) {
        crc.reset();

        checkCrc(crc, file);
      } else {
        Logger.warn("{} was not found.", file);
        return lineCount;
      }

      Logger.info("{}", filePart);
      try {
        final var sum1 = Long.parseLong(matcher.group(3), 16);
        final var sum2 = crc.getValue();

        if (sum1 == sum2) {

          Logger.info(" ... good.");
        } else {
          Logger.info(" ... bad.");
        }
      } catch (final NumberFormatException nfe) {
        Logger.error(" ... error.");
      }
    } else {
      onMalformedLine(lineCount, line, matcher);
    }
    lineCount++;
    return lineCount;
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

  private static void onMalformedLine(
      final int lineCount, final String line, final Matcher matcher) {
    if (matcher.hitEnd() && (!line.isEmpty())) {
      Logger.error("Malformed SFV line (#{}): \"{}\"", lineCount, line);
    }
  }
}
