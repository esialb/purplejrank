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
import java.util.Map;

import org.objenesis.ObjenesisHelper;
import org.objenesis.instantiator.basic.ConstructorInstantiator;
import org.objenesis.strategy.SerializingInstantiatorStrategy;

public class PurpleJrankInput extends ObjectInputStream implements ObjectInput {

	private boolean isClosed = false;
	private ReadableByteChannel in;
	private ByteBuffer buf = ByteBuffer.allocateDirect(JrankConstants.MAX_BLOCK_SIZE);
	private int blockRemaining = 0;
	
	private ClassLoader cl = PurpleJrankInput.class.getClassLoader();
	private List<Object> wired = new ArrayList<Object>();
	private Deque<JrankContext> context = new ArrayDeque<JrankContext>(Arrays.asList(JrankContext.NO_CONTEXT));
	
	public PurpleJrankInput(ReadableByteChannel in) throws IOException {
		this.in = in;
	}
	
	private PurpleJrankInput ensureOpen() throws IOException {
		if(isClosed) throw new IOException("channel closed");
		return this;
	}
	
	private byte peek() throws IOException {
		return ensureOpen().ensureAvailable(1).get(buf.position());
	}
	
	private PurpleJrankInput setBlockMode(boolean blockMode) throws IOException {
		ensureOpen();
		if(blockRemaining > 0 && !blockMode) {
			buf.position(buf.position() + blockRemaining);
			buf.compact();
			return this;
		}
		if(blockRemaining > 0)
			return this;
		if(blockMode) {
			ensureAvailable(1);
			if(buf.get() != JrankConstants.BLOCK_DATA)
				throw new StreamCorruptedException("Not at block boundary");
			ensureAvailable(blockRemaining = readEscapedInt());
		}
		return this;
	}
	
	private ByteBuffer ensureAvailable(int available) throws IOException {
		if(buf.remaining() < available) {
			in.read(buf.compact());
			buf.flip();
		}
		return buf;
	}
	
	private int readEscapedInt() throws IOException {
		return readEscapedInt(25);
	}
	
