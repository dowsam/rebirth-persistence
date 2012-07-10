/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-persistence PersistenceProvider.java 2012-2-12 10:48:46 l.xue.nong$$
 */
package cn.com.summall.persistence.query;


import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.ejb.HibernateQuery;



/**
 * The Enum PersistenceProvider.
 *
 * @author l.xue.nong
 */
public enum PersistenceProvider implements QueryExtractor {
	/**
	 * Hibernate persistence provider.
	 */
	HIBERNATE("org.hibernate.ejb.HibernateEntityManager") {
		public String extractQueryString(Query query) {

			return ((HibernateQuery) query).getHibernateQuery().getQueryString();
		}

		/**
		 * Return custom placeholder ({@code *}) as Hibernate does create invalid queries for count queries for objects with
		 * compound keys.
		 * 
		 * @see HHH-4044
		 * @see HHH-3096
		 */
		@Override
		public String getCountQueryPlaceholder() {

			return "*";
		}
	},



	/**
	 * Unknown special provider. Use standard JPA.
	 */
	GENERIC_JPA("javax.persistence.EntityManager") {

		public String extractQueryString(Query query) {

			return null;
		}

		@Override
		public boolean canExtractQuery() {

			return false;
		}
	};

	/** The entity manager class name. */
	private String entityManagerClassName;

	/**
	 * Creates a new {@link PersistenceProvider}.
	 * 
	 * @param entityManagerClassName the name of the provider specific {@link EntityManager} implementation
	 */
	private PersistenceProvider(String entityManagerClassName) {

		this.entityManagerClassName = entityManagerClassName;
	}

	/**
	 * Determines the {@link PersistenceProvider} from the given {@link EntityManager}. If no special one can be
	 * determined {@value #GENERIC_JPA} will be returned.
	 *
	 * @param em the em
	 * @return the persistence provider
	 */
	public static PersistenceProvider fromEntityManager(EntityManager em) {

		for (PersistenceProvider provider : values()) {
			if (isEntityManagerOfType(em, provider.entityManagerClassName)) {
				return provider;
			}
		}

		return GENERIC_JPA;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.data.jpa.repository.query.QueryExtractor#canExtractQuery
	 * ()
	 */
	public boolean canExtractQuery() {

		return true;
	}

	/**
	 * Returns the placeholder to be used for simple count queries. Default implementation returns {@code *}.
	 *
	 * @return the count query placeholder
	 */
	public String getCountQueryPlaceholder() {

		return "x";
	}
	
	/**
	 * Checks if is entity manager of type.
	 *
	 * @param em the em
	 * @param type the type
	 * @return true, if is entity manager of type
	 */
	@SuppressWarnings("unchecked")
	public static boolean isEntityManagerOfType(EntityManager em, String type) {

		try {

			Class<? extends EntityManager> emType = (Class<? extends EntityManager>) Class.forName(type);

			emType.cast(em);

			return true;

		} catch (Exception e) {
			return false;
		}
	}
}
