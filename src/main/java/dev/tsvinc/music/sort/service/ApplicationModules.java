package dev.tsvinc.music.sort.service;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import dev.tsvinc.music.sort.infrastructure.dao.NitriteInstance;
import dev.tsvinc.music.sort.infrastructure.dao.NitriteInstanceImp;
import dev.tsvinc.music.sort.infrastructure.dao.ReleaseDao;
import dev.tsvinc.music.sort.infrastructure.dao.ReleaseDaoImpl;

public class ApplicationModules extends AbstractModule {

    @Override
    protected void configure() {
        this.bind(PropertiesService.class).to(PropertiesServiceImpl.class).in(Singleton.class);
        this.bind(FileService.class).to(FileServiceImpl.class).in(Singleton.class);
        this.bind(CleanUpService.class).to(CleanUpServiceImpl.class).in(Singleton.class);
        this.bind(AudioFileService.class).to(AudioFileServiceImpl.class).in(Singleton.class);
        this.bind(ReleaseDao.class).to(ReleaseDaoImpl.class).in(Singleton.class);
        this.bind(NitriteInstance.class).to(NitriteInstanceImp.class).in(Singleton.class);
    }
}
