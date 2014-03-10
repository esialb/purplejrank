package org.purplejrank;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PurpleJrankTest implements Serializable {
	private static final long serialVersionUID = 0;
	
	@Parameters
	public static Iterable<Object[]> params() {
		List<Object> objs = Arrays.asList(
				1,
				"two",
				Arrays.asList(1,1,1),
				new PurpleJrankTest(null).new S(),
				Calendar.getInstance(),
				new Properties(),
				new Vector<Object>(),
				new Date()
				);
		List<Object[]> ret = new ArrayList<Object[]>(objs.size());
		for(int i = 0; i < objs.size(); i++)
			ret.add(new Object[] {objs.get(i)});
		return ret;
	}
	
	public class S implements Serializable {
		private static final long serialVersionUID = 0;
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
		Util.serialize(obj);
	}
	
	@Test
	public void testRead() throws Exception {
		byte[] buf;
		try {
			buf = Util.serialize(obj);
		} catch(Exception e) {
			Assume.assumeNoException(e);;
			throw new InternalError();
		}
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
		
		Object actual = Util.deserialize(buf);
		Assert.assertEquals(obj, actual);;
	}
}
