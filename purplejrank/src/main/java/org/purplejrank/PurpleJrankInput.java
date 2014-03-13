package org.purplejrank;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.NotActiveException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectInputValidation;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.objenesis.ObjenesisHelper;
import org.objenesis.instantiator.basic.ConstructorInstantiator;
import org.purplejrank.io.StreamReadableByteChannel;
import org.purplejrank.reflect.FieldCache;
import org.purplejrank.reflect.MethodCache;

import static org.purplejrank.JrankConstants.*;

/**
 * Extension of {@link ObjectInputStream} with a protocol based on {@link ObjectInputStream},
 * but slightly more robust.
 * @author robin
 *
 */
public class PurpleJrankInput extends ObjectInputStream implements ObjectInput {

	protected ReadableByteChannel in;
	protected ByteBuffer buf = ByteBuffer.allocateDirect(J_MAX_BLOCK_SIZE);
	protected ByteBuffer blockHeader = ByteBuffer.allocateDirect(6);
	protected boolean blockMode = false;
	protected int unbufferedBlock = 0;

	protected ClassLoader cl;
	protected FieldCache fieldCache = new FieldCache();
	protected MethodCache methodCache = new MethodCache();
	protected List<Object> wired = new ArrayList<Object>();
	protected Deque<JrankContext> context = new ArrayDeque<JrankContext>(Arrays.asList(JrankContext.NO_CONTEXT));
	protected NavigableMap<Integer, List<ObjectInputValidation>> validation = new TreeMap<Integer, List<ObjectInputValidation>>();

	public PurpleJrankInput(InputStream in) throws IOException {
		this(in, PurpleJrankInput.class.getClassLoader());
	}

	public PurpleJrankInput(InputStream in, ClassLoader cl) throws IOException {
		this(new StreamReadableByteChannel(in), cl);
	}

	public PurpleJrankInput(ReadableByteChannel in) throws IOException {
		this(in, PurpleJrankInput.class.getClassLoader());
	}

	public PurpleJrankInput(ReadableByteChannel in, ClassLoader cl) throws IOException {
		this.in = in;
		this.cl = cl;
		buf.limit(0);
		ensureAvailable(8);
		int magic = buf.getInt();
		int version = buf.getInt();
		if(magic != J_MAGIC)
			throw new JrankStreamException("invalid magic");
		if(version != J_VERSION)
			throw new JrankStreamException("invalid version");
	}

	protected PurpleJrankInput ensureOpen() throws IOException {
		if(!in.isOpen()) throw new JrankStreamException("channel closed");
		return this;
	}

	protected byte peek() throws IOException {
		return ensureOpen().ensureAvailable(1).get(buf.position());
	}
	
	protected PurpleJrankInput setBlockMode(boolean blockMode) throws IOException {
		ensureOpen();
		if(this.blockMode && blockMode)
			return this;
		if(blockMode) { // turn on block mode
			ensureAvailable(1);
			if(buf.get() != J_BLOCK_DATA)
				throw new StreamCorruptedException("Not at block boundary");
			int blockSize = readEscapedInt();
			buf.clear().limit(Math.min(blockSize, buf.capacity()));
			in.read(buf);
			buf.flip();
			unbufferedBlock = blockSize - buf.limit();
		} else if(this.blockMode) { // turn off block mode
			do {
				buf.clear();
				buf.limit(Math.min(unbufferedBlock, buf.remaining()));
				in.read(buf);
				unbufferedBlock -= buf.limit();
			} while(unbufferedBlock > 0);
			buf.clear().limit(0); // discard the block data
		}
		this.blockMode = blockMode;
		return this;
	}

