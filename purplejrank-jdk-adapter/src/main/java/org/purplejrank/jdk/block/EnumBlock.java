package org.purplejrank.jdk.block;

import java.io.IOException;
import java.io.OutputStream;

import org.purplejrank.jdk.Block;
import org.purplejrank.jdk.JdkBlock;
import org.purplejrank.jdk.JdkStream;
import org.purplejrank.jdk.WiredBlock;
import org.purplejrank.jdk.rule.ClassdescRule;
import org.purplejrank.jdk.rule.ObjectRule;
import org.purplejrank.jdk.rule.StringRule;

public class EnumBlock extends JdkBlock implements ObjectRule, WiredBlock {

	protected ClassdescRule classDesc;
	protected StringRule enumConstantName;
	
	public EnumBlock(JdkStream jdk) {
		super(jdk);
	}

	@Override
	public Block parse() throws IOException {
		classDesc = jdk.readBlock(ClassdescRule.class);
		jdk.wireBlock(this);
		enumConstantName = jdk.readBlock(StringRule.class);
		return this;
	}

	@Override
	public void writeJrank(OutputStream out) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
