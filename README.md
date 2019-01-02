document-generator-consumer
============================

Kafka consumer that hooks into the document-generator process asynchronously

Requirements
--------------

In order to build document-generator locally you will need the following:
- [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/downloads)

Getting started
-----------------

1. Run make
2. Run ./start.sh

Environment Variables
---------------------
The supported environmental variables have been categorised by use case and are as follows.

### Deployment Variables
Name                                      | Description                                                                                                                                                               | Mandatory | Default | Example
----------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------- | ------- | ----------------------------------------
DOCUMENT_GENERATION_CONSUMER_SERVICE_PORT | Configured port application runs on.                                                                                                                                      | ✓         |         | 10097                                                                                                                                                       | ✓         |         | example-bucket
CONSUMER_TOPIC                            | Topic for the consumer to pick up                                                                                                                                         | ✓         |         | render-submitted-data-document                                                                                                                                                       | ✓         |         | example-bucket
GROUP_NAME                                | Group name for the consumer                                                                                                                                               | ✓         |         | document-generator
CHS_API_KEY                               | Chs api key encoded and used to make APi calls                                                                                                                            | ✓         |         | valid Api key


