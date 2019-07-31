package by.epam.coursira.command;

import java.util.regex.Pattern;

public abstract class CommandAbstract implements Command {
  private final Pattern pattern;

  protected CommandAbstract(String pattern) {
    this.pattern = Pattern.compile(pattern);
  }

  public Pattern getPattern() {
    return pattern;
  }
}
