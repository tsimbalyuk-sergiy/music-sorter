package dev.tsvinc.music.sort.service;

import dev.tsvinc.music.sort.domain.AppProperties;

public interface PropertiesService {
  boolean initProperties();

  AppProperties getProperties();
}
