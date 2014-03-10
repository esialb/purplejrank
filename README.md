# Purple Jrank
_Fault tolerant serialization mechanism with a stream protocol that can be parsed unambiguously with zero knowledge of class implementation._

## Reliable Relaxed Serialization
Purple Jrank is a more robust way to serialize objects than the JDK's **ObjectOutputStream** and **ObjectInputStream**.  It aims to be fully feature-compatible with JDK serialization, but uses a different stream protocol that more clearly delineates data boundaries, allowing streams to be parsed with zero knowledge of the class implementations.  Purple Jrank takes advantage of this zero-knowledge parseability to recover and continue deserializing streams that, if written with the JDK's **ObjectOutputStream**, would be unreadable or ambiguous.  Streams written by PurpleJrank are unambiguous.

Purple Jrank means reliable and flexible serialization even in the face of changing (or even missing) classes.

Purple Jrank has no dependencies.

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
        <version>0.1-SNAPSHOT</version>
    </dependency>

## Why use Purple Jrank?
### tl;dr
Purple Jrank can deserialize streams that specify classes not found on the classpath.

### Boring Details of Stream Corruption
There exist bugs in the JDK serialization stream protocol that make it unparseable with zero knowledge of the classes.  Basically, if you don't have all the classes from the stream, you can't even parse it, let alone deserialize it.  The biggest culprit is **ObjectOutputStream**, which lets you omit the call to **defaultWriteObject()** if you have a **writeObject(ObjectOutputStream)** method.  If any class in the stream does this then the stream cannot be parsed without actually executing the equivalent **readObject(ObjectInputStream)** method from that class.  The JDK stream protocol specifies that calls to writeObject are preceded by the field data, outside of a data block.  Because **writeObject(ObjectOutputStream)** lets you omit the call to **defaultWriteObject()**, the stream can be corrupted.

Purple Jrank properly writes headers and boundaries to fix this bug.  The output of Purple Jrank can be parsed with zero knowledge of the classes used to write the stream.  This makes it possible to handle missing classes in useful ways, such as replacing instances of missing classes with **null** while still being able to continue reading objects.

### Stream Corruption Example
The following class writes a corrupt stream, corrupt in that it cannot be parsed without knowledge of the implementation of **writeObject**, and that it is possible to read the stream totally incorrectly without any exceptions being thrown.

	/**
	 * Class that demonstrates the ambiguity in JDK object streams
	 * @author robin
	 *
	 */
	public static class A implements Serializable {
		private static final long serialVersionUID = 0;

		/*
		 * This magic value is the encoding of the string "Fail" followed by a null,
		 * as it would be written by defaultWriteObject
		 */
		public long fail = 0x7400044661696c70L;

		/*
		 * A single field, a long, should be written to the stream
		 */
		private void writeObject(ObjectOutputStream out) throws IOException {
			out.defaultWriteObject(); // written as raw stream metadata, not contained in any block
		}

		/*
		 * Because of ambiguity, the JDK allows the long to be interpreted as 
		 * a string reference followed by a null reference
		 */
		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			/*
			 * The first call to readObject should throw a StreamCorruptedException.
			 * JDK streams don't throw it.  PurpleJrank does.
			 */
			String fail = (String) in.readObject();
			Object nil = in.readObject();
			/*
			 * Verify that we _did_ manage to deserialize a default-written long as a string and
			 * a null
			 */
			if(!"Fail".equals(fail) || nil != null)
				throw new StreamCorruptedException();
		}
	}


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
    readExternal(ObjectInput)

However, non-public (e.g. **protected**) methods of **ObjectOutputStream** and **ObjectInputStream** are not supported.  The have been declared **final** and **@Deprecated**, and throw **UnsupportedOperationException** if invoked.  Purple Jrank provides its own protected methods for customizing the object serialization or deserialization process.  Additionally, **PutField.write(ObjectOutput)** is not supported, and also declared **final** and **@Deprecated**.