package com.seiama.sentinel.common.discord;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import org.jspecify.annotations.NullMarked;
import reactor.core.publisher.Mono;

@NullMarked
public final class Pagination {
  private final String previousButtonId = UUID.randomUUID().toString();
  private final String nextButtonId = UUID.randomUUID().toString();

  private final int pages;
  private final OnClick onClick;

  private int page;

  public Pagination(
    final int pages,
    final OnClick onClick
  ) {
    this.pages = pages;
    this.onClick = onClick;
  }

  public Mono<Void> createListener(final GatewayDiscordClient client) {
    return client
      .on(ButtonInteractionEvent.class, event -> {
        final String id = event.getCustomId();
        if (id.equals(this.previousButtonId)) {
          this.page--;
          return event.deferEdit().then(this.onClick.on(event, this.page, this::createButtons));
        } else if (id.equals(this.nextButtonId)) {
          this.page++;
          return event.deferEdit().then(this.onClick.on(event, this.page, this::createButtons));
        } else {
          return Mono.empty();
        }
      })
      .timeout(Duration.ofMinutes(15))
      .onErrorResume(TimeoutException.class, ignored -> Mono.empty())
      .then();
  }

  public ActionRow createButtons() {
    final boolean hasPreviousButton = this.pages > 1 && this.page > 0;
    final boolean hasNextButton = this.pages > 1 && this.page < this.pages - 1;
    return ActionRow.of(
      Button.primary(this.previousButtonId, Emojis.ARROW_LEFT, "Previous Page").disabled(!hasPreviousButton),
      Button.primary(this.nextButtonId, Emojis.ARROW_RIGHT, "Next Page").disabled(!hasNextButton)
    );
  }

  @NullMarked
  public interface OnClick {
    Mono<Void> on(final ButtonInteractionEvent event, final int page, final Supplier<ActionRow> buttons);
  }
}