	protected ByteBuffer ensureAvailable(int available) throws IOException {
		if(!blockMode) {
			if(buf.remaining() < available) {
				buf.compact();
				buf.limit(available);
				in.read(buf);
				buf.position(0);
			}
		} else {
			while(buf.remaining() < available) {
				// first try to read unbuffered block data
				if(unbufferedBlock > 0) {
					buf.compact().limit(Math.min(unbufferedBlock, buf.remaining()));
					in.read(buf);
					buf.position(0);
					unbufferedBlock -= buf.limit();
					continue;
				}
				// then try to read the next block
				blockHeader.clear();
				in.read(blockHeader);
				blockHeader.flip();
				if(blockHeader.get() != J_BLOCK_DATA)
					throw new StreamCorruptedException("Not at block boundary");
				int blockSize = readEscapedInt(blockHeader);
				buf.clear().limit(Math.min(blockSize, buf.capacity()));
				buf.put(blockHeader);
				in.read(buf);
				buf.flip();
				unbufferedBlock = blockSize - buf.limit();
			}
		}
		
		return buf;
	}

	protected int readEscapedInt() throws IOException {
		return readEscapedInt(0);
	}

	private int readEscapedInt(int shift) throws IOException {
		ensureOpen().ensureAvailable(1);
		int v = 0xff & buf.get();
		boolean more = (v & 0x80) != 0;
		v &= 0x7f;
		v <<= shift;
		if(more) {
			v |= readEscapedInt(shift + 7);
		}
		return v;
	}

	protected int readEscapedInt(ByteBuffer buf) throws IOException {
		return readEscapedInt(buf, 0);
	}

	private int readEscapedInt(ByteBuffer buf, int shift) throws IOException {
		int v = 0xff & buf.get();
		boolean more = (v & 0x80) != 0;
		v &= 0x7f;
		v <<= shift;
		if(more) {
			v |= readEscapedInt(buf, shift + 7);
		}
		return v;
	}

	protected long readEscapedLong() throws IOException {
		return readEscapedLong(0);
	}

	private long readEscapedLong(int shift) throws IOException {
		ensureOpen().ensureAvailable(1);
		long v = 0xff & buf.get();
		boolean more = (v & 0x80) != 0;
		v &= 0x7f;
		v <<= shift;
		if(more) {
			v |= readEscapedLong(shift + 7);
		}
		return v;
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		read(b);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		read(b, off, len);
	}

	@Override
	public int skipBytes(int n) throws IOException {
		return (int) skip(n);
	}

	@Override
	public boolean readBoolean() throws IOException {
		return ensureOpen().setBlockMode(true).ensureAvailable(1).get() != 0;
	}

	@Override
	public byte readByte() throws IOException {
		return ensureOpen().setBlockMode(true).ensureAvailable(1).get();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return 0xff & (int) readByte();
	}

	@Override
	public short readShort() throws IOException {
		return ensureOpen().setBlockMode(true).ensureAvailable(2).getShort();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return 0xffff & (int) readShort();
	}

	@Override
	public char readChar() throws IOException {
		return ensureOpen().setBlockMode(true).ensureAvailable(2).getChar();
	}

	@Override
	public int readInt() throws IOException {
		return ensureOpen().setBlockMode(true).ensureAvailable(4).getInt();
	}

	@Override
	public long readLong() throws IOException {
		return ensureOpen().setBlockMode(true).ensureAvailable(8).getLong();
	}

	@Override
	public float readFloat() throws IOException {
		return ensureOpen().setBlockMode(true).ensureAvailable(4).getFloat();
	}

	@Override
	public double readDouble() throws IOException {
		return ensureOpen().setBlockMode(true).ensureAvailable(8).getDouble();
	}

	@Override
	public String readLine() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String readUTF() throws IOException {
		return readUTF(true);
	}

	protected String readUTF(boolean blockMode) throws IOException {
		ensureOpen().setBlockMode(blockMode);
		StringBuilder sb = new StringBuilder();
		int i;
		do {
			i = readEscapedInt();
			if(i != 0) {
				if(i == 0x1ffff)
					i = 0;
				sb.append((char) i);
			}
		} while(i != 0);
		return sb.toString();
	}

