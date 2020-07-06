package dev.tsvinc.music.sort.service;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import dev.tsvinc.music.sort.infrastructure.nosql.dao.NitriteInstance;
import dev.tsvinc.music.sort.infrastructure.nosql.dao.NitriteInstanceImp;
import dev.tsvinc.music.sort.infrastructure.nosql.dao.ReleaseDao;
import dev.tsvinc.music.sort.infrastructure.nosql.dao.ReleaseDaoImpl;

public class ApplicationModules extends AbstractModule {

  @Override
  protected void configure() {
    bind(PropertiesService.class).to(PropertiesServiceImpl.class).in(Singleton.class);
    bind(FileService.class).to(FileServiceImpl.class).in(Singleton.class);
    bind(CleanUpService.class).to(CleanUpServiceImpl.class).in(Singleton.class);
    bind(AudioFileService.class).to(AudioFileServiceImpl.class).in(Singleton.class);
    bind(ReleaseDao.class).to(ReleaseDaoImpl.class).in(Singleton.class);
    bind(NitriteInstance.class).to(NitriteInstanceImp.class)
        .in(Singleton.class);
  }
}
