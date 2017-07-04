# Narayana (e.g. JBoss) JTA with Geode/GemFire

[ ![Download](https://api.bintray.com/packages/big-data/maven/narayana-jta-geode-support/images/download.svg) ](https://bintray.com/big-data/maven/narayana-jta-geode-support/_latestVersion)

Use Narayana JTA provider as global transaction manager to coordinate Geode/GemFire cache transactions along with JPA/JDBC and/or JMS resources.

[Narayana](http://narayana.io//docs/project/index.html) is light-weight (e.g. out-of-container), embeddable global transaction manager. Narayana is JTA compliant and can be integrated with Geode/GemFire to perform XA transaction across Geode, JPA/JDBC and JMS operations. 

Also Narayana supports [Last Resource Commit Optiomization](http://narayana.io//docs/project/index.html#d0e1859) allowing with Geode/GemFire transactions to be run as last resources.

The `narayana-geode` extends the existing Geode JTA support by integrating a standalone (open-source) JTA provider and 
enabling LRCO for transaction-atomicity and data-consistency. The utility includes two sub-projects:

#### Narayana Geode/Gemfire Core 
The [narayana-geode-core](./narayana-geode-core) library uses minimal external dependencies. Only Narayana and the 
Apache Geode/Gemfire APIs are needed (e.g. no dependencies on Spring or Spring Data Gemfire and so.)
The [narayana-geode-core](./narayana-geode-core) README explains how to use the core utility. 

#### Narayana Geode/Gemfire SpringBoot
The [narayana-geode-springboot](./narayana-geode-springboot) library extends `narayana-geode-core` to provide seamless 
integration with `Spring Boot` and `Spring Data Gemfire` (SDG).

The [narayana-geode-springboot](./narayana-geode-springboot) README explains how to use the core utility.
 
#### POM dependencies 
All `narayana-geode` dependencies can be resolved from Maven Central: [io.datalake.geode.jta](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.datalake.geode.jta%22) 

Currently `narayana-geode` support `Gemfire 8.2.x`, `Gemfire 9.0.x` and `Geode 1.1.x`. See matrix for details:

|       POM Group       |          POM Artifact         | POM Version | Compatible Apache Geode/Gemfire Versions |
| --------------------- | ----------------------------- | ----------- | ------------------------------------------- |
| io.datalake.geode.jta | narayana-geode-core           | 0.1.11+     | Apache Geode 1.1.x or newer, Gemfire 9.0.4 or newer  |
| io.datalake.geode.jta | narayana-geode-springboot     | 0.1.11+     | Apache Geode 1.1.x or newer, Gemfire 9.0.4 or newer, SpringBoot 1.5.4 or newer. No SDG GA for this Geode/Gemfire version yet. |
| io.datalake.geode.jta | narayana-gemfire82-core       | 0.1.11+     | Gemfire 8.2.x  |
| io.datalake.geode.jta | narayana-gemfire82-springboot | 0.1.11+     | Gemfire 8.2.x, SpringBoot 1.5.4 or newer, Spring Data Gemfire 1.9.4  |

#### Documentation
The following blog describes the problem and explains the solution: [Apache Geode (GemFire) + Narayana JTA = Global Transactions with Last-Resource Optimization](http://blog.tzolov.net/2017/07/apache-geode-gemfire-narayana-jta.html?view=sidebar)

#### Build
To build the projects run
```
./mvn clean install
```