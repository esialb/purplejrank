package org.purplejrank.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * {@link ReadableByteChannel} backed by an {@link InputStream}
 * @author robin
 *
 */
public class StreamReadableByteChannel implements ReadableByteChannel {
	private InputStream in;
	private boolean open = true;
	private byte[] buf = new byte[8192];
	
	public StreamReadableByteChannel(InputStream in) {
		this.in = in;
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public void close() throws IOException {
		open = false;
		in.close();
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		int count = 0;
		while(dst.remaining() > 0) {
			int r = in.read(buf, 0, Math.min(buf.length, dst.remaining()));
			if(r > 0) {
				dst.put(buf, 0, r);
				count += r;
			} else
				break;
		}
		return count;
	}
}
