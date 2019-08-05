INSERT INTO users(email, password, firstname, lastname, role, organization, age, interests)
VALUES ('', '', '', '', 'ANONYMOUS', NULL, NULL, NULL),
       ('dorogushelena+snow@gmail.com', '$2a$10$wiEIavGl5U6sQUnnmffMPeBpzh9VDA9tVgN0MYpsF6o3sEWQ13Zv6', 'Snow', 'White', 'STUDENT',
        'school', 45, 'chemistry'),
       ('dorogushelena+redhat@gmail.com', '$2a$10$wiEIavGl5U6sQUnnmffMPeBpzh9VDA9tVgN0MYpsF6o3sEWQ13Zv6', 'Red', 'Hat', 'STUDENT',
        'school', 20, 'biology'),
       ('dorogushelena+pinokkio@gmail.com', '$2a$10$wiEIavGl5U6sQUnnmffMPeBpzh9VDA9tVgN0MYpsF6o3sEWQ13Zv6', 'Pinokkio', 'Wood',
        'STUDENT', 'BSU', 120, 'math'),
       ('dorogushelena+muromets@gmail.com', '$2a$10$wiEIavGl5U6sQUnnmffMPeBpzh9VDA9tVgN0MYpsF6o3sEWQ13Zv6', 'Илья', 'Муромец',
        'STUDENT', 'school', 30, 'квантовая физика'),
       ('dorogushelena+kolobok@gmail.com', '$2a$10$wiEIavGl5U6sQUnnmffMPeBpzh9VDA9tVgN0MYpsF6o3sEWQ13Zv6', 'Kolobok', 'Baker', 'STUDENT',
        'school', 1, 'chemistry'),
       ('dorogushelena+cinderella@gmail.com', '$2a$10$wiEIavGl5U6sQUnnmffMPeBpzh9VDA9tVgN0MYpsF6o3sEWQ13Zv6', '', '', 'STUDENT',
        'school', 35, 'math'),
       ('dorogushelena+samiam@gmail.com', '$2a$10$vH0HPvt/3bLlEA/fF9VtlO9NxDg8xKZ1Yw2fnSDmYBqZXsnP4H9gO', 'I-am-Sam', 'Sam-I-am',
        'LECTURER', 'Epam', '45', 'computer science'),
       ('dorogushelena+scroodge@gmail.com', '$2a$10$vH0HPvt/3bLlEA/fF9VtlO9NxDg8xKZ1Yw2fnSDmYBqZXsnP4H9gO', 'Scroodge', 'McDuck',
        'LECTURER', 'Epam', '45', 'computer science'),
       ('dorogushelena+aibolit@gamil.com', '$2a$10$vH0HPvt/3bLlEA/fF9VtlO9NxDg8xKZ1Yw2fnSDmYBqZXsnP4H9gO', 'Айболит', 'Доктор',
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



