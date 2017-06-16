# In Memery JCR

The goal of this project is to make it easier for developers to set up 
a full feature JCR repository (In this case Jackrabbit) for testing purposes.
 
 
# Usage

The first is to add this library to your project as a test dependency. If you are using Maven
you can do so by adding the following snippet to your pom.xml file

```xml
<repositories>
  ..
	<repository>
	  <id>openweb-maven-public</id>
	  <url>https://maven.open-web.nl/content/repositories/public/</url>
	</repository>
</repositories>
...
<dependencies>
  ...
  <dependency>
    <groupId>nl.openweb.jcr</groupId>
    <artifactId>in-memory-jcr</artifactId>
    <version>1.1.0</version>
    <scope>test</scope>
  </dependency>
  ...
</dependencies>
```

then in your test you can instantiate a new repository simple by instantiating a new 
instance of InMemoryJcrRepository class.
```java
InMemoryJcrRepository repository = new InMemoryJcrRepository();
```
to obtain an instance of java session via
```java
Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
```
Please notice that you need to shutdown the repository at the end of your test. Because unlike
what the name of this library might suggest the repository is not 100% in the memory, Still the 
indexes are keep in a temporary folder. So shutting down the repository insures that the index
files are deleted. To shutdown the repository you can use the following snippet

```java
repository.shutdown();
```
Please notice that InMemoryJcrRepository implements AutoCloseable. Therefore it can be in
a try with resources statement as well.
```java
try (InMemoryJcrRepository repository = new InMemoryJcrRepository()) {
	Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
	// here is your test
}
```
## Extra tools

Node definition constrains and namespace definitions can be a headache during testing. To make
testing easier, there is a utility class that you can use to register your node types and mixin types 
free of constrains. Let say I want to have a node of type "mynamespace:mynode" then I can 
simple use
```java
NodeTypeDefUtils.createNodeType(session, "mynamespace:mynode");
```
Please notice that you do not need to register "mynamespace". If "mynamespace" namespace is not 
register the NodeTypeDefUtils is going to register it automatically.

Here is a complete example

```java
try (InMemoryJcrRepository repository = new InMemoryJcrRepository()) {
    Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
    NodeTypeDefUtils.createNodeType(session, "mynamespace:mynode");

    Node node = session.getRootNode().addNode("nodeName", "mynamespace:mynode");
    session.save();
    // your test
}
```


