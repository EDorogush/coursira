INSERT INTO users(email, password, firstname, lastname, role, organization, age, interests)
VALUES ('', '', '', '', 'ANONYMOUS', NULL, NULL, NULL),
       ('snow@bsu.by', '$2a$10$wiEIavGl5U6sQUnnmffMPeBpzh9VDA9tVgN0MYpsF6o3sEWQ13Zv6', 'Snow', 'White', 'STUDENT',
        'school', 45, 'chemistry'),
       ('redhat@bsu.by', '$2a$10$wiEIavGl5U6sQUnnmffMPeBpzh9VDA9tVgN0MYpsF6o3sEWQ13Zv6', 'Red', 'Hat', 'STUDENT',
        'school', 20, 'biology'),
       ('pinokkio@bsu.by', '$2a$10$wiEIavGl5U6sQUnnmffMPeBpzh9VDA9tVgN0MYpsF6o3sEWQ13Zv6', 'Pinokkio', 'Wood',
        'STUDENT', 'BSU', 120, 'math'),
       ('muromets@bsu.by', '$2a$10$wiEIavGl5U6sQUnnmffMPeBpzh9VDA9tVgN0MYpsF6o3sEWQ13Zv6', 'Илья', 'Муромец',
        'STUDENT', 'school', 30, 'квантовая физика'),
       ('kolobok@bsu.by', '$2a$10$wiEIavGl5U6sQUnnmffMPeBpzh9VDA9tVgN0MYpsF6o3sEWQ13Zv6', 'Kolobok', 'Baker', 'STUDENT',
        'school', 1, 'chemistry'),
       ('cinderella@bsu.by', '$2a$10$wiEIavGl5U6sQUnnmffMPeBpzh9VDA9tVgN0MYpsF6o3sEWQ13Zv6', '', '', 'STUDENT',
        'school', 35, 'math'),

       ('first@epam.by', '$2a$10$vH0HPvt/3bLlEA/fF9VtlO9NxDg8xKZ1Yw2fnSDmYBqZXsnP4H9gO', 'I-am-Sam', 'Sam-I-am',
        'LECTURER', 'Epam', '45', 'computer science'),
       ('scroodge@epam.by', '$2a$10$vH0HPvt/3bLlEA/fF9VtlO9NxDg8xKZ1Yw2fnSDmYBqZXsnP4H9gO', 'Scroodge', 'McDuck',
        'LECTURER', 'Epam', '45', 'computer science'),
       ('aibolit@epam.by', '$2a$10$vH0HPvt/3bLlEA/fF9VtlO9NxDg8xKZ1Yw2fnSDmYBqZXsnP4H9gO', 'Айболит', 'Доктор',
        'LECTURER', 'Epam', '45', 'медицина');

-- insert courses

INSERT INTO courses (title, description, capacity, ready)
VALUES ('OOP', 'basic knowledge about OOP principles', 3, TRUE),
       ('Java:core', 'all you need to know about Java: core', 3, TRUE),
       ('Java:Web', 'all you need to know about Java: web', 3, TRUE),
       ('Java:Collections', 'all you need to know about Java: collections', 5, TRUE);

INSERT INTO course_students (course_id, student_id)
VALUES (1, 0),
       (1, 0),
       (1, 0),
       (2, 0),
       (2, 0),
       (2, 0),
       (3, 0),
       (3, 0),
       (3, 0),
       (4, 0),
       (4, 0),
       (4, 0),
       (4, 0),
       (4, 0);

INSERT INTO course_lecturers (course_id, lecturer_id)
VALUES (1, 7),
       (1, 8),
       (2, 9),
       (3, 8),
       (3, 7),
       (4, 8);


-- courses 1 and 2 crosses

INSERT INTO lectures (course_lecturer_id, time_start, time_end, description)
-- course1, lector 1
VALUES (1, '2019-09-01 9:00:00', '2019-09-01 10:00:00', 'Lection1.OOP'),
       (1, '2019-09-02 9:00:00', '2019-09-02 10:00:00', 'Lection2.OOP'),
       (1, '2019-09-03 9:00:00', '2019-09-03 10:00:00', 'Lection3.OOP'),
       (1, '2019-09-04 9:00:00', '2019-09-04 10:00:00', 'Lection4.OOP'),
       (1, '2019-09-05 9:00:00', '2019-09-05 10:00:00', 'Lection5.OOP'),
