

## init database file structure in directory
```
initdb --locale=C -E UTF-8 postgres
```

## start in foreground
```
postgres -D postgres
```

## start in background
```
pg_ctl -D postgres -l postgres.log start
```

## find out which program is running on port 5432
```
netstat -vanp tcp | grep 5432
```

## find out details of program by it's pid
```
ps aux | grep <pid>
```

## database init
```
schema.sql 
```
