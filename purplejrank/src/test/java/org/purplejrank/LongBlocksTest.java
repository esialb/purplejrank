package org.purplejrank;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

public class LongBlocksTest {
	@Test
	public void testLongBlocks() throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new PurpleJrankOutput(bout);
		out.writeObject("foo");
		out.write(new byte[1024*1024]);
		out.writeObject("bar");
		out.close();
		
		ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
		ObjectInputStream in = new PurpleJrankInput(bin);
		in.readObject();
		in.read(new byte[1024*1024]);
		in.readObject();
		in.close();
	}
}
