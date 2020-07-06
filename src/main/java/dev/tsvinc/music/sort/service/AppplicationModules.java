package dev.tsvinc.music.sort.service;

import com.google.inject.AbstractModule;
import dev.tsvinc.music.sort.infrastructure.nosql.dao.ReleaseDao;
import dev.tsvinc.music.sort.infrastructure.nosql.dao.ReleaseDaoImpl;

public class AppplicationModules extends AbstractModule {
  @Override
  protected void configure() {
    bind(PropertiesService.class).to(PropertiesServiceImpl.class);
    bind(FileService.class).to(FileServiceImpl.class);
    bind(CleanUpService.class).to(CleanUpServiceImpl.class);
    bind(AudioFileService.class).to(AudioFileServiceImpl.class);
    bind(ReleaseDao.class).to(ReleaseDaoImpl.class);
  }
}
