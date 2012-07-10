/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-persistence TypeVariableTypeInformation.java 2012-2-11 21:28:59 l.xue.nong$$
 */
package cn.com.summall.persistence.utils;

import static org.springframework.util.ObjectUtils.nullSafeEquals;
import static org.springframework.util.ObjectUtils.nullSafeHashCode;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * The Class TypeVariableTypeInformation.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
class TypeVariableTypeInformation<T> extends ParentTypeAwareTypeInformation<T> {

	/** The variable. */
	private final TypeVariable<?> variable;

	/** The owning type. */
	private final Type owningType;

	/**
	 * Creates a bew {@link TypeVariableTypeInformation} for the given {@link TypeVariable} owning {@link Type} and parent.
	 *
	 * @param variable must not be {@literal null}
	 * @param owningType must not be {@literal null}
	 * @param parent the parent
	 * @param map the map
	 * {@link TypeDiscoverer}.
	 */
	@SuppressWarnings("rawtypes")
	public TypeVariableTypeInformation(TypeVariable<?> variable, Type owningType, TypeDiscoverer<?> parent,
			Map<TypeVariable, Type> map) {

		super(variable, parent, map);
		Assert.notNull(variable);
		this.variable = variable;
		this.owningType = owningType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.document.mongodb.TypeDiscovererTest.TypeDiscoverer#getType()
	 */
	@Override
	public Class<T> getType() {

		int index = getIndex(variable);

		if (owningType instanceof ParameterizedType && index != -1) {
			Type fieldType = ((ParameterizedType) owningType).getActualTypeArguments()[index];
			return resolveType(fieldType);
		}

		return resolveType(variable);
	}

	/**
	 * Returns the index of the type parameter binding the given {@link TypeVariable}.
	 *
	 * @param variable the variable
	 * @return the index
	 */
	private int getIndex(TypeVariable<?> variable) {

		Class<?> rawType = resolveType(owningType);
		TypeVariable<?>[] typeParameters = rawType.getTypeParameters();

		for (int i = 0; i < typeParameters.length; i++) {
			if (variable.equals(typeParameters[i])) {
				return i;
			}
		}

		return -1;
	}

	/*
		 * (non-Javadoc)
		 *
		 * @see org.springframework.data.util.TypeDiscoverer#equals(java.lang.Object)
		 */
	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}

		TypeVariableTypeInformation<?> that = (TypeVariableTypeInformation<?>) obj;
		return nullSafeEquals(this.owningType, that.owningType) && nullSafeEquals(this.variable, that.variable);
	}

	/*
		 * (non-Javadoc)
		 *
		 * @see org.springframework.data.util.TypeDiscoverer#hashCode()
		 */
	@Override
	public int hashCode() {
		int result = super.hashCode();
		result += 31 * nullSafeHashCode(this.owningType);
		result += 31 * nullSafeHashCode(this.variable);
		return result;
	}
}