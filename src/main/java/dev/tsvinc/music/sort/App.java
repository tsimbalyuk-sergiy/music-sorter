package dev.tsvinc.music.sort;

import dev.tsvinc.music.sort.service.FileService;
import java.util.logging.LogManager;
import org.tinylog.Logger;

public class App {

    public static void main(final String[] args) {
        LogManager.getLogManager().getLogger("").setLevel(java.util.logging.Level.OFF);
        Logger.info("Starting application ...");
        FileService.getInstance().processDirectories();
        FileService.disposeInstance();
    }
}
