package by.epam.coursira.command;

import by.epam.coursira.exception.PageNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This class is intended for choosing Command to process the request from the client. All Commands
 * are kept in {@link List}<{@link Command}> commandList injected to class's constructor. Choosing
 * appropriate Command is performed by matching {@link String} servletPath (Request URI without
 * context path) specified in getCommand's method argument with Commands Patterns.
 */
public class CommandFactory {
  private final List<Command> commandList;

  public CommandFactory(List<Command> commandList) {
    this.commandList = commandList;
  }

  /**
   * This method matches path, specified in argument with patterns from {@link CommandAbstract} in
   * {@code commandList}. When first matching found procedure stops and return {@link Command}
   * object with such {@link Pattern}. Throw PageNotFoundException when no matcher found.
   *
   * @param path {@link String} object which is going to be matched with patterns.
   * @return {@link Command}
   * @throws PageNotFoundException when no matcher found.
   */
  public Command getCommand(String path) throws PageNotFoundException {
    Optional<Command> command =
        commandList.stream().filter(c -> c.urlPattern().matcher(path).matches()).findFirst();
    return command.orElseThrow(PageNotFoundException::new);
  }
}
