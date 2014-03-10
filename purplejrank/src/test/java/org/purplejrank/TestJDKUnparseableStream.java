package org.purplejrank;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import org.junit.Assert;
import org.junit.Test;

public class TestJDKUnparseableStream {
	public static class A implements Serializable {
		private static final long serialVersionUID = 0;

		public byte fail = 0x70;

		private void writeObject(ObjectOutputStream out) throws IOException {
			out.defaultWriteObject(); // written as raw stream metadata, not contained in any block
		}

		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.readObject(); // interprets the "fail" byte as a null object reference
		}
	}

	public static class B extends A {
		private static final long serialVersionUID = 0;
	}

	@Test
	public void testJdkFailsToFail() throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bout);
		out.writeObject(new B());
		out.close();

		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
		in.readObject(); // this would fail if JDK streams weren't ambiguous
		in.close();
	}

	@Test
	public void testJrankSucceedsToFail() throws Exception {
		try {
			Util.cycle(new B());
			Assert.fail();
		} catch(ObjectStreamException e) {
			// expected
		}
	}
}
