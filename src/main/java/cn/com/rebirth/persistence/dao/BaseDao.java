/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-persistence BaseDao.java 2012-2-10 15:42:04 l.xue.nong$$
 */
package cn.com.rebirth.persistence.dao;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Query;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import cn.com.rebirth.commons.Page;
import cn.com.rebirth.commons.PageRequest;
import cn.com.rebirth.commons.PageRequest.Order;

/**
 * The Class BaseDao.
 *
 * @author l.xue.nong
 */
@Repository
public class BaseDao extends BaseJpaDao {

	/** The Constant DEFAULT_ALIAS. */
	public static final String DEFAULT_ALIAS = "x";

	/**
	 * Find page.
	 *
	 * @param <T> the generic type
	 * @param pageRequest the page request
	 * @param qlString the ql string
	 * @param values the values
	 * @return the page
	 */
	@SuppressWarnings("unchecked")
	public <T> Page<T> findPage(final PageRequest pageRequest, String qlString, final Object... values) {
		Validate.notNull(pageRequest, "pageRequest不能为空");
		Page<T> page = new Page<T>(pageRequest);
		if (pageRequest.isCountTotal()) {
			long totalCount = countqlResult(qlString, values);
			page.setTotalItems(totalCount);
		}
		if (pageRequest.getSort() != null) {
			qlString = setOrderParameterToql(removeOrders(qlString), pageRequest);
		}
		Query q = createQuery(qlString, values);

		setPageParameterToQuery(q, pageRequest);

		List<T> result = q.getResultList();
		page.setResult(result);
		return page;
	}

	/**
	 * Find page.
	 *
	 * @param <T> the generic type
	 * @param pageRequest the page request
	 * @param hql the hql
	 * @param values the values
	 * @return the page
	 */
	@SuppressWarnings("unchecked")
	public <T> Page<T> findPage(final PageRequest pageRequest, String hql, final Map<String, ?> values) {
		Validate.notNull(pageRequest, "page不能为空");

		Page<T> page = new Page<T>(pageRequest);

		if (pageRequest.isCountTotal()) {
			long totalCount = countqlResult(hql, values);
			page.setTotalItems(totalCount);
		}

		if (pageRequest.getSort() != null) {
			hql = setOrderParameterToql(removeOrders(hql), pageRequest);
		}

		Query q = createQuery(hql, values);
		setPageParameterToQuery(q, pageRequest);

		List<T> result = q.getResultList();
		page.setResult(result);
		return page;
	}

	/**
	 * Sets the page parameter to query.
	 *
	 * @param q the q
	 * @param pageRequest the page request
	 * @return the query
	 */
	protected Query setPageParameterToQuery(Query q, PageRequest pageRequest) {
		q.setFirstResult(pageRequest.getOffset());
		q.setMaxResults(pageRequest.getPageSize());
		return q;
	}

	/**
	 * Sets the order parameter toql.
	 *
	 * @param qlString the ql string
	 * @param pageRequest the page request
	 * @return the string
	 */
	private String setOrderParameterToql(String qlString, PageRequest pageRequest) {
		StringBuilder builder = new StringBuilder(qlString);
		builder.append(" order by");

		for (Order order : pageRequest.getSort()) {
			builder.append(String.format(" %s.%s %s,", DEFAULT_ALIAS, order.getProperty(), order.getDirection().name()));
		}

		builder.deleteCharAt(builder.length() - 1);

		return builder.toString();
	}

	/**
	 * Countql result.
	 *
	 * @param qlString the ql string
	 * @param values the values
	 * @return the long
	 */
	private long countqlResult(String qlString, Object... values) {
		String countql = prepareCountql(qlString);
		try {
			Long count = findUnique(countql, values);
			return count;
		} catch (Exception e) {
			throw new RuntimeException("hql can't be auto count, hql is:" + countql, e);
		}
	}

