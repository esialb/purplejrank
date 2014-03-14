package org.purplejrank.jdk;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;

import org.purplejrank.jdk.block.HeaderBlock;

public class JdkStream extends DataInputStream {

	public JdkStream(InputStream in) throws IOException {
		super(in);
		new HeaderBlock(this).parse();
	}
	
	public Block readBlock() throws IOException {
		int t = read();
		switch(t) {
		case -1:
			throw new EOFException();
		default:
			throw new StreamCorruptedException();
		}
	}
}
