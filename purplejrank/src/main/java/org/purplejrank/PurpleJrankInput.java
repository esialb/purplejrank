package org.purplejrank;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.NotActiveException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectInputValidation;
import java.io.ObjectStreamClass;
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
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.objenesis.ObjenesisHelper;
import org.objenesis.instantiator.basic.ConstructorInstantiator;
import org.objenesis.strategy.SerializingInstantiatorStrategy;

public class PurpleJrankInput extends ObjectInputStream implements ObjectInput {

	protected boolean isClosed = false;
	protected ReadableByteChannel in;
	protected ByteBuffer buf = ByteBuffer.allocate(JrankConstants.MAX_BLOCK_SIZE);
	protected int blockEnd = 0;
	
	protected ClassLoader cl;
	protected FieldCache fieldCache = new FieldCache();
	protected List<Object> wired = new ArrayList<Object>();
	protected Deque<JrankContext> context = new ArrayDeque<JrankContext>(Arrays.asList(JrankContext.NO_CONTEXT));
	protected NavigableMap<Integer, List<ObjectInputValidation>> validation = new TreeMap<Integer, List<ObjectInputValidation>>();
	
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
		if(magic != JrankConstants.MAGIC)
			throw new IOException("invalid magic");
		if(version != JrankConstants.VERSION)
			throw new IOException("invalid version");
	}
	
	protected PurpleJrankInput ensureOpen() throws IOException {
		if(isClosed) throw new IOException("channel closed");
		return this;
	}
	
	protected byte peek() throws IOException {
		return ensureOpen().ensureAvailable(1).get(buf.position());
	}
	
	protected PurpleJrankInput setBlockMode(boolean blockMode) throws IOException {
		ensureOpen();
		if(blockEnd > 0 && blockMode)
			return this;
		if(blockMode) {
			ensureAvailable(1);
			if(buf.get() != JrankConstants.BLOCK_DATA)
				throw new StreamCorruptedException("Not at block boundary");
			ensureAvailable(blockEnd = readEscapedInt());
			buf.limit(blockEnd);
		} else if(blockEnd > 0) {
			blockEnd = 0;
			buf.position(0);
			buf.limit(0);
		}
		return this;
	}
	
	protected ByteBuffer ensureAvailable(int available) throws IOException {
		if(buf.remaining() < available) {
			int r = buf.remaining();
			buf.compact();
			buf.position(r);
			buf.limit(r + available);
			in.read(buf);
			buf.position(0);
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
			if(i != 0)
				sb.append((char)(i-1));
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
		
		Object obj = null;
		int handle = -1;
		
		switch(ensureAvailable(1).get()) {
		case JrankConstants.NULL:
			return null;
			
		case JrankConstants.REFERENCE:
			handle = readEscapedInt();
			if(shared)
				return wired.get(handle);
			obj = clone(wired.get(handle));
			break;
			
		case JrankConstants.ARRAY:
			JrankClass d = readClassDesc();
			int size = setBlockMode(false).readEscapedInt();
			Class<?> cmp = null;
			if(d.getType() != null)
				cmp = d.getType().getComponentType();
			obj = newArray(d, size);
			wired.add(obj);
			context.offerLast(new JrankContext(d, obj));
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
			context.pollLast();
			break;
			
		case JrankConstants.STRING:
			obj = readUTF(false);
			wired.add(obj);
			break;
			
		case JrankConstants.ENUM:
			d = readClassDesc();
			wired.add(obj);
			String name = (String) readObject0(true);
			obj = Enum.valueOf(d.getType().asSubclass(Enum.class), name);
			break;
			
		case JrankConstants.OBJECT:
			d = readClassDesc();
			obj = newOrdinaryObject(d);
			handle = wired.size();
			wired.add(obj);
			
			if(d.getFlags() == JrankConstants.SC_WRITE_EXTERNAL) {
				context.offerLast(new JrankContext(d, obj));
				((Externalizable) obj).readExternal(this);
				context.pollLast();
			} else {
				for(JrankClass t = d; t != null; t = t.getParent()) {
					context.offerLast(new JrankContext(t, obj));
					if(obj == null)
						skipOptionalData();
					else if(t.getFlags() == JrankConstants.SC_WRITE_OBJECT) {
						try {
							Method m = t.getType().getDeclaredMethod("readObject", ObjectInputStream.class);
							m.setAccessible(true);
							m.invoke(obj, this);
						} catch(NoSuchMethodException e) {
						} catch(Exception ex) {
							throw new IOException(ex);
						}
						skipOptionalData();
					} else
						defaultReadObject();
					setBlockMode(false).ensureAvailable(1);
					if(buf.get() != JrankConstants.WALL)
						throw new StreamCorruptedException();
					context.pollLast();
				}
			}
			
			Method m = findReadResolve(obj);
			if(m != null) {
				try {
					obj = m.invoke(obj);
					wired.set(handle, obj);
				} catch(Exception e) {
					throw new IOException(e);
				}
			}
			
			break;
			
		default:
			throw new StreamCorruptedException();
		}
		
		return obj;
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
		Array.set(array, index, value);
	}
	
	protected void skipOptionalData() throws IOException, ClassNotFoundException {
		setBlockMode(false);
		for(byte b = peek(); b != JrankConstants.WALL; b = peek()) {
			switch(b) {
			case JrankConstants.BLOCK_DATA:
				setBlockMode(true).setBlockMode(false);
				break;
			case JrankConstants.FIELDS:
				readFields();
				break;
			default:
				readObject0(true);
			}
			setBlockMode(false);
		}
	}
	
	protected Object clone(Object obj) {
		Object clone = ObjenesisHelper.newInstance(obj.getClass());
		for(Class<?> cls = obj.getClass(); cls != null; cls = cls.getSuperclass()) {
			Field[] fields = fieldCache.get(cls);
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
		case JrankConstants.NULL:
			return null;
			
		case JrankConstants.REFERENCE:
			int handle = readEscapedInt();
			return (JrankClass) wired.get(handle);
			
		case JrankConstants.PROXYCLASSDESC:
			d = new JrankClass();
			wired.add(d);
			
			int nifc = readEscapedInt();
			String[] ifcs = new String[nifc];
			for(int i = 0; i < nifc; i++)
				ifcs[i] = readUTF(false);
			
			d.setProxy(true);
			d.setProxyInterfaceNames(ifcs);
			
			d.setType(resolveProxyClass(d.getProxyInterfaceNames()));
			
			d.setParent(readClassDesc());

			return d;
			
		case JrankConstants.CLASSDESC:
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
				try {
					if(d.getType() == null)
						continue;
					Field f = fieldCache.get(d.getType(), fieldNames[i]);
					if(Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers()))
						continue;
					if(!resolveClass(fieldTypes[i]).equals(f.getType()))
						continue;
					fields[fc++] = f;
				} catch(NoSuchFieldException e) {}
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
		while(cls != null) {
			try {
				Method m = cls.getDeclaredMethod("readResolve");
				m.setAccessible(true);
				return m;
			} catch(NoSuchMethodException e) {}
			cls = cls.getSuperclass();
		}
		return null;
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
		name = name.substring(1, name.length() - 1);
		return resolveOrdinaryClass(name);
	}
	
	protected Class<?> resolveArrayClass(Class<?> baseComponentType, int depth) {
		return Array.newInstance(baseComponentType, new int[depth]).getClass();
	}
	
	protected Class<?> resolveOrdinaryClass(String name) throws ClassNotFoundException {
		return Class.forName(name, false, cl);
	}
	
	@Override
	protected Class<?> resolveProxyClass(String[] interfaces)
			throws IOException, ClassNotFoundException {
		Class<?>[] ifcs = new Class<?>[interfaces.length];
		for(int i = 0; i < ifcs.length; i++)
			ifcs[i] = Class.forName(interfaces[i], false, cl);
		return Proxy.getProxyClass(cl, ifcs);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Object newOrdinaryObject(JrankClass desc) {
		if(desc.getType() == null)
			return null;
		
		if(desc.getFlags() == JrankConstants.SC_WRITE_EXTERNAL)
			return new ConstructorInstantiator(desc.getType()).newInstance();
		return new SerializingInstantiatorStrategy().newInstantiatorOf(desc.getType()).newInstance();
	}
	
	@Override
	public int read() throws IOException {
		return 0xff & (int) ensureOpen().setBlockMode(true).ensureAvailable(1).get();
	}

	@Override
	public int read(byte[] b) throws IOException {
		ensureOpen().setBlockMode(true).ensureAvailable(b.length);
		buf.get(b);
		return b.length;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		ensureOpen().setBlockMode(true).ensureAvailable(len);
		buf.get(b, off, len);
		return len;
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
		if(blockEnd == 0)
			return 0;
		return blockEnd - buf.position();
	}

	@Override
	public void close() throws IOException {
		isClosed = true;
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
			throw new IOException(e);
		}
	}
	
	@Override
	public JrankGetFields readFields() throws IOException, ClassNotFoundException {
		JrankContext ctx = context.peekLast();
		if(ctx == JrankContext.NO_CONTEXT)
			throw new NotActiveException();
		byte b = setBlockMode(false).ensureAvailable(1).get();
		if(b != JrankConstants.FIELDS)
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
		if(wall != JrankConstants.WALL)
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
