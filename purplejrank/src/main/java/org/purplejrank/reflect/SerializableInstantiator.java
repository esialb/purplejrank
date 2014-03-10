package org.purplejrank.reflect;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Proxy;

/**
 * {@link Instantiator} that writes a dummy object stream and fools
 * {@link ObjectInputStream} into doing the native instantiation
 * @author robin
 *
 */
public class SerializableInstantiator implements Instantiator {
	private static final byte[] header;
	private static final byte[] footer;
	static {
		int[] hdr = new int[] {
				0xac, 0xed, // stream magic 
				0x00, 0x05, // stream version
				0x73, // TC_OBJECT
				0x72, // TC_CLASSDESC
		};
		// classname
		// serialVersionUID
		int[] ftr = new int[] {
				0x02, // SC_SERIALIZABLE
				0x00, 0x00, // field count
				0x78, // end block data for class annotation
				0x70, // null superclass
		};
		header = new byte[hdr.length];
		for(int i = 0; i < hdr.length; i++)
			header[i] = (byte) hdr[i];
		footer = new byte[ftr.length];
		for(int i = 0; i < ftr.length; i++)
			footer[i] = (byte) ftr[i];
	}
	
	private byte[] buf;
	
	public SerializableInstantiator(Class<?> cls) throws NoSuchMethodException {
		if(Proxy.isProxyClass(cls))
			throw new IllegalArgumentException();
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream d = new DataOutputStream(bout);
		
		try {
			d.write(header);
			d.writeUTF(cls.getName());
			d.writeLong(ObjectStreamClass.lookupAny(cls).getSerialVersionUID());
			d.write(footer);
			d.close();
		} catch(IOException e) {
			// IO exception writing to a ByteArrayOutputStream?
			throw new RuntimeException(e);
		}
		
		buf = bout.toByteArray();
	}

	@Override
	public Object newInstance() throws IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buf));
		Object obj = in.readObject();
		in.close();
		return obj;
	}

}
