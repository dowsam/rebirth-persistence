/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-persistence TypeDiscoverer.java 2012-2-11 21:24:50 l.xue.nong$$
 */
package cn.com.summall.persistence.utils;

import static org.springframework.util.ObjectUtils.*;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeanUtils;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import cn.com.summall.persistence.TypeInformation;

/**
 * The Class TypeDiscoverer.
 *
 * @param <S> the generic type
 * @author l.xue.nong
 */
class TypeDiscoverer<S> implements TypeInformation<S> {
	
	/** The type. */
	private final Type type;
	
	/** The type variable map. */
	@SuppressWarnings("rawtypes")
	private final Map<TypeVariable, Type> typeVariableMap;
	
	/** The field types. */
	private final Map<String, TypeInformation<?>> fieldTypes = new ConcurrentHashMap<String, TypeInformation<?>>();

	/**
	 * Creates a ne {@link TypeDiscoverer} for the given type, type variable map and parent.
	 *
	 * @param type must not be null.
	 * @param typeVariableMap the type variable map
	 */
	@SuppressWarnings("rawtypes")
	protected TypeDiscoverer(Type type, Map<TypeVariable, Type> typeVariableMap) {

		Assert.notNull(type);
		this.type = type;
		this.typeVariableMap = typeVariableMap;
	}

	/**
	 * Returns the type variable map. Will traverse the parents up to the root on and use it's map.
	 *
	 * @return the type variable map
	 */
	@SuppressWarnings("rawtypes")
	protected Map<TypeVariable, Type> getTypeVariableMap() {

		return typeVariableMap;
	}

	/**
	 * Creates {@link TypeInformation} for the given {@link Type}.
	 *
	 * @param fieldType the field type
	 * @return the type information
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected TypeInformation<?> createInfo(Type fieldType) {

		if (fieldType.equals(this.type)) {
			return this;
		}

		if (fieldType instanceof Class) {
			return new ClassTypeInformation((Class<?>) fieldType);
		}

		Map<TypeVariable, Type> variableMap = GenericTypeResolver.getTypeVariableMap(resolveType(fieldType));

		if (fieldType instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) fieldType;
			return new ParameterizedTypeInformation(parameterizedType, this);
		}

		if (fieldType instanceof TypeVariable) {
			TypeVariable<?> variable = (TypeVariable<?>) fieldType;
			return new TypeVariableTypeInformation(variable, type, this, variableMap);
		}

		if (fieldType instanceof GenericArrayType) {
			return new GenericArrayTypeInformation((GenericArrayType) fieldType, this);
		}

		if (fieldType instanceof WildcardType) {

			WildcardType wildcardType = (WildcardType) fieldType;
			Type[] bounds = wildcardType.getLowerBounds();

			if (bounds.length > 0) {
				return createInfo(bounds[0]);
			}

			bounds = wildcardType.getUpperBounds();

			if (bounds.length > 0) {
				return createInfo(bounds[0]);
			}
		}

		throw new IllegalArgumentException();
	}

	/**
	 * Resolves the given type into a plain {@link Class}.
	 *
	 * @param type the type
	 * @return the class
	 */
	@SuppressWarnings("unchecked")
	protected Class<S> resolveType(Type type) {

		return (Class<S>) GenericTypeResolver.resolveType(type, getTypeVariableMap());
	}

	/*
		 * (non-Javadoc)
		 * @see org.springframework.data.util.TypeInformation#getParameterTypes(java.lang.reflect.Constructor)
		 */
	public List<TypeInformation<?>> getParameterTypes(Constructor<?> constructor) {

		List<TypeInformation<?>> result = new ArrayList<TypeInformation<?>>();

		for (Type type : constructor.getGenericParameterTypes()) {
			result.add(createInfo(type));
		}

		return result;
	}

	/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.springframework.data.document.mongodb.TypeDiscovererTest.FieldInformation
		 * #getField(java.lang.String)
		 */
	public TypeInformation<?> getProperty(String fieldname) {

		int separatorIndex = fieldname.indexOf('.');

		if (separatorIndex == -1) {
			if (fieldTypes.containsKey(fieldname)) {
				return fieldTypes.get(fieldname);
			}

			TypeInformation<?> propertyInformation = getPropertyInformation(fieldname);
			if (propertyInformation != null) {
				fieldTypes.put(fieldname, propertyInformation);
			}
			return propertyInformation;
		}

		String head = fieldname.substring(0, separatorIndex);
		TypeInformation<?> info = fieldTypes.get(head);
		return info == null ? null : info.getProperty(fieldname.substring(separatorIndex + 1));
	}

