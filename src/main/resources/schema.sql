SET CLIENT_ENCODING TO 'utf8';

CREATE SEQUENCE users_id_seq
    MINVALUE 0;

ALTER SEQUENCE users_id_seq OWNER TO coursirauser;

CREATE TABLE users
(
    id                       integer DEFAULT nextval('users_id_seq'::regclass) NOT NULL
        CONSTRAINT users_pk
            PRIMARY KEY,
    email                    varchar(128)                                      NOT NULL,
    password                 varchar(128)                                      NOT NULL,
    firstname                varchar(128)                                      NOT NULL,
    lastname                 varchar(128)                                      NOT NULL,
    role                     varchar(16),
    organization             varchar(128),
    age                      integer,
    interests                varchar(128),
    photo                    bytea,
    registration_code        varchar(36),
    registration_expire_date timestamp
);

ALTER TABLE users
    OWNER TO coursirauser;

CREATE UNIQUE INDEX users_email_uindex
    ON users (email);

CREATE TABLE sessions
(
    id                           varchar(128) NOT NULL
        CONSTRAINT sessions_pk
            PRIMARY KEY,
    expire_date                  timestamp    NOT NULL,
    user_id                      integer
        CONSTRAINT sessions_users_id_fk
            REFERENCES users,
    language                     varchar(8),
    zone_offset_of_total_seconds integer DEFAULT 0
);

ALTER TABLE sessions
    OWNER TO coursirauser;

CREATE TABLE courses
(
    course_id   serial                     NOT NULL
        CONSTRAINT course_pk
            PRIMARY KEY,
    title       varchar(128)               NOT NULL,
    description varchar(128) DEFAULT 'something'::character varying,
    capacity    integer                    NOT NULL,
    ready       boolean      DEFAULT FALSE NOT NULL
);

ALTER TABLE courses
    OWNER TO coursirauser;

CREATE TABLE course_students
(
    entry_id   serial  NOT NULL
        CONSTRAINT course_students_pk
            PRIMARY KEY,
    course_id  integer
        CONSTRAINT course_students_courses_course_id_fk
            REFERENCES courses,
    student_id integer NOT NULL
        CONSTRAINT course_students_users_id_fk
            REFERENCES users
);

ALTER TABLE course_students
    OWNER TO coursirauser;

CREATE TABLE course_lecturers
(
    entry_id    serial  NOT NULL
        CONSTRAINT course_lecturers_pk
            PRIMARY KEY,
    lecturer_id integer NOT NULL
        CONSTRAINT course_lecturers_users_id_fk
            REFERENCES users,
    course_id   integer
        CONSTRAINT course_lecturers_courses_course_id_fk
            REFERENCES courses
);

ALTER TABLE course_lecturers
    OWNER TO coursirauser;

CREATE UNIQUE INDEX course_lecturers_lecturer_id_course_id_uindex
    ON course_lecturers (lecturer_id, course_id);

CREATE TABLE lectures
(
    lecture_id         serial NOT NULL
        CONSTRAINT course_schedule_pk
            PRIMARY KEY,
    course_lecturer_id integer
        CONSTRAINT lectures_course_lecturers_entry_id_fk
            REFERENCES course_lecturers,
    time_start         timestamp,
    time_end           timestamp,
    description        varchar(128)
);

ALTER TABLE lectures
    OWNER TO coursirauser;

