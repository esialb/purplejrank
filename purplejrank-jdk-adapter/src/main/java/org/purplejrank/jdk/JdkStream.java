package org.purplejrank.jdk;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.io.UTFDataFormatException;
import java.util.ArrayList;
import java.util.List;

import org.purplejrank.jdk.block.ArrayBlock;
import org.purplejrank.jdk.block.BlockdataBlock;
import org.purplejrank.jdk.block.BlockdatalongBlock;
import org.purplejrank.jdk.block.ClassBlock;
import org.purplejrank.jdk.block.ClassdescBlock;
import org.purplejrank.jdk.block.EndblockdataBlock;
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
import org.purplejrank.jdk.rule.ContentRule;

import static java.io.ObjectStreamConstants.*;

public class JdkStream extends DataInputStream {

	protected List<WiredBlock> wiredBlocks = new ArrayList<WiredBlock>();
	
	public JdkStream(InputStream in) throws IOException {
		super(in);
		new HeaderBlock(this).parse();
	}
	
	public void verify() throws IOException {
		try {
			while(true)
				readBlock(ContentRule.class);
		} catch(EOFException e) {
		}
	}
	
	public int wireBlock(WiredBlock ref) {
		wiredBlocks.add(ref);
		return baseWireHandle + wiredBlocks.size() - 1;
	}

	public List<WiredBlock> getWiredBlocks() {
		return wiredBlocks;
	}
	
	public <T extends Block> T readBlock(Class<T> type) throws IOException {
		Block b = readBlock();
		if(type.isInstance(b))
			return type.cast(b);
		throw new StreamCorruptedException("Unexpected block:" + b);
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
		case TC_ENDBLOCKDATA: return new EndblockdataBlock(this).parse();
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

	public String readLongUTF() throws IOException {
		int utflen = (int) readLong();
		byte[] bytearr = null;
		char[] chararr = null;
		bytearr = new byte[utflen];
		chararr = new char[utflen];

		int c, char2, char3;
		int count = 0;
		int chararr_count=0;

		readFully(bytearr, 0, utflen);

		while (count < utflen) {
			c = (int) bytearr[count] & 0xff;      
			if (c > 127) break;
			count++;
			chararr[chararr_count++]=(char)c;
		}

		while (count < utflen) {
			c = (int) bytearr[count] & 0xff;
			switch (c >> 4) {
			case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
				/* 0xxxxxxx*/
				count++;
				chararr[chararr_count++]=(char)c;
				break;
			case 12: case 13:
				/* 110x xxxx   10xx xxxx*/
				count += 2;
				if (count > utflen)
					throw new UTFDataFormatException(
							"malformed input: partial character at end");
				char2 = (int) bytearr[count-1];
				if ((char2 & 0xC0) != 0x80)
					throw new UTFDataFormatException(
							"malformed input around byte " + count); 
				chararr[chararr_count++]=(char)(((c & 0x1F) << 6) | 
						(char2 & 0x3F));  
				break;
			case 14:
				/* 1110 xxxx  10xx xxxx  10xx xxxx */
				count += 3;
				if (count > utflen)
					throw new UTFDataFormatException(
							"malformed input: partial character at end");
				char2 = (int) bytearr[count-2];
				char3 = (int) bytearr[count-1];
				if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
					throw new UTFDataFormatException(
							"malformed input around byte " + (count-1));
				chararr[chararr_count++]=(char)(((c     & 0x0F) << 12) |
						((char2 & 0x3F) << 6)  |
						((char3 & 0x3F) << 0));
				break;
			default:
				/* 10xx xxxx,  1111 xxxx */
				throw new UTFDataFormatException(
						"malformed input around byte " + count);
			}
		}
		// The number of chars produced may be less than utflen
		return new String(chararr, 0, chararr_count);
	}

	public static void writeEscapedInt(OutputStream out, int v) throws IOException {
		if((v & 0x7f) != v) {
			out.write((byte)(0x80 | (0x7f & v)));
			writeEscapedInt(out, v >>> 7);
		} else
			out.write((byte)v);
	}

	public static void writeUTF(OutputStream out, String s) throws IOException {
		// Instead of the JRE's modified UTF-8, write bit-8-escaped ints
		for(int i = 0; i < s.length(); i++) {
			int c = s.charAt(i);
			if(c == 0)
				c = 0x1ffff;
			writeEscapedInt(out, c);
		}
		writeEscapedInt(out, 0);
	}

}
