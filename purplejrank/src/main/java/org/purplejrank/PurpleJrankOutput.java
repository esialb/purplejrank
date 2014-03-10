package org.purplejrank;

import java.io.Externalizable;
import java.io.Flushable;
import java.io.IOException;
import java.io.NotActiveException;
import java.io.NotSerializableException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;

import org.purplejrank.io.StreamWritableByteChannel;
import org.purplejrank.reflect.FieldCache;
import org.purplejrank.reflect.MethodCache;

/**
 * Extension of {@link ObjectOutputStream} with a protocol based on {@link ObjectOutputStream}
 * but slightly more robust.
 * @author robin
 *
 */
public class PurpleJrankOutput extends ObjectOutputStream implements ObjectOutput {
	protected WritableByteChannel out;
	protected ByteBuffer buf = ByteBuffer.allocateDirect(JrankConstants.MAX_BLOCK_SIZE);
	protected boolean blockMode = false;
	protected ByteBuffer blockHeader = ByteBuffer.allocateDirect(5);
	
	protected FieldCache fieldCache = new FieldCache();
	protected MethodCache methodCache = new MethodCache();
	protected Map<Object, Integer> wired = new IdentityHashMap<Object, Integer>();
	protected int nextHandle = 0;
	protected Map<Class<?>, JrankClass> classdesc = new IdentityHashMap<Class<?>, JrankClass>();
	protected Deque<JrankContext> context = new ArrayDeque<JrankContext>(Arrays.asList(JrankContext.NO_CONTEXT));
	
	public PurpleJrankOutput(OutputStream out) throws IOException {
		this(new StreamWritableByteChannel(out));
	}
	
	public PurpleJrankOutput(WritableByteChannel out) throws IOException {
		this.out = out;
		buf.putInt(JrankConstants.MAGIC);
		buf.putInt(JrankConstants.VERSION);
		flush();
	}
	
	protected PurpleJrankOutput ensureOpen() throws IOException {
		if(!out.isOpen())
			throw new JrankStreamException("channel closed");
		return this;
	}
	
	protected PurpleJrankOutput setBlockMode(boolean blockMode) throws IOException {
		if(blockMode != this.blockMode)
			flush();
		this.blockMode = blockMode;
		return this;
	} 
	
