/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-persistence PropertyPath.java 2012-2-11 21:16:27 l.xue.nong$$
 */
package cn.com.rebirth.persistence.mapping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import cn.com.rebirth.persistence.TypeInformation;
import cn.com.rebirth.persistence.utils.ClassTypeInformation;

/**
 * The Class PropertyPath.
 *
 * @author l.xue.nong
 */
public class PropertyPath implements Iterable<PropertyPath> {
	
	/** The Constant DELIMITERS. */
	private static final String DELIMITERS = "_\\.";
	
	/** The Constant SPLITTER. */
	private static final Pattern SPLITTER = Pattern.compile("(?:[%s]?([%s]*?[^%s]+))".replaceAll("%s", DELIMITERS));
	
	/** The Constant ERROR_TEMPLATE. */
	private static final String ERROR_TEMPLATE = "No property %s found for type %s";

	/** The owning type. */
	private final TypeInformation<?> owningType;
	
	/** The name. */
	private final String name;
	
	/** The type. */
	private final TypeInformation<?> type;
	
	/** The is collection. */
	private final boolean isCollection;

	/** The next. */
	private PropertyPath next;

	/**
	 * Creates a leaf {@link PropertyPath} (no nested ones) with the given name inside the given owning type.
	 * 
	 * @param name must not be {@literal null} or empty.
	 * @param owningType must not be {@literal null}.
	 */
	PropertyPath(String name, Class<?> owningType) {

		this(name, ClassTypeInformation.from(owningType));
	}

	/**
	 * Creates a leaf {@link PropertyPath} (no nested ones with the given name and owning type.
	 * 
	 * @param name must not be {@literal null} or empty.
	 * @param owningType must not be {@literal null}.
	 */
	PropertyPath(String name, TypeInformation<?> owningType) {

		Assert.hasText(name);
		Assert.notNull(owningType);

		String propertyName = StringUtils.uncapitalize(name);
		TypeInformation<?> type = owningType.getProperty(propertyName);

		if (type == null) {
			throw new IllegalArgumentException(String.format(ERROR_TEMPLATE, propertyName, owningType.getType()));
		}

		this.owningType = owningType;
		this.isCollection = type.isCollectionLike();
		this.type = type.getActualType();
		this.name = propertyName;
	}

	/**
	 * Creates a {@link PropertyPath} with the given name inside the given owning type and tries to resolve the other
	 * {@link String} to create nested properties.
	 * 
	 * @param name must not be {@literal null} or empty.
	 * @param owningType must not be {@literal null}.
	 * @param toTraverse
	 */
	PropertyPath(String name, TypeInformation<?> owningType, String toTraverse) {

		this(name, owningType);

		if (StringUtils.hasText(toTraverse)) {
			this.next = from(toTraverse, type);
		}
	}

	/**
	 * Returns the owning type of the {@link PropertyPath}.
	 * 
	 * @return the owningType will never be {@literal null}.
	 */
	public TypeInformation<?> getOwningType() {
		return owningType;
	}

	/**
	 * Returns the name of the {@link PropertyPath}.
	 * 
	 * @return the name will never be {@literal null}.
	 */
	public String getSegment() {

		return name;
	}

	/**
	 * Returns the type of the property will return the plain resolved type for simple properties, the component type for
	 * any {@link Iterable} or the value type of a {@link java.util.Map} if the property is one.
	 * 
	 * @return
	 */
	public Class<?> getType() {

		return this.type.getType();
	}

	/**
	 * Returns the next nested {@link PropertyPath}.
	 * 
	 * @return the next nested {@link PropertyPath} or {@literal null} if no nested {@link PropertyPath} available.
	 * @see #hasNext()
	 */
	public PropertyPath next() {

		return next;
	}

	/**
	 * Returns whether there is a nested {@link PropertyPath}. If this returns {@literal true} you can expect
	 * {@link #next()} to return a non- {@literal null} value.
	 * 
	 * @return
	 */
	public boolean hasNext() {

		return next != null;
	}

