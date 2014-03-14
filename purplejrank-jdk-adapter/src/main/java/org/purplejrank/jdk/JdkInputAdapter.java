package org.purplejrank.jdk;

import java.io.IOException;
import java.io.InputStream;

import org.purplejrank.io.PullInputStream;

public class JdkInputAdapter extends PullInputStream {

	protected JdkInputAdapter(InputStream in) {
		super(in);
	}

	@Override
	protected int pull() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

}
