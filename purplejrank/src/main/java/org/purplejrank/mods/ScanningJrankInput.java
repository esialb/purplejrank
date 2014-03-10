package org.purplejrank.mods;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

import org.purplejrank.JrankClass;
import org.purplejrank.PurpleJrankInput;

public class ScanningJrankInput extends PurpleJrankInput {
	
	protected List<JrankClass> streamClasses;

	public ScanningJrankInput(InputStream in, ClassLoader cl)
			throws IOException {
		super(in, cl);
	}

	public ScanningJrankInput(InputStream in) throws IOException {
		super(in);
	}

	public ScanningJrankInput(ReadableByteChannel in, ClassLoader cl)
			throws IOException {
		super(in, cl);
	}

	public ScanningJrankInput(ReadableByteChannel in) throws IOException {
		super(in);
	}

	public List<JrankClass> scan() throws IOException, ClassNotFoundException {
		try {
			skipOptionalData();
		} catch(EOFException e) {
		}
		streamClasses = new ArrayList<JrankClass>();
		for(Object w : wired) {
			if(w instanceof JrankClass)
				streamClasses.add((JrankClass) w);
		}
		return streamClasses;
	}

	public List<JrankClass> getStreamClasses() {
		if(streamClasses == null)
			throw new IllegalStateException("scan() not called");
		return streamClasses;
	}
	
	@Override
	protected Class<?> resolveClass(String name) throws IOException,
			ClassNotFoundException {
		return null;
	}
	
}
