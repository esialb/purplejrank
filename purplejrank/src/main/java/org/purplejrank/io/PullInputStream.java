package org.purplejrank.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.purplejrank.JrankConstants;

/**
 * Buffered {@link InputStream} that uses a "pull" mechanism so that
 * the data can be loaded as needed.
 * @author robin
 *
 */
public abstract class PullInputStream extends InputStream {
	/**
	 * Data buffer, loaded by the pull() methoid
	 */
	protected ByteBuffer buf = ByteBuffer.allocateDirect(JrankConstants.J_MAX_BLOCK_SIZE);
	
	/**
	 * Abstract constructor for a new {@link PullInputStream}
	 */
	public PullInputStream() {
	}
	
	/**
	 * Pull data into {@link #buf} so it can be read
	 * @return
	 * @throws IOException
	 */
	protected abstract int pull() throws IOException;
	
	@Override
	public int read() throws IOException {
		while(!buf.hasRemaining())
			if(pull() == -1)
				return -1;
		return 0xff & (int) buf.get();
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		while(!buf.hasRemaining())
			if(pull() == -1)
				return -1;
		int rem = len;
		while(rem > 0) {
			b[off++] = buf.get();
			rem--;
			if(rem > 0 && !buf.hasRemaining())
				if(pull() < 1)
					return len - rem;
		}
		return len;
	}
	
}
