package org.purplejrank.io;

import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * {@link WritableByteChannel} backed by an {@link OutputStream}
 * @author robin
 *
 */
public class StreamWritableByteChannel implements WritableByteChannel, Flushable {
	/**
	 * The backing {@link OutputStream}
	 */
	private OutputStream out;
	/**
	 * Whether this {@link WritableByteChannel} is open
	 */
	private boolean open = true;
	/**
	 * Write buffer
	 */
	private byte[] buf = new byte[8192];
	
	/**
	 * Adapt an {@link OutputStream} to a {@link WritableByteChannel}
	 * @param out
	 */
	public StreamWritableByteChannel(OutputStream out) {
		this.out = out;
	}
	
	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public void close() throws IOException {
		open = false;
		out.close();
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		int count = 0;
		while(src.remaining() > 0) {
			int r = Math.min(buf.length, src.remaining());
			src.get(buf, 0, r);
			out.write(buf, 0, r);
			count += r;
		}
		return count;
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}

}
