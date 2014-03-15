package org.purplejrank.jdk.block;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectStreamConstants;
import java.util.ArrayList;
import java.util.List;

import org.purplejrank.JrankConstants;
import org.purplejrank.jdk.Block;
import org.purplejrank.jdk.JdkBlock;
import org.purplejrank.jdk.JdkStream;
import org.purplejrank.jdk.WiredBlock;
import org.purplejrank.jdk.block.ClassdescBlock.Field;
import org.purplejrank.jdk.rule.ClassdescRule;
import org.purplejrank.jdk.rule.ContentRule;
import org.purplejrank.jdk.rule.ObjectRule;

public class ObjectBlock extends JdkBlock implements ObjectRule, WiredBlock {

	protected ClassdescRule classDesc;
	protected List<Classdata> classdata = new ArrayList<Classdata>();
	
	public class Classdata extends JdkBlock {

		public class FieldValue extends JdkBlock {

			protected Field field;
			protected Object primValue;
			protected ObjectRule objValue;
			
			public FieldValue(JdkStream jdk, Field field) {
				super(jdk);
				this.field = field;
			}

			@Override
			public FieldValue parse() throws IOException {
				switch(field.getTypeCode()) {
				case 'B': primValue = jdk.readByte(); break;
				case 'C': primValue = jdk.readChar(); break;
				case 'D': primValue = jdk.readDouble(); break;
				case 'F': primValue = jdk.readFloat(); break;
				case 'I': primValue = jdk.readInt(); break;
				case 'J': primValue = jdk.readLong(); break;
				case 'S': primValue = jdk.readShort(); break;
				case 'Z': primValue = jdk.readBoolean(); break;
				case 'L': case '[': objValue = jdk.readBlock(ObjectRule.class); break;
				}
				return this;
			}

			@Override
			public void writeJrank(DataOutputStream out) throws IOException {
				if(primValue != null)
					out.write(JrankConstants.J_BLOCK_DATA);
				switch(field.getTypeCode()) {
				case 'B':
					JdkStream.writeEscapedInt(out, 1);
					out.writeByte((Byte) primValue); 
					break;
				case 'C':
					JdkStream.writeEscapedInt(out, 2);
					out.writeChar((Character) primValue);
					break;
				case 'D':
					JdkStream.writeEscapedInt(out, 8);
					out.writeDouble((Double) primValue);
					break;
				case 'F':
					JdkStream.writeEscapedInt(out, 4);
					out.writeFloat((Float) primValue);
					break;
				case 'I':
					JdkStream.writeEscapedInt(out, 4);
					out.writeInt((Integer) primValue);
					break;
				case 'J':
					JdkStream.writeEscapedInt(out, 8);
					out.writeLong((Long) primValue);
					break;
				case 'S':
					JdkStream.writeEscapedInt(out, 2);
					out.writeShort((Short) primValue);
					break;
				case 'Z':
					JdkStream.writeEscapedInt(out, 1);
					out.writeBoolean((Boolean) primValue);
					break;
				case 'L': case '[':
					objValue.writeJrank(out);
					break;
				}
			}
			
		}
		
		protected ClassdescBlock classDesc;
		protected List<FieldValue> values = new ArrayList<FieldValue>();
		protected List<ContentRule> objectAnnotation = new ArrayList<ContentRule>();

		public Classdata(JdkStream jdk, ClassdescBlock classDesc) {
			super(jdk);
			this.classDesc = classDesc;
		}

		@Override
		public Classdata parse() throws IOException {
			byte flags = classDesc.getClassDescFlags();
			if((flags & ObjectStreamConstants.SC_EXTERNALIZABLE) == 0) {
				for(Field f : classDesc.getFields())
					values.add(new FieldValue(jdk, f).parse());
			}
			if(
					(flags & ObjectStreamConstants.SC_SERIALIZABLE) == ObjectStreamConstants.SC_SERIALIZABLE
					&& (flags & ObjectStreamConstants.SC_WRITE_METHOD) == 0)
				return this;
			Block a;
			while((a = jdk.readBlock()) instanceof ContentRule)
				objectAnnotation.add((ContentRule) a);
			EndblockdataBlock.class.cast(a);
			return this;
		}

		@Override
		public void writeJrank(DataOutputStream out) throws IOException {
			byte flags = classDesc.getClassDescFlags();
			if((flags & ObjectStreamConstants.SC_SERIALIZABLE) == ObjectStreamConstants.SC_SERIALIZABLE) {
				out.write(JrankConstants.J_FIELDS);
				for(FieldValue fv : values) {
					fv.writeJrank(out);
				}
				out.write(JrankConstants.J_WALL);
				if((flags & ObjectStreamConstants.SC_WRITE_METHOD) == ObjectStreamConstants.SC_WRITE_METHOD) {
					for(ContentRule c : objectAnnotation)
						c.writeJrank(out);
				}
				out.write(JrankConstants.J_WALL);
			} else if((flags & ObjectStreamConstants.SC_EXTERNALIZABLE) == ObjectStreamConstants.SC_EXTERNALIZABLE) {
				for(ContentRule c : objectAnnotation)
					c.writeJrank(out);
				out.write(JrankConstants.J_WALL);
			}
			
		}
		
	}
	
	public ObjectBlock(JdkStream jdk) {
		super(jdk);
	}

	@Override
	public Block parse() throws IOException {
		classDesc = jdk.readBlock(ClassdescRule.class);
		jdk.wireBlock(this);
		byte flags = 0;
		ClassdescRule cr = classDesc;
		if(cr instanceof ReferenceBlock)
			cr = (ClassdescRule) ((ReferenceBlock) cr).getWired();
		if(cr instanceof ClassdescBlock)
			flags = ((ClassdescBlock) cr).getClassDescFlags();
		
		if((flags & ObjectStreamConstants.SC_EXTERNALIZABLE) == ObjectStreamConstants.SC_EXTERNALIZABLE) {
			classdata.add(new Classdata(jdk, (ClassdescBlock) cr).parse());
		} else if((flags & ObjectStreamConstants.SC_SERIALIZABLE) == ObjectStreamConstants.SC_SERIALIZABLE) {
			List<ClassdescBlock> cbl = new ArrayList<ClassdescBlock>();
			cbl.add(0, (ClassdescBlock) cr);
			ClassdescBlock cb = cr.getSuperClassDesc();
			while(cb != null) {
				cbl.add(0, cb);
				cb = cb.getSuperClassDesc();
			}
			for(ClassdescBlock c : cbl)
				classdata.add(new Classdata(jdk, c).parse());
		}
		
		return this;
	}

	@Override
	public void writeJrank(DataOutputStream out) throws IOException {
		out.write(JrankConstants.J_OBJECT);
		classDesc.writeJrank(out);
		for(Classdata c : classdata)
			c.writeJrank(out);
	}

}
