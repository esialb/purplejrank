package org.purplejrank.mods;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Vector;

import org.junit.Test;
import org.purplejrank.Util;

public class ScanningJrankInputTest {
	@Test
	public void testScanning() throws Exception {
		byte[] buf = Util.serialize(new Vector<Object>(Arrays.<Object>asList(1, "two", 3f)));
		ScanningJrankInput scanner = new ScanningJrankInput(new ByteArrayInputStream(buf));
		scanner.scan();
		scanner.close();
		System.out.println(scanner.getStreamClasses());
		
	}
}
