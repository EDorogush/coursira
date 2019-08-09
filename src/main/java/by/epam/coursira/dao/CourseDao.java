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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is DAO class providing CRUD operations for database containing data about next application
 * entities : {@link Course}, {@link Lecture}.
 */
public class CourseDao {

  private static final Logger logger = LogManager.getLogger();
  private static final String COLUMN_NAME_EXISTS = "exists";
  private static final String COLUMN_NAME_LECTURE_ID = "lecture_id";
  private static final String COLUMN_NAME_LECTURER_ID = "lecturer_id";
  private static final String COLUMN_NAME_FIRST_NAME = "firstname";
  private static final String COLUMN_NAME_LAST_NAME = "lastname";
  private static final String COLUMN_NAME_COUNT = "count";

  private static final String FOREIGN_KEY_VIOLATION_CODE = "23503";

  private static final String SQL_EXISTS_COURSE_ID =
      "SELECT EXISTS(SELECT 1 FROM courses WHERE course_id = ?);";

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

  // SELECT

  private static final String SQL_SELECT_COURSE_CAPACITY =
      "SELECT c.capacity\n" + "FROM courses c\n" + "WHERE c.course_id = ?\n";

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

  private static final String SQL_SELECT_COUNT_LECTURES_OF_COURSE_GROUP_BY_LECTURERS =
      "SELECT count(lecture_id), lecturer_id, u.firstname, u.lastname\n"
          + "FROM lectures\n"
          + "         RIGHT JOIN course_lecturers ON lectures.course_lecturer_id = course_lecturers.entry_id\n"
          + "         JOIN users u ON course_lecturers.lecturer_id = u.id\n"
          + "\n"
          + "WHERE course_id = ?\n"
          + "GROUP BY lecturer_id, u.firstname, u.lastname;";

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
          + "        WHERE lecturer_id = ? AND  ready\n"
          + "        ORDER BY course_id ASC\n"
          + "        LIMIT ? OFFSET ?\n"
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
          + "ORDER BY time_start ASC\n"
          + "LIMIT ? OFFSET ?;\n";

  // UPDATE

  private static final String SQL_UPDATE_COURSE_TABLE_BY_COURSE_ID =
      "UPDATE courses SET title = ?, description = ?, capacity = ? " + "WHERE course_id = ?";

  private static final String SQL_UPDATE_LECTURES_TABLE_BY_LECTURE_ID =
      "UPDATE lectures SET time_start = ?, time_end = ?, description = ?" + "WHERE lecture_id = ?";

  private static final String SQL_UPDATE_COURSE_SET_READY =
      "UPDATE courses SET ready = ? WHERE course_id = ?;";

  private static final String SQL_UPSERT_COURSE_LECTURER =
      "INSERT INTO course_lecturers (lecturer_id, course_id)\n"
          + "VALUES (?,?)\n"
          + "ON CONFLICT (lecturer_id,course_id)\n"
          + "  DO NOTHING";

  private static final String SQL_DELETE_FROM_COURSE_STUDENTS_TABLE =
      "DELETE FROM course_students WHERE entry_id =\n"
          + "      (SELECT entry_id FROM course_students WHERE course_id = ? AND student_id = ? LIMIT 1);";

  private static final String SQL_DELETE_COURSE_LECTURER =
      "DELETE FROM course_lecturers WHERE course_id = ? AND lecturer_id =?;";

