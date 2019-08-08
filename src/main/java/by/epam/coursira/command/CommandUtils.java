package by.epam.coursira.command;

import by.epam.coursira.entity.Language;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.CommandException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** This Class is used by Commands to parse data from request parameters. */
public final class CommandUtils {
  private static final Logger logger = LogManager.getLogger();
  private static final String VALIDATION_MESSAGE_PATTERN = "Wrong parameters %s value.";

  private CommandUtils() {}

  /**
   * Method parse from {@link Map}<{@link String}, {@link String}[]> queryParams {@link
   * Optional}<{@link String}> value with {@code paramName} key.
   *
   * @param queryParams {@link Map}<{@link String}, {@link String}[]> of parameters
   * @param paramName the key of desired parameter in {@link Map}
   * @return {@link Optional}<{@link String}> value from the queryParams {@link Map}
   */
  // package-private
  static Optional<String> parseOptionalString(Map<String, String[]> queryParams, String paramName) {
    final Optional<String> parsedValue;
    parsedValue =
        Optional.ofNullable(queryParams)
            .map(params -> params.get(paramName))
            .filter(r -> r.length > 0)
            .map(r -> r[0]);
    logger.debug("String {} value parsed {}", paramName, parsedValue.orElse("not defined"));
    return parsedValue;
  }

  /**
   * Method parse from {@link Map}<{@link String}, {@link String}[]> queryParams {@link
   * Optional}<{@link Boolean}> value with {@code paramName} key.
   *
   * @param queryParams {@link Map}<{@link String}, {@link String}[]> of parameters
   * @param paramName the key of desired parameter in {@link Map}
   * @return {@link Optional}<{@link Boolean}> value from the queryParams {@link Map}
   */
  // package-private
  static Optional<Boolean> parseOptionalBoolean(
      Map<String, String[]> queryParams, String paramName) {
    return parseOptionalString(queryParams, paramName).map(Boolean::parseBoolean);
  }

  /**
   * Method parse from {@link Map}<{@link String}, {@link String}[]> queryParams {@link
   * Optional}<{@link Integer}> value with {@code paramName} key. Throws {@code
   * ClientCommandException} when value is exists but is not able to be parsed from {@link String}
   * to {@link Integer}
   *
   * @param queryParams {@link Map}<{@link String}, {@link String}[]> of parameters
   * @param paramName the key of desired parameter in {@link Map}
   * @return {@link Optional}<{@link Integer}> value from the queryParams {@link Map}
   * @throws ClientCommandException when paramName value is not able to be parsed from {@link
   *     String} to {@link Integer}
   */
  // package-private
  static Optional<Integer> parseOptionalInt(Map<String, String[]> queryParams, String paramName)
      throws ClientCommandException {
    final Optional<Integer> parsedValue;
    try {
      parsedValue = parseOptionalString(queryParams, paramName).map(Integer::valueOf);
    } catch (NumberFormatException e) {
      logger.info("can't parse value to int");
      throw new ClientCommandException(String.format(VALIDATION_MESSAGE_PATTERN, paramName));
    }
    return parsedValue;
  }

