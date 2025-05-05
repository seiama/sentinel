package com.seiama.sentinel.feature.punishment.display;

import org.jspecify.annotations.NullMarked;

@NullMarked
public enum PunishmentDisplayStyle {
  FULL(
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true,
    true
  ),
  CREATED(
    true,
    false,
    false,
    false,
    false,
    false,
    true,
    true,
    true,
    false
  ),
  LOG(
    true,
    false,
    false,
    false,
    true,
    true,
    true,
    true,
    true,
    true
  ),
  APPEAL(
    true,
    true,
    true,
    true,
    true,
    // Providing this information can sometimes lead to harassment against the staff member
    // who issued this punishment against the punished user. Instead, we just don't include it
    // in the details provided during the appeal process.
    false,
    true,
    // There are circumstances when a staff member may not (initially, or if the
    // staff member forgets to edit the reason, ever) provide a suitable reason when
    // punishing a user. This may result in an invalid reason (such as "fuck off") instead
    // of the actual reason (such as "inappropriate avatar"), and we don't want to display
    // that to the punished user.
    //
    // Additionally, not displaying the punishment reason during the appeal process provides
    // the punished user a chance to explain why they think the punishment was issued against
    // them, which can be revealing in terms of remorse/apology and from the perspective of the
    // punished user.
    //
    // A downside to not providing the punishment reason is that the punished user may not
    // remember why they received the punishment. For cases like this, a staff member may
    // choose to provide the punished user with the reason specified on the punishment.
    //
    // Note that the punished user may have the punishment reason (either original or current)
    // available to them if it was provided in the past.
    false,
    true,
    false
  );

  public final boolean type;
  public final boolean stale;
  public final boolean expunged;
  public final boolean permanent;
  public final boolean time;
  public final boolean issuedBy;
  public final boolean issuedTo;
  public final boolean reason;
  public final boolean duration;
  public final boolean notified;

  PunishmentDisplayStyle(
    final boolean type,
    final boolean stale,
    final boolean expunged,
    final boolean permanent,
    final boolean time,
    final boolean issuedBy,
    final boolean issuedTo,
    final boolean reason,
    final boolean duration,
    final boolean notified
  ) {
    this.type = type;
    this.stale = stale;
    this.expunged = expunged;
    this.permanent = permanent;
    this.time = time;
    this.issuedBy = issuedBy;
    this.issuedTo = issuedTo;
    this.reason = reason;
    this.duration = duration;
    this.notified = notified;
  }
}
