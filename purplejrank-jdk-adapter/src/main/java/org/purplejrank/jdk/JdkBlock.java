package org.purplejrank.jdk;

public abstract class JdkBlock implements Block {

	protected JdkStream jdk;
	
	public JdkBlock(JdkStream jdk) {
		this.jdk = jdk;
	}

}
