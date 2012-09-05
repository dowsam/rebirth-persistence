/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-persistence Specifications.java 2012-2-12 13:09:21 l.xue.nong$$
 */
package cn.com.rebirth.persistence;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * The Class Specifications.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public class Specifications<T> implements Specification<T> {

	/** The spec. */
	private final Specification<T> spec;

	/**
	 * Creates a new {@link Specifications} wrapper for the given {@link Specification}.
	 *
	 * @param spec the spec
	 */
	private Specifications(Specification<T> spec) {

		this.spec = spec;
	}

	/**
	 * Simple static factory method to add some syntactic sugar around a {@link Specification}.
	 *
	 * @param <T> the generic type
	 * @param spec the spec
	 * @return the specifications
	 */
	public static <T> Specifications<T> where(Specification<T> spec) {

		return new Specifications<T>(spec);
	}

	/**
	 * ANDs the given {@link Specification} to the current one.
	 *
	 * @param other the other
	 * @return the specifications
	 */
	public Specifications<T> and(final Specification<T> other) {

		return new Specifications<T>(new Specification<T>() {

			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

				return builder.and(spec.toPredicate(root, query, builder), other.toPredicate(root, query, builder));
			}
		});
	}

	/**
	 * ORs the given specification to the current one.
	 *
	 * @param other the other
	 * @return the specifications
	 */
	public Specifications<T> or(final Specification<T> other) {

		return new Specifications<T>(new Specification<T>() {

			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

				return builder.or(spec.toPredicate(root, query, builder), other.toPredicate(root, query, builder));
			}
		});
	}

	/**
	 * Negates the given {@link Specification}.
	 *
	 * @param <T> the generic type
	 * @param spec the spec
	 * @return the specifications
	 */
	public static <T> Specifications<T> not(final Specification<T> spec) {

		return new Specifications<T>(spec) {

			@Override
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

				return builder.not(spec.toPredicate(root, query, builder));
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.jpa.domain.Specification#toPredicate(javax.
	 * persistence.criteria.Root, javax.persistence.criteria.CriteriaQuery,
	 * javax.persistence.criteria.CriteriaBuilder)
	 */
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

		return spec.toPredicate(root, query, builder);
	}
}
