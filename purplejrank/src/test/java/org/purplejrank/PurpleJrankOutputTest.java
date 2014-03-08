package org.purplejrank;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.Vector;

import org.junit.Test;

public class PurpleJrankOutputTest {
	@Test
	public void testSerializeStuff() throws Exception {
		Object a = new Vector<Object>(Arrays.<Object>asList(1, "two", 3f));
		
		WritableByteChannel ch = new StreamWritableByteChannel(new ByteArrayOutputStream());
		ObjectOutputStream out = new PurpleJrankOutput(ch);
		out.writeObject(a);
	}
}
