package org.purplejrank.jdk.block;

import java.io.IOException;
import java.io.OutputStream;

import org.purplejrank.jdk.Block;
import org.purplejrank.jdk.JdkBlock;
import org.purplejrank.jdk.JdkStream;
import org.purplejrank.jdk.WiredBlock;
import org.purplejrank.jdk.rule.ObjectRule;
import org.purplejrank.jdk.rule.StringRule;

public class StringBlock extends JdkBlock implements ObjectRule, StringRule, WiredBlock {

	protected String s;
	
	public StringBlock(JdkStream jdk) {
		super(jdk);
	}

	@Override
	public Block parse() throws IOException {
		jdk.wireBlock(this);
		s = jdk.readUTF();
		return this;
	}

	@Override
	public void writeJrank(OutputStream out) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