  private static final String SQL_DELETE_LECTURE = "DELETE FROM lectures WHERE lecture_id = ?";

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
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() && rs.getBoolean(COLUMN_NAME_EXISTS);
      }
    } catch (SQLException | PoolConnectionException e) {
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
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() && rs.getBoolean(COLUMN_NAME_EXISTS);
      }
    } catch (SQLException | PoolConnectionException e) {
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
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() && rs.getBoolean(COLUMN_NAME_EXISTS);
      }
    } catch (SQLException | PoolConnectionException e) {
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
        try (ResultSet gKeysCourseId = psInsertCourse.getGeneratedKeys()) {
          if (!gKeysCourseId.next()) {
            // do not need roollback
            connection.setAutoCommit(true);
            throw new SQLException("No record was inserted to courses table");
          }
          courseId = gKeysCourseId.getInt(1);
        }
        logger.debug("new Course Id is {}", courseId);
        // 2. insert to course_students empty spots
        psInsertCourseStudents.setInt(1, courseId);
        psInsertCourseStudents.setInt(2, 0); // empty spot
        for (int i = 0; i < course.getCapacity(); i++) {
          psInsertCourseStudents.executeUpdate();
        }
        // 3. insert Lecturers:
        psInsertCourseLecturer.setInt(1, courseId);
        for (Lecturer lecturer : course.getLecturers()) {
          psInsertCourseLecturer.setInt(2, lecturer.getId());
          psInsertCourseLecturer.executeUpdate();
          int entryId;
          try (ResultSet gKeysEntryId = psInsertCourseLecturer.getGeneratedKeys()) {
            if (!gKeysEntryId.next()) {
              logger.warn("transaction rollback");
              connection.rollback();
              throw new SQLException("No record was inserted to course_lecturers table");
            }
            // 4 insertLecturers
            entryId = gKeysEntryId.getInt(1);
          }
          logger.debug("new entry_Id is {}", entryId);
          psInsertLecture.setInt(1, entryId);
          for (Lecture lecture : course.getLectures()) {
            if (lecture.getLecturer().getId() == lecturer.getId()) {
              psInsertLecture.setTimestamp(2, Timestamp.from(lecture.getStartTime()));
              psInsertLecture.setTimestamp(3, Timestamp.from(lecture.getEndTime()));
              psInsertLecture.setString(4, course.getTitle());
              psInsertLecture.executeUpdate();
              logger.debug("one record was added to lectures");
            }
          }
        }
        logger.debug("finish insert successfully");
        connection.commit();
        connection.setAutoCommit(true);
        return courseId;
      } catch (SQLException e) {
        logger.debug("Can't add course record. rollback transaction");
        connection.rollback();
        connection.setAutoCommit(true);
        throw new DaoException(e);
      }
    } catch (PoolConnectionException | SQLException e) {
      throw new DaoException(e);
    }
  }

  public List<Lecture> selectScheduleByLecturerId(int lecturerId, int limit, int offset)
      throws DaoException {
    List<Lecture> lectureList = new ArrayList<>();
    try (Connection connection = pool.getConnection();
        PreparedStatement ps = connection.prepareStatement(SQL_SELECT_SCHEDULE_BY_LECTURER_ID)) {
      ps.setInt(1, lecturerId);
      ps.setInt(2, limit);
      ps.setInt(3, offset);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Lecture current =
              new Lecture.Builder()
                  .withLectureId(rs.getInt(COLUMN_NAME_LECTURE_ID))
                  .withTimeStart(rs.getTimestamp("time_start").toInstant())
                  .withTimeEnd(rs.getTimestamp("time_end").toInstant())
                  .withCourseId(rs.getInt("course_id"))
                  .withDescription(rs.getString("description"))
                  .build();
          lectureList.add(current);
        }
      }
      logger.debug("list contains {}", lectureList.size());
    } catch (SQLException | PoolConnectionException e) {
      throw new DaoException(e);
    }
    return lectureList;
  }

  public int upsertCourseLecturer(int courseId, int lecturerId) throws DaoException {
    final int entryId;
    try (Connection connection = pool.getConnection();
        PreparedStatement ps =
            connection.prepareStatement(
                SQL_UPSERT_COURSE_LECTURER, Statement.RETURN_GENERATED_KEYS)) {
      ps.setInt(1, lecturerId);
      ps.setInt(2, courseId);
      ps.executeUpdate();
      try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          entryId = generatedKeys.getInt(1);
          logger.debug(
              "lecturer {} added to course {} : entryId is {}", lecturerId, courseId, entryId);
        } else {
          throw new SQLException("Creating user failed, no ID obtained.");
        }
      }
    } catch (SQLException | PoolConnectionException e) {
      throw new DaoException(e);
    }
    return entryId;
  }

  public int deleteCourseLecturer(int courseId, int lecturerId) throws DaoException {
    final int updatedRows;
    try (Connection connection = pool.getConnection();
        PreparedStatement ps = connection.prepareStatement(SQL_DELETE_COURSE_LECTURER)) {
      ps.setInt(1, courseId);
      ps.setInt(2, lecturerId);
      ps.executeUpdate();
      updatedRows = ps.getUpdateCount();
      logger.debug("{} records was deleted from course_lecturers table", updatedRows);
    } catch (SQLException e) {
      // if lecturer has constraint records on lectures table, SQLException with code 23503 will be
      // thrown.
      logger.debug(e);
      if (e.getSQLState().equals(FOREIGN_KEY_VIOLATION_CODE)) {
        return 0;
      } else throw new DaoException(e);
    } catch (PoolConnectionException e) {
      throw new DaoException(e);
    }
    return updatedRows;
  }

  public int countLecturerReadyCourses(int lecturerId) throws DaoException {
    try (Connection connection = pool.getConnection();
        PreparedStatement ps =
            connection.prepareStatement(SQL_COUNT_READY_COURSES_BY_LECTURER_ID)) {
      ps.setInt(1, lecturerId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(COLUMN_NAME_COUNT);
        } else throw new DaoException("ResultSet returns null");
      }

    } catch (SQLException | PoolConnectionException e) {
      throw new DaoException(e);
    }
  }

  public List<Course> selectAllCoursesByLecturerId(int lecturerId, int limit, int offset)
      throws DaoException {
    List<Course> courses;
    try (Connection connection = pool.getConnection();
        PreparedStatement ps = connection.prepareStatement(SQL_SELECT_ALL_COURSES_BY_LECTURER_ID)) {
      ps.setInt(1, lecturerId);
      ps.setInt(2, limit);
      ps.setInt(3, offset);
      try (ResultSet rs = ps.executeQuery()) {
        courses = parseResultSetToCoursesList(rs);
      }
    } catch (SQLException | PoolConnectionException e) {
      throw new DaoException(e);
    }
    return courses;
  }

  public List<Course> selectReadyCoursesByLecturerId(int lecturerId, int limit, int offset)
      throws DaoException {
    List<Course> courses;
    try (Connection connection = pool.getConnection();
        PreparedStatement ps =
            connection.prepareStatement(SQL_SELECT_READY_COURSES_BY_LECTURER_ID)) {
      ps.setInt(1, lecturerId);
      ps.setInt(2, limit);
      ps.setInt(3, offset);
      try (ResultSet rs = ps.executeQuery()) {
        courses = parseResultSetToCoursesList(rs);
      }
    } catch (SQLException | PoolConnectionException e) {
      throw new DaoException(e);
    }
    return courses;
  }

  public List<Course> selectCoursesAllReady(int limit, int offset) throws DaoException {
    List<Course> courses;
    try (Connection connection = pool.getConnection();
        PreparedStatement ps =
            connection.prepareStatement(SQL_SELECT_ALL_READY_COURSES_LIMIT_OFFSET)) {

      ps.setInt(1, limit);
      ps.setInt(2, offset);
      try (ResultSet rs = ps.executeQuery()) {
        courses = parseResultSetToCoursesList(rs);
      }
    } catch (SQLException | PoolConnectionException e) {
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

      try (ResultSet rs = ps.executeQuery()) {
        List<Lecturer> lecturers = new ArrayList<>();
        int previousLecturer = 0;
        while (rs.next()) {
          int currentLecturer = rs.getInt(COLUMN_NAME_LECTURER_ID);
          if (currentLecturer != previousLecturer) {
            Lecturer lecturer =
                new Lecturer(
                    rs.getInt(COLUMN_NAME_LECTURER_ID),
                    rs.getString(COLUMN_NAME_FIRST_NAME),
                    rs.getString(COLUMN_NAME_LAST_NAME));
            lecturers.add(lecturer);
            previousLecturer = currentLecturer;
          }
          Lecturer lecturer =
              new Lecturer(
                  rs.getInt(COLUMN_NAME_LECTURER_ID),
                  rs.getString(COLUMN_NAME_FIRST_NAME),
                  rs.getString(COLUMN_NAME_LAST_NAME));
          if (rs.getInt(COLUMN_NAME_LECTURE_ID) != 0) {
            Lecture current =
                new Lecture.Builder()
                    .withLectureId(rs.getInt(COLUMN_NAME_LECTURE_ID))
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
      }
    } catch (SQLException | PoolConnectionException e) {
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

  /**
   * Method provide updating next course's data: "title", "description", "capacity". when "capacity"
   * value changes the table "course_students" updates too.
   *
   * @param course
   * @return
   * @throws DaoException
   */
  public boolean updateCourseData(Course course) throws DaoException {
    try (Connection connection = pool.getConnection();
        PreparedStatement psSelectCourseCapacity =
            connection.prepareStatement(SQL_SELECT_COURSE_CAPACITY)) {
      // 1 step - select previous course capacity
      final int capacityPrevious;
      psSelectCourseCapacity.setInt(1, course.getId());
      try (ResultSet rs = psSelectCourseCapacity.executeQuery()) {
        if (rs.next()) {
          capacityPrevious = rs.getInt("capacity");
        } else throw new DaoException("can't read course capacity");
      }
      // start transaction
      connection.setAutoCommit(false);
      // decide what operation needed in course_student table: insert or delete
      String query =
          course.getCapacity() > capacityPrevious
              ? SQL_INSERT_INTO_COURSE_STUDENTS_TABLE
              : SQL_DELETE_FROM_COURSE_STUDENTS_TABLE;
      try (PreparedStatement psUpdateCourseTable =
              connection.prepareStatement(SQL_UPDATE_COURSE_TABLE_BY_COURSE_ID);
          PreparedStatement psUpdateCourseStudentsTable = connection.prepareStatement(query)) {
        psUpdateCourseTable.setString(1, course.getTitle());
        psUpdateCourseTable.setString(2, course.getDescription());
        psUpdateCourseTable.setInt(3, course.getCapacity());
        psUpdateCourseTable.setInt(4, course.getId());
        psUpdateCourseTable.executeUpdate();
        logger.debug("course {} data updated", course.getId());
        psUpdateCourseStudentsTable.setInt(1, course.getId());
        psUpdateCourseStudentsTable.setInt(2, 0); // empty spot
        for (int i = 0; i < Math.abs(course.getCapacity() - capacityPrevious); i++) {
          psUpdateCourseStudentsTable.executeUpdate();
        }
        logger.debug("Table course_students updated");
        connection.commit();
      } catch (SQLException e) {
        connection.rollback();
        connection.setAutoCommit(true);
        logger.warn("SQL Exception. transaction rollback");
        throw e;
      }
    } catch (PoolConnectionException | SQLException e) {
      throw new DaoException(e);
    }
    return true;
  }

  public Map<Lecturer, Integer> countLecturesInCourse(int courseId) throws DaoException {
    Map<Lecturer, Integer> lecturerMap = new HashMap<>();
    try (Connection connection = pool.getConnection();
        PreparedStatement ps =
            connection.prepareStatement(SQL_SELECT_COUNT_LECTURES_OF_COURSE_GROUP_BY_LECTURERS)) {
      ps.setInt(1, courseId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Lecturer lecturer =
              new Lecturer(
                  rs.getInt(COLUMN_NAME_LECTURER_ID),
                  rs.getString(COLUMN_NAME_FIRST_NAME),
                  rs.getString(COLUMN_NAME_LAST_NAME));
          lecturerMap.put(lecturer, rs.getInt(COLUMN_NAME_COUNT));
        }
      }
    } catch (PoolConnectionException | SQLException e) {
      throw new DaoException(e);
    }
    return lecturerMap;
  }

  /**
   * Method update "ready" column in "courses" table to set TRUE. It is important to exclude
   * activation the course having lecturers without lectures. Since the default isolation level of
   * db is READ COMMITTED, the method provide activation in two steps (without transaction). At
   * first "ready" column is set to TRUE. This step prevents lecturers to update current course.
   * Secondly the lecturesAmount for each lecturer involved in course is checked. If there were any
   * zero lectureAmount, "ready" state of course is updated again with FALSE value. In this
   * situation method returns false. To minimize possible problems with temporary false-activated
   * course it is recommended to use method {@code countLecturesInCourse} to check if lecturers have
   * no lectures.
   *
   * @param courseId id of current course.
   * @return true if course was activated, false otherwise
   * @throws DaoException - when attempt to provide work with db fails.
   */
  public boolean updateCourseOnSetReady(int courseId) throws DaoException {
    try (Connection connection = pool.getConnection();
        PreparedStatement psUpdate = connection.prepareStatement(SQL_UPDATE_COURSE_SET_READY);
        PreparedStatement psSelect =
            connection.prepareStatement(SQL_SELECT_COUNT_LECTURES_OF_COURSE_GROUP_BY_LECTURERS)) {

      psUpdate.setBoolean(1, true);
      psUpdate.setInt(2, courseId);
      psUpdate.executeUpdate();
      logger.debug("course {} activated", courseId);

      psSelect.setInt(1, courseId);
      try (ResultSet rs = psSelect.executeQuery()) {
        while (rs.next()) {
          if (rs.getInt(COLUMN_NAME_COUNT) == 0) {
            logger.debug("lectureAmount = 0 was found");
            psUpdate.setBoolean(1, false);
            psUpdate.setInt(2, courseId);
            psUpdate.executeUpdate();
            logger.debug("course {} deactivated", courseId);
            return false;
          }
        }
        // if we here means all OK
        return true;
      }
    } catch (PoolConnectionException | SQLException e) {
      throw new DaoException(e);
    }
  }

  public Map<String, Integer> countCoursesAndLecturesAndLecturersAndStudents() throws DaoException {
    Map<String, Integer> pairs = new HashMap<>();
    try (Connection connection = pool.getConnection();
        Statement ps = connection.createStatement();
        ResultSet rs = ps.executeQuery(SQL_SELECT_TABLE_COUNTS)) {
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

  public int insertLecture(Lecture lecture) throws DaoException {
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
      final int entryId;
      try (ResultSet rs = psSelectEntry.executeQuery()) {
        if (rs.next()) {
          entryId = rs.getInt("entry_id");
        } else {
          throw new DaoException(
              String.format(
                  "Can't Add lecture: no entry_id with course_id [%d] and lecturer_id [%d] found",
                  lecture.getCourseId(), lecture.getLecturer().getId()));
        }
      }
      // insert lecture
      psInsert.setInt(1, entryId);
      psInsert.setTimestamp(2, Timestamp.from(lecture.getStartTime()));
      psInsert.setTimestamp(3, Timestamp.from(lecture.getEndTime()));
      psInsert.setString(4, lecture.getDescription());
      psInsert.executeUpdate();
      try (ResultSet generatedKeys = psInsert.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          lectureId = generatedKeys.getInt(1);
        } else {
          throw new SQLException("Inserting lecture failed, no ID obtained.");
        }
      }
    } catch (SQLException | PoolConnectionException e) {
      throw new DaoException(e);
    }
    return lectureId;
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
                rs.getInt(COLUMN_NAME_LECTURER_ID),
                rs.getString(COLUMN_NAME_FIRST_NAME),
                rs.getString(COLUMN_NAME_LAST_NAME));
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
                rs.getInt(COLUMN_NAME_LECTURER_ID),
                rs.getString(COLUMN_NAME_FIRST_NAME),
                rs.getString(COLUMN_NAME_LAST_NAME));
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
