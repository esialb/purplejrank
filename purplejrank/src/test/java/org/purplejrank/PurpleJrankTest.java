package org.purplejrank;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PurpleJrankTest {
	
	@Parameters
	public static Iterable<Object[]> params() {
		return Arrays.asList(
				new Object[] {1},
				new Object[] {"two"}
				);
	}

	private Object obj;
	
	public PurpleJrankTest(Object obj) {
		this.obj = obj;
	}

	@Test
	public void testWrite() throws Exception {
		StreamWritableByteChannel ch = new StreamWritableByteChannel(new ByteArrayOutputStream());
		ObjectOutputStream out = new PurpleJrankOutput(ch);
		out.writeObject(obj);
		out.close();
	}
	
	@Test
	public void testRead() throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
			StreamWritableByteChannel ch = new StreamWritableByteChannel(bout);
			ObjectOutputStream out = new PurpleJrankOutput(ch);
			out.writeObject(obj);
			out.close();
		} catch(Exception e) {
			Assume.assumeNoException(e);;
		}
		ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
		StreamReadableByteChannel ch = new StreamReadableByteChannel(bin);
		ObjectInputStream in = new PurpleJrankInput(ch);
		in.readObject();
		in.close();
	}
}
