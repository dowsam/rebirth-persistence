/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-persistence ClassTypeInformation.java 2012-2-11 21:25:55 l.xue.nong$$
 */
package cn.com.rebirth.persistence.utils;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.springframework.core.GenericTypeResolver;
import org.springframework.util.Assert;

import cn.com.rebirth.persistence.TypeInformation;

/**
 * The Class ClassTypeInformation.
 *
 * @param <S> the generic type
 * @author l.xue.nong
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ClassTypeInformation<S> extends TypeDiscoverer<S> {

	/** The Constant COLLECTION. */
	public static final TypeInformation<Collection> COLLECTION = new ClassTypeInformation(Collection.class);

	/** The Constant LIST. */
	public static final TypeInformation<List> LIST = new ClassTypeInformation(List.class);

	/** The Constant SET. */
	public static final TypeInformation<Set> SET = new ClassTypeInformation(Set.class);

	/** The Constant MAP. */
	public static final TypeInformation<Map> MAP = new ClassTypeInformation(Map.class);

	/** The Constant OBJECT. */
	public static final TypeInformation<Object> OBJECT = new ClassTypeInformation(Object.class);

	/** The Constant CACHE. */
	private static final Map<Class<?>, Reference<TypeInformation<?>>> CACHE = Collections
			.synchronizedMap(new WeakHashMap<Class<?>, Reference<TypeInformation<?>>>());

	static {
		for (TypeInformation<?> info : Arrays.asList(COLLECTION, LIST, SET, MAP, OBJECT)) {
			CACHE.put(info.getType(), new WeakReference<TypeInformation<?>>(info));
		}
	}

	/** The type. */
	private final Class<S> type;

	/**
	 * Simple factory method to easily create new instances of {@link ClassTypeInformation}.
	 *
	 * @param <S> the generic type
	 * @param type the type
	 * @return the type information
	 */
	public static <S> TypeInformation<S> from(Class<S> type) {

		Reference<TypeInformation<?>> cachedReference = CACHE.get(type);
		TypeInformation<?> cachedTypeInfo = cachedReference == null ? null : cachedReference.get();

		if (cachedTypeInfo != null) {
			return (TypeInformation<S>) cachedTypeInfo;
		}

		TypeInformation<S> result = new ClassTypeInformation<S>(type);
		CACHE.put(type, new WeakReference<TypeInformation<?>>(result));
		return result;
	}

	/**
	 * Creates a {@link TypeInformation} from the given method's return type.
	 *
	 * @param <S> the generic type
	 * @param method the method
	 * @return the type information
	 */
	public static <S> TypeInformation<S> fromReturnTypeOf(Method method) {
		return new ClassTypeInformation(method.getDeclaringClass()).createInfo(method.getGenericReturnType());
	}

	/**
	 * Creates {@link ClassTypeInformation} for the given type.
	 *
	 * @param type the type
	 */
	public ClassTypeInformation(Class<S> type) {
		this(type, GenericTypeResolver.getTypeVariableMap(type));
	}

	/**
	 * Instantiates a new class type information.
	 *
	 * @param type the type
	 * @param typeVariableMap the type variable map
	 */
	ClassTypeInformation(Class<S> type, Map<TypeVariable, Type> typeVariableMap) {
		super(type, typeVariableMap);
		this.type = type;
	}

	/*
		 * (non-Javadoc)
		 *
		 * @see org.springframework.data.document.mongodb.TypeDiscovererTest.FieldInformation#getType()
		 */
	@Override
	public Class<S> getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.util.TypeDiscoverer#getComponentType()
	 */
	@Override
	public TypeInformation<?> getComponentType() {

		if (type.isArray()) {
			return createInfo(resolveArrayType(type));
		}

		return super.getComponentType();
	}

	/**
	 * Resolve array type.
	 *
	 * @param type the type
	 * @return the type
	 */
	private static Type resolveArrayType(Class<?> type) {
		Assert.isTrue(type.isArray());
		Class<?> componentType = type.getComponentType();
		return componentType.isArray() ? resolveArrayType(componentType) : componentType;
	}
}