-- course1, lector 2
       (2, '2019-09-06 9:00:00', '2019-09-06 10:00:00', 'Lection6.OOP'),
       (2, '2019-09-07 9:00:00', '2019-09-07 10:00:00', 'Lection7.OOP'),
-- course2, lector3 cross with course1
       (3, '2019-09-01 9:00:00', '2019-09-01 10:00:00', 'Lection1.Java:core'),
       (3, '2019-09-02 9:00:00', '2019-09-02 10:00:00', 'Lection2.Java:core'),
       (3, '2019-09-03 9:00:00', '2019-09-03 10:00:00', 'Lection3.Java:core'),
       (3, '2019-09-04 9:00:00', '2019-09-04 10:00:00', 'Lection4.Java:core'),
       (3, '2019-09-05 9:00:00', '2019-09-05 10:00:00', 'Lection5.Java:core'),
-- course3, lector1
       (5, '2019-09-01 13:00:00', '2019-09-01 15:00:00', 'Lection1.Java:Web'),
       (5, '2019-09-02 13:00:00', '2019-09-02 15:00:00', 'Lection2.Java:Web'),
       (5, '2019-09-03 13:00:00', '2019-09-03 15:00:00', 'Lection3.Java:Web'),
       (5, '2019-09-04 13:00:00', '2019-09-04 15:00:00', 'Lection4.Java:Web'),
       (5, '2019-09-05 13:00:00', '2019-09-05 15:00:00', 'Lection5.Java:Web'),
-- course3, lector 2
       (4, '2019-09-06 13:00:00', '2019-09-06 15:00:00', 'Lection6.Java:Web'),
       (4, '2019-09-07 13:00:00', '2019-09-07 15:00:00', 'Lection7.Java:Web'),
-- course4, lector2
       (6, '2019-09-10 13:00:00', '2019-09-10 15:00:00', 'Lection1.Java:Collections'),
       (6, '2019-09-11 13:00:00', '2019-09-11 15:00:00', 'Lection2.Java:Collections'),
       (6, '2019-09-12 13:00:00', '2019-09-12 15:00:00', 'Lection3.Java:Collections'),
       (6, '2019-09-13 13:00:00', '2019-09-13 15:00:00', 'Lection4.Java:Collections'),
       (6, '2019-09-14 13:00:00', '2019-09-14 15:00:00', 'Lection5.Java:Collections');


-- update course_students
UPDATE course_students
SET student_id = 1
WHERE entry_id =
      (SELECT entry_id FROM course_students WHERE course_id = 2 LIMIT 1);

UPDATE course_students
SET student_id = 2
WHERE entry_id =
      (SELECT entry_id FROM course_students WHERE course_id = 2 LIMIT 1);

UPDATE course_students
SET student_id = 2
WHERE entry_id =
      (SELECT entry_id FROM course_students WHERE course_id = 4 LIMIT 1);

UPDATE course_students
SET student_id = 3
WHERE entry_id =
      (SELECT entry_id FROM course_students WHERE course_id = 1 LIMIT 1);

UPDATE course_students
SET student_id = 3
WHERE entry_id =
      (SELECT entry_id FROM course_students WHERE course_id = 3 LIMIT 1);


UPDATE course_students
SET student_id = 4
WHERE entry_id =
      (SELECT entry_id FROM course_students WHERE course_id = 1 LIMIT 1);

UPDATE course_students
SET student_id = 4
WHERE entry_id =
      (SELECT entry_id FROM course_students WHERE course_id = 3 LIMIT 1);

UPDATE course_students
SET student_id = 4
WHERE entry_id =
      (SELECT entry_id FROM course_students WHERE course_id = 4 LIMIT 1);


UPDATE course_students
SET student_id = 6
WHERE entry_id =
      (SELECT entry_id FROM course_students WHERE course_id = 1 LIMIT 1);


