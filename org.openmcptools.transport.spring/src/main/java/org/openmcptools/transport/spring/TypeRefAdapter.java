package org.openmcptools.transport.spring;

import java.lang.reflect.Type;

import org.openmcptools.transport.util.GenericTypeRef;

import io.modelcontextprotocol.json.TypeRef;

public class TypeRefAdapter<T> extends TypeRef<T> {

	private GenericTypeRef<T> ref;

	public TypeRefAdapter(GenericTypeRef<T> g) {
		this.ref = g;
	}

	public Type getType() {
		return this.ref.getType();
	}
}