	/**
	 * Returns the {@link TypeInformation} for the given atomic field. Will inspect fields first and return the type of a
	 * field if available. Otherwise it will fall back to a {@link PropertyDescriptor}.
	 *
	 * @param fieldname the fieldname
	 * @return the property information
	 * @see #getGenericType(PropertyDescriptor)
	 */
	private TypeInformation<?> getPropertyInformation(String fieldname) {

		Class<?> type = getType();
		Field field = ReflectionUtils.findField(type, fieldname);

		if (field != null) {
			return createInfo(field.getGenericType());
		}

		PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(type, fieldname);

		return descriptor == null ? null : createInfo(getGenericType(descriptor));
	}

	/**
	 * Returns the generic type for the given {@link PropertyDescriptor}. Will inspect its read method followed by the
	 * first parameter of the write method.
	 *
	 * @param descriptor must not be {@literal null}
	 * @return the generic type
	 */
	private static Type getGenericType(PropertyDescriptor descriptor) {

		Method method = descriptor.getReadMethod();

		if (method != null) {
			return method.getGenericReturnType();
		}

		method = descriptor.getWriteMethod();

		if (method == null) {
			return null;
		}

		Type[] parameterTypes = method.getGenericParameterTypes();
		return parameterTypes.length == 0 ? null : parameterTypes[0];
	}

	/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.springframework.data.document.mongodb.TypeDiscovererTest.FieldInformation
		 * #getType()
		 */
	public Class<S> getType() {
		return resolveType(type);
	}

	/* (non-Javadoc)
	 * @see org.springframework.data.util.TypeInformation#getActualType()
	 */
	public TypeInformation<?> getActualType() {
		if (isMap()) {
			return getMapValueType();
		} else if (isCollectionLike()) {
			return getComponentType();
		} else {
			return this;
		}
	}

	/* (non-Javadoc)
		 * @see org.springframework.data.util.TypeInformation#isMap()
		 */
	public boolean isMap() {
		return Map.class.isAssignableFrom(getType());
	}

	/* (non-Javadoc)
		 * @see org.springframework.data.util.TypeInformation#getMapValueType()
		 */
	public TypeInformation<?> getMapValueType() {

		if (!isMap()) {
			return null;
		}

		return getTypeArgument(getType(), Map.class, 1);
	}

	/**
	 * Gets the type argument.
	 *
	 * @param type the type
	 * @param bound the bound
	 * @param index the index
	 * @return the type argument
	 */
	private TypeInformation<?> getTypeArgument(Class<?> type, Class<?> bound, int index) {
		Class<?>[] arguments = GenericTypeResolver.resolveTypeArguments(type, bound);
		return arguments == null ? null : createInfo(arguments[index]);
	}

	/* (non-Javadoc)
		 * @see org.springframework.data.util.TypeInformation#isCollectionLike()
		 */
	public boolean isCollectionLike() {

		Class<?> rawType = getType();
		return rawType.isArray() || Iterable.class.isAssignableFrom(rawType);
	}

	/* (non-Javadoc)
		 * @see org.springframework.data.util.TypeInformation#getComponentType()
		 */
	public TypeInformation<?> getComponentType() {

		if (!(isMap() || isCollectionLike())) {
			return null;
		}

		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			return createInfo(parameterizedType.getActualTypeArguments()[0]);
		}

		Class<S> rawType = getType();

		if (isMap()) {
			return getTypeArgument(rawType, Map.class, 0);
		}

		if (Iterable.class.isAssignableFrom(rawType)) {
			return getTypeArgument(rawType, Iterable.class, 0);
		}

		if (rawType.isArray()) {
			return createInfo(rawType.getComponentType());
		}

		return null;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.util.TypeInformation#getReturnType(java.lang.reflect.Method)
	 */
	public TypeInformation<?> getReturnType(Method method) {

		Assert.notNull(method);
		return createInfo(method.getGenericReturnType());
	}

	/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (!this.getClass().equals(obj.getClass())) {
			return false;
		}

		TypeDiscoverer<?> that = (TypeDiscoverer<?>) obj;

		boolean typeEqual = nullSafeEquals(this.type, that.type);
		boolean typeVariableMapEqual = nullSafeEquals(this.typeVariableMap, that.typeVariableMap);

		return typeEqual && typeVariableMapEqual;
	}

	/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#hashCode()
		 */
	@Override
	public int hashCode() {

		int result = 17;
		result += nullSafeHashCode(type);
		result += nullSafeHashCode(typeVariableMap);
		return result;
	}
}
