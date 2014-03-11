package org.purplejrank;

/**
 * Constants used when serializing or deserializing
 * @author robin
 *
 */
public class JrankConstants {
	public static final int MAX_BLOCK_SIZE = 1024*1024;
	public static final int MAGIC = 0xdeadbeef;
	public static final int VERSION = 1;
	
	public static final byte NULL = 0;
	public static final byte REFERENCE = 1;
	public static final byte CLASSDESC = 2;
	public static final byte OBJECT = 3;
	public static final byte STRING = 4;
	public static final byte ARRAY = 5;
	public static final byte CLASS = 6;
	public static final byte BLOCK_DATA = 7;
	public static final byte WALL = 8;
	public static final byte RESET = 9;
	public static final byte EXCEPTION = 10;
	public static final byte PROXYCLASSDESC = 11;
	public static final byte ENUM = 11;
	public static final byte FIELDS = 12;
	
	public static final byte SC_WRITE_FIELDS = 0;
	public static final byte SC_WRITE_OBJECT = 1;
	public static final byte SC_WRITE_EXTERNAL = 2;
	public static final byte SC_WRITE_ENUM = 3;
}
