/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-persistence GenericArrayTypeInformation.java 2012-2-11 21:29:51 l.xue.nong$$
 */
package cn.com.summall.persistence.utils;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

import cn.com.summall.persistence.TypeInformation;

/**
 * The Class GenericArrayTypeInformation.
 *
 * @param <S> the generic type
 * @author l.xue.nong
 */
class GenericArrayTypeInformation<S> extends ParentTypeAwareTypeInformation<S> {

	/** The type. */
	private GenericArrayType type;

	/**
	 * Creates a new {@link GenericArrayTypeInformation} for the given {@link GenericArrayTypeInformation} and.
	 *
	 * @param type the type
	 * @param parent the parent
	 * {@link TypeDiscoverer}.
	 */
	protected GenericArrayTypeInformation(GenericArrayType type, TypeDiscoverer<?> parent) {
		super(type, parent, parent.getTypeVariableMap());
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.util.TypeDiscoverer#getType()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Class<S> getType() {

		return (Class<S>) Array.newInstance(resolveType(type.getGenericComponentType()), 0).getClass();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.util.TypeDiscoverer#getComponentType()
	 */
	@Override
	public TypeInformation<?> getComponentType() {

		Type componentType = type.getGenericComponentType();
		return createInfo(componentType);
	}
}
