/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-persistence ParameterizedTypeInformation.java 2012-2-11 21:29:56 l.xue.nong$$
 */
package cn.com.summall.persistence.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.core.GenericTypeResolver;

import cn.com.summall.persistence.TypeInformation;

/**
 * The Class ParameterizedTypeInformation.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
class ParameterizedTypeInformation<T> extends ParentTypeAwareTypeInformation<T> {

	/** The type. */
	private final ParameterizedType type;

	/**
	 * Creates a new {@link ParameterizedTypeInformation} for the given {@link Type} and parent {@link TypeDiscoverer}.
	 * 
	 * @param type must not be {@literal null}
	 * @param parent must not be {@literal null}
	 */
	public ParameterizedTypeInformation(ParameterizedType type, TypeDiscoverer<?> parent) {
		super(type, parent, null);
		this.type = type;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.util.TypeDiscoverer#getMapValueType()
	 */
	@Override
	public TypeInformation<?> getMapValueType() {

		if (Map.class.equals(getType())) {
			Type[] arguments = type.getActualTypeArguments();
			return createInfo(arguments[1]);
		}

		Class<?> rawType = getType();

		Set<Type> supertypes = new HashSet<Type>();
		supertypes.add(rawType.getGenericSuperclass());
		supertypes.addAll(Arrays.asList(rawType.getGenericInterfaces()));

		for (Type supertype : supertypes) {
			Class<?> rawSuperType = GenericTypeResolver.resolveType(supertype, getTypeVariableMap());
			if (Map.class.isAssignableFrom(rawSuperType)) {
				ParameterizedType parameterizedSupertype = (ParameterizedType) supertype;
				Type[] arguments = parameterizedSupertype.getActualTypeArguments();
				return createInfo(arguments[1]);
			}
		}

		return super.getMapValueType();
	}
}