	private int readEscapedInt(int maxBits) throws IOException {
		ensureOpen().ensureAvailable(1);
		int v = 0xff & buf.get();
		boolean more = (v & 0x80) != 0;
		v &= 0x7f;
		int shift = maxBits - 7;
		if(shift > 0 && more) {
			v = v << shift;
			v |= readEscapedInt(shift);
		} else if(shift < 0)
			v = v >>> -shift;
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
	
	private String readUTF(boolean blockMode) throws IOException {
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
		return readObject0(true);
	}
	
	private Object readObject0(boolean shared) throws IOException, ClassNotFoundException {
		ensureOpen().setBlockMode(false);
		
		Object obj = null;
		int handle;
		
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
			int size = readEscapedInt();
			Class<?> cmp = d.getType().getComponentType();
			obj = Array.newInstance(cmp, size);
			wired.add(obj);
			context.offerLast(new JrankContext(d, obj));
			for(int i = 0; i < size; i++) {
				if(cmp == byte.class) Array.setByte(obj, i, ensureAvailable(1).get());
				else if(cmp == char.class) Array.setChar(obj, i, ensureAvailable(2).getChar());
				else if(cmp == double.class) Array.setDouble(obj, i, ensureAvailable(8).getDouble());
				else if(cmp == float.class) Array.setFloat(obj, i, ensureAvailable(4).getFloat());
				else if(cmp == int.class) Array.setInt(obj, i, ensureAvailable(4).getInt());
				else if(cmp == long.class) Array.setLong(obj, i, ensureAvailable(8).getLong());
				else if(cmp == short.class) Array.setShort(obj, i, ensureAvailable(2).getShort());
				else if(cmp == boolean.class) Array.setBoolean(obj, i, ensureAvailable(1).get() != 0);
				else Array.set(obj, i, readObject0(true));
				
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
			String name = (String) readObject0(true);
			obj = Enum.valueOf(d.getType().asSubclass(Enum.class), name);
			wired.add(obj);
			break;
			
		case JrankConstants.OBJECT:
			d = readClassDesc();
			obj = instantiate(d);
			if(d.getFlags() == JrankConstants.SC_WRITE_EXTERNAL) {
				context.offerLast(new JrankContext(d, obj));
				((Externalizable) obj).readExternal(this);
				context.pollLast();
			} else {
				for(JrankClass t = d; t != null; t = t.getParent()) {
					context.offerLast(new JrankContext(t, obj));
					if(t.getFlags() == JrankConstants.SC_WRITE_OBJECT) {
						try {
							Method m = t.getType().getDeclaredMethod("readObject", ObjectInputStream.class);
							m.invoke(obj, this);
						} catch(Exception ex) {
							throw new IOException(ex);
						}
						skipOptionalData();
					} else
						defaultReadObject();
					context.pollLast();
				}
			}
			
		default:
			throw new StreamCorruptedException();
		}
		
		return obj;
	}
	
	private void skipOptionalData() throws IOException, ClassNotFoundException {
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
	
	private Object clone(Object obj) {
		Object clone = ObjenesisHelper.newInstance(obj.getClass());
		for(Class<?> cls = obj.getClass(); cls != null; cls = cls.getSuperclass()) {
			Field[] fields = cls.getDeclaredFields();
			Field.setAccessible(fields, true);
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
	
	private JrankClass readClassDesc() throws IOException, ClassNotFoundException {
		ensureOpen().setBlockMode(false).ensureAvailable(1);
		
		switch(buf.get()) {
		case JrankConstants.NULL:
			return null;
			
		case JrankConstants.REFERENCE:
			int handle = readEscapedInt();
			return (JrankClass) wired.get(handle);
			
		case JrankConstants.PROXYCLASSDESC:
			JrankClass d = new JrankClass();
			int nifc = readEscapedInt();
			String[] ifcs = new String[nifc];
			for(int i = 0; i < nifc; i++)
				ifcs[i] = readUTF(false);
			
			d.setProxy(true);
			d.setProxyInterfaceNames(ifcs);
			d.setParent(readClassDesc());
			
			d.setType(resolveProxyClass(d.getProxyInterfaceNames()));
			
			return d;
			
		case JrankConstants.CLASSDESC:
			String name = readUTF(false);
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
			d.setProxy(false);
			d.setName(name);
			d.setFlags(flags);
			d.setFieldNames(fieldNames);
			d.setFieldTypes(fieldTypes);
			d.setParent(readClassDesc());

			d.setType(resolveClass(d.getName()));
			Field[] fields = new Field[nfields];
			int fc = 0;
			for(int i = 0; i < nfields; i++) {
				try {
					Field f = d.getType().getDeclaredField(fieldNames[i]);
					if(Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers()))
						continue;
					if(!resolveClass(fieldTypes[i]).equals(f.getType()))
						continue;
					fields[fc++] = f;
				} catch(NoSuchFieldException e) {}
			}
			fields = Arrays.copyOf(fields, fc);
			Field.setAccessible(fields, true);
			d.setFields(fields);
			
			return d;
		}
		
		throw new StreamCorruptedException();
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
			Class<?> c = Class.forName(name.replaceAll("[", "").replace(";", ""), false, cl);
			int depth = name.replaceAll("[^\\[]", "").length();
			return Array.newInstance(c, new int[depth]).getClass();
		}
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
	
	protected Object instantiate(JrankClass desc) {
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
		return blockRemaining;
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
				try {
					f.set(ctx.getObject(), fields.get(f.getName(), null));
				} catch (Exception e) {
					throw new IOException(e);
				}
			}
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
			setBlockMode(false);
			Object obj;
			if("B".equals(type)) obj = ensureAvailable(1).get();
			else if("C".equals(type)) obj = ensureAvailable(2).getChar();
			else if("D".equals(type)) obj = ensureAvailable(8).getDouble();
			else if("F".equals(type)) obj = ensureAvailable(4).getFloat();
			else if("I".equals(type)) obj = ensureAvailable(4).getInt();
			else if("J".equals(type)) obj = ensureAvailable(8).getLong();
			else if("S".equals(type)) obj = ensureAvailable(2).getShort();
			else if("Z".equals(type)) obj = ensureAvailable(1).get() != 0;
			else obj = readObject0(true);
			fields.put(name, obj);
		}
		setBlockMode(false).ensureAvailable(1);
		if(buf.get() != JrankConstants.WALL)
			throw new StreamCorruptedException();
		return fields;
	}

	@Override
	public void registerValidation(ObjectInputValidation obj, int prio)
			throws NotActiveException, InvalidObjectException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

}
