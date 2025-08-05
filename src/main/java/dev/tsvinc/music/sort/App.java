package dev.tsvinc.music.sort;

import com.google.inject.Guice;
import com.google.inject.Inject;
import dev.tsvinc.music.sort.service.ApplicationModules;
import dev.tsvinc.music.sort.service.FileService;

public class App {

    @Inject
    private FileService fileService;

    public static void main(final String[] args) {
        org.tinylog.jul.JulTinylogBridge.activate();
        final var app = new App();
        Guice.createInjector(new ApplicationModules()).injectMembers(app);
        app.fileService.processDirectories();
    }
}
// Simple test comment
