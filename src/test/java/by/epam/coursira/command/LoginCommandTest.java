package by.epam.coursira.command;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

import by.epam.coursira.entity.Principal;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.ClientServiceException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.model.LoginModel;
import by.epam.coursira.service.PrincipalService;
import by.epam.coursira.servlet.CoursiraJspPath;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LoginCommandTest {
  @Mock HttpServletRequest mockRequest;
  @Mock PrincipalService mockPrincipalService;
  @Mock Principal mockPrincipal;
  private LoginCommand command;
  private HashMap<String, String[]> queryParams;

  @BeforeMethod
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    command = new LoginCommand(mockPrincipalService);
    queryParams = new HashMap<>();
  }

  @Test
  public void testExecutePostWithoutParametersThrowsClientCommandException() {
    // given
    when(mockRequest.getParameterMap()).thenReturn(queryParams);
    when(mockPrincipal.toString()).thenReturn("mockPrincipal");
    // when
    queryParams = null;
    when(mockRequest.getMethod()).thenReturn("POST");
    // then
    assertThrows(ClientCommandException.class, () -> command.execute(mockPrincipal, mockRequest));
  }

  @Test
  public void testExecutePostWhenLoginFailsReturnMessage()
      throws ServiceException, ClientServiceException, ClientCommandException, CommandException {
    // given
    queryParams = new HashMap<>();
    queryParams.put("login", new String[] {"1"});
    queryParams.put("password", new String[] {"1"});
    when(mockRequest.getParameterMap()).thenReturn(queryParams);
    ClientServiceException exception = new ClientServiceException("message");
    LoginModel model = new LoginModel();
    model.setPrincipal(mockPrincipal);
    model.setErrorMessage("message");

    // when
    when(mockRequest.getMethod()).thenReturn("POST");
    when(mockPrincipalService.verifyPrincipleByPass(mockPrincipal, "1", "1")).thenThrow(exception);

    // then

    CommandResult expected = new CommandResult(CoursiraJspPath.LOGIN, model);
    CommandResult actual = command.execute(mockPrincipal, mockRequest);
    assertEquals(actual, expected);
  }
}
