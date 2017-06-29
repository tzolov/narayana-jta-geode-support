# SpringBoot - Geode/Gemfire integration with Standalone JTA Narayana

Utility that integrates Geode/Gemfire with the Narayana (e.g. JBoss) standalone JTA server, levering the 
`Last Resource Commit Optimization`. 

The `narayana-geode-springboot` extends [narayana-geode-core](../narayana-geode-core) by providing a smooth integration 
with `SpringBoot` and `Spring Data Gemfire` applications. By enabling  the `spring-boot-starter-jta-narayana` starter 
and adding a single `@NarayanaLastResourceCommitOptimization` you can configure Geode/Gemfire as Last-Resource-Commit 
in Narayana JTA.

Internally the `@NarayanaLastResourceCommitOptimization` starts a standalone JNDI server and binds the Narayana JTA 
transaction managers to it. This makes it visible Geode/Gemfire for participating in JTA transactions. Furthermore once 
selected the annotation activates the `NarayanaLrcoAspect` that to automatically enlist Geode/GemFire as Last Resource 
in any class or method with by @Tranasactional annotation. 

Typical usage would look like this:
```java

@SpringBootApplication
@NarayanaLastResourceCommitOptimization
@EnableTransactionManagement(order = 1)
public class SampleNarayanaApplication implements CommandLineRunner { 
  ... 
}
```

Note that the `@NarayanaLastResourceCommitOptimization` annotation may only be used on a Spring application that 
is also annotated with `@EnableTransactionManagement` and the `order` attribute is explicit set to a value other 
than `Integer#MAX_VALUE` or `Integer#MIN_VALUE`!

For a complete example check the [narayana-geode-springboot-example](../narayana-geode-springboot-example) project. 

The `narayana-geode-springboot` can be use with plain `Geode/Gemfire API`  or with `Spring Data Gemfire`.

You need to add the `narayana-geode-springboot` dependency to your pom:
```xml
    <dependency>
        <groupId>io.datalake.geode.jta</groupId>
        <artifactId>narayana-geode-springboot</artifactId>
        <version>0.1.9</version>
    </dependency>
```
Check for the latest version: [ ![Download](https://api.bintray.com/packages/big-data/maven/narayana-jta-geode-support/images/download.svg) ](https://bintray.com/big-data/maven/narayana-jta-geode-support/_latestVersion).

The `narayana-geode-springboot` can be resolved from the `JCentral` or from the `Maven Central` repositories. For
 `JCentral` you need to add the repository to your application pom:
 
```xml
    <repository>
        <id>central</id>
        <name>bintray</name>
        <url>http://jcenter.bintray.com</url>
    </repository>
```
