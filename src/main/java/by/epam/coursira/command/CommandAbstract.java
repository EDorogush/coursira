package by.epam.coursira.command;

import java.util.regex.Pattern;

/**
 * Parent class for particular Commands classes, are responsible for client's request processing.
 * Private field {@link Pattern} is used in {@link CommandFactory} class to distinguish request's
 * URL pattern and to delegate request to appropriate Command.
 */
public abstract class CommandAbstract implements Command {
  private final Pattern pattern;

  protected CommandAbstract(String pattern) {
    this.pattern = Pattern.compile(pattern);
  }

  public Pattern getPattern() {
    return pattern;
  }
}
