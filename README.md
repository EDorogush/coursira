CoursIra is study project within the Java Web Development training course. The application is intended as study courses manager. 

# Getting Started

## Prerequisites:

JDK: [Version 11 or higher](https://openjdk.java.net).\
[PostgreSQL 11.3](https://www.postgresql.org) \
SMTP credentials.

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

### Create database with name coursiradb: 
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

#### You may also need following commands:
##### Start db in background:
```
 -D postgres -l postgres.log start
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
 

