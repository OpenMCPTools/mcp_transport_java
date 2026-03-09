package org.openmcptools.transport.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class GenericTypeRef<T> {

	private final Type type;

	protected GenericTypeRef(Type type) {
		this.type = type;
	}
	
	protected GenericTypeRef() {
		Type superClass = getClass().getGenericSuperclass();
		if (superClass instanceof Class) {
			throw new IllegalStateException("GenericTypeRef constructed without actual type information");
		}
		this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
	}

	public Type getType() {
		return type;
	}

}