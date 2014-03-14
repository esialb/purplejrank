package org.purplejrank.jdk;

import org.junit.Test;

public class JdkNFARuleTest {
	@Test
	public void testRules() {
		for(JdkNFARule rule : JdkNFARule.rules()) {
			System.out.println(rule);
		}
	}
}
