package org.purplejrank.mods;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

import org.purplejrank.JrankClass;

public class ScanningJrankInput extends NullsJrankInput {
	
	protected List<JrankClass> streamClasses;
	protected List<JrankClass> resolvedClasses;
	protected List<JrankClass> unresolvedClasses;

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
		resolvedClasses = new ArrayList<JrankClass>();
		unresolvedClasses = new ArrayList<JrankClass>();
		for(Object w : wired) {
			if(w instanceof JrankClass) {
				JrankClass d = (JrankClass) w;
				streamClasses.add(d);
				if(d.getType() != null)
					resolvedClasses.add(d);
				else
					unresolvedClasses.add(d);
			}
		}
		return streamClasses;
	}

	public List<JrankClass> getStreamClasses() {
		if(streamClasses == null)
			throw new IllegalStateException("scan() not called");
		return streamClasses;
	}

	public List<JrankClass> getResolvedClasses() {
		if(resolvedClasses == null)
			throw new IllegalStateException("scan() not called");
		return resolvedClasses;
	}
	
	public List<JrankClass> getUnresolvedClasses() {
		if(unresolvedClasses == null)
			throw new IllegalStateException("scan() not called");
		return unresolvedClasses;
	}
	
	@Override
	protected Object newOrdinaryObject(JrankClass desc) throws IOException,
			ClassNotFoundException {
		return null;
	}
	
	@Override
	protected Object newArray(JrankClass desc, int size) {
		return null;
	}
}
