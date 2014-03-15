package org.purplejrank.jdk;

import java.io.ObjectInputStream;
import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;
import org.purplejrank.MappingClassLoader;
import org.purplejrank.mods.NullsJrankInput;

public class MissingClassTest {
	public static class A implements Serializable {
		private static final long serialVersionUID = 0;
	}
	
	@Test
	public void testMissing() throws Exception {
		MappingClassLoader cl = new MappingClassLoader(MissingClassTest.class.getClassLoader());
		cl.map(A.class, null);
		ObjectInputStream in = new NullsJrankInput(Util.jjis(new A()), cl);
		Assert.assertNull(in.readObject());
		in.close();
	}
}
