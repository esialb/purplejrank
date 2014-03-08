package org.purplejrank;

import java.io.IOException;
import java.io.ObjectOutput;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

public class PurpleJrankOutput implements ObjectOutput {
	private boolean isClosed = false;
	private WritableByteChannel out;
	private ByteBuffer buf = ByteBuffer.allocateDirect(JrankableConstants.MAX_BLOCK_SIZE);
	private boolean blockMode = false;
	private ByteBuffer blockHeader = ByteBuffer.allocateDirect(5);
	
	private Map<Object, Integer> wired = new IdentityHashMap<Object, Integer>();
	
	public PurpleJrankOutput(WritableByteChannel out) throws IOException {
		this.out = out;
		buf.putInt(JrankableConstants.MAGIC);
		buf.putInt(JrankableConstants.VERSION);
		flush();
	}
	
	private PurpleJrankOutput ensureOpen() throws IOException {
		if(isClosed)
			throw new IOException("channel closed");
		return this;
	}
	
	private PurpleJrankOutput setBlockMode(boolean blockMode) throws IOException {
		if(blockMode != this.blockMode)
			flush();
		this.blockMode = blockMode;
		return this;
	} 
	
	private ByteBuffer ensureCapacity(int capacity) throws IOException {
		if(buf.remaining() < capacity)
			flush();
		return buf;
	}
	
	@Override
	public void writeBoolean(boolean v) throws IOException {
		ensureOpen().setBlockMode(true).ensureCapacity(1).put((byte)(v ? 1 : 0));
	}

	@Override
	public void writeByte(int v) throws IOException {
		ensureOpen().setBlockMode(true).ensureCapacity(1).put((byte) v);
	}

	@Override
	public void writeShort(int v) throws IOException {
		ensureOpen().setBlockMode(true).ensureCapacity(2).putShort((short) v);
	}

	@Override
	public void writeChar(int v) throws IOException {
		ensureOpen().setBlockMode(true).ensureCapacity(2).putChar((char) v);
	}

	@Override
	public void writeInt(int v) throws IOException {
		ensureOpen().setBlockMode(true).ensureCapacity(4).putInt(v);
	}

	@Override
	public void writeLong(long v) throws IOException {
		ensureOpen().setBlockMode(true).ensureCapacity(8).putLong(v);
	}

	@Override
	public void writeFloat(float v) throws IOException {
		ensureOpen().setBlockMode(true).ensureCapacity(4).putFloat(v);
	}

	@Override
	public void writeDouble(double v) throws IOException {
		ensureOpen().setBlockMode(true).ensureCapacity(8).putDouble(v);
	}

	@Override
	public void writeBytes(String s) throws IOException {
		for(int i = 0; i < s.length(); i++)
			writeByte(s.charAt(i));
	}

	@Override
	public void writeChars(String s) throws IOException {
		for(int i = 0; i < s.length(); i++)
			writeChar(s.charAt(i));
	}
	
	private void writeEscapedInt(int v) throws IOException {
		ensureCapacity(1);
		if((v & 0x7f) != v) {
			buf.put((byte)(0x80 | (0x7f & v)));
			writeEscapedInt(v >>> 7);
		} else
			buf.put((byte)v);
	}

	@Override
	public void writeUTF(String s) throws IOException {
		writeUTF(s, true);
	}
	
	private void writeUTF(String s, boolean blockMode) throws IOException {
		// Instead of the JRE's modified UTF-8, write bit-8-escaped ints
		setBlockMode(blockMode);
		for(int i = 0; i < s.length(); i++) {
			writeEscapedInt(s.charAt(i) + 1);
		}
		writeEscapedInt(0);
	}

	@Override
	public void writeObject(Object obj) throws IOException {
		// TODO Auto-generated method stub
		ensureOpen().setBlockMode(false);
	}

	private void writeClassDesc(Class<?> cls) throws IOException {
		setBlockMode(false);
		
		if(cls == null) {
			ensureCapacity(1);
			buf.put(JrankableConstants.NULL);
			return;
		}
			
		if(wired.containsKey(cls)) {
			ensureCapacity(6);
			buf.put(JrankableConstants.REFERENCE);
			writeEscapedInt(wired.get(cls));
			return;
		}
		
		wired.put(cls, wired.size());
		
		if(Proxy.isProxyClass(cls)) {
			ensureCapacity(6).put(JrankableConstants.PROXYCLASSDESC);
			Class<?>[] ifcs = cls.getInterfaces();
			writeEscapedInt(ifcs.length);
			for(Class<?> ifc : ifcs)
				writeUTF(ifc.getName(), false);
		} else {
			byte flags = JrankableConstants.SC_WRITE_FIELDS;
			if(Enum.class.isAssignableFrom(cls))
				flags = JrankableConstants.SC_WRITE_ENUM;
			else if(Jrankternalizable.class.isAssignableFrom(cls))
				flags = JrankableConstants.SC_WRITE_EXTERNAL;
			else {
				try {
					cls.getDeclaredMethod("writeObject", PurpleJrankOutput.class);
					flags = JrankableConstants.SC_WRITE_OBJECT;
				} catch(NoSuchMethodException e) {
				}
			}
			Field[] fields = cls.getDeclaredFields();
			ensureCapacity(4).put(JrankableConstants.CLASSDESC).put(flags).putShort((short) fields.length);
			for(Field f : fields) {
				ensureCapacity(1);
				if(f.getType() == byte.class) buf.put((byte) 'B');
				else if(f.getType() == char.class) buf.put((byte) 'C');
				else if(f.getType() == double.class) buf.put((byte) 'D');
				else if(f.getType() == float.class) buf.put((byte) 'F');
				else if(f.getType() == int.class) buf.put((byte) 'I');
				else if(f.getType() == long.class) buf.put((byte) 'J');
				else if(f.getType() == short.class) buf.put((byte) 'S');
				else if(f.getType() == boolean.class) buf.put((byte) 'Z');
				else if(f.getType().isArray()) buf.put((byte) '[');
				else buf.put((byte) 'L');
				
				writeUTF(f.getName(), false);
				
				if(f.getType().isArray())
					writeObject("[" + f.getType().getComponentType().getName() + ";");
				else if(!f.getType().isPrimitive())
					writeObject("L" + f.getType().getName() + ";");
			}
		}
		
		writeClassDesc(cls.getSuperclass());
	}
	
	@Override
	public void write(int b) throws IOException {
		ensureOpen().setBlockMode(true).ensureCapacity(1).put((byte) b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		ensureOpen().setBlockMode(true);
		int pos = 0;
		while(pos < b.length) {
			int r = Math.min(buf.remaining(), b.length - pos);
			buf.put(b, pos, r);
			pos += r;
			if(pos < b.length)
				flush();
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		write(Arrays.copyOfRange(b, off, off + len));
	}

	@Override
	public void flush() throws IOException {
		ensureOpen();
		if(this.blockMode) {
			blockHeader.put(JrankableConstants.BLOCK_DATA);
			blockHeader.putInt(buf.position());
			out.write((ByteBuffer) blockHeader.flip());
			blockHeader.clear();
		}
		out.write((ByteBuffer) buf.flip());
		buf.clear();
	}

	@Override
	public void close() throws IOException {
		if(isClosed)
			return;
		flush();
		isClosed = true;
		out.close();
	}
}
