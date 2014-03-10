package org.purplejrank;

import java.util.Arrays;
import java.util.Vector;

import org.junit.Test;

public class PurpleJrankOutputTest {
	@Test
	public void testSerializeStuff() throws Exception {
		Object a = new Vector<Object>(Arrays.<Object>asList(1, "two", 3f));
		
		Util.serialize(a);
	}
}
