package org.purplejrank.jdk;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;

import org.purplejrank.jdk.block.ArrayBlock;
import org.purplejrank.jdk.block.BlockdataBlock;
import org.purplejrank.jdk.block.BlockdatalongBlock;
import org.purplejrank.jdk.block.ClassBlock;
import org.purplejrank.jdk.block.ClassdescBlock;
import org.purplejrank.jdk.block.EnumBlock;
import org.purplejrank.jdk.block.ExceptionBlock;
import org.purplejrank.jdk.block.HeaderBlock;
import org.purplejrank.jdk.block.LongstringBlock;
import org.purplejrank.jdk.block.NullBlock;
import org.purplejrank.jdk.block.ObjectBlock;
import org.purplejrank.jdk.block.ProxyclassdescBlock;
import org.purplejrank.jdk.block.ReferenceBlock;
import org.purplejrank.jdk.block.ResetBlock;
import org.purplejrank.jdk.block.StringBlock;

import static java.io.ObjectStreamConstants.*;

public class JdkStream extends DataInputStream {

	public JdkStream(InputStream in) throws IOException {
		super(in);
		new HeaderBlock(this).parse();
	}
	
	public Block readBlock() throws IOException {
		int t = read();
		switch(t) {
		case TC_NULL: return new NullBlock(this).parse();
		case TC_REFERENCE: return new ReferenceBlock(this).parse();
		case TC_CLASSDESC: return new ClassdescBlock(this).parse();
		case TC_OBJECT: return new ObjectBlock(this).parse();
		case TC_STRING: return new StringBlock(this).parse();
		case TC_ARRAY: return new ArrayBlock(this).parse();
		case TC_CLASS: return new ClassBlock(this).parse();
		case TC_BLOCKDATA: return new BlockdataBlock(this).parse();
		case TC_RESET: return new ResetBlock(this).parse();
		case TC_BLOCKDATALONG: return new BlockdatalongBlock(this).parse();
		case TC_EXCEPTION: return new ExceptionBlock(this).parse();
		case TC_LONGSTRING: return new LongstringBlock(this).parse();
		case TC_PROXYCLASSDESC: return new ProxyclassdescBlock(this).parse();
		case TC_ENUM: return new EnumBlock(this).parse();
		case -1:
			throw new EOFException();
		default:
			throw new StreamCorruptedException();
		}
	}
}
