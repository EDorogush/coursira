package by.epam.coursira.dao;

import by.epam.coursira.entity.Course;
import by.epam.coursira.entity.Lecture;
import by.epam.coursira.entity.Lecturer;
import by.epam.coursira.exception.DaoException;
import by.epam.coursira.exception.PoolConnectionException;
import by.epam.coursira.pool.ConnectionPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StudentDao {
  private static final Logger logger = LogManager.getLogger();
  // column names
  private static final String LECTURER_ID = "lecturer_id";
  private static final String FIRST_NAME = "firstname";
  private static final String LAST_NAME = "lastname";

  private static final String SQL_SELECT_COURSES_BY_STUDENT_ID =
      "SELECT c.course_id,\n"
          + "       c.title,\n"
          + "       c.description,\n"
          + "       c.capacity,"
          + "       c.ready,\n"
          + "       count(cs.student_id) AS student_amount,\n"
          + "       cl.lecturer_id,\n"
          + "       u.firstname,\n"
          + "       u.lastname\n"
          + "FROM courses c\n"
          + "       LEFT JOIN course_lecturers cl ON c.course_id = cl.course_id\n"
          + "       LEFT JOIN course_students cs ON c.course_id = cs.course_id AND student_id != 0\n"
          + "       LEFT JOIN users u ON cl.lecturer_id = u.id\n"
          + "\n"
          + "WHERE c.course_id IN\n"
          + "      (\n"
          + "        SELECT course_id\n"
          + "        FROM course_students\n"
          + "        WHERE student_id = ?\n"
          + "        ORDER BY course_id ASC\n"
          + "        LIMIT ? OFFSET ?\n"
          + "      )\n"
          + "GROUP BY c.course_id, cl.lecturer_id, u.firstname, u.lastname\n"
          + "ORDER BY course_id ASC;";

  private static final String SQL_EXISTS_COURSE_IN_STUDENT_LIST =
      "SELECT exists(\n"
          + "         SELECT c.course_id\n"
          + "         FROM courses c\n"
          + "                JOIN course_students cs ON c.course_id = cs.course_id\n"
          + "         WHERE student_id = ?\n"
          + "           AND c.course_id = ?);\n";

  private static final String SQL_SELECT_SCHEDULE_BY_STUDENT_ID =
      "SELECT l.lecture_id,\n"
          + "       l.description,\n"
          + "       l.time_start,\n"
          + "       l.time_end,\n"
          + "       cl.course_id,\n"
          + "       c.title,\n"
          + "       cl.lecturer_id,\n"
          + "       u.firstname,\n"
          + "       u.lastname\n"
          + "FROM lectures l\n"
          + "       JOIN course_lecturers cl ON l.course_lecturer_id = cl.entry_id\n"
          + "       JOIN course_students cs ON cl.course_id = cs.course_id\n"
          + "       JOIN courses c ON cl.course_id = c.course_id\n"
          + "       JOIN users u ON cl.lecturer_id = u.id\n"
          + "WHERE cs.student_id = ?\n"
          + "ORDER BY time_start ASC\n"
          + "LIMIT ? OFFSET ?;\n";

  private static final String SQL_UPDATE_COURSE_STUDENTS_ON_SET_CURRENT_STUDENT =
      "UPDATE course_students\n"
          + "SET student_id = ?\n"
          + "WHERE entry_id =\n"
          + "      (SELECT entry_id FROM course_students WHERE course_id = ? AND student_id = 0 LIMIT 1);";

  private static final String SQL_UPDATE_COURSE_STUDENTS_ON_SET_DEFAULT_STUDENT =
      "  UPDATE course_students\n"
          + "  SET student_id = 0\n"
          + "  WHERE student_id = ? AND course_id = ?;";

  private static final String SQL_COUNT_COURSES_BY_STUDENT_ID =
      "SELECT count(course_id)\n" + "FROM course_students\n" + "WHERE student_id = ?;";

  private final DataSource pool;

  public StudentDao(DataSource pool) {
    this.pool = pool;
  }

  public List<Course> selectCoursesByStudentId(int studentId, int limit, int offset)
      throws DaoException {
    List<Course> courses = new ArrayList<>();
    try (Connection connection = pool.getConnection();
        PreparedStatement ps = connection.prepareStatement(SQL_SELECT_COURSES_BY_STUDENT_ID)) {

      ps.setInt(1, studentId);
      ps.setInt(2, limit);
      ps.setInt(3, offset);
      try (ResultSet rs = ps.executeQuery()) {
        courses = parseResultSetToCoursesList(rs);
      }
    } catch (SQLException e) {
      throw new DaoException(e);
    }
    return courses;
  }

  public boolean isExistsCourseInStudentList(int courseId, int studentId) throws DaoException {
    try (Connection connection = pool.getConnection();
        PreparedStatement ps = connection.prepareStatement(SQL_EXISTS_COURSE_IN_STUDENT_LIST)) {
      ps.setInt(1, studentId);
      ps.setInt(2, courseId);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() && rs.getBoolean("exists");
      }
    } catch (SQLException e) {
      throw new DaoException(e);
    }
  }

  public List<Lecture> selectScheduleByStudentId(int studentId, int limit, int offset)
      throws DaoException {
    List<Lecture> lectureList = new ArrayList<>();
    try (Connection connection = pool.getConnection();
        PreparedStatement ps = connection.prepareStatement(SQL_SELECT_SCHEDULE_BY_STUDENT_ID)) {
      ps.setInt(1, studentId);
      ps.setInt(2, limit);
      ps.setInt(3, offset);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Lecturer lecturer =
              new Lecturer(
                  rs.getInt(LECTURER_ID), rs.getString(FIRST_NAME), rs.getString(LAST_NAME));
          Lecture current =
              new Lecture.Builder()
                  .withLectureId(rs.getInt("lecture_id"))
                  .withTimeStart(rs.getTimestamp("time_start").toInstant())
                  .withTimeEnd(rs.getTimestamp("time_end").toInstant())
                  .withCourseId(rs.getInt("course_id"))
                  .withDescription(rs.getString("description"))
                  .withLecturer(lecturer)
                  .build();
          lectureList.add(current);
        }
      }
      logger.debug("list contains {}", lectureList.size());
    } catch (SQLException e) {
      throw new DaoException(e);
    }
    return lectureList;
  }

  public boolean updateCourseStudent(int studentId, int courseId) throws DaoException {
    try (Connection connection = pool.getConnection();
        PreparedStatement psUpdate =
            connection.prepareStatement(SQL_UPDATE_COURSE_STUDENTS_ON_SET_CURRENT_STUDENT)) {
      psUpdate.setInt(1, studentId);
      psUpdate.setInt(2, courseId);
      psUpdate.executeUpdate();
      int updatedRows = psUpdate.getUpdateCount();
      logger.debug("{} rows had been updated", updatedRows);
      if (updatedRows == 0) {
        logger.debug("can't add student {} to course {} : course has filled.", studentId, courseId);
        return false;
      }
      return true;
    } catch (SQLException e) {
      throw new DaoException(e);
    }
  }

  public int updateOnRemoveCourseFromStudentSchedule(int studentId, int courseId)
      throws DaoException {
    try (Connection connection = pool.getConnection();
        PreparedStatement ps =
            connection.prepareStatement(SQL_UPDATE_COURSE_STUDENTS_ON_SET_DEFAULT_STUDENT)) {
      ps.setInt(1, studentId);
      ps.setInt(2, courseId);
      return ps.executeUpdate();
    } catch (SQLException e) {
      throw new DaoException(e);
    }
  }

  public int countStudentCourses(int studentId) throws DaoException {
    try {
      try (Connection connection = pool.getConnection();
          PreparedStatement ps = connection.prepareStatement(SQL_COUNT_COURSES_BY_STUDENT_ID)) {
        ps.setInt(1, studentId);
        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            return rs.getInt("count");
          } else throw new DaoException("ResultSet returns null");
        }
      }
    } catch (SQLException e) {
      throw new DaoException(e);
    }
  }

  private List<Course> parseResultSetToCoursesList(ResultSet rs) throws SQLException {
    List<Course> courses = new ArrayList<>();
    int previousCourseId = 0;
    int size = 0;
    while (rs.next()) {
      int currentCourseId = rs.getInt("course_id");
      if (currentCourseId == previousCourseId) {
        logger.debug("next lector");
        // create lecturer
        Lecturer lecturer =
            new Lecturer(rs.getInt(LECTURER_ID), rs.getString(FIRST_NAME), rs.getString(LAST_NAME));
        logger.debug("size,{}", size);
        courses.get(size - 1).addLecturer(lecturer);
      } else {
        // next course record
        Lecturer lecturer =
            new Lecturer(rs.getInt(LECTURER_ID), rs.getString(FIRST_NAME), rs.getString(LAST_NAME));
        logger.debug("next course record");
        Course course = new Course();
        course.setId(currentCourseId);
        course.setTitle(rs.getString("title"));
        course.setDescription(rs.getString("description"));
        course.setCapacity(rs.getInt("capacity"));
        course.addLecturer(lecturer);
        course.setStudentsAmount(rs.getInt("student_amount"));
        course.setReady(rs.getBoolean("ready"));
        previousCourseId = currentCourseId;
        courses.add(course);
        size++;
      }
    }
    return courses;
  }
}
