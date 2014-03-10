package org.purplejrank.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public class ByteBufferByteChannel implements ByteChannel {

	private boolean open = true;
	private ByteBuffer buf;
	
	public ByteBufferByteChannel(ByteBuffer buf) {
		this.buf = buf;
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		if(!open)
			throw new IOException("closed");
		int r = dst.remaining();
		ByteBuffer b = (ByteBuffer) buf.duplicate().limit(buf.position() + r);
		dst.put(b);
		buf.position(buf.position() + r);
		return r;
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public void close() throws IOException {
		open = false;
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		int r = src.remaining();
		buf.put(src);
		return r;
	}

	public ByteBuffer buf() {
		return buf;
	}
}
