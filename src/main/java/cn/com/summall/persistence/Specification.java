/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-persistence Specification.java 2012-2-11 21:33:06 l.xue.nong$$
 */
package cn.com.summall.persistence;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * The Interface Specification.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public interface Specification<T> {

	/**
	 * To predicate.
	 *
	 * @param root the root
	 * @param query the query
	 * @param cb the cb
	 * @return the predicate
	 */
	Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);
}
