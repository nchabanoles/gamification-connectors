package org.exoplatform.gamification.connectors.common;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.services.security.ConversationState;

public class Utils {

  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM uuuu");

  private Utils() {
  }

  public static String formatTime(Object timeInSeconds, String lang) {
    Locale userLocale = StringUtils.isBlank(lang) ? Locale.getDefault() : new Locale(lang);
    LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(String.valueOf(timeInSeconds))),
                                                     TimeZone.getDefault().toZoneId());
    return dateTime.format(DATE_FORMATTER.withLocale(userLocale));
  }

  public static LocalDateTime timeFromSeconds(long createdDate) {
    return LocalDateTime.ofInstant(Instant.ofEpochSecond(createdDate), TimeZone.getDefault().toZoneId());
  }

  public static long timeToSecondsAtDayStart(LocalDateTime time) {
    return time.toLocalDate().atStartOfDay().atZone(ZoneOffset.systemDefault()).toEpochSecond();
  }

  public static long getCurrentTimeInSeconds() {
    return System.currentTimeMillis() / 1000 + 10;
  }

  public static final String getCurrentUserId() {
    if (ConversationState.getCurrent() != null && ConversationState.getCurrent().getIdentity() != null) {
      return ConversationState.getCurrent().getIdentity().getUserId();
    }
    return null;
  }
}
