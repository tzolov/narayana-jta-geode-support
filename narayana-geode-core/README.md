# Geode/Gemfire integration with Standalone JTA Narayana

Library that helps to run Geode/Gemfire as a Last Resource Commit in a global JTA transactions run by a standalone Narayana JTA server.

It uses the `NamingBeanImpl` standalone JNDI server to bind the Narayana TransactionManager under `java:/TransactionManager`.

To enlist the Geode as Last Resource Commit, from within the global transaction run: `NarayanaGeodeSupport.enlistGeodeAsLastCommitResource()`.
 
A simple implementation would look like this:

```java
private static NamingBeanImpl jndiServer = new NamingBeanImpl();

    public static void main(String[] args) throws Exception {

        // 1.1 Start the JNDI server
        jndiServer.start();

        // Bind JTA TM implementations with default names. Concerning Geode, this bind will register the
        // Narayana Transaction Manager under name "java:/TransactionManager"
        JNDIManager.bindJTAImplementation();
                
        // Obtain the Geode Region instance
        // ..........
        
        // Start Narayana JTA transaction
        UserTransaction jta = com.arjuna.ats.jta.UserTransaction.userTransaction();
        jta.begin();        
        
        // Enlist Geode as Last Resource Commit Resource
        NarayanaGeodeSupport.enlistGeodeAsLastCommitResource();

        // Create adn use Global transactions
        // ........................
        
        // Perform Geode put
        region.put("666", 666);

        // Commit the Narayana JTA transaction
        jta.commit();

        // Stop the JNDI server
        jndiServer.stop();        
    }

```
Check [SimpleApplication.java](src/test/java/io/datalake/geode/jta/narayana/SimpleApplication.java) for a complete example. 
 

   

