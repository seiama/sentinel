package com.seiama.sentinel.model;

import discord4j.common.util.Snowflake;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "temporary_message_links")
public class TemporaryMessageLink {
  @Id
  private Long id;
  @Column
  private String guild;
  @Column
  private Long sourceChannelId;
  @Column
  private Long sourceMessageId;
  @Column
  private Long targetChannelId;
  @Column
  private Long targetMessageId;

  public TemporaryMessageLink() {
  }

  public TemporaryMessageLink(
    final ObjectId guild,
    final Snowflake sourceChannelId,
    final Snowflake sourceMessageId,
    final Snowflake targetChannelId,
    final Snowflake targetMessageId
  ) {
    this.guild = guild.toHexString();
    this.sourceChannelId = sourceChannelId.asLong();
    this.sourceMessageId = sourceMessageId.asLong();
    this.targetChannelId = targetChannelId.asLong();
    this.targetMessageId = targetMessageId.asLong();
  }

  public ObjectId guild() {
    return new ObjectId(this.guild);
  }

  public Snowflake sourceChannelId() {
    return Snowflake.of(this.sourceChannelId);
  }

  public Snowflake sourceMessageId() {
    return Snowflake.of(this.sourceMessageId);
  }

  public Snowflake targetChannelId() {
    return Snowflake.of(this.targetChannelId);
  }

  public Snowflake targetMessageId() {
    return Snowflake.of(this.targetMessageId);
  }
}
