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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.purplejrank.io.StreamWritableByteChannel;
import org.purplejrank.reflect.FieldCache;
import org.purplejrank.reflect.MethodCache;

import static org.purplejrank.JrankConstants.*;

/**
 * Extension of {@link ObjectOutputStream} with a protocol based on {@link ObjectOutputStream}
 * but slightly more robust.  Writes to a {@link WritableByteChannel} rather than an {@link OutputStream}
 * @author robin
 *
 */
public class PurpleJrankOutput extends ObjectOutputStream implements ObjectOutput {
	/**
	 * The backing {@link WritableByteChannel}
	 */
	protected WritableByteChannel out;
	/**
	 * Write buffer
	 */
	protected ByteBuffer buf = ByteBuffer.allocateDirect(J_MAX_BLOCK_SIZE);
	/**
	 * Whether the write buffer currently contains block data
	 */
	protected boolean blockMode = false;
	/**
	 * Temp buffer used for writing block headers
	 */
	protected ByteBuffer blockHeader = ByteBuffer.allocateDirect(6);
	
	/**
	 * Reflection cache of fields
	 */
	protected FieldCache fieldCache = new FieldCache();
	/**
	 * Reflection cache of methods
	 */
	protected MethodCache methodCache = new MethodCache();
	/**
	 * Wire handles by object
	 */
	protected Map<Object, Integer> wired = new IdentityHashMap<Object, Integer>();
	/**
	 * The next wire handle
	 */
	protected int nextHandle = 0;
	/**
	 * Class descriptors by class
	 */
	protected Map<Class<?>, JrankClass> classdesc = new IdentityHashMap<Class<?>, JrankClass>();
	/**
	 * Serialization contects for writeObject
	 */
	protected Deque<JrankContext> context = new ArrayDeque<JrankContext>(Arrays.asList(JrankContext.NO_CONTEXT));
	
	public PurpleJrankOutput(OutputStream out) throws IOException {
		this(new StreamWritableByteChannel(out));
	}
	
	public PurpleJrankOutput(WritableByteChannel out) throws IOException {
		this.out = out;
		buf.putInt(J_MAGIC);
		buf.putInt(J_VERSION);
		dump();
	}
	
	/**
	 * Ensure the output is open
	 * @return
	 * @throws IOException
	 */
	protected PurpleJrankOutput ensureOpen() throws IOException {
		if(!out.isOpen())
			throw new JrankStreamException("channel closed");
		return this;
	}
	
	/**
	 * Set the block writing mode.  Dumps the buffer if the mode changes.
	 * @param blockMode
	 * @return
	 * @throws IOException
	 */
	protected PurpleJrankOutput setBlockMode(boolean blockMode) throws IOException {
		if(blockMode != this.blockMode)
			dump();
		this.blockMode = blockMode;
		return this;
	} 
	
