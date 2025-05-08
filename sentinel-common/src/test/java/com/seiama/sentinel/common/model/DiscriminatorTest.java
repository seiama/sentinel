package com.seiama.sentinel.common.model;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SuppressWarnings("deprecation")
class DiscriminatorTest {
  @ParameterizedTest
  @CsvSource(value = {
    "null,true",
    "0,true",
    "0000,false",
    "0001,false",
    "1000,false"
  }, nullValues = "null")
  void testMigrated(final String value, final boolean expected) {
    final Discriminator discriminator = Discriminator.of(value);
    assertEquals(expected, discriminator.migrated());
  }

  @Test
  void testUnbox() {
    assertNull(Discriminator.unbox(Discriminator.of((String) null)));
    assertNull(Discriminator.unbox(Discriminator.of(Discriminator.TEMPORARY_MIGRATION_MARKER)));
    assertEquals("0001", Discriminator.unbox(Discriminator.of("0001")));
  }

  @ParameterizedTest
  @CsvSource({
    "0,true,false",
    "0000,false,true",
    "0001,false,true",
    "1000,false,true"
  })
  void testWrite(final String value, final boolean migrated, final boolean unmigrated) {
    final Discriminator discriminator = Discriminator.of(value);
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
