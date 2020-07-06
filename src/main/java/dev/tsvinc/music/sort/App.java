package dev.tsvinc.music.sort;

import com.google.inject.Guice;
import dev.tsvinc.music.sort.service.ApplicationModules;
import dev.tsvinc.music.sort.service.FileService;
import java.util.Locale;
import java.util.logging.Level;
import javax.inject.Inject;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.writers.ConsoleWriter;

public class App {

  static {
    java.util.logging.Logger.getLogger("org.jaudiotagger").setLevel(Level.SEVERE);

    Configurator.defaultConfig()
        .writer(new ConsoleWriter())
        .level(org.pmw.tinylog.Level.INFO)
        .locale(Locale.US)
        .formatPattern(
            "[{level}:{class_name}:{line}] {message}") /*https://tinylog.org/configuration*/
        .activate();
  }

  @Inject private FileService fileService;

  public static void main(final String[] args) {
    App app = new App();
    Guice.createInjector(new ApplicationModules()).injectMembers(app);

    app.fileService.processDirectories();
  }
}
