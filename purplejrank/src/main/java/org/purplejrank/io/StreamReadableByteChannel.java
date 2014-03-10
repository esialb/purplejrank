package org.purplejrank.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class StreamReadableByteChannel implements ReadableByteChannel {
	private InputStream in;
	private boolean open = true;
	
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
		while(dst.position() < dst.limit()) {
			int b = in.read();
			if(b == -1)
				return count;
			dst.put((byte) b);
			count++;
		}
		return count;
	}
}
