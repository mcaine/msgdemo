# Demo Social Networking App

A Twitter style app using java 8.

## Building

mvn clean package

## Running

java -jar target/msgdemo-0.0.1-SNAPSHOT.jar

Application will run on port 8080.

## API

### Posting

POST a message to the URL localhost:8080/messageApi/[USER_NAME]/createMessage

eg
curl -X POST -H "Content-Type: text/plain" -d "Here's a message" http://localhost:8080/messageApi/Mike/createMessage

A representation of the submitted message is returned.

### Wall

GET localhost:8080/messageApi/[USER_NAME]/wall to get a JSON array of messages posted by that user. 

eg 
curl -X GET http://localhost:8080/messageApi/Mike/wall

### Following

POST to localhost:8080/messageApi/[USER_NAME]/follow/[USER_TO_FOLLOW] to follow a User's messages.

eg 
curl -X POST http://localhost:8080/messageApi/Mike/follow/Bobby

### Timeline

GET localhost:8080/messageApi/[USER_NAME]/timeline to return a JSON array of messages submitted by the Users you follow.
 
eg
curl -X GET http://localhost:8080/messageApi/Mike/timeline
 
 