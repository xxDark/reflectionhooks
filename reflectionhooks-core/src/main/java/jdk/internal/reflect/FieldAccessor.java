package jdk.internal.reflect;

// Dummy JDK class
public interface FieldAccessor {

	Object get(Object var1) throws IllegalArgumentException;

	boolean getBoolean(Object var1) throws IllegalArgumentException;

	byte getByte(Object var1) throws IllegalArgumentException;

	char getChar(Object var1) throws IllegalArgumentException;

	short getShort(Object var1) throws IllegalArgumentException;

	int getInt(Object var1) throws IllegalArgumentException;

	long getLong(Object var1) throws IllegalArgumentException;

	float getFloat(Object var1) throws IllegalArgumentException;

	double getDouble(Object var1) throws IllegalArgumentException;

	void set(Object var1, Object var2) throws IllegalArgumentException, IllegalAccessException;

	void setBoolean(Object var1, boolean var2)
			throws IllegalArgumentException, IllegalAccessException;

	void setByte(Object var1, byte var2) throws IllegalArgumentException, IllegalAccessException;

	void setChar(Object var1, char var2) throws IllegalArgumentException, IllegalAccessException;

	void setShort(Object var1, short var2) throws IllegalArgumentException, IllegalAccessException;

	void setInt(Object var1, int var2) throws IllegalArgumentException, IllegalAccessException;

	void setLong(Object var1, long var2) throws IllegalArgumentException, IllegalAccessException;

	void setFloat(Object var1, float var2) throws IllegalArgumentException, IllegalAccessException;

	void setDouble(Object var1, double var2) throws IllegalArgumentException, IllegalAccessException;
}
