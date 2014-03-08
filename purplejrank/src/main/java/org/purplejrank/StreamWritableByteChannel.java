package org.purplejrank;

import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public class StreamWritableByteChannel implements WritableByteChannel, Flushable {
	private OutputStream out;
	private boolean open = true;
	
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
		byte[] buf = new byte[src.limit() - src.position()];
		src.get(buf);
		out.write(buf);
		return buf.length;
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}

}
