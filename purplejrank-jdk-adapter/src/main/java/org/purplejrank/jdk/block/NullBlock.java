package org.purplejrank.jdk.block;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.purplejrank.JrankConstants;
import org.purplejrank.jdk.Block;
import org.purplejrank.jdk.JdkBlock;
import org.purplejrank.jdk.JdkStream;
import org.purplejrank.jdk.rule.ClassdescRule;
import org.purplejrank.jdk.rule.ObjectRule;

public class NullBlock extends JdkBlock implements ObjectRule, ClassdescRule {

	public NullBlock(JdkStream jdk) {
		super(jdk);
	}

	@Override
	public Block parse() throws IOException {
		return this;
	}

	@Override
	public String getClassName() {
		return null;
	}

	@Override
	public ClassdescBlock getSuperClassDesc() {
		return null;
	}

	@Override
	public void writeJrank(DataOutputStream out) throws IOException {
		out.write(JrankConstants.J_NULL);
	}

}
