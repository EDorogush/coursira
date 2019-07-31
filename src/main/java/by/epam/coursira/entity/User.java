package by.epam.coursira.entity;

import java.time.Instant;
import java.util.Objects;
import java.util.StringJoiner;

public class User extends AbstractEntity {
  private int id;
  private String email;
  private String password;
  private String firstName;
  private String lastName;
  private Role role;
  private String organization;
  private Integer age;
  private String interests;
  private String base64Image;
  private String registrationCode;
  private Instant registrationExpDate;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
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

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public String getOrganization() {
    return organization;
  }

  public void setOrganization(String organization) {
    this.organization = organization;
  }

  public Integer getAge() {
    return age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  public String getInterests() {
    return interests;
  }

  public void setInterests(String interests) {
    this.interests = interests;
  }

  public String getBase64Image() {
    return base64Image;
  }

  public void setBase64Image(String base64Image) {
    this.base64Image = base64Image;
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

  public static class Builder {
    private User user;

    public Builder() {
      user = new User();
    }

    public User build() {
      return user;
    }

    public Builder setId(int id) {
      user.id = id;
      return this;
    }

    public Builder setEmail(String email) {
      user.email = email;
      return this;
    }

    public Builder setPassword(String password) {
      user.password = password;
      return this;
    }

    public Builder setFirstName(String firstName) {
      user.firstName = firstName;
      return this;
    }

    public Builder setLastName(String lastName) {
      user.lastName = lastName;
      return this;
    }

    public Builder setRole(Role role) {
      user.role = role;
      return this;
    }

    public Builder setOrganization(String organization) {
      user.organization = organization;
      return this;
    }

    public Builder setAge(Integer age) {
      user.age = age;
      return this;
    }

    public Builder setInterests(String interests) {
      user.interests = interests;
      return this;
    }

    public Builder setBase64Image(String base64Image) {
      user.base64Image = base64Image;
      return this;
    }

    public Builder setRegistrationCode(String registrationCode) {
      user.registrationCode = registrationCode;
      return this;
    }

    public Builder setRegistrationExpDate(Instant registrationExpDate) {
      user.registrationExpDate = registrationExpDate;
      return this;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User)) return false;
    User user = (User) o;
    return id == user.id
        && age == user.age
        && Objects.equals(email, user.email)
        && Objects.equals(password, user.password)
        && Objects.equals(firstName, user.firstName)
        && Objects.equals(lastName, user.lastName)
        && role == user.role
        && Objects.equals(organization, user.organization)
        && Objects.equals(interests, user.interests)
        && Objects.equals(base64Image, user.base64Image)
        && Objects.equals(registrationCode, user.registrationCode)
        && Objects.equals(registrationExpDate, user.registrationExpDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id,
        email,
        password,
        firstName,
        lastName,
        role,
        organization,
        age,
        interests,
        base64Image,
        registrationCode,
        registrationExpDate);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", User.class.getSimpleName() + "[", "]")
        .add("id=" + id)
        .add("email='" + email + "'")
        .add("password='" + password + "'")
        .add("firstName='" + firstName + "'")
        .add("lastName='" + lastName + "'")
        .add("role=" + role)
        .add("organization='" + organization + "'")
        .add("age=" + age)
        .add("interests='" + interests + "'")
        //      .add("base64Image='" + base64Image+ "'")
        .add("registrationCode='" + registrationCode + "'")
        .add("registrationExpDate=" + registrationExpDate)
        .toString();
  }
}
