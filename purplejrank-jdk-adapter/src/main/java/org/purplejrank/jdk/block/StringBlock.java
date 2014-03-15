package org.purplejrank.jdk.block;

import java.io.DataOutputStream;
import java.io.IOException;
import org.purplejrank.JrankConstants;
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
	public void writeJrank(DataOutputStream out) throws IOException {
		out.write(JrankConstants.J_STRING);
		JdkStream.writeUTF(out, s);
	}

	@Override
	public String getString() {
		return s;
	}

}
