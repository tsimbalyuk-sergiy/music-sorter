package dev.tsvinc.music.sort.util;

import java.nio.file.Path;
import java.util.function.Predicate;

public class Predicates {

    private Predicates() {}

    public static final Predicate<Path> IS_MUSIC_FILE =
            path -> path.getFileName().toString().contains(".mp3")
                    || path.getFileName().toString().contains(".flac");
}
