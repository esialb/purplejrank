package org.purplejrank;

import java.io.Externalizable;
import java.io.Serializable;

/**
 * Constants used when serializing or deserializing
 * @author robin
 *
 */
public class JrankConstants {
	/**
	 * Size of the input buffers, and max block size
	 */
	public static final int J_MAX_BLOCK_SIZE = 64*1024;
	/**
	 * Stream magic
	 */
	public static final int J_MAGIC = 0xdeadbeef;
	/**
	 * Stream version
	 */
	public static final int J_VERSION = 3;
	
	/**
	 * Token for a null
	 */
	public static final byte J_NULL = 0;
	
	/**
	 * Token for a backreference
	 */
	public static final byte J_REFERENCE = 1;
	
	/**
	 * Token for a concrete class descriptor
	 */
	public static final byte J_CLASSDESC = 2;
	
	/**
	 * Token for a new object
	 */
	public static final byte J_OBJECT = 3;
	
	/**
	 * Token for a new string
	 */
	public static final byte J_STRING = 4;
	
	/**
	 * Token for a new array
	 */
	public static final byte J_ARRAY = 5;
	
	/**
	 * Token for a local class
	 */
	public static final byte J_CLASS = 6;
	
	/**
	 * Token for a block of primitive data
	 */
	public static final byte J_BLOCK_DATA = 7;
	
	/**
	 * Token for a section divider
	 */
	public static final byte J_WALL = 8;
	
	/**
	 * Token to reset the stream
	 */
	public static final byte J_RESET = 9;
	
	/**
	 * Token for stream exceptions
	 */
	public static final byte J_EXCEPTION = 10;
	
	/**
	 * Token for proxy class descriptors
	 */
	public static final byte J_PROXYCLASSDESC = 11;
	
	/**
	 * Token for enums
	 */
	public static final byte J_ENUM = 11;
	
	/**
	 * Token for field blocks
	 */
	public static final byte J_FIELDS = 12;
	
	/**
	 * Class doesn't have writeObject
	 */
	public static final byte J_SC_WRITE_FIELDS = 0x01;
	/**
	 * Class has writeObject
	 */
	public static final byte J_SC_WRITE_OBJECT = 0x02;
	/**
	 * Class is {@link Externalizable}
	 */
	public static final byte J_SC_WRITE_EXTERNAL = 0x04;
	/**
	 * Class is an enum
	 */
	public static final byte J_SC_WRITE_ENUM = 0x08;
	/**
	 * Class is {@link Serializable}
	 */
	public static final byte J_SC_SERIALIZABLE = 0x10;
			
}
