module music.sorter {
  requires java.logging;
  requires java.base;
  requires java.desktop;
  requires java.management;
  requires java.naming;
  requires java.sql;
  requires java.xml;
  requires tinylog;
  requires javax.inject;
  requires com.google.guice;
  requires io.vavr;
  requires progressbar;
  requires ealvatag;
  requires nitrite;
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.datatype.jsr310;
  requires com.fasterxml.jackson.datatype.jdk8;
  requires org.apache.commons.lang3;
  requires com.google.common;

  exports dev.tsvinc.music.sort;
}
