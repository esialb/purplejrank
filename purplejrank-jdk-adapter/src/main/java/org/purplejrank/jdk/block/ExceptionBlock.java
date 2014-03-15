package org.purplejrank.jdk.block;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.purplejrank.jdk.Block;
import org.purplejrank.jdk.JdkBlock;
import org.purplejrank.jdk.JdkStream;
import org.purplejrank.jdk.rule.ObjectRule;

public class ExceptionBlock extends JdkBlock implements ObjectRule {

	public ExceptionBlock(JdkStream jdk) {
		super(jdk);
	}

	@Override
	public Block parse() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeJrank(DataOutputStream out) throws IOException {
		throw new UnsupportedOperationException();
	}

}
