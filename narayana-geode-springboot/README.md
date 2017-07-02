# SpringBoot - Geode/Gemfire integration with Standalone JTA Narayana

Utility that integrates Geode/Gemfire with the Narayana (e.g. JBoss) standalone JTA server, levering the 
`Last Resource Commit Optimization`. 

The `narayana-geode-springboot` extends [narayana-geode-core](../narayana-geode-core) by providing a smooth integration 
with `SpringBoot` and `Spring Data Gemfire` applications. By enabling  the `spring-boot-starter-jta-narayana` starter 
and adding a single `@EnableGeodeNarayanaJta` you can configure Geode/Gemfire as Last-Resource-Commit 
in Narayana JTA.

Internally the `@EnableGeodeNarayanaJta` starts a standalone JNDI server and binds the Narayana JTA 
transaction managers to it. This makes it visible Geode/Gemfire for participating in JTA transactions. Furthermore once 
selected the annotation activates the `NarayanaLrcoAspect` that to automatically enlist Geode/GemFire as Last Resource 
in any class or method with by @Tranasactional annotation. 

Typical usage would look like this:
```java

@SpringBootApplication
@EnableGeodeNarayanaJta
@EnableTransactionManagement(order = 1)
public class SampleNarayanaApplication implements CommandLineRunner { 
  ... 
}
```

Note that the `@EnableGeodeNarayanaJta` annotation may only be used on a Spring application that 
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
If the `@EnableGeodeNarayanaJta` is not set but is required Geode/Gemfire to participate as
`javax.transaction.Synchronization` resource in the JTA transactions than add the following `@Been` definition to your 
`@Configuration` definitions.

```java
    @Bean(name = "NarayanaNamingServer")
    @ConditionalOnMissingBean(NamingServer.class)
    public NarayanaNamingServerFactoryBean narayanaNamingServer(TransactionManager tm) {
        System.out.println(tm.getClass().getName());
        return new NarayanaNamingServerFactoryBean();
    }
```
The `NarayanaNamingServer` will start a standalone JNDI server and will bind the Narayana TransactionManager. Later 
allows Geode/Gemfire to find and participate in JTA transaction as `javax.transaction.Synchronization` resource.
Note that the execution order of XA and non-XA resources in the transactions is undefined.   
 
