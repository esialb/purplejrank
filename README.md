# Purple Jrank
Purple Jrank is a more robust way to serialize objects than the JDK's **ObjectOutputStream** and **ObjectInputStream**.  It aims to be fully feature-compatible with JDK serialization, but uses a different stream protocol that more clearly delineates data boundaries.

## Where to Get Purple Jrank

Purple Jrank is built and deployed with Apache Maven.  Right now it isn't on Maven Central, so you'll have to add the development repository.

The repository:
    <repository>
        <id>purplejrank</id>
        <url>http://repo.purplejrank.org/</url>
    </repository>

The artifact itself:
    <dependency>
        <groupId>org.purplejrank</groupId>
        <artifactId>purplejrank</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>

## Why use Purple Jrank?
There exist bugs in the JDK serialization stream protocol that make it unparseable with zero knowledge of the classes.  Basically, if you don't have all the classes from the stream, you can't even parse it, let alone deserialize it.  The biggest culprit is **ObjectOutputStream**, which lets you omit the call to **defaultWriteObject()** if you have a **writeObject(ObjectOutputStream)** method.  If any class in the stream does this then the stream cannot be parsed without actually executing the equivalent **readObject(ObjectInputStream)** method from that class.

Purple Jrank properly writes headers and boundaries to fix this bug.  The output of Purple Jrank can be parsed with zero knowledge of the classes used to write the stream.  This makes it possible to handle missing classes in useful ways, such as replacing instances of missing classes with **null** while still being able to continue reading objects.

## java.nio

Purple Jrank is based on the **java.nio** framework, but will accept **java.io** streams too.  (To accept **java.io** streams, it simply wraps them to turn them into **WritableByteChannel** and **ReadableByteChannel** instances.)

## How to use Purple Jrank

Using Purple Jrank is just like using **ObjectInputStream** and **ObjectOutputStream**:

To write an object:
    WritableByteChannel ch = ...
    ObjectOutputStream out = new PurpleJrankOutput(ch);
    out.writeObject(someObject);

To read an object:
    ReadableByteChannel ch = ...
    ObjectInputStream in = new PurpleJrankInput(ch);
    in.readObject();

## Reading Streams with Missing Classes

Purple Jrank has built-in support for reading streams that require classes not found on the classpath.  In this case, objects of these missing classes are replaced with nulls.

To read a missing-classes stream:
    ReadableByteChannel ch = ...
    ObjectInputStream in = new NullsJrankInput(ch);
    in.readObject();

## Supported Serialization Methods

All the normal **Serializable** and **Externalizable** methods are supported.

From **Serializable**:
    writeObject(ObjectOutputStream)
    readObject(ObjectInputStream)
    readObjectNoData()
    writeReplace()
    readResolve()

From **Externalizable**:
    writeExternal(ObjectOutput)
    **readExternal**(ObjectInput)

However, non-public (e.g. **protected**) methods of **ObjectOutputStream** and **ObjectInputStream** are not supported.  The have been declared **final** and **@Deprecated**, and throw **UnsupportedOperationException** if invoked.  Purple Jrank provides its own protected methods for customizing the object serialization or deserialization process.  Additionally, **PutField.write(ObjectOutput)** is not supported, and also declared **final** and **@Deprecated**.