package org.purplejrank.jdk.block;

import java.io.DataOutputStream;
import java.io.IOException;
import org.purplejrank.jdk.Block;
import org.purplejrank.jdk.JdkBlock;
import org.purplejrank.jdk.JdkStream;

public class EndblockdataBlock extends JdkBlock {

	public EndblockdataBlock(JdkStream jdk) {
		super(jdk);
	}

	@Override
	public Block parse() throws IOException {
		return this;
	}

	@Override
	public void writeJrank(DataOutputStream out) throws IOException {
		throw new UnsupportedOperationException();
	}

}
