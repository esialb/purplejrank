package org.purplejrank.jdk.block;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.purplejrank.JrankConstants;
import org.purplejrank.jdk.JdkBlock;
import org.purplejrank.jdk.JdkStream;
import org.purplejrank.jdk.rule.BlockdataRule;

public class BlockdatalongBlock extends JdkBlock implements BlockdataRule {

	protected byte[] buf;
	
	public BlockdatalongBlock(JdkStream jdk) {
		super(jdk);
	}

	@Override
	public BlockdatalongBlock parse() throws IOException {
		int size = jdk.readInt();
		buf = new byte[size];
		jdk.readFully(buf);
		return this;
	}

	@Override
	public void writeJrank(DataOutputStream out) throws IOException {
		out.write(JrankConstants.J_BLOCK_DATA);
		JdkStream.writeEscapedInt(out, buf.length);
		out.write(buf);
	}

}
