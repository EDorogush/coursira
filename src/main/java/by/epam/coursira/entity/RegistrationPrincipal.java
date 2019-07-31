package by.epam.coursira.entity;

import java.time.Instant;
import java.time.ZoneId;
import java.util.StringJoiner;

public class RegistrationPrincipal extends AbstractEntity {
  private int userId;
  private String email;
  private String password;
  private String firstName;
  private String lastName;
  private Role role;
  private String registrationCode;
  private Instant registrationExpDate;
  private ZoneId zoneId;

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getRegistrationCode() {
    return registrationCode;
  }

  public void setRegistrationCode(String registrationCode) {
    this.registrationCode = registrationCode;
  }

  public Instant getRegistrationExpDate() {
    return registrationExpDate;
  }

  public void setRegistrationExpDate(Instant registrationExpDate) {
    this.registrationExpDate = registrationExpDate;
  }

  public ZoneId getZoneId() {
    return zoneId;
  }

  public void setZoneId(ZoneId zoneId) {
    this.zoneId = zoneId;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", RegistrationPrincipal.class.getSimpleName() + "[", "]")
        .add("userId=" + userId)
        .add("email='" + email + "'")
        .add("password='" + password + "'")
        .add("firstName='" + firstName + "'")
        .add("lastName='" + lastName + "'")
        .add("role=" + role)
        .add("registrationCode=" + registrationCode)
        .add("registrationExpDate=" + registrationExpDate)
        .toString();
  }

  public static class Builder {
    private RegistrationPrincipal principal;

    public Builder() {
      principal = new RegistrationPrincipal();
    }

    public RegistrationPrincipal build() {
      return principal;
    }

    public Builder withUserId(int userId) {
      principal.userId = userId;
      return this;
    }

    public Builder withRole(Role role) {
      principal.role = role;
      return this;
    }

    public Builder withEmail(String email) {
      principal.email = email;
      return this;
    }

    public Builder withPassword(String password) {
      principal.password = password;
      return this;
    }

    public Builder withFirstName(String firstName) {
      principal.firstName = firstName;
      return this;
    }

    public Builder withLastName(String lastName) {
      principal.lastName = lastName;
      return this;
    }

    public Builder withRegistrationCode(String registrationCode) {
      principal.registrationCode = registrationCode;
      return this;
    }

    public Builder withRegistrationExpDate(Instant registrationExpDate) {
      principal.registrationExpDate = registrationExpDate;
      return this;
    }

    public Builder withZoneId(ZoneId zoneId) {
      principal.zoneId = zoneId;
      return this;
    }
  }
}
