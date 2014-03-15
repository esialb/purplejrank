package org.purplejrank.jdk.block;

import java.io.IOException;
import java.io.OutputStream;

import org.purplejrank.jdk.Block;
import org.purplejrank.jdk.JdkBlock;
import org.purplejrank.jdk.JdkStream;
import org.purplejrank.jdk.rule.BlockdataRule;

public class BlockdataBlock extends JdkBlock implements BlockdataRule {

	public BlockdataBlock(JdkStream jdk) {
		super(jdk);
	}

	@Override
	public Block parse() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeJrank(OutputStream out) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
