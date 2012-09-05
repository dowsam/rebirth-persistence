/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-persistence TypeInformation.java 2012-2-11 21:19:29 l.xue.nong$$
 */
package cn.com.rebirth.persistence;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * The Interface TypeInformation.
 *
 * @param <S> the generic type
 * @author l.xue.nong
 */
public interface TypeInformation<S> {
	
	/**
	 * Gets the parameter types.
	 *
	 * @param constructor the constructor
	 * @return the parameter types
	 */
	List<TypeInformation<?>> getParameterTypes(Constructor<?> constructor);

	/**
	 * Returns the property information for the property with the given name. Supports proeprty traversal through dot
	 * notation.
	 *
	 * @param fieldname the fieldname
	 * @return the property
	 */
	TypeInformation<?> getProperty(String fieldname);

	/**
	 * Returns whether the type can be considered a collection, which means it's a container of elements, e.g. a
	 *
	 * @return true, if is collection like
	 * {@link java.util.Collection} and {@link java.lang.reflect.Array} or anything implementing {@link Iterable}. If this
	 * returns {@literal true} you can expect {@link #getComponentType()} to return a non-{@literal null} value.
	 */
	boolean isCollectionLike();

	/**
	 * Returns the component type for {@link java.util.Collection}s or the key type for {@link java.util.Map}s.
	 *
	 * @return the component type
	 */
	TypeInformation<?> getComponentType();

	/**
	 * Returns whether the property is a {@link java.util.Map}. If this returns {@literal true} you can expect
	 *
	 * @return true, if is map
	 * {@link #getComponentType()} as well as {@link #getMapValueType()} to return something not {@literal null}.
	 */
	boolean isMap();

	/**
	 * Will return the type of the value in case the underlying type is a {@link java.util.Map}.
	 *
	 * @return the map value type
	 */
	TypeInformation<?> getMapValueType();

	/**
	 * Returns the type of the property. Will resolve generics and the generic context of
	 *
	 * @return the type
	 */
	Class<S> getType();

	/**
	 * Transparently returns the {@link java.util.Map} value type if the type is a {@link java.util.Map}, returns the
	 * component type if the type {@link #isCollectionLike()} or the simple type if none of this applies.
	 *
	 * @return the actual type
	 */
	TypeInformation<?> getActualType();

	/**
	 * Returns a {@link TypeInformation} for the return type of the given {@link Method}. Will potentially resolve
	 * generics information against the current types type parameter bindings.
	 *
	 * @param method must not be {@literal null}.
	 * @return the return type
	 */
	TypeInformation<?> getReturnType(Method method);
}
