package org.purplejrank;

/**
 * Constants used when serializing or deserializing
 * @author robin
 *
 */
public class JrankConstants {
	public static final int J_MAX_BLOCK_SIZE = 1024*1024;
	public static final int J_MAGIC = 0xdeadbeef;
	public static final int J_VERSION = 1;
	
	public static final byte J_NULL = 0;
	public static final byte J_REFERENCE = 1;
	public static final byte J_CLASSDESC = 2;
	public static final byte J_OBJECT = 3;
	public static final byte J_STRING = 4;
	public static final byte J_ARRAY = 5;
	public static final byte J_CLASS = 6;
	public static final byte J_BLOCK_DATA = 7;
	public static final byte J_WALL = 8;
	public static final byte J_RESET = 9;
	public static final byte J_EXCEPTION = 10;
	public static final byte J_PROXYCLASSDESC = 11;
	public static final byte J_ENUM = 11;
	public static final byte J_FIELDS = 12;
	
	public static final byte J_SC_WRITE_FIELDS = 0x01;
	public static final byte J_SC_WRITE_OBJECT = 0x02;
	public static final byte J_SC_WRITE_EXTERNAL = 0x04;
	public static final byte J_SC_WRITE_ENUM = 0x08;
	public static final byte J_SC_SERIALIZABLE = 0x10;
			
}
