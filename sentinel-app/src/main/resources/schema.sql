create table if not exists temporary_message_links
(
  guild             char(24) not null,
  source_channel_id bigint   not null,
  source_message_id bigint   not null,
  target_channel_id bigint   not null,
  target_message_id bigint   not null,
  primary key (source_channel_id, source_message_id)
);
