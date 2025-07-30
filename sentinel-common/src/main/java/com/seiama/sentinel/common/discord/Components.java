package com.seiama.sentinel.common.discord;

import discord4j.core.object.component.Container;
import discord4j.core.object.component.IAccessoryComponent;
import discord4j.core.object.component.ICanBeUsedInContainerComponent;
import discord4j.core.object.component.ICanBeUsedInSectionComponent;
import discord4j.core.object.component.MessageComponent;
import discord4j.core.object.component.Section;
import discord4j.rest.util.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Consumer;
import org.jspecify.annotations.NullMarked;
import reactor.util.annotation.Nullable;

@NullMarked
public final class Components {
  private Components() {
  }

  public static <C extends MessageComponent & ICanBeUsedInContainerComponent> Container container(final OptionalInt id, final Consumer<Builder<C>> consumer, final @Nullable Color color, final boolean spoiler) {
    final List<C> components = createList(consumer);
    return id.isPresent()
      ? Container.of(id.getAsInt(), color, spoiler, components)
      : Container.of(color, spoiler, components);
  }

  public static <C extends MessageComponent & ICanBeUsedInSectionComponent> Section section(final OptionalInt id, final Consumer<Builder<C>> consumer, final IAccessoryComponent accessory) {
    final List<C> components = createList(consumer);
    return id.isPresent()
      ? Section.of(id.getAsInt(), accessory, components)
      : Section.of(accessory, components);
  }

  private static <T> List<T> createList(final Consumer<Builder<T>> consumer) {
    final List<T> list = new ArrayList<>();
    consumer.accept(list::add);
    return list;
  }

  @NullMarked
  public interface Builder<T> {
    void add(final T value);
  }
}