	protected ByteBuffer ensureCapacity(int capacity) throws IOException {
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
	
	protected void writeEscapedInt(int v) throws IOException {
		ensureCapacity(1);
		if((v & 0x7f) != v) {
			buf.put((byte)(0x80 | (0x7f & v)));
			writeEscapedInt(v >>> 7);
		} else
			buf.put((byte)v);
	}
	
	protected void writeEscapedInt(ByteBuffer buf, int v) throws IOException {
		ensureCapacity(1);
		if((v & 0x7f) != v) {
			buf.put((byte)(0x80 | (0x7f & v)));
			writeEscapedInt(buf, v >>> 7);
		} else
			buf.put((byte)v);
	}

	protected void writeEscapedLong(long v) throws IOException {
		ensureCapacity(1);
		if((v & 0x7f) != v) {
			buf.put((byte)(0x80 | (0x7f & v)));
			writeEscapedLong(v >>> 7);
		} else
			buf.put((byte)v);
	}
	
	@Override
	public void writeUTF(String s) throws IOException {
		writeUTF(s, true);
	}
	
	protected void writeUTF(String s, boolean blockMode) throws IOException {
		// Instead of the JRE's modified UTF-8, write bit-8-escaped ints
		setBlockMode(blockMode);
		for(int i = 0; i < s.length(); i++) {
			int c = s.charAt(i);
			if(c == 0)
				c = 0x1ffff;
			writeEscapedInt(c);
		}
		writeEscapedInt(0);
	}

	@Override
	protected void writeObjectOverride(Object obj) throws IOException {
		writeObject0(obj, true);
		flush();
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
		
		Object preReplace = obj;
		
		Method writeReplace = findWriteReplace(obj);
		if(writeReplace != null) {
			try {
				obj = writeReplace.invoke(obj);
			} catch(Exception ex) {
				throw new JrankStreamException(ex);
			}
		}
		
		if(obj.getClass().isArray()) {
			ensureCapacity(1).put(JrankConstants.ARRAY);
			JrankClass d = writeClassDesc(obj.getClass());
			if(!wired.containsKey(obj))
				wired.put(obj, nextHandle++);
			int size;
			setBlockMode(false).writeEscapedInt(size = Array.getLength(obj));
			Class<?> cmp = obj.getClass().getComponentType();
			context.offerLast(new JrankContext(d, obj));
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
			if(!wired.containsKey(obj))
				wired.put(obj, nextHandle++);
			ensureCapacity(1).put(JrankConstants.STRING);
			writeUTF((String) obj, false);
			return;
		}
		
		if(obj instanceof Enum<?>) {
			ensureCapacity(1).put(JrankConstants.ENUM);
			writeClassDesc(obj.getClass());
			writeObject0(((Enum<?>) obj).name(), true);
			wired.put(obj, nextHandle++);
			return;
		}
		
		ensureCapacity(1).put(JrankConstants.OBJECT);
		JrankClass d = writeClassDesc(obj.getClass());
		if(!wired.containsKey(obj)) {
			wired.put(obj, nextHandle);
			wired.put(preReplace, nextHandle);
			nextHandle++;
		}
		
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
						Method m = methodCache.get(t.getType(), "writeObject", ObjectOutputStream.class);
						m.invoke(obj, this);
					} catch(Exception e) {
						throw new JrankStreamException(e);
					}
				} else {
					defaultWriteObject();
				}
				setBlockMode(false).ensureCapacity(1).put(JrankConstants.WALL);
				context.pollLast();
				t = t.getParent();
			}
		}
		setBlockMode(false).ensureCapacity(1).put(JrankConstants.WALL);
		
	}
	
	protected JrankClass writeClassDesc(Class<?> cls) throws IOException {
		setBlockMode(false);
		
		if(cls == null || !Serializable.class.isAssignableFrom(cls)) {
			ensureCapacity(1).put(JrankConstants.NULL);
			return null;
		}
			
		if(classdesc.containsKey(cls)) {
			ensureCapacity(6).put(JrankConstants.REFERENCE);
			writeEscapedInt(wired.get(cls));
			return classdesc.get(cls);
		}
		
		JrankClass d;
		wired.put(cls, nextHandle++);
		classdesc.put(cls, d = new JrankClass(cls, fieldCache));
		
		if(d.isProxy()) {
			ensureCapacity(1).put(JrankConstants.PROXYCLASSDESC);
			writeEscapedInt(d.getProxyInterfaceNames().length);
			for(String ifc : d.getProxyInterfaceNames())
				writeUTF(ifc, false);
		} else {
			ensureCapacity(1).put(JrankConstants.CLASSDESC);
			writeUTF(d.getName(), false);
			writeEscapedLong(ObjectStreamClass.lookupAny(cls).getSerialVersionUID());
			ensureCapacity(3).put(d.getFlags()).putShort((short) d.getFieldNames().length);
			for(int i = 0; i < d.getFieldNames().length; i++) {
				ensureCapacity(1).put((byte) d.getFieldTypes()[i].charAt(0));
				writeUTF(d.getFieldNames()[i], false);
				if(d.getFieldTypes()[i].length() > 1)
					writeUTF(d.getFieldTypes()[i].substring(1), false);
			}
		}
		
		d.setParent(writeClassDesc(cls.getSuperclass()));
		
		return d;
	}
	
	protected Method findWriteReplace(Object obj) {
		Class<?> cls = obj.getClass();
		while(cls != null) {
			try {
				Method m = methodCache.get(cls, "writeReplace");
				return m;
			} catch(NoSuchMethodException e) {}
			cls = cls.getSuperclass();
		}
		return null;
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
			writeEscapedInt(blockHeader, buf.position());
			out.write((ByteBuffer) blockHeader.flip());
			blockHeader.clear();
		}
		out.write((ByteBuffer) buf.flip());
		buf.clear();
		if(out instanceof Flushable)
			((Flushable) out).flush();
	}

	@Override
	public void close() throws IOException {
		flush();
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
		putFields();
		writeFields();
	}

	@Override
	public PutField putFields() throws IOException {
		JrankContext ctx = context.peekLast();
		if(ctx == JrankContext.NO_CONTEXT)
			throw new NotActiveException();
		JrankPutFields pf = ctx.getPutFields();
		try {
			for(Field f : ctx.getType().getFields()) {
				pf.put(f.getName(), f.get(ctx.getObject()));
			}
		} catch(Exception e) {
			throw new JrankStreamException(e);
		}
		return pf;
	}

	@Override
	public void writeFields() throws IOException {
		JrankContext ctx = context.peekLast();
		if(ctx == JrankContext.NO_CONTEXT)
			throw new NotActiveException();
		setBlockMode(false).ensureCapacity(1).put(JrankConstants.FIELDS);
//		for(String fn : ctx.getType().getFieldNames()) {
		for(int i = 0; i < ctx.getType().getFieldNames().length; i++) {
			String name = ctx.getType().getFieldNames()[i];
			Class<?> fc = ctx.getType().getFieldClasses()[i];
			Object val = ctx.getPutFields().get(name);
			System.out.println(ctx.getType() + ":" + name + " " + fc + " " + val);
			try {
				if(fc == byte.class) writeByte((Byte) val);
				else if(fc == char.class) writeChar((Character) val);
				else if(fc == double.class) writeDouble((Double) val);
				else if(fc == float.class) writeFloat((Float) val);
				else if(fc == int.class) writeInt((Integer) val);
				else if(fc == long.class) writeLong((Long) val);
				else if(fc == short.class) writeShort((Short) val);
				else if(fc == boolean.class) writeBoolean((Boolean) val);
				else writeObject0(val, true);
			} catch(IOException ioe) {
				throw ioe;
			} catch(Exception e) {
				throw new JrankStreamException(e);
			}
		}
		setBlockMode(false).ensureCapacity(1).put(JrankConstants.WALL);
	}

	@Override
	public void reset() throws IOException {
		setBlockMode(false).ensureCapacity(1).put(JrankConstants.RESET);
		wired.clear();
		classdesc.clear();
	}

	@Deprecated
	@Override
	public final void useProtocolVersion(int version) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	protected final void annotateClass(Class<?> cl) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	protected final void annotateProxyClass(Class<?> cl) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	protected final Object replaceObject(Object obj) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	protected final boolean enableReplaceObject(boolean enable)
			throws SecurityException {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	protected final void writeStreamHeader() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	protected final void writeClassDescriptor(ObjectStreamClass desc)
			throws IOException {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	protected final void drain() throws IOException {
		throw new UnsupportedOperationException();
	}
}
