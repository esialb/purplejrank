package org.purplejrank.jdk.block;

import java.io.IOException;
import java.io.OutputStream;

import org.purplejrank.jdk.JdkBlock;
import org.purplejrank.jdk.JdkStream;
import org.purplejrank.jdk.rule.BlockdataRule;

public class BlockdataBlock extends JdkBlock implements BlockdataRule {

	protected byte[] buf;
	
	public BlockdataBlock(JdkStream jdk) {
		super(jdk);
	}

	@Override
	public BlockdataBlock parse() throws IOException {
		int size = 0xff & (int) jdk.readByte();
		buf = new byte[size];
		jdk.readFully(buf);;
		return this;
	}

	@Override
	public void writeJrank(OutputStream out) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
