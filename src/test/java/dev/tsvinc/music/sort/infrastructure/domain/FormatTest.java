package dev.tsvinc.music.sort.infrastructure.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FormatTest {

  @Test
  void getValue() {
    String flac = Format.FLAC.getValue();
    Assertions.assertEquals("flac", flac);
  }

  @Test
  void values() {
    assertEquals(2, Format.values().length);
  }

  @Test
  void valueOf() {
    assertEquals(Format.FLAC, Format.valueOf("FLAC"));
  }
}
