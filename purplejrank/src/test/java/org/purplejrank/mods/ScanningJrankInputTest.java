package org.purplejrank.mods;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Vector;

import org.junit.Assert;
import org.junit.Test;
import org.purplejrank.MappingClassLoader;
import org.purplejrank.Util;

public class ScanningJrankInputTest {
	public static class M implements Serializable {
		private static final long serialVersionUID = 0;
	}
	
	@Test
	public void testScanning() throws Exception {
		byte[] buf = Util.serialize(new Vector<Object>(Arrays.<Object>asList(1, "two", 3f, new M())));
		
		MappingClassLoader mcl = new MappingClassLoader(ScanningJrankInputTest.class.getClassLoader());
		mcl.map(M.class, null);
		
		ScanningJrankInput scanner = new ScanningJrankInput(new ByteArrayInputStream(buf), mcl);
		scanner.scan();
		scanner.close();
		Assert.assertEquals(6, scanner.getStreamClasses().size());
		Assert.assertEquals(5, scanner.getResolvedClasses().size());
		Assert.assertEquals(1, scanner.getUnresolvedClasses().size());
	}
}
