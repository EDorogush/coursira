package by.epam.coursira.command;

import by.epam.coursira.exception.ClientCommandException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class CommandUtilsTest {

  @Test
  public void testParseOptionalStringWhenQueryParamsIsNullThenEmpty() {
    // when
    HashMap<String, String[]> queryParams = null;
    Optional<String> expected = Optional.empty();
    Optional<String> actual = CommandUtils.parseOptionalString(queryParams, "hello");
    assertEquals(expected, actual);
  }

  @Test
  public void testParseOptionalStringWhenQueryParamsIsEmptyThenEmpty() {
    // when
    HashMap<String, String[]> queryParams = new HashMap<>();
    Optional<String> expected = Optional.empty();
    Optional<String> actual = CommandUtils.parseOptionalString(queryParams, "hello");
    assertEquals(expected, actual);
  }

  @Test
  public void testParseOptionalStringWhenValueIsNullThenEmpty() {
    // given
    HashMap<String, String[]> queryParams = new HashMap<>();
    queryParams.put("hello", null);
    // when
    Optional<String> expected = Optional.empty();
    Optional<String> actual = CommandUtils.parseOptionalString(queryParams, "hello");
    // then
    assertEquals(expected, actual);
  }

  @Test
  public void testParseOptionalStringWhenValueZerosArrayThenEmpty() {
    // when
    HashMap<String, String[]> queryParams = new HashMap<>();
    queryParams.put("hello", new String[0]);
    Optional<String> expected = Optional.empty();
    Optional<String> actual = CommandUtils.parseOptionalString(queryParams, "hello");
    assertEquals(expected, actual);
  }

  @Test
  public void testParseOptionalStringWhenValueEmptyArrayThenEmpty() {
    // when
    HashMap<String, String[]> queryParams = new HashMap<>();
    queryParams.put("hello", new String[10]);
    Optional<String> expected = Optional.empty();
    Optional<String> actual = CommandUtils.parseOptionalString(queryParams, "hello");
    assertEquals(expected, actual);
  }

  @Test
  public void testParseOptionalIntWhenValueIsNotIntThenThrowException() {
    // when
    HashMap<String, String[]> queryParams = new HashMap<>();
    queryParams.put("hello", new String[]{"Winter", "Spring", "Summer", "Autumn"});
    assertThrows(ClientCommandException.class, () -> CommandUtils.parseOptionalInt(queryParams, "hello"));
  }

  @Test
  public void testParseOptionalIntWhenSucceed() throws ClientCommandException {
    // when
    HashMap<String, String[]> queryParams = new HashMap<>();
    queryParams.put("hello", new String[]{"1"});
    Optional<Integer> expected = Optional.of(1);
    Optional<Integer> actual = CommandUtils.parseOptionalInt(queryParams, "hello");
    assertEquals(expected, actual);
  }

  @Test
  public void testParseBooleanSucceedWithTrue() {
    HashMap<String, String[]> queryParams = new HashMap<>();
    queryParams.put("hello", new String[]{"true"});
    Optional<Boolean> expected = Optional.of(true);
    Optional<Boolean> actual = CommandUtils.parseOptionalBoolean(queryParams, "hello");
    assertEquals(expected, actual);
  }

  @Test
  public void testParseBooleanSucceedWithFalse() {
    HashMap<String, String[]> queryParams = new HashMap<>();
    queryParams.put("hello", new String[]{"any"});
    Optional<Boolean> expected = Optional.of(false);
    Optional<Boolean> actual = CommandUtils.parseOptionalBoolean(queryParams, "hello");
    assertEquals(expected, actual);
  }

  @Test
  public void testParseOptionalLocalDateWhenValueIsNotLocalDateThenThrowException() {
    HashMap<String, String[]> queryParams = new HashMap<>();
    queryParams.put("hello", new String[]{"abc"});
    assertThrows(
      ClientCommandException.class, () -> CommandUtils.parseOptionalLocalDate(queryParams, "hello"));
  }

  @Test
  public void testParseOptionalLocalDateSucceed() throws ClientCommandException {
    // given
    HashMap<String, String[]> queryParams = new HashMap<>();
    LocalDate date = Instant.now().atOffset(ZoneOffset.UTC).toLocalDate();
    queryParams.put("hello", new String[]{date.toString()});
    // when
    Optional<LocalDate> expected = Optional.of(date);
    Optional<LocalDate> actual = CommandUtils.parseOptionalLocalDate(queryParams, "hello");
    assertEquals(expected, actual);
  }
}
