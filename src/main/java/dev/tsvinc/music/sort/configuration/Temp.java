//package dev.tsvinc.music.sort.configuration;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//
//import static dev.tsvinc.music.sort.Constants.FLAC_EXT;
//
//public class Temp {
//  private static final Logger log = LoggerFactory.getLogger(Temp.class);
//
//  public static void main(String[] args) {
//    //    String property = "-A-,-B-,-C-";
//    //    final var strings = Collections.unmodifiableList(List.of(property.split(",")));
//    //    strings.forEach(System.out::println);
//    String path = "/home/tsv/test/in/Liam_Gallagher-Why_Me_Why_Not.-(Deluxe_Edition)-2019-RiBS/";
//
//    try (final var stream = Files.newDirectoryStream(Paths.get(path), "mp3")) {
//      stream.forEach(o -> System.out.println(o.toString()));
//    } catch (final IOException e) {
//      log.error(
//          "Error listing directory: {} with filter: {}, {}", path, FLAC_EXT, e.getMessage(), e);
//    }
//    //    try (final var stream = Files.newDirectoryStream(Paths.get(path), "mp3")) {
//    //      stream.forEach(o -> System.out.println(o.toString()));
//    //    } catch (final IOException e) {
//    //      Logger.error(
//    //          "Error listing directory: {} with filter: {}, {}",
//    //          path,
//    //          FLAC_EXT,
//    //          e.getMessage(),
//    //          e);
//    //    }
//    //    try (final var stream = Files.newDirectoryStream(Paths.get(path), ".mp3")) {
//    //      stream.forEach(o -> System.out.println(o.toString()));
//    //    } catch (final IOException e) {
//    //      Logger.error(
//    //          "Error listing directory: {} with filter: {}, {}",
//    //          path,
//    //          FLAC_EXT,
//    //          e.getMessage(),
//    //          e);
//    //    }
//  }
//}
