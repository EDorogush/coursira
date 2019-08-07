package by.epam.coursira.command;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

import by.epam.coursira.entity.Principal;
import by.epam.coursira.exception.ClientCommandException;
import by.epam.coursira.exception.CommandException;
import by.epam.coursira.exception.PageNotFoundException;
import by.epam.coursira.exception.ServiceException;
import by.epam.coursira.model.IndexModel;
import by.epam.coursira.service.CourseService;
import by.epam.coursira.servlet.CoursiraJspPath;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IndexCommandTest {

  @Mock HttpServletRequest mockRequest;
  @Mock CourseService mockCourseService;
  @Mock Principal mockPrincipal;
  private IndexCommand command;

  @BeforeMethod
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    command = new IndexCommand(mockCourseService);
  }

  @Test
  public void testExecuteWithGetMethod()
      throws ServiceException, PageNotFoundException, ClientCommandException, CommandException {
    // given
    Map<String, Integer> pairs = new HashMap<>();
    pairs.put("courses", 1);
    pairs.put("lecturers", 1);
    pairs.put("students", 1);
    IndexModel model = new IndexModel();
    model.setCoursesAmount(pairs.get("courses"));
    model.setLecturerAmount(pairs.get("lecturers"));
    model.setStudentsAmount(pairs.get("students"));
    model.setPrincipal(mockPrincipal);
    when(mockCourseService.countStatistics()).thenReturn(pairs);

    // when
    when(mockRequest.getMethod()).thenReturn("GET");
    CommandResult expected = new CommandResult(CoursiraJspPath.INDEX, model);
    CommandResult actual = command.execute(mockPrincipal, mockRequest);
    // then

    assertEquals(actual, expected);
  }

  @Test
  public void testExecuteWithPostMethodThrowsPageNotFoundException() {
    // when
    when(mockRequest.getMethod()).thenReturn("POST");
    // then
    assertThrows(PageNotFoundException.class, () -> command.execute(mockPrincipal, mockRequest));
  }
}
