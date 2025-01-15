package com.seiama.sentinel.model;

import com.seiama.sentinel.common.converter.SnowflakeFromStringConverter;
import discord4j.common.util.Snowflake;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.bson.types.ObjectId;
import org.springframework.data.relational.core.mapping.Table;

@Entity
@Table(name = "temporary_message_links")
public class TemporaryMessageLink {
  @Id
  private Long id;
  @Column
  private ObjectId guild;
  @Column
  @Convert(converter = SnowflakeFromStringConverter.class)
  private Snowflake sourceChannelId;
  @Column
  @Convert(converter = SnowflakeFromStringConverter.class)
  private Snowflake sourceMessageId;
  @Column
  @Convert(converter = SnowflakeFromStringConverter.class)
  private Snowflake targetChannelId;
  @Column
  @Convert(converter = SnowflakeFromStringConverter.class)
  private Snowflake targetMessageId;

  public TemporaryMessageLink() {
  }

  public TemporaryMessageLink(
    final ObjectId guild,
    final Snowflake sourceChannelId,
    final Snowflake sourceMessageId,
    final Snowflake targetChannelId,
    final Snowflake targetMessageId
  ) {
    this.guild = guild;
    this.sourceChannelId = sourceChannelId;
    this.sourceMessageId = sourceMessageId;
    this.targetChannelId = targetChannelId;
    this.targetMessageId = targetMessageId;
  }

  public ObjectId guild() {
    return this.guild;
  }

  public Snowflake sourceChannelId() {
    return this.sourceChannelId;
  }

  public Snowflake sourceMessageId() {
    return this.sourceMessageId;
  }

  public Snowflake targetChannelId() {
    return this.targetChannelId;
  }

  public Snowflake targetMessageId() {
    return this.targetMessageId;
  }
}