	/**
	 * Find.
	 *
	 * @param <X> the generic type
	 * @param queryString the query string
	 * @param values the values
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	public <X> List<X> find(final String queryString, final Object... values) {
		return createQuery(queryString, values).getResultList();
	}

	/**
	 * Find.
	 *
	 * @param <X> the generic type
	 * @param queryString the query string
	 * @param values the values
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	public <X> List<X> find(final String queryString, final Map<String, ?> values) {
		return createQuery(queryString, values).getResultList();
	}

	/**
	 * Find unique.
	 *
	 * @param <X> the generic type
	 * @param queryString the query string
	 * @param values the values
	 * @return the x
	 */
	@SuppressWarnings("unchecked")
	public <X> X findUnique(final String queryString, final Object... values) {
		return (X) createQuery(queryString, values).getSingleResult();
	}

	/**
	 * Find unique.
	 *
	 * @param <X> the generic type
	 * @param queryString the query string
	 * @param values the values
	 * @return the x
	 */
	@SuppressWarnings("unchecked")
	public <X> X findUnique(final String queryString, final Map<String, ?> values) {
		return (X) createQuery(queryString, values).getSingleResult();
	}

	/**
	 * Batch execute.
	 *
	 * @param hql the hql
	 * @param values the values
	 * @return the int
	 */
	public int batchExecute(final String hql, final Object... values) {
		return createQuery(hql, values).executeUpdate();
	}

	/**
	 * Batch execute.
	 *
	 * @param hql the hql
	 * @param values the values
	 * @return the int
	 */
	public int batchExecute(final String hql, final Map<String, ?> values) {
		return createQuery(hql, values).executeUpdate();
	}

	/**
	 * Checks if is property unique.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 * @param propertyName the property name
	 * @param newValue the new value
	 * @param oldValue the old value
	 * @return true, if is property unique
	 */
	public <T> boolean isPropertyUnique(final Class<T> entityClass, final String propertyName, final Object newValue,
			final Object oldValue) {
		if (newValue == null || newValue.equals(oldValue)) {
			return true;
		}
		Object object = findUniqueBy(entityClass, propertyName, newValue);
		return (object == null);
	}

	/**
	 * Creates the query.
	 *
	 * @param queryString the query string
	 * @param values the values
	 * @return the query
	 */
	private Query createQuery(final String queryString, final Object... values) {
		Assert.hasText(queryString, "queryString不能为空");
		Query query = this.em.createQuery(queryString);
		if (values != null) {
			for (int i = 1; i <= values.length; i++) {
				query.setParameter(i, values[i - 1]);
			}
		}
		return query;
	}

	/**
	 * Creates the query.
	 *
	 * @param queryString the query string
	 * @param values the values
	 * @return the query
	 */
	private Query createQuery(final String queryString, final Map<String, ?> values) {
		Assert.hasText(queryString, "queryString不能为空");
		Query query = em.createQuery(queryString);
		if (values != null) {
			for (Map.Entry<String, ?> entry : values.entrySet()) {
				query.setParameter(entry.getKey(), entry.getValue());
			}
		}
		return query;
	}

	/**
	 * Prepare countql.
	 *
	 * @param qlString the ql string
	 * @return the string
	 */
	private String prepareCountql(String qlString) {
		String countHql = "select count (*) " + removeSelect(removeOrders(qlString));
		return countHql;
	}

	/**
	 * Removes the select.
	 *
	 * @param ql the ql
	 * @return the string
	 */
	private static String removeSelect(String ql) {
		int beginPos = ql.toLowerCase().indexOf("from");
		return ql.substring(beginPos);
	}

	/**
	 * Removes the orders.
	 *
	 * @param ql the hql
	 * @return the string
	 */
	private static String removeOrders(String ql) {
		Pattern p = Pattern.compile("order\\s*by[\\w|\\W|\\s|\\S]*", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(ql);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, "");
		}
		m.appendTail(sb);
		return sb.toString();
	}
}