	/**
	 * Returns the {@link PropertyPath} in dot notation.
	 * 
	 * @return
	 */
	public String toDotPath() {

		if (hasNext()) {
			return getSegment() + "." + next().toDotPath();
		}

		return getSegment();
	}

	/**
	 * Returns whether the {@link PropertyPath} is actually a collection.
	 * 
	 * @return
	 */
	public boolean isCollection() {

		return isCollection;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (obj == null || !getClass().equals(obj.getClass())) {
			return false;
		}

		PropertyPath that = (PropertyPath) obj;

		return this.name.equals(that.name) && this.type.equals(that.type);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return name.hashCode() + type.hashCode();
	}

	/* 
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<PropertyPath> iterator() {
		return new Iterator<PropertyPath>() {

			private PropertyPath current = PropertyPath.this;

			public boolean hasNext() {
				return current != null;
			}

			public PropertyPath next() {
				PropertyPath result = current;
				this.current = current.next();
				return result;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Extracts the {@link PropertyPath} chain from the given source {@link String} and type.
	 * 
	 * @param source
	 * @param type
	 * @return
	 */
	public static PropertyPath from(String source, Class<?> type) {

		return from(source, ClassTypeInformation.from(type));
	}

	/**
	 * Extracts the {@link PropertyPath} chain from the given source {@link String} and {@link TypeInformation}.
	 * 
	 * @param source must not be {@literal null}.
	 * @param type
	 * @return
	 */
	public static PropertyPath from(String source, TypeInformation<?> type) {

		List<String> iteratorSource = new ArrayList<String>();
		Matcher matcher = SPLITTER.matcher("_" + source);

		while (matcher.find()) {
			iteratorSource.add(matcher.group(1));
		}

		Iterator<String> parts = iteratorSource.iterator();

		PropertyPath result = null;
		PropertyPath current = null;

		while (parts.hasNext()) {
			if (result == null) {
				result = create(parts.next(), type);
				current = result;
			} else {
				current = create(parts.next(), current);
			}
		}

		return result;
	}

	/**
	 * Creates a new {@link PropertyPath} as subordinary of the given {@link PropertyPath}.
	 * 
	 * @param source
	 * @param base
	 * @return
	 */
	private static PropertyPath create(String source, PropertyPath base) {

		PropertyPath propertyPath = create(source, base.type);
		base.next = propertyPath;
		return propertyPath;
	}

	/**
	 * Factory method to create a new {@link PropertyPath} for the given {@link String} and owning type. It will inspect
	 * the given source for camel-case parts and traverse the {@link String} along its parts starting with the entire one
	 * and chewing off parts from the right side then. Whenever a valid property for the given class is found, the tail
	 * will be traversed for subordinary properties of the just found one and so on.
	 * 
	 * @param source
	 * @param type
	 * @return
	 */
	private static PropertyPath create(String source, TypeInformation<?> type) {

		return create(source, type, "");
	}

	/**
	 * Tries to look up a chain of {@link PropertyPath}s by trying the givne source first. If that fails it will split the
	 * source apart at camel case borders (starting from the right side) and try to look up a {@link PropertyPath} from
	 * the calculated head and recombined new tail and additional tail.
	 * 
	 * @param source
	 * @param type
	 * @param addTail
	 * @return
	 */
	private static PropertyPath create(String source, TypeInformation<?> type, String addTail) {

		IllegalArgumentException exception = null;

		try {
			return new PropertyPath(source, type, addTail);
		} catch (IllegalArgumentException e) {
			exception = e;
		}

		Pattern pattern = Pattern.compile("[A-Z]?[a-z]*$");
		Matcher matcher = pattern.matcher(source);

		if (matcher.find() && matcher.start() != 0) {

			int position = matcher.start();
			String head = source.substring(0, position);
			String tail = source.substring(position);

			return create(head, type, tail + addTail);
		}

		throw exception;
	}
}
