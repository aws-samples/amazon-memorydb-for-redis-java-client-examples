## Amazon MemoryDB for Redis - Java client examples

This repository contains examples for using Java clients to interact with [Amazon MemoryDB for Redis](https://aws.amazon.com/memorydb/).  These examples demonstrate the following,

* How to connect/disconnect to Amazon MemoryDB for Redis using the open-source Redis client [Jedis](https://github.com/redis/jedis).
* Add/update records to the database.
* Retrieve records by key and field/path.
* Delete records by key.

### Overview

[Amazon MemoryDB for Redis](https://docs.aws.amazon.com/memorydb/latest/devguide/what-is-memorydb-for-redis.html) is a durable, in-memory database service that delivers ultra-fast performance, while removing the complexity associated with deploying and managing a distributed environment.  It is compatible with the open source [Redis](https://redis.io/) data store.

### Repository structure

This repository contains the following directories,

* [src](https://github.com/aws-samples/amazon-memorydb-for-redis-java-client-examples/tree/main/src/com/amazonaws/amazonmemorydbdemo) - the Java source files for the sample programs.

* [pom](https://github.com/aws-samples/amazon-memorydb-for-redis-java-client-examples/tree/main/pom) - the Maven POM file to build the sample programs.

* [config](https://github.com/aws-samples/amazon-memorydb-for-redis-java-client-examples/tree/main/config) - the configuration properties used by the sample programs.

### Prerequisites to run the examples

Prior to running these examples, make sure you have all the following components configured in the same AWS region and in the same AWS account.

**Amazon MemoryDB for Redis clusters:**

1. An Amazon MemoryDB for Redis cluster in private subnet groups in the same VPC.  Refer [here](https://docs.aws.amazon.com/memorydb/latest/devguide/getting-started.html).
2. A username with the appropriate access string configured in the cluster's Access Control List (ACL).  Refer [here](https://docs.aws.amazon.com/memorydb/latest/devguide/clusters.acls.html).

**Security groups:**

1. The security group should provide inbound TCP access on port 6379 from the EC2 instance that is to be configured in the following steps.

**Amazon EC2 instance:**
1. An Amazon EC2 instance in Linux/Windows in the same VPC as the MemoryDB clusters created in the previous steps.  The subnet should be private, preferably one that is part of the private subnet group configured for the MemoryDB clusters.
2. Java 11 ([Amazon Corretto 11](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/what-is-corretto-11.html) or [Oracle JDK 11](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)) should be installed in this instance.

### How to run the examples

1. Build the source code provided in the `src` folder along with the Maven POM file provided in the `pom` folder.

2. Configure the following in the `MemoryDB_for_Redis_config.properties` file provided in the `config` folder:

    a. Specify the MemoryDB cluster endpoint hostname.

    b. Specify the MemoryDB cluster endpoint port.

    c. The username for connecting to the cluster as configured in the cluster's ACL.

    d. The password corresponding to the above username.

    e. If you prefer, you can leave the other properties as-is; else update them as required.

3. Run the built programs and observe the output.

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This library is licensed under the MIT-0 License. See the LICENSE file.

