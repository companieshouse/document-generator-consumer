document-generator-consumer
============================

Kafka consumer that hooks into the document-generator process asynchronously

Requirements
--------------

In order to build document-generator locally you will need the following:
- [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/downloads)
- Kafka
- [document-render-service](https://github.com/companieshouse/document-render-service)
- [document-generator](https://github.com/companieshouse/document-generator)

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

Kafka
--------------
#### Consumer
A consumer group will poll the render-submitted-data-document topic for requests to generate a document.

#### Docker
To build a Docker image run the following command:

```
mvn compile jib:dockerBuild
```

This will output a Docker imaged named: `169942020521.dkr.ecr.eu-west-1.amazonaws.com/local/document-generator-consumer`

You can specify a different image name run `mvn compile jib:dockerBuild -Dimage=<YOUR NAME HERE>`

#### Producer
The following topics are produced to during the generation process:

| Name                          | Description                                                  |
| ----------------------------- | ------------------------------------------------------------ |
| document-generation-started   | Notification that the generation of the document has started |
| document-generation-completed | Details of the generated document                            |
| document-generation-failed    | Notification that the generation of the document has failed  |

## Terraform ECS

### What does this code do?

The code present in this repository is used to define and deploy a dockerised container in AWS ECS.
This is done by calling a [module](https://github.com/companieshouse/terraform-modules/tree/main/aws/ecs) from terraform-modules. Application specific attributes are injected and the service is then deployed using Terraform via the CICD platform 'Concourse'.


Application specific attributes | Value                                | Description
:---------|:-----------------------------------------------------------------------------|:-----------
**ECS Cluster**        |document-generation                                      | ECS cluster (stack) the service belongs to
**Load balancer**      |N/A consumer serivce                                           | The load balancer that sits in front of the service
**Concourse pipeline**     |[Pipeline link](https://ci-platform.companieshouse.gov.uk/teams/team-development/pipelines/document-generator-consumer) <br> [Pipeline code](https://github.com/companieshouse/ci-pipelines/blob/master/pipelines/ssplatform/team-development/document-generator-consumer)                                  | Concourse pipeline link in shared services


### Contributing
- Please refer to the [ECS Development and Infrastructure Documentation](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/4390649858/Copy+of+ECS+Development+and+Infrastructure+Documentation+Updated) for detailed information on the infrastructure being deployed.

### Testing
- Ensure the terraform runner local plan executes without issues. For information on terraform runners please see the [Terraform Runner Quickstart guide](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/1694236886/Terraform+Runner+Quickstart).
- If you encounter any issues or have questions, reach out to the team on the **#platform** slack channel.

### Vault Configuration Updates
- Any secrets required for this service will be stored in Vault. For any updates to the Vault configuration, please consult with the **#platform** team and submit a workflow request.

### Useful Links
- [ECS service config dev repository](https://github.com/companieshouse/ecs-service-configs-dev)
- [ECS service config production repository](https://github.com/companieshouse/ecs-service-configs-production)