	@Override
	protected Object readObjectOverride() throws ClassNotFoundException, IOException {
		try {
			return readObject0(true);
		} finally {
			if(context.size() == 1) {
				for(int prio : validation.descendingKeySet()) {
					for(ObjectInputValidation v : validation.get(prio))
						v.validateObject();
				}
				validation.clear();
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected Object readObject0(boolean shared) throws IOException, ClassNotFoundException {
		ensureOpen().setBlockMode(false);

		context.offerLast(JrankContext.NO_CONTEXT);
		try {

			Object obj = null;
			int handle = -1;

			byte b;
			switch(b = ensureAvailable(1).get()) {
			case J_NULL:
				return null;

			case J_REFERENCE:
				handle = readEscapedInt();
				if(shared)
					return wired.get(handle);
				obj = clone(wired.get(handle));
				break;

			case J_ARRAY:
				JrankClass d = readClassDesc();
				int size = setBlockMode(false).readEscapedInt();
				Class<?> cmp = null;
				if(d.getType() != null)
					cmp = d.getType().getComponentType();
				obj = newArray(d, size);
				wired.add(obj);
				context.offerLast(new JrankContext(d, obj));
				try {
					for(int i = 0; i < size; i++) {
						Object v;

						if(cmp == byte.class) v = ensureAvailable(1).get();
						else if(cmp == char.class) v = ensureAvailable(2).getChar();
						else if(cmp == double.class) v = ensureAvailable(8).getDouble();
						else if(cmp == float.class) v = ensureAvailable(4).getFloat();
						else if(cmp == int.class) v = ensureAvailable(4).getInt();
						else if(cmp == long.class) v = ensureAvailable(8).getLong();
						else if(cmp == short.class) v = ensureAvailable(2).getShort();
						else if(cmp == boolean.class) v = ensureAvailable(1).get() != 0;
						else v = readObject0(true);

						setArrayElement(obj, i, v);

						setBlockMode(false);
					}
				} finally {
					context.pollLast();
				}
				break;

			case J_STRING:
				obj = readUTF(false);
				wired.add(obj);
				break;

			case J_ENUM:
				d = readClassDesc();
				String name = (String) readObject0(true);
				obj = Enum.valueOf(d.getType().asSubclass(Enum.class), name);
				wired.add(obj);
				break;

			case J_CLASS:
				d = readClassDesc();
				obj = resolveClass(d);
				wired.add(obj);
				break;

			case J_OBJECT:
				d = readClassDesc();
				obj = newOrdinaryObject(d);
				handle = wired.size();
				wired.add(obj);

				if((d.getFlags() & J_SC_WRITE_EXTERNAL) == J_SC_WRITE_EXTERNAL) {
					readExternalizableObject(d, obj);
				} else {
					readSerializableObject(d, obj);
				}

				setBlockMode(false).ensureAvailable(1);
				if(buf.get() != J_WALL)
					throw new StreamCorruptedException();

				Method m = findReadResolve(obj);
				if(m != null) {
					try {
						obj = m.invoke(obj);
						wired.set(handle, obj);
					} catch(Exception e) {
						throw new JrankStreamException(e);
					}
				}

				break;

			default:
				throw new StreamCorruptedException(Byte.toString(b));
			}

			return obj;
		} finally {
			context.pollLast();
		}
	}

	protected void readExternalizableObject(JrankClass desc, Object obj)
	throws IOException, ClassNotFoundException  {
		context.offerLast(JrankContext.NO_CONTEXT); // context not available for Externalizable
		try {
			if(obj != null) {
				if(obj instanceof Externalizable)
					((Externalizable) obj).readExternal(this);
				else {
					Method m = methodCache.find(obj.getClass(), "readExternal", ObjectInput.class);
					if(m != null) {
						try {
							m.invoke(obj, this);
						} catch(Exception e) {
							throw new JrankStreamException(e);
						}
					}
				}
			}
			skipOptionalData();
		} finally {
			context.pollLast();
		}
	}
	
	protected void readSerializableObject(JrankClass desc, Object obj)
	throws IOException, ClassNotFoundException {
		Set<Class<?>> restoredClasses = new HashSet<Class<?>>();
		for(JrankClass t = desc; t != null; t = t.getParent()) {
			context.offerLast(new JrankContext(t, obj));
			try {
				Method m = null;
				if(
						t.getType() != null 
						&& (t.getFlags() & J_SC_WRITE_OBJECT) == J_SC_WRITE_OBJECT)
					m = methodCache.declared(t.getType(), "readObject", ObjectInputStream.class);
				if(obj == null || t.getType() == null || !t.getType().isInstance(obj))
					;
				else if((t.getFlags() & J_SC_WRITE_OBJECT) == J_SC_WRITE_OBJECT) {
					restoredClasses.add(t.getType());
					try {
						if(m != null)
							m.invoke(obj, this);
						else
							defaultReadObject();
					} catch(Exception ex) {
						throw new JrankStreamException(ex);
					}
				} else {
					restoredClasses.add(t.getType());
					defaultReadObject();
				}
				skipOptionalData();
				setBlockMode(false).ensureAvailable(1);
				if(buf.get() != J_WALL)
					throw new StreamCorruptedException();
			} finally {
				context.pollLast();
			}
		}
		Class<?> unrestored = obj != null ? obj.getClass() : null;
		while(unrestored != null && Serializable.class.isAssignableFrom(unrestored)) {
			if(!restoredClasses.contains(unrestored)) {
				Method m = methodCache.declared(unrestored, "readObjectNoData");
				try {
					if(m != null)
						m.invoke(obj);
				} catch(Exception ex) {
					throw new JrankStreamException(ex);
				}
			}
			unrestored = unrestored.getSuperclass();
		}
	}
	
	protected Object newArray(JrankClass desc, int size) {
		Class<?> cmp;
		if(desc.getType() != null) {
			cmp = desc.getType().getComponentType();
			return Array.newInstance(cmp, size);
		}
		return null;
	}

	protected void setArrayElement(Object array, int index, Object value) {
		if(array == null)
			return;
		Array.set(array, index, value);
	}

	protected void skipOptionalData() throws IOException, ClassNotFoundException {
		setBlockMode(false);
		for(byte b = peek(); b != J_WALL; b = peek()) {
			switch(b) {
			case J_BLOCK_DATA:
				setBlockMode(true).setBlockMode(false);
				break;
			case J_FIELDS:
				readFields();
				break;
			default:
				readObject0(true);
			}
			setBlockMode(false);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Object clone(Object obj) throws IOException {
		Class<?> cls = obj.getClass();
		Object clone;
		if(Externalizable.class.isAssignableFrom(cls))
			clone = new ConstructorInstantiator(cls).newInstance();
		else
			clone = ObjenesisHelper.newInstance(cls);
		for(; cls != null; cls = cls.getSuperclass()) {
			Field[] fields = fieldCache.declared(cls);
			for(Field f : fields) {
				if(Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers()))
					continue;
				try {
					f.set(clone, f.get(obj));
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return clone;
	}

	protected JrankClass readClassDesc() throws IOException, ClassNotFoundException {
		ensureOpen().setBlockMode(false).ensureAvailable(1);

		JrankClass d;

		switch(buf.get()) {
		case J_NULL:
			return null;

		case J_REFERENCE:
			int handle = readEscapedInt();
			return (JrankClass) wired.get(handle);

		case J_PROXYCLASSDESC:
			d = new JrankClass();
			wired.add(d);

			int nifc = readEscapedInt();
			String[] ifcs = new String[nifc];
			for(int i = 0; i < nifc; i++)
				ifcs[i] = readUTF(false);

			d.setProxy(true);
			d.setProxyInterfaceNames(ifcs);
			d.setFields(new Field[0]);
			d.setFieldNames(new String[0]);
			d.setFieldTypes(new String[0]);

			d.setType(resolveProxyClass(d.getProxyInterfaceNames()));

			d.setParent(readClassDesc());

			return d;

		case J_CLASSDESC:
			String name = readUTF(false);
			long serialVersion = readEscapedLong();
			byte flags = ensureAvailable(1).get();
			short nfields = ensureAvailable(2).getShort();
			String[] fieldNames = new String[nfields];
			String[] fieldTypes = new String[nfields];
			for(int i = 0; i < nfields; i++) {
				char t = (char)(0xff & (int)ensureAvailable(1).get());
				fieldTypes[i] = Character.toString(t);
				fieldNames[i] = readUTF(false);
				if(t == '[' || t == 'L')
					fieldTypes[i] += readUTF(false);
			}

			d = new JrankClass();
			wired.add(d);

			d.setProxy(false);
			d.setName(name);
			d.setSerialVersion(serialVersion);
			d.setFlags(flags);
			d.setFieldNames(fieldNames);
			d.setFieldTypes(fieldTypes);

			d.setType(resolveClass(d));
			Field[] fields = new Field[nfields];
			int fc = 0;
			for(int i = 0; i < nfields; i++) {
				if(d.getType() == null)
					continue;
				Field f = fieldCache.declared(d.getType(), fieldNames[i]);
				if(f == null)
					continue;
				if(Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers()))
					continue;
				if(!resolveClass(fieldTypes[i]).equals(f.getType()))
					continue;
				fields[fc++] = f;
			}
			fields = Arrays.copyOf(fields, fc);
			d.setFields(fields);

			d.setParent(readClassDesc());

			return d;
		}

		throw new StreamCorruptedException();
	}

	protected Method findReadResolve(Object obj) {
		if(obj == null)
			return null;
		Class<?> cls = obj.getClass();
		return methodCache.find(cls, "readResolve");
	}

	protected Class<?> resolveClass(JrankClass desc) throws IOException, ClassNotFoundException {
		Class<?> cls = resolveClass(desc.getName());
		if(cls == null)
			return null;
		checkSerialVersion(desc, cls);
		return cls;
	}

	protected void checkSerialVersion(JrankClass desc, Class<?> cls) throws ClassNotFoundException {
		long clsv = ObjectStreamClass.lookupAny(cls).getSerialVersionUID();
		if(clsv != desc.getSerialVersion())
			throw new ClassNotFoundException("Mismatched serialVersionUID: stream:" + desc.getSerialVersion() + " local:" + clsv);
	}

	protected Class<?> resolveClass(String name) throws IOException,
	ClassNotFoundException {
		if("B".equals(name)) return byte.class;
		if("C".equals(name)) return char.class;
		if("D".equals(name)) return double.class;
		if("F".equals(name)) return float.class;
		if("I".equals(name)) return int.class;
		if("J".equals(name)) return long.class;
		if("S".equals(name)) return short.class;
		if("Z".equals(name)) return boolean.class;
		if(name.startsWith("[")) {
			Class<?> c = resolveClass(name.replaceAll("\\[", ""));
			if(c == null)
				return null;
			int depth = name.replaceAll("[^\\[]", "").length();
			return resolveArrayClass(c, depth);
		}
		name = name.substring(1, name.length() - 1).replaceAll("/", ".");
		return resolveOrdinaryClass(name);
	}

	protected Class<?> resolveArrayClass(Class<?> baseComponentType, int depth) {
		return Array.newInstance(baseComponentType, new int[depth]).getClass();
	}

	protected Class<?> resolveOrdinaryClass(String name) throws ClassNotFoundException {
		return cl.loadClass(name);
	}

	@Override
	protected Class<?> resolveProxyClass(String[] interfaces)
			throws IOException, ClassNotFoundException {
		Class<?>[] ifcs = new Class<?>[interfaces.length];
		for(int i = 0; i < ifcs.length; i++)
			ifcs[i] = cl.loadClass(interfaces[i]);
		return Proxy.getProxyClass(cl, ifcs);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Object newOrdinaryObject(JrankClass desc) throws IOException, ClassNotFoundException {
		if(desc.getType() == null)
			return null;

		if(Externalizable.class.isAssignableFrom(desc.getType()))
			return new ConstructorInstantiator(desc.getType()).newInstance();
		return ObjenesisHelper.newInstance(desc.getType());
	}

	@Override
	public int read() throws IOException {
		return 0xff & (int) ensureOpen().setBlockMode(true).ensureAvailable(1).get();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		setBlockMode(true);
		int count = 0;
		while(count < len) {
			int r = Math.min(len, buf.capacity());
			ensureAvailable(r);
			buf.get(b, off + count, r);
			count += r;
		}
		return count;
	}

	@Override
	public long skip(long n) throws IOException {
		ensureOpen().setBlockMode(true);
		for(long i = 0; i < n; i++)
			readByte();
		return n;
	}

	@Override
	public int available() throws IOException {
		if(!blockMode)
			return 0;
		return buf.remaining();
	}

	@Override
	public void close() throws IOException {
		in.close();
	}

	@Override
	public Object readUnshared() throws IOException, ClassNotFoundException {
		return readObject0(false);
	}

	@Override
	public void defaultReadObject() throws IOException, ClassNotFoundException {
		JrankContext ctx = context.peekLast();
		if(ctx == JrankContext.NO_CONTEXT)
			throw new NotActiveException();
		JrankGetFields fields = readFields();
		for(Field f : ctx.getType().getFields()) {
			if(!fields.defaulted(f.getName())) {
				setField(f, ctx.getObject(), fields);
			}
		}
	}

	protected void setField(Field f, Object obj, JrankGetFields fields) throws IOException {
		try {
			f.set(obj, fields.get(f.getName(), null));
		} catch (Exception e) {
			throw new JrankStreamException(e);
		}
	}

	@Override
	public JrankGetFields readFields() throws IOException, ClassNotFoundException {
		JrankContext ctx = context.peekLast();
		if(ctx == JrankContext.NO_CONTEXT)
			throw new NotActiveException();
		byte b = setBlockMode(false).ensureAvailable(1).get();
		if(b != J_FIELDS)
			throw new StreamCorruptedException();
		JrankClass desc = ctx.getType();
		JrankGetFields fields = new JrankGetFields();
		for(int i = 0; i < desc.getFieldNames().length; i++) {
			String name = desc.getFieldNames()[i];
			String type = desc.getFieldTypes()[i];
			Object obj;
			if("B".equals(type)) obj = readByte();
			else if("C".equals(type)) obj = readChar();
			else if("D".equals(type)) obj = readDouble();
			else if("F".equals(type)) obj = readFloat();
			else if("I".equals(type)) obj = readInt();
			else if("J".equals(type)) obj = readLong();
			else if("S".equals(type)) obj = readShort();
			else if("Z".equals(type)) obj = readBoolean();
			else obj = readObject0(true);
			fields.put(name, obj);
		}
		setBlockMode(false).ensureAvailable(1);
		byte wall = buf.get();
		if(wall != J_WALL)
			throw new StreamCorruptedException();
		return fields;
	}

	@Override
	public void registerValidation(ObjectInputValidation obj, int prio)
			throws NotActiveException, InvalidObjectException {
		if(!validation.containsKey(prio))
			validation.put(prio, new ArrayList<ObjectInputValidation>());
		validation.get(prio).add(obj);
	}

	@Deprecated
	@Override
	protected final Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
	ClassNotFoundException {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	protected final Object resolveObject(Object obj) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	protected final boolean enableResolveObject(boolean enable)
			throws SecurityException {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	protected final void readStreamHeader() throws IOException,
	StreamCorruptedException {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	protected final ObjectStreamClass readClassDescriptor() throws IOException,
	ClassNotFoundException {
		throw new UnsupportedOperationException();
	}



}
