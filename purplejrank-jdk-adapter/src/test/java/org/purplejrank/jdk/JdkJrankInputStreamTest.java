package org.purplejrank.jdk;

import java.util.Calendar;

import org.junit.Test;
import org.purplejrank.PurpleJrankInput;

public class JdkJrankInputStreamTest {
	@Test
	public void testInput() throws Exception {
		JdkJrankInputStream jjis = Util.jjis(Calendar.getInstance());
		PurpleJrankInput in = new PurpleJrankInput(jjis);
		in.readObject();
	}
}
