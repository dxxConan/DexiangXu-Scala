## Project description
To play with Akka, Kafka, Scala, ElasticSearch and REST (micro)service. To build a data processing pipeline illustrated below.

![1](https://cloud.githubusercontent.com/assets/27907550/25785490/595d7f20-3347-11e7-9031-8d467cd61dcc.jpg)

## Installation
[Docker](https://www.docker.com/)

[Kafka](https://kafka.apache.org/quickstart)

## Tasks
### Phase 1
Implements the first part of the data pipeline. Read the data from CSV file and send it to Kafka topic by producer. And check if the topic has been set up properly by build a consumer subscriped to that topic.

### Phase 2
Set up a commitable source subscribe to the configured topic in Phase 1. And do some data process by micro service(optional) asyncronously. Use another producer to send the processed data to a new topic by Akka flow.

### Phase 3
Built Akka stream which listens the data on Kafka consumer of the topic created in phase 2. Use REST Api to send data to Elastic search by connection flow based on HttpRequest. And notice that the data we got in phase 2 is in String format, we need to transfer it into JSON object before sending it to elastic search

### Phase 4
Implements a REST service based on Elestic search which will provide user methods to search the data stored in the CSV file. Use Akka HTTP DSL listen to the incoming request from users,query data from elastic search and send back to user. 

## Usage and test
* Start each project by sbt run
* The csv file path is hard coded in phase 1
* Producer should run before start loading to consumer
* Can check the Kafka topics status by [Kafka tool](http://www.kafkatool.com/)
* Or use command line bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning. 
* For docker the ip is set to be 192.168.99.100 
* Kafka port: 9092, ElasticSearch port: 9200
* Http request for elastic search port: locahost:8200

## Reference
* Akka Scala Seed : https://github.com/typesafehub/activator-akka-scala-seed
* Akka HTTP Microservice : https://github.com/theiterators/akka-http-microservice
* Reactive Kafka : https://github.com/softwaremill/activator-reactive-kafka-scala
* REST service : https://github.com/colaberry/scala-course-projects-lokesh973
* Csv parser : https://github.com/tototoshi/scala-csv

