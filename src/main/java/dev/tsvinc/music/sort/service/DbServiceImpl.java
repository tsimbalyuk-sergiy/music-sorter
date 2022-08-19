package dev.tsvinc.music.sort.service;

import com.google.inject.Inject;
import dev.tsvinc.music.sort.infrastructure.dao.ReleaseDaoImpl;

public class DbServiceImpl implements DbService {

    @Inject
    private ReleaseDaoImpl releaseDao;

    public void something_() {
        //    releaseDao.findAll()
    }
}
