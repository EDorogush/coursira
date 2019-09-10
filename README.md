
# Introduction

CoursIra is study project within the Java Web Development training course. The application is intended as study courses manager

**Three roles are possible:** 
- Unauthorised user;
- Student;
- Lecturer.

**Unauthorised user is allowed to:**
- look through available courses details (title, description, schedule, lecturers are involved in course, amount of students are subscribed to course, full amount of students are able to subscribe the course);
- change localization RU/EN;
- log in;
- sign up.

**Students are available all unauthorised users are allowed to do and also :**
- look through subscribed courses;
- to get information about possibility to subscribe new course (application do now allow subscribe course if new course's schedule conflicts with current student schedule);
- to subscribe/unsubscribe courses;
- look throw personal data (photo, name, email, age, interests, organisation, schedule);
- update personal data (photo, name, age, interests, organisation);
- log out.

**Lecturers are available all unauthorised users are allowed to do and also :**
- look through courses they are involved in;
- create new courses;
- update unready courses: 
     - Update course's title, description, student's amount;
     - Invite other lecturers to take part in new course;
     - Delete other lecturers who was invited but do not take part in course (do not have any lecture);
     - add/update/delete new lectures to course (application do not allow adding new lecture which start/end time conflicts with lecturer schedule or current course schedule);
    - activate the course (all updates will be finished, users will get possibility to look through and subscribe the course);
- look throw personal data(photo, name, email, age, interests, organisation, schedule);
- update personal data (photo, name, age, interests, organisation);
- log out.

## Features
- Stateless application;
- Static content support;
- Xss attack defense;
- Form resubmission defence;
- Mail sending support;


# Getting Started

## Prerequisites:

JDK: [Version 11 or higher](https://openjdk.java.net).\
[PostgreSQL 11.3](https://www.postgresql.org) \
SMTP credentials.
 
## Jenkins Credentials:
use [Jenkins service](http://epbyminw7566:8081) to deploy application
#### as administrator: 
login: administrator\
password: password
#### as developer: 
login: developer\
password: password

 
## How to start PostgreSQL 

Database is installed entirely inside the application folder. 
Please, follow the instructions:

### Init database file structure in current application directory with the command ([See details](https://www.postgresql.org/docs/11/app-initdb.html)):
```
initdb --locale=C -E UTF-8 postgres
```

### Start the server in foreground with the command ([See details](https://www.postgresql.org/docs/11/app-postgres.html))
```
postgres -D postgres
```

### Create database with name coursiradb (IMPORTANT : if You use Windows OS here and below use " instead of '): 
```
psql -d postgres -c 'create database coursiradb'
```
 
### Create user with name coursirauser: 
```
psql -d postgres  -c 'create user coursirauser'
```

### Grant privileges to coursirauser: 
```
psql -d postgres -c 'grant all privileges on database coursiradb to coursirauser'
```

### Create application schemas on coursiradb  
```
psql -d coursiradb -U coursirauser -f schema.sql
``` 
### Insert dbInitData  
```
psql -d coursiradb -U coursirauser -f dbInitData.sql
``` 
#### You may also need following commands:
##### Start db in background:
```
pg_ctl -D postgres -l postgres.log start
```

##### Find out which program is running on port 5432
```
netstat -vanp tcp | grep 5432
```

##### Find out details of program by it's pid
```
ps aux | grep <pid>
```
## How to get SMTP credential:
To allow this application to send email via your Gmail box you need to get App password form [Gmail Security Service](https://support.google.com/accounts/answer/185833?p=InvalidSecondFactor).
After getting the App password, export Email address and password as environment variables named "GMAIL_ADDRESS" and "GMAIL_PASSWORD"  respectively   

## Start Application
Use the command:
```
./gradlew appRun
```
 

