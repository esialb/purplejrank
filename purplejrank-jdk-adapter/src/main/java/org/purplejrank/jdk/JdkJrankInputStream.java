package org.purplejrank.jdk;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.purplejrank.io.PullInputStream;
import org.purplejrank.jdk.block.HeaderBlock;
import org.purplejrank.jdk.rule.ContentRule;

public class JdkJrankInputStream extends PullInputStream {

	protected ByteArrayOutputStream bout = new ByteArrayOutputStream();
	protected JdkStream jdk;
	protected byte[] bbuf;
	protected int pos;
	
	public JdkJrankInputStream(InputStream in) throws IOException {
		super(in);
		jdk = new JdkStream(in);
		pull(new HeaderBlock(jdk));
		pull();
	}

	protected void pull(Block b) throws IOException {
		bout.reset();
		b.writeJrank(new DataOutputStream(bout));
		bbuf = bout.toByteArray();
		pos = 0;
	}
	
	@Override
	protected int pull() throws IOException {
		if(pos == bbuf.length)
			pull(jdk.readBlock(ContentRule.class));
		buf.clear();
		int r = Math.min(buf.remaining(), bbuf.length - pos);
		buf.put(bbuf, pos, r);
		pos += r;
		buf.flip();
		return r;
	}

}
