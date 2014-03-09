package org.purplejrank;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.NotActiveException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectInputValidation;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class PurpleJrankInput extends ObjectInputStream implements ObjectInput {

	private boolean isClosed = false;
	private ReadableByteChannel in;
	private ByteBuffer buf = ByteBuffer.allocateDirect(JrankConstants.MAX_BLOCK_SIZE);
	private int blockRemaining = 0;
	
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
			ensureAvailable(blockRemaining = readEscapedInt(buf));
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
	
	private int readEscapedInt(ByteBuffer buf) throws IOException {
		return readEscapedInt(buf, 25);
	}
	
	private int readEscapedInt(ByteBuffer buf, int maxBits) throws IOException {
		ensureOpen().ensureAvailable(1);
		int v = 0xff & buf.get();
		boolean more = (v & 0x80) != 0;
		v &= 0x7f;
		int shift = maxBits - 7;
		if(shift > 0 && more) {
			v = v << shift;
			v |= readEscapedInt(buf, shift);
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
			i = readEscapedInt(buf);
			if(i != 0)
				sb.append((char)(i-1));
		} while(i != 0);
		return sb.toString();
	}

	@Override
	protected Object readObjectOverride() throws ClassNotFoundException, IOException {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return super.readUnshared();
	}

	@Override
	public void defaultReadObject() throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public GetField readFields() throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void registerValidation(ObjectInputValidation obj, int prio)
			throws NotActiveException, InvalidObjectException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

}
