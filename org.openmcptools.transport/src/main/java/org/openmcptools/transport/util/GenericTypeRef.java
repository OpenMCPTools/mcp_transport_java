package org.openmcptools.transport.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Utility class for capturing and retaining generic type information at runtime.
 *
 * @param <T> the type to be referenced
 */
public abstract class GenericTypeRef<T> {

	private final Type type;

	/**
	 * Constructs a type reference with an explicit type.
	 *
	 * @param type the type to reference
	 */
	protected GenericTypeRef(Type type) {
		this.type = type;
	}
	
	/**
	 * Constructs a type reference by extracting the type argument from the subclass.
	 *
	 * @throws IllegalStateException if the class is not parameterized
	 */
	protected GenericTypeRef() {
		Type superClass = getClass().getGenericSuperclass();
		if (superClass instanceof Class) {
			throw new IllegalStateException("GenericTypeRef constructed without actual type information");
		}
		this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
	}

	/**
	 * Returns the captured type.
	 *
	 * @return the type
	 */
	public Type getType() {
		return type;
	}
}