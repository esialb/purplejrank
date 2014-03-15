package org.purplejrank.jdk;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A token block in a JDK stream
 * @author robin
 *
 */
public interface Block {
	/**
	 * Parse the token block
	 * @return this
	 * @throws IOException
	 */
	public Block parse() throws IOException;
	/**
	 * Write the token block in Jrank format
	 * @param out
	 * @throws IOException
	 */
	public void writeJrank(DataOutputStream out) throws IOException;
}
