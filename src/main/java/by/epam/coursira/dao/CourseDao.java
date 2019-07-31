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
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CourseDao {

  private static final Logger logger = LogManager.getLogger();

  private static final String SQL_EXISTS_COURSE_ID =
      "SELECT EXISTS(SELECT 1 FROM courses WHERE course_id = ?);";

  private static final String SQL_EXISTS_COURSE_IN_LECTURER_READY_LIST =
      "SELECT exists(\n"
          + "         SELECT c.course_id\n"
          + "         FROM courses c\n"
          + "                JOIN course_lecturers cl ON c.course_id = cl.course_id\n"
          + "         WHERE lecturer_id = ?\n"
          + "           AND c.course_id = ?"
          + "           AND c.ready);";

  private static final String SQL_EXISTS_COURSE_IN_LECTURER_UPDATE_LIST =
      "SELECT exists(\n"
          + "         SELECT c.course_id\n"
          + "         FROM courses c\n"
          + "                JOIN course_lecturers cl ON c.course_id = cl.course_id\n"
          + "         WHERE lecturer_id = ?\n"
          + "           AND c.course_id = ?"
          + "           AND NOT c.ready);";

  private static final String SQL_EXISTS_LECTURE_IN_LECTURER_UPDATE_LIST =
      "SELECT exists(\n"
          + "         SELECT l.lecture_id\n"
          + "         FROM lectures l\n"
          + "                JOIN course_lecturers cl ON l.course_lecturer_id = cl.entry_id\n"
          + "                JOIN courses c ON cl.course_id = c.course_id\n"
          + "         WHERE lecture_id = ?\n"
          + "           AND lecturer_id = ?"
          + "           AND NOT c.ready);";

  // INSERT

  private static final String SQL_INSERT_INTO_COURSE_TABLE =
      "INSERT INTO courses(title, description, capacity, ready) VALUES (?,?,?,?)";

  private static final String SQL_INSERT_INTO_LECTURE_TABLE =
      "INSERT INTO lectures(course_lecturer_id, time_start, time_end, description) VALUES (?,?,?,?)";

  private static final String SQL_INSERT_INTO_COURSE_LECTURER_TABLE =
      "INSERT INTO course_lecturers(course_id, lecturer_id ) VALUES (?,?)";

  private static final String SQL_INSERT_INTO_COURSE_STUDENTS_TABLE =
      "INSERT INTO course_students(course_id, student_id) VALUES (?,?)";

  private static final String SQL_SELECT_COURSE_DETAILS_BY_COURSE_ID =
      "SELECT l.lecture_id,\n"
          + "       l.description  AS lecture_description,\n"
          + "       l.time_start,\n"
          + "       l.time_end,\n"
          + "       cl.lecturer_id,\n"
          + "       u.firstname,\n"
          + "       u.lastname,\n"
          + "       c.title,\n"
          + "       c.description AS course_description,\n"
          + "       c.capacity,"
          + "       c.ready,\n"
          + "       count(cs.student_id) AS student_amount\n"
          + "FROM lectures l\n"
          + "       RIGHT JOIN course_lecturers cl ON l.course_lecturer_id = cl.entry_id\n"
          + "       JOIN users u ON cl.lecturer_id = u.id\n"
          + "       JOIN courses c ON cl.course_id = c.course_id\n"
          + "       LEFT JOIN course_students cs ON c.course_id = cs.course_id AND student_id != 0\n"
          + "WHERE cl.course_id = ?\n"
          + "GROUP BY l.lecture_id,cl.lecturer_id,u.firstname, u.lastname, c.title, c.description, c.capacity, c.ready\n"
          + "ORDER BY time_start ASC LIMIT ? OFFSET ?;";

  private static final String SQL_SELECT_ALL_READY_COURSES_LIMIT_OFFSET =
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
          + "WHERE ready AND c.course_id IN\n"
          + "      (\n"
          + "        SELECT course_id\n"
          + "        FROM courses\n"
          + "        ORDER BY course_id ASC\n"
          + "        LIMIT ? OFFSET ?\n"
          + "      )\n"
          + "GROUP BY c.course_id, cl.lecturer_id, u.firstname, u.lastname\n"
          + "ORDER BY course_id ASC;";

  /*private static final String SQL_SELECT_SCHEDULE_BY_COURSE_ID =
      "SELECT l.lecture_id,\n" +
        "       l.time_start,\n" +
        "       l.time_end,\n" +
        "       cl.course_id\n" +
        "FROM lectures l\n" +
        "       join course_lecturers cl on l.course_lecturer_id = cl.entry_id\n" +
        "where cl.course_id = ?\n" +
        "ORDER BY time_start ASC;";
  */

  private static final String SQL_SELECT_SCHEDULE_UNION_LECTURER =
      "(SELECT l.lecture_id,\n"
          + "        l.time_start AS time\n"
          + " FROM lectures l\n"
          + "        JOIN course_lecturers cl ON l.course_lecturer_id = cl.entry_id\n"
          + " WHERE cl.lecturer_id = ?\n"
          + ")\n"
          + "UNION\n"
          + "(SELECT l.lecture_id,\n"
          + "        l.time_end AS time\n"
          + " FROM lectures l\n"
          + "        JOIN course_lecturers cl ON l.course_lecturer_id = cl.entry_id\n"
          + " WHERE cl.lecturer_id = ?\n"
          + ")\n"
          + "ORDER BY time ASC;";

  private static final String SQL_SELECT_COUNT_LECTURES_OF_COURSE_GROUP_BY_LECTURERS =
      "SELECT count(lecture_id) FROM lectures\n"
          + "      JOIN course_lecturers ON lectures.course_lecturer_id = course_lecturers.entry_id\n"
          + "WHERE course_id = ? GROUP BY lecturer_id";

  /*private static final String SQL_SELECT_EXISTS_LECTURER_ID = "SELECT EXISTS(SELECT 1 FROM courses WHERE course_id
  = ?);";*/

  // UPDATE

  private static final String SQL_UPDATE_COURSE_TABLE_BY_COURSE_ID =
      "UPDATE courses SET title = ?, description = ?, capacity = ? " + "WHERE course_id = ?";

  private static final String SQL_UPDATE_LECTURES_TABLE_BY_LECTURE_ID =
      "UPDATE lectures SET time_start = ?, time_end = ?, description = ?" + "WHERE lecture_id = ?";

  private static final String SQL_UPDATE_LECTURES_TABLE_BY_LECTURER_ID =
      "UPDATE lectures SET course_lecturer_id = cl.entry_id "
          + "FROM course_lecturers cl "
          + "WHERE lecture_id = ?";

  private static final String SQL_SELECT_TABLE_COUNTS =
      "SELECT (\n"
          + "         SELECT count(course_id)\n"
          + "         FROM courses WHERE ready"
          + "       ) AS courses,\n"
          + "       (\n"
          + "         SELECT count(lecture_id)\n"
          + "         FROM lectures\n"
          + "           JOIN course_lecturers cl ON lectures.course_lecturer_id = cl.entry_id\n"
          + "           JOIN courses c ON cl.course_id = c.course_id\n"
          + "         WHERE c.ready"
          + "       ) AS lectures,\n"
          + "       (\n"
          + "         SELECT count(email)\n"
          + "         FROM users\n"
          + "         WHERE role = 'LECTURER' AND registration_code ISNULL \n"
          + "       ) AS lecturers,\n"
          + "       (\n"
          + "         SELECT count(email)\n"
          + "         FROM users\n"
          + "         WHERE role = 'STUDENT'\n AND registration_code ISNULL"
          + "       ) AS students\n";

  private static final String SQL_SELECT_COUNT_COURSE = "SELECT COUNT(course_id) FROM courses";

  private static final String SQL_UPDATE_COURSE_SET_READY =
      "UPDATE courses SET ready = TRUE WHERE course_id = ?;";

  private static final String SQL_SELECT_ENTRY_ID_FROM_COURSE_LECTURERS =
      "SELECT entry_id FROM course_lecturers WHERE course_id = ? AND lecturer_id = ?;";

  private static final String SQL_SELECT_ALL_COURSES_BY_LECTURER_ID =
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
          + "WHERE c.course_id IN\n"
          + "      (\n"
          + "        SELECT course_id\n"
          + "        FROM course_lecturers\n"
          + "        WHERE lecturer_id = ?\n"
          + "        ORDER BY course_id ASC\n"
          + "        LIMIT ? OFFSET ?\n"
          + "      )\n"
          + "GROUP BY c.course_id, cl.lecturer_id, u.firstname, u.lastname\n"
          + "ORDER BY course_id ASC;\n";

  private static final String SQL_SELECT_READY_COURSES_BY_LECTURER_ID =
      "SELECT c.course_id,\n"
          + "       c.title,\n"
          + "       c.description,\n"
          + "       c.capacity,\n"
          + "       c.ready,\n"
          + "       count(cs.student_id) AS student_amount,\n"
          + "       cl.lecturer_id,\n"
          + "       u.firstname,\n"
          + "       u.lastname\n"
          + "FROM courses c\n"
          + "       LEFT JOIN course_lecturers cl ON c.course_id = cl.course_id\n"
          + "       LEFT JOIN course_students cs ON c.course_id = cs.course_id AND student_id != 0\n"
          + "       LEFT JOIN users u ON cl.lecturer_id = u.id\n"
          + "WHERE c.course_id IN\n"
          + "      (\n"
          + "        SELECT course_id\n"
          + "        FROM course_lecturers\n"
          + "        WHERE lecturer_id = 7 AND  ready\n"
          + "        ORDER BY course_id ASC\n"
          + "        LIMIT 3 OFFSET 0\n"
          + "      )\n"
          + "GROUP BY c.course_id, cl.lecturer_id, u.firstname, u.lastname\n"
          + "ORDER BY course_id ASC;";

  private static final String SQL_SELECT_SCHEDULE_BY_LECTURER_ID =
      "SELECT l.lecture_id,\n"
          + "       l.description,\n"
          + "       l.time_start,\n"
          + "       l.time_end,\n"
          + "       cl.course_id,\n"
          + "       c.title\n"
          + "FROM lectures l\n"
          + "       JOIN course_lecturers cl ON l.course_lecturer_id = cl.entry_id\n"
          + "       JOIN courses c ON cl.course_id = c.course_id\n"
          + "WHERE cl.lecturer_id = ? AND c.ready\n"
          + "ORDER BY time_start ASC;";

  private static final String SQL_SELECT_FUTURE_SCHEDULE_BY_LECTURER_ID =
      "SELECT l.lecture_id,\n"
          + "       l.description,\n"
          + "       l.time_start,\n"
          + "       l.time_end,\n"
          + "       cl.course_id,\n"
          + "       c.title\n"
          + "FROM lectures l\n"
          + "       JOIN course_lecturers cl ON l.course_lecturer_id = cl.entry_id\n"
          + "       JOIN courses c ON cl.course_id = c.course_id\n"
          + "WHERE cl.lecturer_id = ?\n"
          + "ORDER BY time_start ASC;";

  private static final String SQL_UPSERT_COURSE_LECTURER =
      "INSERT INTO course_lecturers (lecturer_id, course_id)\n"
          + "VALUES (?,?)\n"
          + "ON CONFLICT (lecturer_id,course_id)\n"
          + "  DO NOTHING";

  private static final String SQL_DELETE_COURSE_LECTURER =
      "DELETE FROM course_lecturers " + "WHERE course_id = ? AND lecturer_id =?;";

  private static final String SQL_DELETE_COURSE = "DELETE FROM courses WHERE course_id = ?";
  private static final String SQL_DELETE_LECTURE = "DELETE FROM lectures WHERE lecture_id = ?";
  private static final String SQL_SELECT_LECTURE_BEFORE_DELETE =
      "SELECT FROM lectures WHERE lecture_id = ?";

  private static final String SQL_COUNT_READY_COURSES_BY_LECTURER_ID =
      "SELECT count(cl.course_id)\n"
          + "FROM course_lecturers cl\n"
          + "JOIN courses c ON cl.course_id = c.course_id\n"
          + "WHERE lecturer_id = ? AND c.ready;";

  private final ConnectionPool pool;

  public CourseDao(ConnectionPool pool) {
    this.pool = pool;
  }

  public boolean isExistsCourse(int courseId) throws DaoException {
    try (Connection connection = pool.getConnection();
        PreparedStatement ps = connection.prepareStatement(SQL_EXISTS_COURSE_ID)) {
      ps.setInt(1, courseId);
      ResultSet rs = ps.executeQuery();
      return rs.next() && rs.getBoolean("exists");
    } catch (SQLException | PoolConnectionException e) {
      logger.info(e);
      throw new DaoException(e);
    }
  }

  public boolean isExistsCourseInLecturerReadyList(int courseId, int lecturerId)
      throws DaoException {
    try (Connection connection = pool.getConnection();
        PreparedStatement ps =
            connection.prepareStatement(SQL_EXISTS_COURSE_IN_LECTURER_READY_LIST)) {
      ps.setInt(1, lecturerId);
      ps.setInt(2, courseId);
      ResultSet rs = ps.executeQuery();
      return rs.next() && rs.getBoolean("exists");
    } catch (SQLException | PoolConnectionException e) {
      logger.info(e);
      throw new DaoException(e);
    }
  }

  public boolean isExistsCourseInLecturerUpdateList(int courseId, int lecturerId)
      throws DaoException {
    try (Connection connection = pool.getConnection();
        PreparedStatement ps =
            connection.prepareStatement(SQL_EXISTS_COURSE_IN_LECTURER_UPDATE_LIST)) {
      ps.setInt(1, lecturerId);
      ps.setInt(2, courseId);
      ResultSet rs = ps.executeQuery();
      return rs.next() && rs.getBoolean("exists");
    } catch (SQLException | PoolConnectionException e) {
      logger.info(e);
      throw new DaoException(e);
    }
  }

  public boolean isExistsLectureInLecturerUpdateList(int lectureId, int lecturerId)
      throws DaoException {
    try (Connection connection = pool.getConnection();
        PreparedStatement ps =
            connection.prepareStatement(SQL_EXISTS_LECTURE_IN_LECTURER_UPDATE_LIST)) {
      ps.setInt(1, lectureId);
      ps.setInt(2, lecturerId);
      ResultSet rs = ps.executeQuery();
      return rs.next() && rs.getBoolean("exists");
    } catch (SQLException | PoolConnectionException e) {
      logger.info(e);
      throw new DaoException(e);
    }
  }

  public int insertCourse(Course course) throws DaoException {
    final int courseId;
    try (Connection connection = pool.getConnection()) {
      connection.setAutoCommit(false);
      try (PreparedStatement psInsertCourse =
              connection.prepareStatement(
                  SQL_INSERT_INTO_COURSE_TABLE, Statement.RETURN_GENERATED_KEYS);
          PreparedStatement psInsertCourseStudents =
              connection.prepareStatement(SQL_INSERT_INTO_COURSE_STUDENTS_TABLE);
          PreparedStatement psInsertCourseLecturer =
              connection.prepareStatement(
                  SQL_INSERT_INTO_COURSE_LECTURER_TABLE, Statement.RETURN_GENERATED_KEYS);
          PreparedStatement psInsertLecture =
              connection.prepareStatement(SQL_INSERT_INTO_LECTURE_TABLE)) {
        // 1. insert Course data
        psInsertCourse.setString(1, course.getTitle());
        psInsertCourse.setString(2, course.getDescription());
        psInsertCourse.setInt(3, course.getCapacity());
        psInsertCourse.setBoolean(4, course.isReady()); // must be false
        psInsertCourse.executeUpdate();
        logger.debug("Course added to course table");
        // get course_id for next table
        ResultSet gKeysCourseId = psInsertCourse.getGeneratedKeys();
        if (!gKeysCourseId.next()) {
          // do not need roollback
          connection.setAutoCommit(true);
          throw new SQLException("No record was inserted to courses table");
        }
        courseId = gKeysCourseId.getInt(1);
        logger.info("new Course Id is {}", courseId);
        // 2. insert to course_students empty spots
        psInsertCourseStudents.setInt(1, courseId);
        for (int i = 0; i < course.getCapacity(); i++) {
          psInsertCourseStudents.setInt(2, 0); // empty spot
          psInsertCourseStudents.executeUpdate();
        }
        // 3. insert Lecturers:
        psInsertCourseLecturer.setInt(1, courseId);
        for (Lecturer lecturer : course.getLecturers()) {
          psInsertCourseLecturer.setInt(2, lecturer.getId());
          psInsertCourseLecturer.executeUpdate();
          ResultSet gKeysEntryId = psInsertCourseLecturer.getGeneratedKeys();
          if (!gKeysEntryId.next()) {
            logger.info("transaction rollback");
            connection.rollback();
            throw new SQLException("No record was inserted to course_lecturers table");
          }
          // 4 insertLecturers
          int entryId = gKeysEntryId.getInt(1);
          logger.info("new entry_Id is {}", entryId);
          psInsertLecture.setInt(1, entryId);
          for (Lecture lecture : course.getLectures()) {
            if (lecture.getLecturer().getId() == lecturer.getId()) {
              psInsertLecture.setTimestamp(2, Timestamp.from(lecture.getStartTime()));
              psInsertLecture.setTimestamp(3, Timestamp.from(lecture.getEndTime()));
              psInsertLecture.setString(4, course.getTitle());
              psInsertLecture.executeUpdate();
              logger.info("one record was added to lectures");
            }
          }
        }
        logger.debug("finish insert successfully");
        connection.commit();
        connection.setAutoCommit(true);
        return courseId;
      } catch (SQLException e) {
        logger.error("Can't add course record. rollback transaction");
        connection.rollback();
        connection.setAutoCommit(true);
        throw new DaoException(e);
      }
    } catch (PoolConnectionException | SQLException e) {
      throw new DaoException(e);
    }
  }

  public int insertEmptyCourse(Course course) throws DaoException {
    final int courseId;
    try (Connection connection = pool.getConnection()) {
      connection.setAutoCommit(false);
      try (PreparedStatement psInsertCourse =
              connection.prepareStatement(
                  SQL_INSERT_INTO_COURSE_TABLE, Statement.RETURN_GENERATED_KEYS);
          PreparedStatement psInsertCourseLecturer =
              connection.prepareStatement(
                  SQL_INSERT_INTO_COURSE_LECTURER_TABLE, Statement.RETURN_GENERATED_KEYS);
          PreparedStatement psInsertLecture =
              connection.prepareStatement(SQL_INSERT_INTO_LECTURE_TABLE)) {
        psInsertCourse.setString(1, course.getTitle());
        psInsertCourse.setString(2, course.getDescription());
        psInsertCourse.setInt(3, course.getCapacity());
        psInsertCourse.setBoolean(4, course.isReady());

        psInsertCourse.executeUpdate();

        logger.debug("Course added to course table");
        // get course_id for next table
        ResultSet generatedKeys = psInsertCourse.getGeneratedKeys();
        if (generatedKeys.next()) {
          courseId = generatedKeys.getInt(1);
          logger.info("new Course Id is {}", courseId);
          psInsertCourseLecturer.setInt(1, courseId);
          for (Lecturer lecturer : course.getLecturers()) {
            psInsertCourseLecturer.setInt(2, lecturer.getId());
            psInsertCourseLecturer.executeUpdate();
            ResultSet generatedKeys1 = psInsertCourseLecturer.getGeneratedKeys();

            if (generatedKeys1.next()) {
              int entry_id = generatedKeys1.getInt(1);
              logger.info("new entry_Id is {}", courseId);
              psInsertLecture.setInt(1, entry_id);
              psInsertLecture.setTimestamp(2, Timestamp.from(Instant.now()));
              psInsertLecture.setTimestamp(3, Timestamp.from(Instant.now()));
              psInsertLecture.setString(4, course.getTitle());
              psInsertLecture.executeUpdate();
              logger.info("one record was added to lectures");
            } else {
              throw new SQLException("No record was inserted to course_lecturers table");
            }
          }
          logger.debug("finish insert successfully");
          connection.commit();
          connection.setAutoCommit(true);
          return courseId;
        } else {
          throw new SQLException("No record was inserted to courses table");
        }
      } catch (SQLException e) {
        logger.error("Can't add course record. rollback transaction");
        connection.rollback();
        connection.setAutoCommit(true);
        throw new DaoException(e);
      }
    } catch (PoolConnectionException | SQLException e) {
      throw new DaoException(e);
    }
  }

  public List<Lecture> selectScheduleByLecturerId(int lecturerId) throws DaoException {
    List<Lecture> lectureList = new ArrayList<>();
    try (Connection connection = pool.getConnection();
        PreparedStatement ps = connection.prepareStatement(SQL_SELECT_SCHEDULE_BY_LECTURER_ID)) {
      ps.setInt(1, lecturerId);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        Lecture current =
            new Lecture.Builder()
                .withLectureId(rs.getInt("lecture_id"))
                .withTimeStart(rs.getTimestamp("time_start").toInstant())
                .withTimeEnd(rs.getTimestamp("time_end").toInstant())
                .withCourseId(rs.getInt("course_id"))
                .withDescription(rs.getString("description"))
                .build();
        lectureList.add(current);
      }
      logger.debug("list contains {}", lectureList.size());
    } catch (SQLException | PoolConnectionException e) {
      throw new DaoException(e);
    }
    return lectureList;
  }

  public void upsertCourseLecturer(int courseId, int lecturerId) throws DaoException {
    try (Connection connection = pool.getConnection()) {
      connection.setAutoCommit(false);
      try (PreparedStatement psLecturer =
              connection.prepareStatement(
                  SQL_UPSERT_COURSE_LECTURER, Statement.RETURN_GENERATED_KEYS);
          PreparedStatement ps = connection.prepareStatement(SQL_INSERT_INTO_LECTURE_TABLE)) {
        ps.setInt(1, courseId);
        ps.setInt(2, lecturerId);
        ps.executeUpdate();
      }

    } catch (SQLException | PoolConnectionException e) {
      throw new DaoException(e);
    }
  }

  public int deleteCourseLecturer(int courseId, int lecturerId) throws DaoException {
    try (Connection connection = pool.getConnection();
        PreparedStatement ps = connection.prepareStatement(SQL_DELETE_COURSE_LECTURER)) {
      ps.setInt(1, courseId);
      ps.setInt(2, lecturerId);
      return ps.executeUpdate();
    } catch (SQLException | PoolConnectionException e) {
      throw new DaoException(e);
    }
  }

  public int countLecturerReadyCourses(int lecturerId) throws DaoException {
    try {
      try (Connection connection = pool.getConnection();
          PreparedStatement ps =
              connection.prepareStatement(SQL_COUNT_READY_COURSES_BY_LECTURER_ID)) {
        ps.setInt(1, lecturerId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
          return rs.getInt("count");
        } else throw new DaoException("ResultSet returns null");
      }
    } catch (SQLException | PoolConnectionException e) {
      throw new DaoException(e);
    }
  }

  public List<Course> selectAllCoursesByLecturerId(int lecturerId, int limit, int offset)
      throws DaoException {
    List<Course> courses = new ArrayList<>();
    try (Connection connection = pool.getConnection();
        PreparedStatement ps = connection.prepareStatement(SQL_SELECT_ALL_COURSES_BY_LECTURER_ID)) {
      ps.setInt(1, lecturerId);
      ps.setInt(2, limit);
      ps.setInt(3, offset);
      ResultSet rs = ps.executeQuery();
      return parseResultSetToCoursesList(rs);
    } catch (SQLException e) {
      logger.info("SQLException in attempt to close PreparedStatement ps.");
      throw new DaoException(e.getMessage());
    } catch (PoolConnectionException e) {
      logger.info("Exception in attempt to get Connection");
      throw new DaoException(e.getMessage());
    }
  }

  public List<Course> selectReadyCoursesByLecturerId(int lecturerId, int limit, int offset)
      throws DaoException {
    List<Course> courses = new ArrayList<>();
    try (Connection connection = pool.getConnection();
        PreparedStatement ps =
            connection.prepareStatement(SQL_SELECT_READY_COURSES_BY_LECTURER_ID)) {
      ps.setInt(1, lecturerId);
      ps.setInt(2, limit);
      ps.setInt(3, offset);
      ResultSet rs = ps.executeQuery();
      return parseResultSetToCoursesList(rs);
    } catch (SQLException e) {
      logger.info("SQLException in attempt to close PreparedStatement ps.");
      throw new DaoException(e.getMessage());
    } catch (PoolConnectionException e) {
      logger.info("Exception in attempt to get Connection");
      throw new DaoException(e.getMessage());
    }
  }

  public List<Course> selectCoursesAllReady(int limit, int offset) throws DaoException {
    List<Course> courses;
    try (Connection connection = pool.getConnection();
        PreparedStatement ps =
            connection.prepareStatement(SQL_SELECT_ALL_READY_COURSES_LIMIT_OFFSET)) {

      ps.setInt(1, limit);
      ps.setInt(2, offset);
      ResultSet rs = ps.executeQuery();
      courses = parseResultSetToCoursesList(rs);
    } catch (SQLException e) {
      logger.info("SQLException in attempt to close PreparedStatement ps.");
      throw new DaoException(e);
    } catch (PoolConnectionException e) {
      logger.info("Exception in attempt to get Connection");
      throw new DaoException(e);
    }
    logger.debug("get {} records", courses.size());
    return courses;
  }

  public Optional<Course> selectCourseById(int courseId, int limit, int offset)
      throws DaoException {
    List<Lecture> lectureList = new ArrayList<>();
    try (Connection connection = pool.getConnection();
        PreparedStatement ps =
            connection.prepareStatement(
                SQL_SELECT_COURSE_DETAILS_BY_COURSE_ID,
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY)) {
      ps.setInt(1, courseId);
      ps.setInt(2, limit);
      ps.setInt(3, offset);

      ResultSet rs = ps.executeQuery();
      List<Lecturer> lecturers = new ArrayList<>();
      int previousLecturer = 0;
      while (rs.next()) {
        int currentLecturer = rs.getInt("lecturer_id");
        if (currentLecturer != previousLecturer) {
          Lecturer lecturer =
              new Lecturer(
                  rs.getInt("lecturer_id"), rs.getString("firstname"), rs.getString("lastname"));
          lecturers.add(lecturer);
          previousLecturer = currentLecturer;
        }

        Lecturer lecturer =
            new Lecturer(
                rs.getInt("lecturer_id"), rs.getString("firstname"), rs.getString("lastname"));
        if (rs.getInt("lecture_id") != 0) {
          Lecture current =
              new Lecture.Builder()
                  .withLectureId(rs.getInt("lecture_id"))
                  .withDescription(rs.getString("lecture_description"))
                  .withTimeStart(rs.getTimestamp("time_start").toInstant())
                  .withTimeEnd(rs.getTimestamp("time_end").toInstant())
                  .withLecturer(lecturer)
                  .build();
          lectureList.add(current);
        }
      }
      logger.debug("list contains {}", lectureList.size());
      if (rs.previous()) {
        logger.debug("here");
        Course course = new Course();
        course.setId(courseId);
        course.setTitle(rs.getString("title"));
        course.setDescription(rs.getString("course_description"));
        course.setCapacity(rs.getInt("capacity"));
        course.setStudentsAmount(rs.getInt("student_amount"));
        course.setLectures(lectureList);
        course.setLecturers(lecturers);
        logger.debug("get {} records", lectureList.size());
        return Optional.of(course);
      }
    } catch (SQLException e) {
      throw new DaoException(e);
    } catch (PoolConnectionException e) {
      logger.info("Exception in attempt to get Connection");
      throw new DaoException(e);
    }
    return Optional.empty();
  }

  public void updateLecture(Lecture lecture) throws DaoException {
    try (Connection connection = pool.getConnection();
        PreparedStatement psUpdate =
            connection.prepareStatement(SQL_UPDATE_LECTURES_TABLE_BY_LECTURE_ID)) {
      psUpdate.setTimestamp(1, Timestamp.from(lecture.getStartTime()));
      psUpdate.setTimestamp(2, Timestamp.from(lecture.getEndTime()));
      psUpdate.setString(3, lecture.getDescription());
      psUpdate.setInt(4, lecture.getId());
      psUpdate.executeUpdate();
    } catch (PoolConnectionException | SQLException e) {
      throw new DaoException(e);
    }
  }

  public boolean updateCourseData(Course course) throws DaoException {
    try (Connection connection = pool.getConnection();
        PreparedStatement ps = connection.prepareStatement(SQL_UPDATE_COURSE_TABLE_BY_COURSE_ID)) {
      ps.setString(1, course.getTitle());
      ps.setString(2, course.getDescription());
      ps.setInt(3, course.getCapacity());
      ps.setInt(4, course.getId());
      ps.executeUpdate();
    } catch (PoolConnectionException | SQLException e) {
      throw new DaoException(e);
    }
    return true;
  }

  public boolean updateCourseOnSetReady(int courseId) throws DaoException {
    try (Connection connection = pool.getConnection()) {
      connection.setAutoCommit(false);
      try (PreparedStatement psUpdate = connection.prepareStatement(SQL_UPDATE_COURSE_SET_READY);
          PreparedStatement psSelect =
              connection.prepareStatement(SQL_SELECT_COUNT_LECTURES_OF_COURSE_GROUP_BY_LECTURERS)) {

        psUpdate.setInt(1, courseId);
        psUpdate.executeUpdate();
        // continue transaction
        psSelect.setInt(1, courseId);
        ResultSet rs = psSelect.executeQuery();
        while (rs.next()) {
          if (rs.getInt("count") == 0) {
            logger.info("find empty lecturer");
            connection.rollback();
            connection.setAutoCommit(true);
            return false;
          }
        }
        // if we here means all OK
        connection.commit();
        connection.setAutoCommit(true);
        return true;
      } catch (SQLException e) {
        connection.rollback();
        connection.setAutoCommit(true);
        throw new DaoException(e);
      }

    } catch (PoolConnectionException | SQLException e) {
      throw new DaoException(e);
    }
  }

  public Map<String, Integer> countCoursesAndLecturesAndLecturersAndStudents() throws DaoException {
    Map<String, Integer> pairs = new HashMap<>();
    try (Connection connection = pool.getConnection();
        Statement ps = connection.createStatement()) {
      ResultSet rs = ps.executeQuery(SQL_SELECT_TABLE_COUNTS);
      if (rs.next()) {
        pairs.put("courses", rs.getInt("courses"));
        pairs.put("lectures", rs.getInt("lectures"));
        pairs.put("lecturers", rs.getInt("lecturers"));
        pairs.put("students", rs.getInt("students"));
      }
    } catch (SQLException | PoolConnectionException e) {
      throw new DaoException(e);
    }
    return pairs;
  }

  public boolean deleteLecture(int lectureId) throws DaoException {
    try (Connection connection = pool.getConnection();
        PreparedStatement ps = connection.prepareStatement(SQL_DELETE_LECTURE)) {
      ps.setInt(1, lectureId);
      ps.executeUpdate();
    } catch (SQLException | PoolConnectionException e) {
      throw new DaoException(e);
    }
    return true;
  }

  public void insertLecture(Lecture lecture) throws DaoException {
    final int lectureId;
    try (Connection connection = pool.getConnection();
        PreparedStatement psInsert =
            connection.prepareStatement(
                SQL_INSERT_INTO_LECTURE_TABLE, Statement.RETURN_GENERATED_KEYS);
        PreparedStatement psSelectEntry =
            connection.prepareStatement(SQL_SELECT_ENTRY_ID_FROM_COURSE_LECTURERS)) {

      // define entryId
      psSelectEntry.setInt(1, lecture.getCourseId());
      psSelectEntry.setInt(2, lecture.getLecturer().getId());
      ResultSet rs = psSelectEntry.executeQuery();
      final int entryId;
      if (rs.next()) {
        entryId = rs.getInt("entry_id");
      } else {
        throw new DaoException(
            String.format(
                "Can't Add lecture: no enty_id with course_id [%d] and lecturer_id [%d] found",
                lecture.getCourseId(), lecture.getLecturer().getId()));
      }
      // insert lecture
      psInsert.setInt(1, entryId);
      psInsert.setTimestamp(2, Timestamp.from(lecture.getStartTime()));
      psInsert.setTimestamp(3, Timestamp.from(lecture.getEndTime()));
      psInsert.setString(4, lecture.getDescription());
      psInsert.executeUpdate();
    } catch (SQLException | PoolConnectionException e) {
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
            new Lecturer(
                rs.getInt("lecturer_id"), rs.getString("firstname"), rs.getString("lastname"));
        logger.debug("size,{}", size);
        courses.get(size - 1).addLecturer(lecturer);
      } else {
        // next course record
        logger.debug("next course record");
        Course course = new Course();
        course.setId(currentCourseId);
        course.setTitle(rs.getString("title"));
        course.setDescription(rs.getString("description"));
        course.setCapacity(rs.getInt("capacity"));
        Lecturer lecturer =
            new Lecturer(
                rs.getInt("lecturer_id"), rs.getString("firstname"), rs.getString("lastname"));
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
