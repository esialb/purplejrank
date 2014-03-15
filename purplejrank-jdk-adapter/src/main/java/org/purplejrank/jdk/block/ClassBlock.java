package org.purplejrank.jdk.block;

import java.io.DataOutputStream;
import java.io.IOException;
import org.purplejrank.JrankConstants;
import org.purplejrank.jdk.Block;
import org.purplejrank.jdk.JdkBlock;
import org.purplejrank.jdk.JdkStream;
import org.purplejrank.jdk.WiredBlock;
import org.purplejrank.jdk.rule.ClassdescRule;
import org.purplejrank.jdk.rule.ObjectRule;

public class ClassBlock extends JdkBlock implements ObjectRule, WiredBlock {

	protected ClassdescRule classDesc;
	
	public ClassBlock(JdkStream jdk) {
		super(jdk);
	}

	@Override
	public Block parse() throws IOException {
		classDesc = jdk.readBlock(ClassdescRule.class);
		jdk.wireBlock(this);
		return this;
	}

	@Override
	public void writeJrank(DataOutputStream out) throws IOException {
		out.write(JrankConstants.J_CLASS);
		classDesc.writeJrank(out);
	}

}
