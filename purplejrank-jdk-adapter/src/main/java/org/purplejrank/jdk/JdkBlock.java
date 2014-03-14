package org.purplejrank.jdk;

import java.io.IOException;

public abstract class JdkBlock implements Block {

	protected JdkStream jdk;
	
	public JdkBlock(JdkStream jdk) {
		this.jdk = jdk;
	}

}
