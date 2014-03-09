package org.purplejrank;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PurpleJrankTest implements Serializable {
	
	@Parameters
	public static Iterable<Object[]> params() {
		List<Object> objs = Arrays.asList(
				1,
				"two",
				Arrays.asList(1,1,1),
				new PurpleJrankTest(null).new S()
				);
		List<Object[]> ret = new ArrayList<Object[]>(objs.size());
		for(int i = 0; i < objs.size(); i++)
			ret.add(new Object[] {objs.get(i)});
		return ret;
	}
	
	public class S implements Serializable {
		private int foo = 1;
		
		@Override
		public boolean equals(Object obj) {
			if(obj == null)
				return false;
			if(obj == this)
				return true;
			if(obj instanceof S)
				return foo == ((S) obj).foo;
			return false;
		}
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
		
		byte[] buf = bout.toByteArray();
/*		
		int pos = 0;
		while(pos < buf.length) {
			byte[] block = Arrays.copyOfRange(buf, pos, Math.min(buf.length, pos + 32));
			char[] chars = new char[block.length];
			for(int i = 0; i < block.length; i++) {
				char c = (char)(0xff & (int) block[i]);
				if(!Character.isLetter(c))
					c = '.';
				System.out.print(c);
			}
			System.out.println(" " + Arrays.toString(block));
			pos += block.length;
		}
*/
		
		ByteArrayInputStream bin = new ByteArrayInputStream(buf);
		StreamReadableByteChannel ch = new StreamReadableByteChannel(bin);
		ObjectInputStream in = new PurpleJrankInput(ch);
		Object actual = in.readObject();
		in.close();
		Assert.assertEquals(obj, actual);;
	}
}
