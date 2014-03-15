package org.purplejrank.jdk.block;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectStreamConstants;
import java.io.StreamCorruptedException;
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
import org.purplejrank.jdk.rule.StringRule;
import org.purplejrank.jdk.rule.SuperclassdescRule;

public class ClassdescBlock extends JdkBlock implements ObjectRule, NewclassdescRule, WiredBlock {

	protected String className;
	protected long serialVersionUID;
	protected byte classDescFlags;
	protected List<Field> fields = new ArrayList<Field>();
	protected List<ContentRule> classAnnotation = new ArrayList<ContentRule>();
	protected SuperclassdescRule superClassDesc;
	
	public class Field extends JdkBlock {
		protected byte typeCode;
		protected String fieldName;
		protected StringRule className;
		
		public Field(JdkStream jdk) {
			super(jdk);
		}

		@Override
		public Field parse() throws IOException {
			typeCode = jdk.readByte();
			fieldName = jdk.readUTF();
			switch(typeCode) {
			case 'L': case '[':
				className = jdk.readBlock(StringRule.class);
				break;
			case 'B': case 'C': case 'D': case 'F': case 'I': case 'J': case 'S': case 'Z':
				break;
			default:
				throw new StreamCorruptedException("Invalid typecode:" + typeCode);
			}
			return this;
		}

		public byte getTypeCode() {
			return typeCode;
		}

		public String getFieldName() {
			return fieldName;
		}

		public StringRule getClassName() {
			return className;
		}

		@Override
		public void writeJrank(DataOutputStream out) throws IOException {
			out.writeByte(typeCode);
			JdkStream.writeUTF(out, fieldName);
			if(className != null)
				className.writeJrank(out);
		}
		
	}
	
	public ClassdescBlock(JdkStream jdk) {
		super(jdk);
	}

	@Override
	public Block parse() throws IOException {
		className = jdk.readUTF();
		serialVersionUID = jdk.readLong();
		jdk.wireBlock(this);
		classDescFlags = jdk.readByte();
		short fieldCount = jdk.readShort();
		for(int i = 0; i < fieldCount; i++)
			fields.add(new Field(jdk).parse());
		Block a;
		while((a = jdk.readBlock()) instanceof ContentRule)
			classAnnotation.add((ContentRule) a);
		EndblockdataBlock.class.cast(a);
		superClassDesc = jdk.readBlock(SuperclassdescRule.class);
		return this;
	}

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public ClassdescBlock getSuperClassDesc() {
		if(superClassDesc instanceof ReferenceBlock)
			return (ClassdescBlock) ((ReferenceBlock) superClassDesc).getWired();
		if(superClassDesc instanceof NullBlock)
			return null;
		return (ClassdescBlock) superClassDesc;
	}

	public long getSerialVersionUID() {
		return serialVersionUID;
	}

	public byte getClassDescFlags() {
		return classDescFlags;
	}

	public List<Field> getFields() {
		return fields;
	}

	public List<ContentRule> getClassAnnotation() {
		return classAnnotation;
	}

	@Override
	public void writeJrank(DataOutputStream out) throws IOException {
		out.write(JrankConstants.J_CLASSDESC);
		String className = this.className;
		if(!className.startsWith("["))
			className = "L" + className + ";";
		JdkStream.writeUTF(out, className);
		JdkStream.writeEscapedLong(out, serialVersionUID);
		byte jrflags = 0;
		byte osflags = classDescFlags;
		if((osflags & ObjectStreamConstants.SC_SERIALIZABLE) != 0) {
			jrflags |= JrankConstants.J_SC_SERIALIZABLE;
			if((osflags & ObjectStreamConstants.SC_WRITE_METHOD) != 0)
				jrflags |= JrankConstants.J_SC_WRITE_OBJECT;
			else
				jrflags |= JrankConstants.J_SC_WRITE_FIELDS;
		}
		if((osflags & ObjectStreamConstants.SC_EXTERNALIZABLE) != 0)
			jrflags |= JrankConstants.J_SC_WRITE_EXTERNAL;
		if((osflags & ObjectStreamConstants.SC_ENUM) != 0)
			jrflags |= JrankConstants.J_SC_WRITE_ENUM;
		out.write(jrflags);
		out.writeShort(fields.size());
		for(Field f : fields)
			f.writeJrank(out);
		superClassDesc.writeJrank(out);
	}

}