UPDATE course_students
SET student_id = 6
WHERE entry_id =
      (SELECT entry_id FROM course_students WHERE course_id = 3 LIMIT 1);



-- select all courses


-- select lecturer courses

DELETE
FROM sessions
WHERE expire_date < TIMESTAMP '2004-10-19 10:23:54+02';


-- coursira stat
SELECT (
           SELECT count(course_id)
           FROM courses
           WHERE ready
       ) AS courses,
       (
           SELECT count(lecture_id)
           FROM lectures
                    JOIN course_lecturers cl ON lectures.course_lecturer_id = cl.entry_id
                    JOIN courses c ON cl.course_id = c.course_id
           WHERE c.ready
       ) AS lectures,
       (
           SELECT count(email)
           FROM users
           WHERE role = 'LECTURER'
       ) AS lecturers,
       (
           SELECT count(email)
           FROM users
           WHERE role = 'STUDENT'
       ) AS students;


-- count student courses
SELECT count(course_id)
FROM course_students
WHERE student_id = 10;

-- count lecturer active courses
SELECT count(cl.course_id)
FROM course_lecturers cl
         JOIN courses c ON cl.course_id = c.course_id
WHERE lecturer_id = 7
  AND c.ready;


-- select student courses
SELECT c.course_id,
       c.title,
       c.description,
       c.capacity,
       count(cs.student_id) AS student_amount,
       cl.lecturer_id,
       u.firstname,
       u.lastname
FROM courses c
         LEFT JOIN course_lecturers cl ON c.course_id = cl.course_id
         LEFT JOIN course_students cs ON c.course_id = cs.course_id AND student_id != 0
         LEFT JOIN users u ON cl.lecturer_id = u.id

WHERE c.course_id IN
      (
          SELECT course_id
          FROM course_students
          WHERE student_id = 2
          ORDER BY course_id ASC
          LIMIT 3 OFFSET 0
      )
GROUP BY c.course_id, cl.lecturer_id, u.firstname, u.lastname
ORDER BY course_id ASC;


-- check if exists in student List
SELECT exists(
               SELECT c.course_id
               FROM courses c
                        JOIN course_students cs ON c.course_id = cs.course_id
               WHERE student_id = 2
                 AND c.course_id = 5);


-- select ALL lecturer courses
SELECT c.course_id,
       c.title,
       c.description,
       c.capacity,
       c.ready,
       count(cs.student_id) AS student_amount,
       cl.lecturer_id,
       u.firstname,
       u.lastname
FROM courses c
         LEFT JOIN course_lecturers cl ON c.course_id = cl.course_id
         LEFT JOIN course_students cs ON c.course_id = cs.course_id AND student_id != 0
         LEFT JOIN users u ON cl.lecturer_id = u.id
WHERE c.course_id IN
      (
          SELECT course_id
          FROM course_lecturers
          WHERE lecturer_id = 7
          ORDER BY course_id ASC
          LIMIT 10 OFFSET 0
      )
GROUP BY c.course_id, cl.lecturer_id, u.firstname, u.lastname
ORDER BY course_id ASC;


-- select ready lecturer courses
SELECT c.course_id,
       c.title,
       c.description,
       c.capacity,
       c.ready,
       count(cs.student_id) AS student_amount,
       cl.lecturer_id,
       u.firstname,
       u.lastname
FROM courses c
         LEFT JOIN course_lecturers cl ON c.course_id = cl.course_id
         LEFT JOIN course_students cs ON c.course_id = cs.course_id AND student_id != 0
         LEFT JOIN users u ON cl.lecturer_id = u.id
WHERE c.course_id IN
      (
          SELECT cl.course_id
          FROM course_lecturers cl
          WHERE cl.lecturer_id = 7
            AND c.ready
          ORDER BY course_id ASC
          LIMIT 10 OFFSET 0
      )
GROUP BY c.course_id, cl.lecturer_id, u.firstname, u.lastname
ORDER BY course_id ASC;


