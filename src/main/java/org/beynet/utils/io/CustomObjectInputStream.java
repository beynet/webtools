package org.beynet.utils.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * use specified class loader to resolveClass
 * @author beynet
 *
 */
public class CustomObjectInputStream extends ObjectInputStream {
	private ClassLoader classLoader;

	public CustomObjectInputStream(InputStream in, ClassLoader classLoader) throws IOException {
		super(in);
		this.classLoader = classLoader;
	}

	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
	ClassNotFoundException {			
		return Class.forName(desc.getName(), false, classLoader);
	}
}