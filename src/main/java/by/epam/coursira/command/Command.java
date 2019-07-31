package by.epam.coursira.command;

import by.epam.coursira.entity.Principal;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.PageNotFoundException;
import javax.servlet.http.HttpServletRequest;

public interface Command {

  CommandResult execute(Principal principal, HttpServletRequest request)
      throws CommandException, ClientCommandException, PageNotFoundException;
}