-- check if exists in LecturerList
SELECT exists(
               SELECT c.course_id
               FROM courses c
                        JOIN course_lecturers cl ON c.course_id = cl.course_id
               WHERE lecturer_id = 7
                 AND c.course_id = 4);


--select  all ready courses
SELECT c.course_id,
       c.title,
       c.description,
       c.capacity,
       count(cs.student_id) AS student_amount,
       cl.lecturer_id,
       u.firstname,
       u.lastname
FROM courses c
         LEFT JOIN course_lecturers cl ON c.course_id = cl.course_id
         LEFT JOIN course_students cs ON c.course_id = cs.course_id AND student_id != 0
         LEFT JOIN users u ON cl.lecturer_id = u.id
WHERE ready
  AND c.course_id IN
      (
          SELECT course_id
          FROM courses
          ORDER BY course_id ASC
          LIMIT 10 OFFSET 0
      )
GROUP BY c.course_id, cl.lecturer_id, u.firstname, u.lastname
ORDER BY course_id ASC;

-- select courseDetails by course id(lectures)
-- without paging
SELECT l.lecture_id,
       l.description,
       l.time_start,
       l.time_end,
       cl.lecturer_id,
       u.firstname,
       u.lastname,
       c.title,
       c.description,
       c.capacity,
       c.ready,
       count(cs.student_id) AS student_amount
FROM lectures l
         RIGHT JOIN course_lecturers cl ON l.course_lecturer_id = cl.entry_id
         JOIN users u ON cl.lecturer_id = u.id
         JOIN courses c ON cl.course_id = c.course_id
         LEFT JOIN course_students cs ON c.course_id = cs.course_id AND student_id != 0
WHERE cl.course_id = 15
GROUP BY l.lecture_id, cl.lecturer_id, u.firstname, u.lastname, c.title, c.description, c.capacity, c.ready
ORDER BY time_start ASC;

-- with paging
SELECT l.lecture_id,
       l.description        AS lecture_description,
       l.time_start,
       l.time_end,
       cl.lecturer_id,
       u.firstname,
       u.lastname,
       c.title,
       c.description        AS course_description,
       c.capacity,
       c.ready,
       count(cs.student_id) AS student_amount
FROM lectures l
         JOIN course_lecturers cl ON l.course_lecturer_id = cl.entry_id
         JOIN users u ON cl.lecturer_id = u.id
         JOIN courses c ON cl.course_id = c.course_id
         LEFT JOIN course_students cs ON c.course_id = cs.course_id AND student_id != 0
WHERE cl.course_id = 1
GROUP BY l.lecture_id, cl.lecturer_id, u.firstname, u.lastname, c.title, c.description, c.capacity, c.ready
ORDER BY time_start ASC
LIMIT 10 OFFSET 0;


SELECT exists(SELECT 1 FROM courses WHERE course_id = -10);
SELECT exists(
               SELECT 1 FROM courses WHERE course_id = -10);


-- select student schedule with offset and limit
SELECT l.lecture_id,
       l.description,
       l.time_start,
       l.time_end,
       cl.course_id,
       c.title,
       cl.lecturer_id,
       u.firstname,
       u.lastname
FROM lectures l
         JOIN course_lecturers cl ON l.course_lecturer_id = cl.entry_id
         JOIN course_students cs ON cl.course_id = cs.course_id
         JOIN courses c ON cl.course_id = c.course_id
         JOIN users u ON cl.lecturer_id = u.id
WHERE cs.student_id = 2
ORDER BY time_start ASC
LIMIT 5 OFFSET 0;


-- select lecturer schedule
SELECT l.lecture_id,
       l.description,
       l.time_start,
       l.time_end,
       cl.course_id,
       c.title
FROM lectures l
         JOIN course_lecturers cl ON l.course_lecturer_id = cl.entry_id
         JOIN courses c ON cl.course_id = c.course_id
WHERE cl.lecturer_id = 7
  AND c.ready
ORDER BY time_start ASC;


-- select course schedule
SELECT l.lecture_id,
       l.time_start,
       l.time_end,
       cl.course_id
