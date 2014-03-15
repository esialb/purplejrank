package org.purplejrank.jdk;

import java.io.DataOutputStream;
import java.io.IOException;

public interface Block {
	public Block parse() throws IOException;
	public void writeJrank(DataOutputStream out) throws IOException;
}
