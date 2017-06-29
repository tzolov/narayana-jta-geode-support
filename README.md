# Narayana (e.g. JBoss) JTA with Geode/GemFire

[ ![Download](https://api.bintray.com/packages/big-data/maven/narayana-jta-geode-support/images/download.svg) ](https://bintray.com/big-data/maven/narayana-jta-geode-support/_latestVersion)

Use Narayana JTA provider as global transaction manager to coordinate Geode/GemFire cache transactions along with JPA/JDBC and/or JMS resources.

[Narayana](http://narayana.io//docs/project/index.html) is light-weight (e.g. out-of-container), embeddable global transaction manager. Narayana is JTA compliant and can be integrated with Geode/GemFire to perform XA transaction across Geode, JPA/JDBC and JMS operations. 

Also Narayana supports [Last Resource Commit Optiomization](http://narayana.io//docs/project/index.html#d0e1859) allowing with Geode/GemFire transactions to be run as last resources.

#### Narayana Geode/Gemfire Core 
Instructions how to use the integration without Spring: [narayana-geode-core](./narayana-geode-core)

#### Narayana Geode/Gemfire SpringBoot
Instructions to automate the integration in SpringBoot applications: [narayana-geode-springboot](./narayana-geode-springboot)

#### Geode/GemFire JTA Background
Out of the box, Geode/GemFire provides the following [JTA Global Transactions](http://geode.apache.org/docs/guide/11/developing/transactions/JTA_transactions.html) integration options:

1. Have Geode/GemFire act as JTA-like transaction manager _(deprecated)_ - This is **not JTA compliant** solution and could cause synchronization and transaction coordination problems. Deprecated in the latest Geode/GemFire releases and in its current state you better not use it as JTA manager!
2. Coordinate with an external JTA transaction manager in a container (such as WebLogic or JBoss). Also Geode/GemFire can be set as the "last resource" while using a container. - While this approach provides a reliable JTA capabilities it requires a heavy-weight JEE container. 

The [SpringBoot Narayana](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-jta.html#boot-features-jta-narayana) 
integration extends option (2) by using Narayana as an external JTA manager without the need of running a J2EE container. 
