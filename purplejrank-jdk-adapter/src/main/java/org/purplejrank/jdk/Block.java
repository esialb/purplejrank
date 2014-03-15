package org.purplejrank.jdk;

import java.io.IOException;
import java.io.OutputStream;

public interface Block {
	public Block parse() throws IOException;
	public void writeJrank(OutputStream out) throws IOException;
}
