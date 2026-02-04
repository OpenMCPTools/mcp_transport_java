package org.openmcptools.transport;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class GenericTypeRef<T> {

	private final Type type;

	protected GenericTypeRef() {
		Type superClass = getClass().getGenericSuperclass();
		if (superClass instanceof Class) {
			throw new IllegalStateException("TypeRef does not have type information");
		}
		this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
	}

	protected GenericTypeRef(Type type) {
		this.type = type;
	}
	
	public Type getType() {
		return type;
	}

}
