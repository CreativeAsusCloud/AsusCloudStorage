package com.asuscloud.storage;
/**
 * ByteUtils.java
 * Copyright 2007 thomas at acrosome dot com
 */


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

public final class ByteUtils
{
	/**
		Private constructor to prevent instanciation
	*/
	private ByteUtils()
	{
		// do nothing!
	}

	private final static char[] HEXCHAR = {	'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};


	public static final long switchEndian(long l)
	{
		return ( l >>> 56 )
				| (( l >>> 40 ) & 0x0000FF00)
				| (( l >>> 24 ) & 0x00FF0000)
				| (( l >>> 8 ) & 0xFF000000L)
				| (( l & 0xFF000000L) << 8 )
				| (( l & 0x00FF0000) << 24 )
				| (( l & 0x0000FF00) << 40 )
				| ( l << 56 )
				;
	}

	public static final int switchEndian(int i)
	{
		return ((i & 0xff000000) >>> 24)|
			   ((i & 0x00ff0000) >>> 8)	|
			   ((i & 0x0000ff00) << 8)	|
			   ((i & 0x000000ff) << 24);
	}

	public static final short switchEndian(short i)
	{
		return (short)(((i & 0xff00) >>> 8)	|
					   ((i & 0x00ff) << 8));
	}

	/**
	 * Convert java short to big/little endian byte sequence
	 *
	 * @param value the java short value
	 * @param bigEndian true if big endian, false if little endian
	 * @return byte[] the byte sequence
	 */
	public static final byte[] getBytes(short value, boolean bigEndian)
	{
		byte[] buf = new byte[2];
		getBytes(value, buf, 0, bigEndian);
		return buf;
	}

	/**
	 * Convert java short to big/little endian byte sequence, and put the
	 * sequence into byte array in the given position
	 *
	 * @param value the java short value
	 * @param buf the target byte array to insert the result
	 * @param pos the target position of array
	 * @param bigEndian true if big endian, false if little endian
	 */
	public static final void getBytes(short value, byte[] buf, int pos, boolean bigEndian)
	{
		if (bigEndian)
		{
			buf[pos + 1] = (byte) value;
			buf[pos] = (byte) (value >>> 8);
		}
		else
		{
			buf[pos] = (byte) value;
			buf[pos + 1] = (byte) (value >>> 8);
		}
	}

	/**
	 * Convert byte sequence into java short from first 2 bytes
	 *
	 * @param buf the source byte array
	 * @param bigEndian true if big endian, false if little endian
	 * @return short the java short
	 */
	public static final short getShort(byte[] buf, boolean bigEndian)
	{
		return getShort(buf, 0, bigEndian);
	}

	/**
	 * Convert byte sequence into java short.
	 *
	 * @param buf the source byte array
	 * @param pos the position of array to convert from
	 * @param bigEndian true if big endian, false if little endian
	 * @return short the java short
	 */
	public static final short getShort(byte[] buf, int pos, boolean bigEndian)
	{
		if (bigEndian)
		{
			return (short) ((buf[pos] << 8) | (buf[pos + 1] & 0xff));
		}
		else
		{
			return (short) ((buf[pos + 1] << 8) | (buf[pos] & 0xff));
		}
	}

	/**
	 * Convert java float to big/little endian byte sequence
	 *
	 * @param value the java float value
	 * @param bigEndian true if big endian, false if little endian
	 * @return byte[] the byte sequence
	 */
	public static final byte[] getBytes(float value, boolean bigEndian)
	{
		return getBytes(Float.floatToRawIntBits(value), bigEndian);
	}

	/**
	 * Convert java int to big/little endian byte sequence
	 *
	 * @param value the java int value
	 * @param bigEndian true if big endian, false if little endian
	 * @return byte[] the byte sequence
	 */
	public static final byte[] getBytes(int value, boolean bigEndian)
	{
		byte[] buf = new byte[4];
		getBytes(value, buf, 0, bigEndian);
		return buf;
	}

	/**
	 * Convert java int to big/little endian byte sequence, and put the sequence
	 * into byte array in the given position
	 *
	 * @param value the java int value
	 * @param buf the target byte array to insert the result
	 * @param pos the target position of array
	 * @param bigEndian true if big endian, false if little endian
	 */
	public static final void getBytes(int value, byte[] buf, int pos, boolean bigEndian)
	{
		if (bigEndian)
		{
			buf[pos + 3] = (byte) value;
			buf[pos + 2] = (byte) (value >>> 8);
			buf[pos + 1] = (byte) (value >>> 16);
			buf[pos] = (byte) (value >>> 24);
		}
		else
		{
			buf[pos] = (byte) value;
			buf[pos + 1] = (byte) (value >>> 8);
			buf[pos + 2] = (byte) (value >>> 16);
			buf[pos + 3] = (byte) (value >>> 24);
		}
	}

	/**
	 * Convert byte sequence into float short from first 4 bytes
	 *
	 * @param buf the source byte array
	 * @param bigEndian true if big endian, false if little endian
	 * @return short the java float
	 */
	public static final float getFloat(byte[] buf, boolean bigEndian)
	{
		return Float.intBitsToFloat(getInt(buf, bigEndian));
	}

	/**
	 * Convert byte sequence into java short from first 4 bytes
	 *
	 * @param buf the source byte array
	 * @param bigEndian true if big endian, false if little endian
	 * @return short the java short
	 */
	public static final int getInt(byte[] buf, boolean bigEndian)
	{
		return getInt(buf, 0, bigEndian);
	}

	/**
	 * Convert byte sequence into java int.
	 *
	 * @param buf the source byte array
	 * @param pos the position of array to convert from
	 * @param bigEndian true if big endian, false if little endian
	 * @return short the java int
	 */
	public static final int getInt(byte[] buf, int pos, boolean bigEndian)
	{
		if (bigEndian)
		{
			return ((buf[pos] & 0xff) << 24)
					| ((buf[pos + 1] & 0xff) << 16)
					| ((buf[pos + 2] & 0xff) << 8)
					| (buf[pos + 3] & 0xff);
		}
		else
		{
			return ((buf[pos + 3] & 0xff) << 24)
					| ((buf[pos + 2] & 0xff) << 16)
					| ((buf[pos + 1] & 0xff) << 8)
					| (buf[pos] & 0xff);
		}
	}

	/**
	 * Convert java double to big/little endian byte sequence
	 *
	 * @param value the java double value
	 * @param bigEndian true if big endian, false if little endian
	 * @return byte[] the byte sequence
	 */
	public static final byte[] getBytes(double value, boolean bigEndian)
	{
		return getBytes(Double.doubleToRawLongBits(value), bigEndian);
	}


	/**
	 * Convert java long to big/little endian byte sequence
	 *
	 * @param value the java long value
	 * @param bigEndian true if big endian, false if little endian
	 * @return byte[] the byte sequence
	 */
	public static final byte[] getBytes(long value, boolean bigEndian)
	{
		byte[] buf = new byte[8];
		getBytes(value, buf, 0, bigEndian);
		return buf;
	}

	/**
	 * Convert java long to big/little endian byte sequence, and put the
	 * sequence into byte array in the given position
	 *
	 * @param value the java long value
	 * @param buf the target byte array to insert the result
	 * @param pos the target position of array
	 * @param bigEndian true if big endian, false if little endian
	 */
	public static final void getBytes(long value, byte[] buf, int pos, boolean bigEndian)
	{
		if (bigEndian)
		{
			buf[pos + 7] = (byte) (value);
			buf[pos + 6] = (byte) (value >>> 8);
			buf[pos + 5] = (byte) (value >>> 16);
			buf[pos + 4] = (byte) (value >>> 24);
			buf[pos + 3] = (byte) (value >>> 32);
			buf[pos + 2] = (byte) (value >>> 40);
			buf[pos + 1] = (byte) (value >>> 48);
			buf[pos] = (byte) (value >>> 56);
		}
		else
		{
			buf[pos] = (byte) (value);
			buf[pos + 1] = (byte) (value >>> 8);
			buf[pos + 2] = (byte) (value >>> 16);
			buf[pos + 3] = (byte) (value >>> 24);
			buf[pos + 4] = (byte) (value >>> 32);
			buf[pos + 5] = (byte) (value >>> 40);
			buf[pos + 6] = (byte) (value >>> 48);
			buf[pos + 7] = (byte) (value >>> 56);
		}
	}

	/**
	 * Convert byte sequence into java double from first 8 bytes
	 *
	 * @param buf the source byte array
	 * @param bigEndian true if big endian, false if little endian
	 * @return short the java double
	 */
	public static final double getDouble(byte[] buf, boolean bigEndian)
	{
		return Double.longBitsToDouble(getLong(buf, bigEndian));
	}

	/**
	 * Convert byte sequence into java long from first 8 bytes
	 *
	 * @param buf the source byte array
	 * @param bigEndian true if big endian, false if little endian
	 * @return short the java long
	 */
	public static final long getLong(byte[] buf, boolean bigEndian)
	{
		return getLong(buf, 0, bigEndian);
	}

	/**
	 * Convert byte sequence into java long.
	 *
	 * @param buf the source byte array
	 * @param pos the position of array to convert from
	 * @param bigEndian true if big endian, false if little endian
	 * @return short the java long
	 */
	public static final long getLong(byte[] buf, int pos, boolean bigEndian)
	{
		if (bigEndian)
		{
			return (((long) buf[pos + 7]) & 0xFF) |
				   ((((long) buf[pos + 6]) & 0xFF) << 8) |
				   ((((long) buf[pos + 5]) & 0xFF) << 16) |
				   ((((long) buf[pos + 4]) & 0xFF) << 24) |
				   ((((long) buf[pos + 3]) & 0xFF) << 32) |
				   ((((long) buf[pos + 2]) & 0xFF) << 40) |
				   ((((long) buf[pos + 1]) & 0xFF) << 48) |
				   ((((long) buf[pos]) & 0xFF) << 56);
		}
		else
		{
			return (((long) buf[pos]) & 0xFF) |
				   ((((long) buf[pos + 1]) & 0xFF) << 8) |
				   ((((long) buf[pos + 2]) & 0xFF) << 16) |
				   ((((long) buf[pos + 3]) & 0xFF) << 24) |
				   ((((long) buf[pos + 4]) & 0xFF) << 32) |
				   ((((long) buf[pos + 5]) & 0xFF) << 40) |
				   ((((long) buf[pos + 6]) & 0xFF) << 48) |
				   ((((long) buf[pos + 7]) & 0xFF) << 56);
		}
	}

	public static final String getHexString(byte[] bytes)
	{
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for ( int i = 0; i < bytes.length; ++i)
		{
			sb.append(HEXCHAR[(bytes[i] & 0xF0) >>> 4]);
			sb.append(HEXCHAR[bytes[i] & 0x0F]);
		}
		return sb.toString();
	}

	/**
	 *	Returns a string representation of the <code>byte</code> argument as an unsigned integer in base 16.
	 *
	 *	@param n the number
	 *	@return the string representation of the unsigned <code>byte</code> value represented by the argument in hexadecimal (base 16).
	 */
	public static final String getHexString(byte n)
	{
		return new String(toUnsignedString(n, 2));
	}

	/**
	 *	Returns a string representation of the <code>short</code> argument as an unsigned integer in base 16.
	 *
	 *	@param n the number
	 *	@return the string representation of the unsigned <code>short</code> value represented by the argument in hexadecimal (base 16).
	 */
	public static final String getHexString(short n)
	{
		return new String(toUnsignedString(n, 4));
	}

	/**
	 *	Returns a string representation of the <code>char</code> argument as an unsigned integer in base 16.
	 *
	 *	@param n the number
	 *	@return the string representation of the unsigned <code>char</code> value represented by the argument in hexadecimal (base 16).
	 */
	public static final String getHexString(char n)
	{
		return new String(toUnsignedString(((short)n), 4));
	}

	/**
	 *	Returns a string representation of the <code>char</code> argument as an unsigned integer in base 16.
	 *	this method is difference form <code>Integer.toHexString()</code>. preserving the leading-zeros.
	 *	<code>
	 *		Integer.toHexString(1718);
	 *		// returns 6B6
	 *		BytesUtil.getHexString(1718);
	 *		// returns 000006B6
	 *	</code>
	 *
	 *	@param n the number
	 *	@return the string representation of the unsigned <code>char</code> value represented by the argument in hexadecimal (base 16).
	 */
	public static final String getHexString(int n)
	{
		return new String(toUnsignedString(n, 8));
	}

	/**
	 *	Returns a string representation of the <code>float</code> argument as an unsigned integer in base 16,
	 *	preserving Not-a-Number (NaN) values.
	 *
	 *	@param n the number
	 *	@return the string representation of the unsigned <code>float</code> value represented by the argument in hexadecimal (base 16).
	 */
	public static final String getHexString(float n)
	{
		return new String(toUnsignedString(Float.floatToRawIntBits(n), 8));
	}

	/**
	 *	Returns a string representation of the <code>long</code> argument as an unsigned integer in base 16.
	 *
	 *	@param n the number
	 *	@return the string representation of the unsigned <code>long</code> value represented by the argument in hexadecimal (base 16).
	 */
	public static final String getHexString(long n)
	{
		return new String(toUnsignedString(n, 16));
	}

	/**
	 *	Returns a string representation of the <code>double</code> argument as an unsigned integer in base 16,
	 *	preserving Not-a-Number (NaN) values.
	 *
	 *	@param n the number
	 *	@return the string representation of the unsigned <code>double</code> value represented by the argument in hexadecimal (base 16).
	 */
	public static final String getHexString(double n)
	{
		return new String(toUnsignedString(Double.doubleToRawLongBits(n), 16));
	}

	private static final char[] toUnsignedString(long x, int length)
	{
		char[] c = new char[length];
		int pos = c.length;
		while ( --pos >= 0)
		{
			c[pos] = HEXCHAR[(int)x & 0x0F];
			x >>>= 4;
		}
		return c;
	}

	public static final byte getByte(char hexChar1, char hexChar2)
	{
		byte b = 0;
		if ( '0' <= hexChar1 && hexChar1 <= '9')
		{
			b += (byte)(hexChar1 - '0') << 4;
		}
		else if ( 'a' <= hexChar1 && hexChar1 <= 'f' )
		{
			b += (byte)(hexChar1 - 'a' + 10) << 4;
		}
		else if ( 'A' <= hexChar1 && hexChar1 <= 'F' )
		{
			b += (byte)(hexChar1 - 'A' + 10) << 4;
		}
		else
		{
			throw new IllegalArgumentException("Bad Character \"" + hexChar1 + "\" to convert to byte!");
		}
		if ( '0' <= hexChar2 && hexChar2 <= '9')
		{
			b += (hexChar2 - '0');
		}
		else if ( 'a' <= hexChar2 && hexChar2 <= 'f' )
		{
			b += (hexChar2 - 'a' + 10);
		}
		else if ( 'A' <= hexChar2 && hexChar2 <= 'F' )
		{
			b += (hexChar2 - 'A' + 10);
		}
		else
		{
			throw new IllegalArgumentException("Bad Character \"" + hexChar2 + "\" to convert to byte!");
		}
		return b;
	}

	public static final byte[] getBytes(String hexString)
	{
		if ( hexString == null || hexString.length() < 2 || hexString.length() % 2 != 0 )
		{
			throw new IllegalArgumentException("\"" + hexString + "\" is null or contains odd number of hex char");
		}
		byte[] bytes = new byte[hexString.length() / 2];
		for ( int i = 0; i < bytes.length; ++i )
		{
			bytes[i] = getByte(hexString.charAt(i * 2), hexString.charAt(i * 2 + 1));
		}
		return bytes;
	}

	public static final byte[] readBytes(InputStream in) throws IOException
	{
		byte[] ret = new byte[0];
		byte[] buf = new byte[2048];
		int len;
		for ( ;; )
		{
			len = in.read(buf);
			if ( len > 0 )
			{
				ret = Arrays.copyOf(ret, ret.length + len);
				System.arraycopy(buf, 0, ret, ret.length - len, len);
			}
			else
			{
				break;
			}
		}
		return ret;
	}

	public static final byte[] readBytes(String path) throws IOException
	{
		return readBytes(new File(path));
	}

	public static final byte[] readBytes(File file) throws IOException
	{
		FileInputStream in = new FileInputStream(file.getAbsolutePath());
		try
		{
			return readBytes(in);
		}
		catch ( IOException e )
		{
			throw e;
		}
		finally
		{
			in.close();
		}
	}

	public static final void saveBytes(byte[] bytes, String file) throws IOException
	{
		saveBytes(bytes, new File(file));
	}

	public static final void saveBytes(byte[] bytes, File file) throws IOException
	{
		file.createNewFile();
		FileOutputStream out = new FileOutputStream(file);
		try
		{
			saveBytes(bytes, out);
		}
		catch ( IOException e )
		{
			throw e;
		}
		finally
		{
			out.close();
		}
	}

	/** this will not close the OutputStream. it may need for further use */
	public static final void saveBytes(byte[] bytes, OutputStream out) throws IOException
	{
		out.write(bytes);
		out.flush();
	}

	public static final void generateBytesToFile(int len, String file) throws IOException
	{
		saveBytes(generateBytes(len), file);
	}

	public static final void generateBytesToFile(int len, File file) throws IOException
	{
		saveBytes(generateBytes(len), file);
	}

	public static final byte[] generateBytes(int len)
	{
		byte[] bytes = new byte[len];
		fillRandomBytes(bytes);
		return bytes;
	}

	public static final void fillRandomBytes(byte[] bytes)
	{
		Random ran = new Random();
		for ( int i = 0; i < bytes.length; ++i )
		{
			if ( i > 0 && i % 120 == 0 )
			{
				bytes[i] = '\n';
			}
			else
			{
				bytes[i] = (byte)( ran.nextInt(10) + 48 );
			}
		}
	}
}
