package org.purplejrank.jdk.block;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.purplejrank.jdk.Block;
import org.purplejrank.jdk.JdkBlock;
import org.purplejrank.jdk.JdkStream;
import org.purplejrank.jdk.WiredBlock;
import org.purplejrank.jdk.rule.ClassdescRule;
import org.purplejrank.jdk.rule.ContentRule;
import org.purplejrank.jdk.rule.ObjectRule;

public class ArrayBlock extends JdkBlock implements ObjectRule, WiredBlock {

	protected ClassdescRule classDesc;
	protected byte[] primValues;
	protected List<ObjectRule> objValues;
	
	public ArrayBlock(JdkStream jdk) {
		super(jdk);
	}

	@Override
	public Block parse() throws IOException {
		classDesc = jdk.readBlock(ClassdescRule.class);
		jdk.wireBlock(this);
		int size = jdk.readInt();
		switch(classDesc.getClassName().charAt(0)) {
		case 'B': primValues = new byte[size]; break;
		case 'C': primValues = new byte[size * 2]; break;
		case 'D': primValues = new byte[size * 8]; break;
		case 'F': primValues = new byte[size * 4]; break;
		case 'I': primValues = new byte[size * 4]; break;
		case 'J': primValues = new byte[size * 8]; break;
		case 'S': primValues = new byte[size * 2]; break;
		case 'Z': primValues = new byte[size]; break;
		case 'L': case '[': objValues = new ArrayList<ObjectRule>(); break;
		}
		
		if(primValues != null)
			jdk.readFully(primValues);
		if(objValues != null)
			for(int i = 0; i < size; i++)
				objValues.add(jdk.readBlock(ObjectRule.class));
		
		return this;
	}

	@Override
	public void writeJrank(OutputStream out) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
