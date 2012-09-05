/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-persistence QueryUtils.java 2012-2-11 21:30:06 l.xue.nong$$
 */
package cn.com.rebirth.persistence.utils;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.springframework.util.Assert;

import cn.com.rebirth.commons.PageRequest.Order;
import cn.com.rebirth.commons.PageRequest.Sort;
import cn.com.rebirth.persistence.mapping.PropertyPath;

/**
 * The Class QueryUtils.
 *
 * @author l.xue.nong
 */
public abstract class QueryUtils {

	/** The Constant COUNT_QUERY_STRING. */
	public static final String COUNT_QUERY_STRING = "select count(%s) from %s x";

	/** The Constant EXISTS_QUERY_STRING. */
	public static final String EXISTS_QUERY_STRING = "select count(%s) from %s x where x.%s = :id";

	/** The Constant DELETE_ALL_QUERY_STRING. */
	public static final String DELETE_ALL_QUERY_STRING = "delete from %s x";

	/** The Constant READ_ALL_QUERY. */
	public static final String READ_ALL_QUERY = "select x from %s x";

	/** The Constant DEFAULT_ALIAS. */
	private static final String DEFAULT_ALIAS = "x";

	/** The Constant COUNT_REPLACEMENT. */
	private static final String COUNT_REPLACEMENT = "select count($3$5) $4$5$6";

	/** The Constant ALIAS_MATCH. */
	private static final Pattern ALIAS_MATCH;

	/** The Constant COUNT_MATCH. */
	private static final Pattern COUNT_MATCH;

	/** The Constant IDENTIFIER. */
	private static final String IDENTIFIER = "[\\p{Alnum}._$]+";

	/** The Constant IDENTIFIER_GROUP. */
	private static final String IDENTIFIER_GROUP = String.format("(%s)", IDENTIFIER);

	static {

		StringBuilder builder = new StringBuilder();
		builder.append("(?<=from)"); // from as starting delimiter
		builder.append("(?: )+"); // at least one space separating
		builder.append(IDENTIFIER_GROUP); // Entity name, can be qualified (any
		builder.append("(?: as)*"); // exclude possible "as" keyword
		builder.append("(?: )+"); // at least one space separating
		builder.append("(\\w*)"); // the actual alias

		ALIAS_MATCH = compile(builder.toString(), CASE_INSENSITIVE);

		builder = new StringBuilder();
		builder.append("(select\\s+((distinct )?.+?)\\s+)?(from\\s+");
		builder.append(IDENTIFIER);
		builder.append("(?:\\s+as)?\\s+)");
		builder.append(IDENTIFIER_GROUP);
		builder.append("(.*)");

		COUNT_MATCH = compile(builder.toString(), CASE_INSENSITIVE);
	}

	/**
	 * Private constructor to prevent instantiation.
	 */
	private QueryUtils() {

	}

	/**
	 * Returns the query string for the given class name.
	 *
	 * @param template the template
	 * @param entityName the entity name
	 * @return the query string
	 */
	public static String getQueryString(String template, String entityName) {

		Assert.hasText(entityName, "Entity name must not be null or empty!");

		return String.format(template, entityName);
	}

	/**
	 * Adds {@literal order by} clause to the JPQL query. Uses the {@link #DEFAULT_ALIAS} to bind the sorting property to.
	 *
	 * @param query the query
	 * @param sort the sort
	 * @return the string
	 */
	public static String applySorting(String query, Sort sort) {

		return applySorting(query, sort, DEFAULT_ALIAS);
	}

	/**
	 * Adds {@literal order by} clause to the JPQL query.
	 *
	 * @param query the query
	 * @param sort the sort
	 * @param alias the alias
	 * @return the string
	 */
	public static String applySorting(String query, Sort sort, String alias) {

		Assert.hasText(query);

		if (null == sort) {
			return query;
		}

		StringBuilder builder = new StringBuilder(query);
		builder.append(" order by");

		for (Order order : sort) {
			builder.append(String.format(" %s.%s %s,", alias, order.getProperty(), toJpaDirection(order)));
		}

		builder.deleteCharAt(builder.length() - 1);

		return builder.toString();
	}

	/**
	 * To jpa direction.
	 *
	 * @param order the order
	 * @return the string
	 */
	public static String toJpaDirection(Order order) {

		return order.getDirection().name().toLowerCase(Locale.US);
	}

	/**
	 * Resolves the alias for the entity to be retrieved from the given JPA query.
	 *
	 * @param query the query
	 * @return the string
	 */
	public static String detectAlias(String query) {

		Matcher matcher = ALIAS_MATCH.matcher(query);

		return matcher.find() ? matcher.group(2) : null;
	}

