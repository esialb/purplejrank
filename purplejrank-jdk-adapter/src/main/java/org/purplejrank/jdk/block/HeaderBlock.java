package org.purplejrank.jdk.block;

import java.io.IOException;
import java.io.ObjectStreamConstants;
import java.io.StreamCorruptedException;

import org.purplejrank.jdk.Block;
import org.purplejrank.jdk.JdkBlock;
import org.purplejrank.jdk.JdkStream;

public class HeaderBlock extends JdkBlock {

	public HeaderBlock(JdkStream jdk) {
		super(jdk);
	}

	@Override
	public Block parse() throws IOException {
		if(jdk.readShort() != ObjectStreamConstants.STREAM_MAGIC)
			throw new StreamCorruptedException();
		if(jdk.readShort() != ObjectStreamConstants.STREAM_VERSION)
			throw new StreamCorruptedException();
		return this;
	}

}
