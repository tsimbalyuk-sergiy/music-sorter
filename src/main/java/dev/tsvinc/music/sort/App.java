package dev.tsvinc.music.sort;

import com.google.inject.Guice;
import dev.tsvinc.music.sort.service.ApplicationModules;
import dev.tsvinc.music.sort.service.FileService;
import org.tinylog.Logger;

import javax.inject.Inject;
import java.util.logging.LogManager;

public class App {

    @Inject
    private FileService fileService;

    public static void main(final String[] args) {
        LogManager.getLogManager().getLogger("").setLevel(java.util.logging.Level.OFF);
        final var app = new App();
        Guice.createInjector(new ApplicationModules()).injectMembers(app);
        Logger.info("Starting application ...");
        app.fileService.processDirectories();
    }
}
