package com.seiama.sentinel.common.model;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("deprecation")
class DiscriminatorTest {
  @ParameterizedTest
  @CsvSource({
    "0,true",
    "0000,false",
    "0001,false",
    "1000,false"
  })
  void testMigrated(final String value, final boolean expected) {
    final Discriminator discriminator = new Discriminator(value);
    assertEquals(expected, discriminator.migrated());
  }

  @ParameterizedTest
  @CsvSource({
    "0,false,true",
    "0000,true,false",
    "0001,true,false",
    "1000,true,false"
  })
  void testWrite(final String value, final boolean present, final boolean absent) {
    final Discriminator discriminator = new Discriminator(value);
    final AtomicBoolean onPresent = new AtomicBoolean();
    final AtomicBoolean onAbsent = new AtomicBoolean();
    assertDoesNotThrow(() -> Discriminator.write(
      discriminator,
      va -> onPresent.setPlain(true),
      () -> onAbsent.setPlain(true)
    ));
    assertEquals(present, onPresent.get());
    assertEquals(absent, onAbsent.get());
  }
}
