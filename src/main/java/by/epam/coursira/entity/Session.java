package by.epam.coursira.entity;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.StringJoiner;

public class Session {
  private String id;
  private int userId;
  private Instant expDate;
  private Language language;
  private ZoneOffset zoneOffset;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public Instant getExpDate() {
    return expDate;
  }

  public void setExpDate(Instant expDate) {
    this.expDate = expDate;
  }

  public Language getLanguage() {
    return language;
  }

  public void setLanguage(Language language) {
    this.language = language;
  }

  public ZoneOffset getZoneOffset() {
    return zoneOffset;
  }

  public void setZoneOffset(ZoneOffset zoneOffset) {
    this.zoneOffset = zoneOffset;
  }

  public static class Builder {
    private Session session;

    public Builder() {
      session = new Session();
    }

    public Session build() {
      return session;
    }

    public Builder setId(String id) {
      session.id = id;
      return this;
    }

    public Builder setUserId(int userId) {
      session.userId = userId;
      return this;
    }

    public Builder setExpDate(Instant expDate) {
      session.expDate = expDate;
      return this;
    }

    public Builder setLanguage(Language language) {
      session.language = language;
      return this;
    }

    public Builder setZoneOffSet(ZoneOffset zoneOffSet) {
      session.zoneOffset = zoneOffSet;
      return this;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Session session = (Session) o;
    return userId == session.userId &&
      Objects.equals(id, session.id) &&
      Objects.equals(expDate, session.expDate) &&
      language == session.language &&
      Objects.equals(zoneOffset, session.zoneOffset);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, userId, expDate, language, zoneOffset);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Session.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("userId=" + userId)
        .add("expDate=" + expDate)
        .add("language=" + language)
        .add("zoneOffset=" + zoneOffset)
        .toString();
  }
}
