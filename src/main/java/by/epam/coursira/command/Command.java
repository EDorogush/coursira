package by.epam.coursira.command;

import by.epam.coursira.entity.Principal;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.PageNotFoundException;
import javax.servlet.http.HttpServletRequest;

/**
 * This is a Functional Interface which provide execution function for client's request. Should be
 * used in Commands are responsible for processing {@link HttpServletRequest} requests.
 */
@FunctionalInterface
public interface Command {
  /**
   * Process given {@link HttpServletRequest} client request, executes it and returns a result of
   * processing as {@link CommandResult} object. Throws {@link CommandException}, {@link
   * ClientCommandException}, {@link PageNotFoundException}.
   *
   * @param principal determined {@link Principal} of client.
   * @param request {@link HttpServletRequest} request of client.
   * @return {@link CommandResult} object
   * @throws ClientCommandException when client's request is not correct (Unsupported HTTP methods,
   *     incorrect request parameters)
   * @throws PageNotFoundException when client uses supported but not implemented HTTP method.
   * @throws CommandException when server trouble occurs
   */
  CommandResult execute(Principal principal, HttpServletRequest request)
      throws ClientCommandException, PageNotFoundException, CommandException;
}
