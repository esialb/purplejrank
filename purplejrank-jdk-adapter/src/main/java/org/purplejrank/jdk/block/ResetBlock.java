package org.purplejrank.jdk.block;

import java.io.DataOutputStream;
import java.io.IOException;
import org.purplejrank.JrankConstants;
import org.purplejrank.jdk.Block;
import org.purplejrank.jdk.JdkBlock;
import org.purplejrank.jdk.JdkStream;
import org.purplejrank.jdk.rule.ObjectRule;

public class ResetBlock extends JdkBlock implements ObjectRule {

	public ResetBlock(JdkStream jdk) {
		super(jdk);
	}

	@Override
	public Block parse() throws IOException {
		return this;
	}

	@Override
	public void writeJrank(DataOutputStream out) throws IOException {
		out.write(JrankConstants.J_RESET);
	}

}
