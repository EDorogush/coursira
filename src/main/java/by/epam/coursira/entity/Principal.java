package by.epam.coursira.entity;

import java.util.Objects;
import java.util.StringJoiner;

public class Principal extends AbstractEntity {
  private Session session;
  private User user;

  public Principal(Session session, User user) {
    this.session = session;
    this.user = user;
  }

  public Principal() {}

  public Session getSession() {
    return session;
  }

  public void setSession(Session session) {
    this.session = session;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Principal)) return false;
    Principal principal = (Principal) o;
    return Objects.equals(session, principal.session) && Objects.equals(user, principal.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(session, user);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Principal.class.getSimpleName() + "[", "]")
        .add("session=" + session)
        .add("user=" + user)
        .toString();
  }
}