	/**
	 * Creates a where-clause referencing the given entities and appends it to the given query string. Binds the given
	 * entities to the query.
	 *
	 * @param <T> the generic type
	 * @param queryString the query string
	 * @param entities the entities
	 * @param entityManager the entity manager
	 * @return the query
	 */
	public static <T> Query applyAndBind(String queryString, Iterable<T> entities, EntityManager entityManager) {

		Assert.notNull(queryString);
		Assert.notNull(entities);
		Assert.notNull(entityManager);

		Iterator<T> iterator = entities.iterator();

		if (!iterator.hasNext()) {
			return entityManager.createQuery(queryString);
		}

		String alias = detectAlias(queryString);
		StringBuilder builder = new StringBuilder(queryString);
		builder.append(" where");

		int i = 0;

		while (iterator.hasNext()) {

			iterator.next();

			builder.append(String.format(" %s = ?%d", alias, ++i));

			if (iterator.hasNext()) {
				builder.append(" or");
			}
		}

		Query query = entityManager.createQuery(builder.toString());

		iterator = entities.iterator();
		i = 0;

		while (iterator.hasNext()) {
			query.setParameter(++i, iterator.next());
		}

		return query;
	}

	/**
	 * Creates a count projected query from the given orginal query.
	 *
	 * @param originalQuery must not be {@literal null} or empty
	 * @return the string
	 */
	public static String createCountQueryFor(String originalQuery) {

		Assert.hasText(originalQuery);

		Matcher matcher = COUNT_MATCH.matcher(originalQuery);
		return matcher.replaceFirst(COUNT_REPLACEMENT);
	}

	/**
	 * Returns whether the given {@link Query} contains named parameters.
	 *
	 * @param query the query
	 * @return true, if successful
	 */
	public static boolean hasNamedParameter(Query query) {

		for (Parameter<?> parameter : query.getParameters()) {
			if (parameter.getName() != null) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Turns the given {@link Sort} into {@link javax.persistence.criteria.Order}s.
	 *
	 * @param sort the {@link Sort} instance to be transformed into JPA {@link javax.persistence.criteria.Order}s.
	 * @param root must not be {@literal null}.
	 * @param cb must not be {@literal null}.
	 * @return the list
	 */
	public static List<javax.persistence.criteria.Order> toOrders(Sort sort, Root<?> root, CriteriaBuilder cb) {

		List<javax.persistence.criteria.Order> orders = new ArrayList<javax.persistence.criteria.Order>();

		if (sort == null) {
			return orders;
		}

		Assert.notNull(root);
		Assert.notNull(cb);

		for (Order order : sort) {
			orders.add(toJpaOrder(order, root, cb));
		}

		return orders;
	}

	/**
	 * Creates a criteria API {@link javax.persistence.criteria.Order} from the given {@link Order}.
	 *
	 * @param order the order to transform into a JPA {@link javax.persistence.criteria.Order}
	 * @param root the {@link Root} the {@link Order} expression is based on
	 * @param cb the {@link CriteriaBuilder} to build the {@link javax.persistence.criteria.Order} with
	 * @return the javax.persistence.criteria. order
	 */
	private static javax.persistence.criteria.Order toJpaOrder(Order order, Root<?> root, CriteriaBuilder cb) {

		Expression<?> expression = toExpressionRecursively(root,
				PropertyPath.from(order.getProperty(), root.getJavaType()));
		return order.isAscending() ? cb.asc(expression) : cb.desc(expression);
	}

	/**
	 * To expression recursively.
	 *
	 * @param <T> the generic type
	 * @param from the from
	 * @param property the property
	 * @return the expression
	 */
	@SuppressWarnings("unchecked")
	static <T> Expression<T> toExpressionRecursively(From<?, ?> from, PropertyPath property) {

		if (property.isCollection()) {
			Join<Object, Object> join = from.join(property.getSegment());
			return (Expression<T>) (property.hasNext() ? toExpressionRecursively((From<?, ?>) join, property.next())
					: join);
		} else {
			Path<Object> path = from.get(property.getSegment());
			return (Expression<T>) (property.hasNext() ? toExpressionRecursively(path, property.next()) : path);
		}
	}

	/**
	 * To expression recursively.
	 *
	 * @param path the path
	 * @param property the property
	 * @return the expression
	 */
	static Expression<Object> toExpressionRecursively(Path<Object> path, PropertyPath property) {

		Path<Object> result = path.get(property.getSegment());
		return property.hasNext() ? toExpressionRecursively(result, property.next()) : result;
	}
}
