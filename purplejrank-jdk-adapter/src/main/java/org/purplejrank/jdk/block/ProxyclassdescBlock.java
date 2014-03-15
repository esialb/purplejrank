package org.purplejrank.jdk.block;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.purplejrank.JrankConstants;
import org.purplejrank.jdk.Block;
import org.purplejrank.jdk.JdkBlock;
import org.purplejrank.jdk.JdkStream;
import org.purplejrank.jdk.WiredBlock;
import org.purplejrank.jdk.rule.ContentRule;
import org.purplejrank.jdk.rule.NewclassdescRule;
import org.purplejrank.jdk.rule.ObjectRule;
import org.purplejrank.jdk.rule.SuperclassdescRule;

public class ProxyclassdescBlock extends JdkBlock implements ObjectRule, NewclassdescRule, WiredBlock {

	protected List<String> proxyInterfaceNames = new ArrayList<String>();
	protected List<ContentRule> classAnnotation = new ArrayList<ContentRule>();
	protected SuperclassdescRule superClassDesc;
	
	
	public ProxyclassdescBlock(JdkStream jdk) {
		super(jdk);
	}

	@Override
	public Block parse() throws IOException {
		int count = jdk.readInt();
		for(int i = 0; i < count; i++)
			proxyInterfaceNames.add(jdk.readUTF());
		Block a;
		while((a = jdk.readBlock()) instanceof ContentRule)
			classAnnotation.add((ContentRule) a);
		EndblockdataBlock.class.cast(a);
		superClassDesc = jdk.readBlock(SuperclassdescRule.class);
		return this;
	}

	@Override
	public String getClassName() {
		return null;
	}

	@Override
	public ClassdescBlock getSuperClassDesc() {
		if(superClassDesc instanceof ReferenceBlock)
			return (ClassdescBlock) ((ReferenceBlock) superClassDesc).getWired();
		if(superClassDesc instanceof NullBlock)
			return null;
		return (ClassdescBlock) superClassDesc;
	}

	@Override
	public void writeJrank(DataOutputStream out) throws IOException {
		out.write(JrankConstants.J_PROXYCLASSDESC);
		JdkStream.writeEscapedInt(out, proxyInterfaceNames.size());
		for(String ifc : proxyInterfaceNames)
			JdkStream.writeUTF(out, ifc);
		superClassDesc.writeJrank(out);
	}

}
