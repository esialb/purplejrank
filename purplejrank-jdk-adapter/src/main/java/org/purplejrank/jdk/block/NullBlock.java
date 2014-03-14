package org.purplejrank.jdk.block;

import java.io.IOException;

import org.purplejrank.jdk.Block;
import org.purplejrank.jdk.JdkBlock;
import org.purplejrank.jdk.JdkStream;

public class NullBlock extends JdkBlock {

	public NullBlock(JdkStream jdk) {
		super(jdk);
	}

	@Override
	public Block parse() throws IOException {
		return this;
	}

}