FROM lectures l
         JOIN course_lecturers cl ON l.course_lecturer_id = cl.entry_id
WHERE cl.course_id = 3
ORDER BY time_start ASC;


-- select cross time
(SELECT l.lecture_id,
        l.time_start AS time
 FROM lectures l
          JOIN course_lecturers cl ON l.course_lecturer_id = cl.entry_id
          JOIN course_students cs ON cl.course_id = cs.course_id
 WHERE cs.student_id = 1
)
UNION
(SELECT l.lecture_id,
        l.time_end AS time
 FROM lectures l
          JOIN course_lecturers cl ON l.course_lecturer_id = cl.entry_id
          JOIN course_students cs ON cl.course_id = cs.course_id
 WHERE cs.student_id = 1
)
ORDER BY time ASC;


(SELECT l.lecture_id,
        l.time_start AS time
 FROM lectures l
          JOIN course_lecturers cl ON l.course_lecturer_id = cl.entry_id
 WHERE cl.lecturer_id = 7
)
UNION
(SELECT l.lecture_id,
        l.time_end AS time
 FROM lectures l
          JOIN course_lecturers cl ON l.course_lecturer_id = cl.entry_id
 WHERE cl.lecturer_id = 7
)
ORDER BY time ASC;


-- remove course from student schedule
UPDATE course_students
SET student_id = 0
WHERE student_id = 1
  AND course_id = 2;

-- add course to student schedule
UPDATE course_students
SET student_id = 1
WHERE entry_id =
      (SELECT entry_id FROM course_students WHERE course_id = 2 AND student_id = 0 LIMIT 1);


SELECT *
FROM users
WHERE registration_expire_date < TIMESTAMP '2019-07-19 08:40:54.979000';



SELECT count(course_id)
FROM (SELECT c.course_id
      FROM courses c
               JOIN course_lecturers cl ON c.course_id = cl.course_id
      WHERE ready
      GROUP BY c.course_id
     ) AS course;


SELECT l.lecture_id,
       l.description,
       l.time_start,
       l.time_end,
       cl.lecturer_id,
       u.firstname,
       u.lastname,
       c.title,
       c.description,
       c.capacity,
       c.ready,
       count(cs.student_id) AS student_amount
FROM lectures l
         RIGHT JOIN course_lecturers cl ON l.course_lecturer_id = cl.entry_id
         JOIN users u ON cl.lecturer_id = u.id
         JOIN courses c ON cl.course_id = c.course_id
         LEFT JOIN course_students cs ON c.course_id = cs.course_id AND student_id != 0
WHERE cl.course_id = 8
GROUP BY l.lecture_id, cl.lecturer_id, u.firstname, u.lastname, c.title, c.description, c.capacity, c.ready
ORDER BY time_start ASC;



-- exists(\n" +
--       "         SELECT l.lecture_id\n" +
--       "         FROM lectures l\n" +
--       "                JOIN course_lecturers cl ON l.course_lecturer_id = cl.entry_id\n" +
--       "                JOIN courses c ON cl.course_id = c.course_id\n" +
--       "         WHERE lecturer_id = ?\n" +
--       "           AND c.course_id = ?" +
--       "           AND NOT c.ready);";

SELECT l.lecture_id
FROM lectures l
         JOIN course_lecturers cl ON l.course_lecturer_id = cl.entry_id
         JOIN courses c ON cl.course_id = c.course_id
WHERE lecture_id = 20
  AND lecturer_id = 8
  AND NOT c.ready


SELECT count(lecture_id)
FROM lectures
         JOIN course_lecturers ON lectures.course_lecturer_id = course_lecturers.entry_id
WHERE course_id = 1
GROUP BY lecturer_id



INSERT INTO course_lecturers (lecturer_id, course_id)
VALUES (7, 2)
ON CONFLICT (lecturer_id,course_id)
    DO NOTHING

SELECT users.id, s.id
FROM users
         LEFT JOIN sessions s ON users.id = s.user_id
WHERE users.id = 2


SELECT EXISTS(SELECT 1 FROM users WHERE id = 7 AND role = 'LECTURER');
