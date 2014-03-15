package org.purplejrank.jdk.block;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectStreamConstants;
import java.io.OutputStream;

import org.purplejrank.JrankConstants;
import org.purplejrank.jdk.Block;
import org.purplejrank.jdk.JdkBlock;
import org.purplejrank.jdk.JdkStream;
import org.purplejrank.jdk.WiredBlock;
import org.purplejrank.jdk.rule.ClassdescRule;
import org.purplejrank.jdk.rule.ObjectRule;
import org.purplejrank.jdk.rule.StringRule;

public class ReferenceBlock extends JdkBlock implements ObjectRule, ClassdescRule, StringRule {

	protected int handle;
	
	public ReferenceBlock(JdkStream jdk) {
		super(jdk);
	}

	@Override
	public Block parse() throws IOException {
		handle = jdk.readInt();
		return this;
	}

	public WiredBlock getWired() {
		return jdk.getWiredBlocks().get(handle - ObjectStreamConstants.baseWireHandle);
	}

	@Override
	public String getClassName() {
		return ((ClassdescRule) getWired()).getClassName();
	}

	@Override
	public ClassdescBlock getSuperClassDesc() {
		return ((ClassdescRule) getWired()).getSuperClassDesc();
	}

	@Override
	public void writeJrank(DataOutputStream out) throws IOException {
		out.write(JrankConstants.J_REFERENCE);
		JdkStream.writeEscapedInt(out, handle - ObjectStreamConstants.baseWireHandle);
	}

}
