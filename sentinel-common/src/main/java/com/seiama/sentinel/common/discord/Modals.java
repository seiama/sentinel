package com.seiama.sentinel.common.discord;

import com.seiama.sentinel.reactive.Reactive;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.TextInput;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public final class Modals {
  private Modals() {
  }

  public static <T> Mono<Void> presentAndCaptureSingleTextInput(
    final DeferrableInteractionEvent event,
    final String modalTitle,
    final String inputTitle,
    final boolean inputRequired,
    final BiFunction<ModalSubmitInteractionEvent, Optional<String>, Mono<T>> onText,
    final @Nullable Function<? super TimeoutException, ? extends Publisher<? extends T>> onExpiry
  ) {
    final String modalId = createRandomId();
    final String inputId = createRandomId();
    return event
      .presentModal(modalTitle, modalId, List.of(ActionRow.of(
        TextInput.paragraph(inputId, inputTitle).required(inputRequired)
      )))
      .then(
        event.getClient()
          .on(ModalSubmitInteractionEvent.class, modal -> {
            if (modalId.equals(modal.getCustomId())) {
              for (final TextInput component : modal.getComponents(TextInput.class)) {
                if (inputId.equals(component.getCustomId())) {
                  return onText.apply(modal, component.getValue());
                }
              }
            }
            return Mono.empty();
          })
          .timeout(Duration.ofMinutes(2))
          .onErrorResume(TimeoutException.class, onExpiry != null ? onExpiry : Reactive.<T>ignoringException())
          .then()
      );
  }

  private static String createRandomId() {
    return UUID.randomUUID().toString();
  }
}
