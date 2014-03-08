package org.purplejrank;

import java.io.Externalizable;
import java.io.IOException;
import java.io.NotActiveException;
import java.io.NotSerializableException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;

public class PurpleJrankOutput extends ObjectOutputStream implements ObjectOutput {
	private boolean isClosed = false;
	private WritableByteChannel out;
	private ByteBuffer buf = ByteBuffer.allocateDirect(JrankConstants.MAX_BLOCK_SIZE);
	private boolean blockMode = false;
	private ByteBuffer blockHeader = ByteBuffer.allocateDirect(5);
	
	private Map<Object, Integer> wired = new IdentityHashMap<Object, Integer>();
	private Map<Class<?>, JrankClass> classdesc = new IdentityHashMap<Class<?>, JrankClass>();
	private Deque<JrankContext> context = new ArrayDeque<JrankContext>(Arrays.asList(JrankContext.NO_CONTEXT));
	
	public PurpleJrankOutput(WritableByteChannel out) throws IOException {
		this.out = out;
		buf.putInt(JrankConstants.MAGIC);
		buf.putInt(JrankConstants.VERSION);
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
	protected void writeObjectOverride(Object obj) throws IOException {
		writeObject0(obj, true);
	}
	
	protected void writeObject0(Object obj, boolean shared) throws IOException {
		ensureOpen().setBlockMode(false);
		
		if(obj == null) {
			ensureCapacity(1).put(JrankConstants.NULL);
			return;
		}
		
		if(!(obj instanceof Serializable))
			throw new NotSerializableException(obj.getClass().getName());
		
		if(shared && wired.containsKey(obj)) {
			ensureCapacity(6).put(JrankConstants.REFERENCE);
			writeEscapedInt(wired.get(obj));
			return;
		}
		
		if(!wired.containsKey(obj))
			wired.put(obj, wired.size());
		
		if(obj.getClass().isArray()) {
			ensureCapacity(1).put(JrankConstants.ARRAY);
			JrankClass d = writeClassDesc(obj.getClass());
			context.offerLast(new JrankContext(d, obj));
			ensureCapacity(5);
			int size;
			writeEscapedInt(size = Array.getLength(obj));
			Class<?> cmp = obj.getClass().getComponentType();
			for(int i = 0; i < size; i++) {
				if(cmp == byte.class) ensureCapacity(1).put(Array.getByte(obj, i));
				else if(cmp == char.class) ensureCapacity(2).putChar(Array.getChar(obj, i));
				else if(cmp == double.class) ensureCapacity(8).putDouble(Array.getDouble(obj, i));
				else if(cmp == float.class) ensureCapacity(4).putFloat(Array.getFloat(obj, i));
				else if(cmp == int.class) ensureCapacity(4).putInt(Array.getInt(obj, i));
				else if(cmp == long.class) ensureCapacity(8).putLong(Array.getLong(obj, i));
				else if(cmp == short.class) ensureCapacity(8).putShort(Array.getShort(obj, i));
				else if(cmp == boolean.class) ensureCapacity(1).put((byte)(Array.getBoolean(obj, i) ? 1 : 0));
				else writeObject(Array.get(obj, i));
			}
			context.pollLast();
			return;
		}
		
		if(obj instanceof String) {
			ensureCapacity(1).put(JrankConstants.STRING);
			writeUTF((String) obj, false);
			return;
		}
		
		if(obj instanceof Enum<?>) {
			ensureCapacity(1).put(JrankConstants.ENUM);
			writeClassDesc(obj.getClass());
			writeObject(((Enum<?>) obj).name());
		}
		
		ensureCapacity(1).put(JrankConstants.OBJECT);
		JrankClass d = writeClassDesc(obj.getClass());
		
		if(d.getFlags() == JrankConstants.SC_WRITE_EXTERNAL) {
			context.offerLast(new JrankContext(d, obj));
			((Externalizable) obj).writeExternal(this);
			context.pollLast();
		} else {
			JrankClass t = d;
			while(t != null) {
				context.offerLast(new JrankContext(t, obj));
				if(t.getFlags() == JrankConstants.SC_WRITE_OBJECT) {
					try {
						t.getType().getDeclaredMethod("writeObject", ObjectOutputStream.class).invoke(obj, this);
					} catch(Exception e) {
						throw new IOException(e);
					}
					setBlockMode(false).ensureCapacity(1).put(JrankConstants.WALL);
				} else {
					writeFields(t, obj);
					setBlockMode(false).ensureCapacity(1).put(JrankConstants.WALL);
				}
				context.pollLast();
				t = t.getParent();
			}
		}
		
	}
	
	private void writeFields(JrankClass t, Object obj) throws IOException {
		setBlockMode(false).ensureCapacity(1).put(JrankConstants.FIELDS);
		for(Field f : t.getFields()) {
			Class<?> fc = f.getType();
			try {
				if(fc == byte.class) writeByte(f.getByte(obj));
				else if(fc == char.class) writeChar(f.getChar(obj));
				else if(fc == double.class) writeDouble(f.getDouble(obj));
				else if(fc == float.class) writeFloat(f.getFloat(obj));
				else if(fc == int.class) writeInt(f.getInt(obj));
				else if(fc == long.class) writeLong(f.getLong(obj));
				else if(fc == short.class) writeShort(f.getShort(obj));
				else if(fc == boolean.class) writeBoolean(f.getBoolean(obj));
				else writeObject(f.get(obj));
			} catch(IOException ioe) {
				throw ioe;
			} catch(Exception e) {
				throw new IOException(e);
			}
		}
	}

	private JrankClass writeClassDesc(Class<?> cls) throws IOException {
		setBlockMode(false);
		
		if(cls == null) {
			ensureCapacity(1).put(JrankConstants.NULL);
			return null;
		}
			
		if(classdesc.containsKey(cls)) {
			ensureCapacity(6).put(JrankConstants.REFERENCE);
			writeEscapedInt(wired.get(cls));
			return classdesc.get(cls);
		}
		
		JrankClass d;
		wired.put(cls, wired.size());
		classdesc.put(cls, d = new JrankClass(cls));
		
		if(d.isProxy()) {
			ensureCapacity(6).put(JrankConstants.PROXYCLASSDESC);
			writeEscapedInt(d.getProxyInterfaceNames().length);
			for(String ifc : d.getProxyInterfaceNames())
				writeUTF(ifc, false);
		} else {
			ensureCapacity(1).put(JrankConstants.CLASSDESC);
			writeUTF(d.getName(), false);
			ensureCapacity(3).put(d.getFlags()).putShort((short) d.getFieldNames().length);
			for(int i = 0; i < d.getFieldNames().length; i++) {
				ensureCapacity(1).put((byte) d.getFieldTypes()[i].charAt(0));
				writeUTF(d.getFieldNames()[i], false);
				if(d.getFieldTypes()[i].length() > 1)
					writeUTF(d.getFieldTypes()[i].substring(1), false);
			}
		}
		
		if(cls.getSuperclass() != null && Serializable.class.isAssignableFrom(cls.getSuperclass()))
			d.setParent(writeClassDesc(cls.getSuperclass()));
		
		return d;
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
			blockHeader.put(JrankConstants.BLOCK_DATA);
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

	@Override
	public void writeUnshared(Object obj) throws IOException {
		writeObject0(obj, false);
	}

	@Override
	public void defaultWriteObject() throws IOException {
		JrankContext ctx = context.peekLast();
		if(ctx == JrankContext.NO_CONTEXT)
			throw new NotActiveException();
		writeFields(ctx.getType(), ctx.getObject());
		setBlockMode(false).ensureCapacity(1).put(JrankConstants.WALL);
	}

	@Override
	public PutField putFields() throws IOException {
		// TODO Auto-generated method stub
		return super.putFields();
	}

	@Override
	public void writeFields() throws IOException {
		// TODO Auto-generated method stub
		super.writeFields();
	}

	@Override
	public void reset() throws IOException {
		// TODO Auto-generated method stub
		super.reset();
	}
}
