package org.purplejrank.jdk.block;

import java.io.IOException;
import java.io.OutputStream;

import org.purplejrank.jdk.Block;
import org.purplejrank.jdk.JdkBlock;
import org.purplejrank.jdk.JdkStream;
import org.purplejrank.jdk.WiredBlock;
import org.purplejrank.jdk.rule.NewclassdescRule;
import org.purplejrank.jdk.rule.ObjectRule;

public class ClassdescBlock extends JdkBlock implements ObjectRule, NewclassdescRule, WiredBlock {

	public ClassdescBlock(JdkStream jdk) {
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
