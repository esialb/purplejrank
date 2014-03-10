package org.purplejrank;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.purplejrank.io.StreamReadableByteChannel;
import org.purplejrank.io.StreamWritableByteChannel;

public class NullsJrankInputTest {
	public static class Missing implements Serializable {
		private static final long serialVersionUID = 0;
		
		@SuppressWarnings("unused")
		private int i = 1;
		
		private void writeObject(ObjectOutputStream out) throws IOException {
			out.defaultWriteObject();
			out.writeInt(2);
		}
		
	}
	
	private static class MissingMissingClassLoader extends ClassLoader {
		public MissingMissingClassLoader() {
			super(MissingMissingClassLoader.class.getClassLoader());
		}
		
		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			Class<?> cls = super.loadClass(name);
			if(cls == Missing.class)
				throw new ClassNotFoundException();
			return cls;
		}
	}
	
	@Test
	public void testMissingAsNull() throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
			StreamWritableByteChannel ch = new StreamWritableByteChannel(bout);
			ObjectOutputStream out = new PurpleJrankOutput(ch);
			out.writeObject(new Missing());
			out.writeObject(new Missing[0]);
			out.close();
		} catch(Exception e) {
			Assume.assumeNoException(e);;
		}
		
		byte[] buf = bout.toByteArray();

		ByteArrayInputStream bin = new ByteArrayInputStream(buf);
		StreamReadableByteChannel ch = new StreamReadableByteChannel(bin);
		ObjectInputStream in = new NullsJrankInput(ch, new MissingMissingClassLoader());

		Assert.assertNull(in.readObject());;
		Assert.assertNull(in.readObject());;

		in.close();
	}
}
