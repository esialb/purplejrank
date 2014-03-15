package org.purplejrank.jdk;

/**
 * Abstract {@link Block}
 * @author robin
 *
 */
public abstract class JdkBlock implements Block {

	protected JdkStream jdk;
	
	public JdkBlock(JdkStream jdk) {
		this.jdk = jdk;
	}

}