	/**
	 * Dump the buffer if its remaining size is less than the required capacity
	 * @param capacity
	 * @return
	 * @throws IOException
	 */
	protected ByteBuffer ensureCapacity(int capacity) throws IOException {
		if(buf.remaining() < capacity)
			dump();
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
	
	/**
	 * Write an escaped int.  For non-negative ints this tends to be smaller than {@link #writeInt(int)}
	 * @param v
	 * @throws IOException
	 */
	protected void writeEscapedInt(int v) throws IOException {
		ensureCapacity(1);
		if((v & 0x7f) != v) {
			buf.put((byte)(0x80 | (0x7f & v)));
			writeEscapedInt(v >>> 7);
		} else
			buf.put((byte)v);
	}

	/**
	 * Write an escaped int.  For non-negative ints this tends to be smaller than {@link #writeInt(int)}
	 * @param buf
	 * @param v
	 * @throws IOException
	 */
	protected void writeEscapedInt(ByteBuffer buf, int v) throws IOException {
		if((v & 0x7f) != v) {
			buf.put((byte)(0x80 | (0x7f & v)));
			writeEscapedInt(buf, v >>> 7);
		} else
			buf.put((byte)v);
	}

	/**
	 * Write an escaped long.  For non-negative longs this tends to be smaller than {@link #writeLong(long)}
	 * @param v
	 * @throws IOException
	 */
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
	
	/**
	 * Write a string using escaped ints for the character numbers, with
	 * a specified block mode
	 * @param s
	 * @param blockMode
	 * @throws IOException
	 */
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
	
	/**
	 * Write an object to the stream
	 * @param obj
	 * @param shared
	 * @throws IOException
	 */
	protected void writeObject0(Object obj, boolean shared) throws IOException {
		ensureOpen().setBlockMode(false);
		
		if(obj == null) { // write a null
			ensureCapacity(1).put(J_NULL);
			return;
		}
		
		if(!(obj instanceof Serializable)) // require objects be Serializable
			throw new NotSerializableException(obj.getClass().getName());
		
		// Write a reference if writing in shared mode and the object has already been written
		if(shared && wired.containsKey(obj)) {
			ensureCapacity(6).put(J_REFERENCE);
			writeEscapedInt(wired.get(obj));
			return;
		}
		
		Object preReplace = obj;
		
		// Call writeReplace if available
		Method writeReplace = findWriteReplace(obj);
		if(writeReplace != null) {
			try {
				obj = writeReplace.invoke(obj);
			} catch(Exception ex) {
				throw new JrankStreamException(ex);
			}
		}
		
		if(obj.getClass().isArray()) { // write an array
			ensureCapacity(1).put(J_ARRAY);
			JrankClass d = writeClassDesc(obj.getClass());
			if(!wired.containsKey(obj))
				wired.put(obj, nextHandle++);
			int size;
			setBlockMode(false).writeEscapedInt(size = Array.getLength(obj));
			Class<?> cmp = obj.getClass().getComponentType();
			context.offerLast(new JrankContext(d, obj));
			for(int i = 0; i < size; i++) {
				// primitive data is blocked
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
		
		if(obj instanceof String) { // write a string
			if(!wired.containsKey(obj))
				wired.put(obj, nextHandle++);
			ensureCapacity(1).put(J_STRING);
			writeUTF((String) obj, false);
			return;
		}
		
		if(obj instanceof Enum<?>) { // write an enum
			ensureCapacity(1).put(J_ENUM);
			writeClassDesc(obj.getClass());
			wired.put(obj, nextHandle++);
			writeObject0(((Enum<?>) obj).name(), true);
			return;
		}
		
		if(obj instanceof Class<?>) { // write a class
			ensureCapacity(1).put(J_CLASS);
			writeClassDesc((Class<?>) obj);
			wired.put(obj, nextHandle++);
			return;
		}
		
		// write a new (or unshared) object
		ensureCapacity(1).put(J_OBJECT);
		JrankClass d = writeClassDesc(obj.getClass());
		if(!wired.containsKey(obj)) {
			wired.put(obj, nextHandle);
			wired.put(preReplace, nextHandle);
			nextHandle++;
		}
		
		if((d.getFlags() & J_SC_WRITE_EXTERNAL) == J_SC_WRITE_EXTERNAL) {
			// externalizable
			context.offerLast(JrankContext.NO_CONTEXT); // no writeObject context for writeExternal
			((Externalizable) obj).writeExternal(this);
			context.pollLast();
			setBlockMode(false).ensureCapacity(1).put(J_WALL);
		} else {
			// serializable
			List<JrankClass> rc = new ArrayList<JrankClass>(); // class descriptors in reverse order
			for(JrankClass t = d; t != null; t = t.getParent()) {
				rc.add(0, t);
			}
			for(JrankClass t : rc) {
				if((t.getFlags() & J_SC_SERIALIZABLE) == 0) // don't write non-serializable classes
					continue;
				context.offerLast(new JrankContext(t, obj)); // writeObject context
				if((t.getFlags() & J_SC_WRITE_OBJECT) == J_SC_WRITE_OBJECT) {
					// invoke writeObject
					Method m = methodCache.declared(t.getType(), "writeObject", ObjectOutputStream.class);
					try {
						m.invoke(obj, this);
					} catch(Exception e) {
						throw new JrankStreamException(e);
					}
				} else {
					// just write the fields
					defaultWriteObject();
				}
				// drop a wall
				setBlockMode(false).ensureCapacity(1).put(J_WALL);
				context.pollLast();
			}
		}
		
	}
	
	/**
	 * Write a class descriptor
	 * @param cls
	 * @return
	 * @throws IOException
	 */
	protected JrankClass writeClassDesc(Class<?> cls) throws IOException {
		setBlockMode(false);
		
		if(cls == null) {
			ensureCapacity(1).put(J_NULL);
			return null;
		}
			
		if(classdesc.containsKey(cls)) { // reference to previous class descriptor
			ensureCapacity(1).put(J_REFERENCE);
			JrankClass d = classdesc.get(cls);
			writeEscapedInt(wired.get(d));
			return d;
		}
		
		JrankClass d = new JrankClass(cls, fieldCache);
		wired.put(d, nextHandle++);
		classdesc.put(cls, d);
		
		if(d.isProxy()) { // proxy class
			ensureCapacity(1).put(J_PROXYCLASSDESC);
			writeEscapedInt(d.getProxyInterfaceNames().length);
			for(String ifc : d.getProxyInterfaceNames())
				writeUTF(ifc, false);
		} else { // concrete class
			ensureCapacity(1).put(J_CLASSDESC);
			writeUTF(d.getName(), false);
			writeEscapedLong(ObjectStreamClass.lookupAny(cls).getSerialVersionUID());
			ensureCapacity(3).put(d.getFlags()).putShort((short) d.getFieldNames().length);
			for(int i = 0; i < d.getFieldNames().length; i++) {
				ensureCapacity(1).put((byte) d.getFieldTypes()[i].charAt(0));
				writeUTF(d.getFieldNames()[i], false);
				if(d.getFieldTypes()[i].length() > 1)
					writeObject0(d.getFieldTypes()[i], true);
			}
		}
		
		// write parent descriptor
		Class<?> sc = cls.getSuperclass();
		if(sc != null && !Serializable.class.isAssignableFrom(sc))
			sc = null;
		d.setParent(writeClassDesc(sc));
		
		return d;
	}
	
	/**
	 * Find a {@link Method} to invoke as writeReplace
	 * @param obj
	 * @return
	 */
	protected Method findWriteReplace(Object obj) {
		Class<?> cls = obj.getClass();
		return methodCache.find(cls, "writeReplace");
	}
	
	@Override
	public void write(int b) throws IOException {
		ensureOpen().setBlockMode(true).ensureCapacity(1).put((byte) b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		ensureOpen().setBlockMode(true);
		int pos = off;
		while(pos < off + len) {
			int r = Math.min(buf.remaining(), off + len - pos);
			buf.put(b, pos, r);
			pos += r;
			if(pos < off + len) {
				dump();
			}
		}
	}

	/**
	 * Dump the buffer to {@link #out} but don't flush
	 * @throws IOException
	 */
	protected void dump() throws IOException {
		ensureOpen();
		if(this.blockMode && buf.position() > 0) {
			blockHeader.clear();
			blockHeader.put(J_BLOCK_DATA);
			writeEscapedInt(blockHeader, buf.position());
			out.write((ByteBuffer) blockHeader.flip());
		}
		out.write((ByteBuffer) buf.flip());
		buf.clear();
	}
	
	@Override
	public void flush() throws IOException {
		dump();
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
		setBlockMode(false).ensureCapacity(1).put(J_FIELDS);
		for(int i = 0; i < ctx.getType().getFieldNames().length; i++) {
			String name = ctx.getType().getFieldNames()[i];
			Class<?> fc = ctx.getType().getFieldClasses()[i];
			Object val = ctx.getPutFields().get(name);
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
		setBlockMode(false).ensureCapacity(1).put(J_WALL);
	}

	@Override
	public void reset() throws IOException {
		setBlockMode(false).ensureCapacity(1).put(J_RESET);
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
