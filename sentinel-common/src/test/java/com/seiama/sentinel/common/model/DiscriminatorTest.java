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
    "0,true,false",
    "0000,false,true",
    "0001,false,true",
    "1000,false,true"
  })
  void testWrite(final String value, final boolean migrated, final boolean unmigrated) {
    final Discriminator discriminator = new Discriminator(value);
    final AtomicBoolean onMigrated = new AtomicBoolean();
    final AtomicBoolean onUnmigrated = new AtomicBoolean();
    assertDoesNotThrow(() -> Discriminator.write(
      discriminator,
      () -> onMigrated.setPlain(true),
      va -> onUnmigrated.setPlain(true)
    ));
    assertEquals(migrated, onMigrated.get());
    assertEquals(unmigrated, onUnmigrated.get());
  }
}
