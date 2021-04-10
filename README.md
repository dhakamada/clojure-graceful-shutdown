# Clojure graceful shutdown

It's just a simple code to exemplify how to grace shutdown a web application and a worker

It's example doesn't have any unit or integration test =/

##### Prerequisite
- Leiningen
- Java

## Web

##### Run
``` 
$ lein with-profile web run
```
##### Run via JAR
Note: using jetty embedded mode
```
$ lein with-profile web uberjar
$ java -jar target/clojure-graceful-shutdown-web-standalone.jar
```

##### Test graceful shutdown manually

When you start web server, you will see the `PID` in application log.
Thereafter, you should request to `http://localhost:8080` via your preferred browser 
and after that, you need to send the `SIGTERM` signal before the response.

Expected:
 1. web server should finish the process request/response
 2. the response should return "Graceful Shutdown in Clojure"
 3. shutdown the server automatically

Note: Sigterm command via terminal (Unix)
```
$ kill -SIGTERM <PID>
OR
$ kill -15 <PID>
```

## Worker

<b>Prerequisite: running a localstack service</b>

##### Create queue
- localstack

```
aws \
  --endpoint-url 'http://localhost:4576' \
  sqs create-queue \
  --queue-name 'GracefulShutdownQueue' \
  --attributes RedrivePolicy.maxReceiveCount=1
```

##### Run local
``` 
$ lein with-profile worker run
```
##### Run via JAR
Note: using jetty embedded mode
```
$ lein with-profile worker uberjar
$ java -jar target/clojure-graceful-shutdown-worker-standalone.jar
```

##### Test graceful shutdown manually

When you start workers, you will see the `PID` in application log.
Thereafter, you should send messages (follow the format bellow) on your terminal 
and after that, you need to send the `SIGTERM` signal before the process the message.

Expected:
 1. pause to consume messages on channel
 2. wait the limited time (default 30s)
 3. shutdown the process automatically

Note: Sigterm command via terminal (Unix)
```
$ kill -SIGTERM <PID>
OR
$ kill -15 <PID>
```

<b>Send a message</b>
 
Note: don't change the format message, just the `value number`
```
aws \
  --endpoint-url 'http://localstack:4576' \
  sqs send-message \
  --queue-url 'http://localstack:4576/queue/GracefulShutdownQueue' \
  --message-body '{"value": 0}}'
```
