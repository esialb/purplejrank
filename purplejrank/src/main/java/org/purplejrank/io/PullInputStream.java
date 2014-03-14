package org.purplejrank.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.purplejrank.JrankConstants;

public abstract class PullInputStream extends FilterInputStream {

	protected ByteBuffer buf = ByteBuffer.allocateDirect(JrankConstants.J_MAX_BLOCK_SIZE);
	
	protected PullInputStream(InputStream in) {
		super(in);
	}
	
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
			if(!buf.hasRemaining())
				if(pull() < 1)
					return len - rem;
		}
		return len;
	}
	
}