  /**
   * Method parse from {@link Map}<{@link String}, {@link String}[]> queryParams {@link
   * Optional}<{@link LocalDate}> value with {@code paramName} key. Throws {@code
   * ClientCommandException} when value is exists but is not able to be parsed from {@link String}
   * to {@link LocalDate}
   *
   * @param queryParams {@link Map}<{@link String}, {@link String}[]> of parameters
   * @param paramName the key of desired parameter in {@link Map}
   * @return {@link Optional}<{@link LocalDate}> value from the queryParams {@link Map}
   * @throws ClientCommandException when paramName value is not able to be parsed from {@link
   *     String} to {@link LocalDate}
   */
  static Optional<LocalDate> parseOptionalLocalDate(
      Map<String, String[]> queryParams, String paramName) throws ClientCommandException {
    final Optional<LocalDate> parsedValue;
    try {
      parsedValue =
          parseOptionalString(queryParams, paramName)
              .map(text -> LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE));
    } catch (DateTimeParseException e) {
      logger.info("can't parse value to Instant");
      throw new ClientCommandException(String.format(VALIDATION_MESSAGE_PATTERN, paramName));
    }
    return parsedValue;
  }

  /**
   * Method parse from {@link Map}<{@link String}, {@link String}[]> queryParams {@link
   * Optional}<{@link LocalTime}> value with {@code paramName} key. Throws {@code
   * ClientCommandException} when value is exists but is not able to be parsed from {@link String}
   * to {@link LocalTime}
   *
   * @param queryParams {@link Map}<{@link String}, {@link String}[]> of parameters
   * @param paramName the key of desired parameter in {@link Map}
   * @return {@link Optional}<{@link LocalTime}> value from the queryParams {@link Map}
   * @throws ClientCommandException when paramName value is not able to be parsed from {@link
   *     String} to {@link LocalTime}
   */
  static Optional<LocalTime> parseOptionalLocalTime(
      Map<String, String[]> queryParams, String paramName) throws ClientCommandException {
    final Optional<LocalTime> parsedValue;
    try {
      parsedValue =
          parseOptionalString(queryParams, paramName)
              .map(text -> LocalTime.parse(text, DateTimeFormatter.ISO_LOCAL_TIME));
    } catch (DateTimeParseException e) {
      logger.info("can't parse value to Instant");
      throw new ClientCommandException(String.format(VALIDATION_MESSAGE_PATTERN, paramName));
    }
    return parsedValue;
  }

  /**
   * Method parse from {@link Map}<{@link String}, {@link String}[]> queryParams {@link
   * Optional}<{@link Language}> value with {@code paramName} key. Throws {@code
   * ClientCommandException} when value is exists but is not able to be parsed from {@link String}
   * to {@link Language}
   *
   * @param queryParams {@link Map}<{@link String}, {@link String}[]> of parameters
   * @param paramName the key of desired parameter in {@link Map}
   * @return {@link Optional}<{@link Language}> value from the queryParams {@link Map}
   * @throws ClientCommandException when paramName value is not able to be parsed from {@link
   *     String} to {@link Language}
   */
  // package-private
  static Optional<Language> parseOptionalLanguage(
      Map<String, String[]> queryParams, String paramName) throws ClientCommandException {
    final Optional<Language> parsedValue;
    try {
      parsedValue = parseOptionalString(queryParams, paramName).map(Language::valueOf);
    } catch (IllegalArgumentException e) {
      throw new ClientCommandException("wrong <language> parameter value.");
    }
    return parsedValue;
  }

  /**
   * this method is used to prepare List for jsp model when list is displayed in the page with
   * paging. It removes last element from the list if List's size exceeds size value (specified in
   * argument) by 1. It is assumed that removed record will appear in next page.
   *
   * @param schedule {@link List} to be trimmed
   * @param size amount of records on one jsp page
   * @return {@code true} if record was removed and {@code false} otherwise.
   * @throws CommandException when size of current list exceeds limit more than 1
   */
  static boolean trimToLimit(List schedule, int size)
      throws CommandException {
    if (schedule.size() > size + 1) {
      throw new CommandException(
          "Can't trim to size for paging. Size of current list exceeds limit more than 1");
    }
    if (schedule.size() == size + 1) {
      // remove last element - it will appear in next page
      schedule.remove(size);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Method parse {@code int} Id value from {@link HttpServletRequest} request servlet path
   * according to {@link Pattern} pattern, specified in arguments.
   *
   * @param pattern {@link Pattern} for matching desired value from request
   * @param request client's {@link HttpServletRequest}
   * @return id value
   * @throws ClientCommandException when id value is not able to be parsed from {@link String} to
   *     int
   * @throws CommandException when no matchers found in request servlet path.
   */
  static int parseIdFromRequest(Pattern pattern, HttpServletRequest request)
      throws ClientCommandException, CommandException {
    final int id;
    Matcher matcher = pattern.matcher(request.getServletPath());
    if (!matcher.matches()) {
      throw new CommandException("No matcher found");
    }
    try {
      id = Integer.parseInt(matcher.group(1));
    } catch (NumberFormatException e) {
      throw new ClientCommandException(
          String.format("Can't parse id from request %s", request.getServletPath()));
    }
    return id;
  }

  static String getReferer(HttpServletRequest request) {
    String referer =
        request.getHeader("referer")
            .split(request.getContextPath())[1]; // ServletPath part of referer link
    logger.debug("referer is {}", referer);
    return referer;
  }
}
