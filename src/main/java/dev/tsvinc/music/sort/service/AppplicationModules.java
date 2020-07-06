package dev.tsvinc.music.sort.service;

import com.google.inject.AbstractModule;

public class AppplicationModules extends AbstractModule {
  @Override
  protected void configure() {
    bind(PropertiesService.class).to(PropertiesServiceImpl.class);
    bind(FileService.class).to(FileServiceImpl.class);
    bind(CleanUpService.class).to(CleanUpServiceImpl.class);
    bind(AudioFileService.class).to(AudioFileServiceImpl.class);
  }
}
