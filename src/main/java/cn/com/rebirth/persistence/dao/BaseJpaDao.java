/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-persistence BaseDao.java 2012-2-10 13:31:24 l.xue.nong$$
 */
package cn.com.rebirth.persistence.dao;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.NotReadablePropertyException;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import cn.com.rebirth.commons.Page;
import cn.com.rebirth.commons.PageRequest;
import cn.com.rebirth.commons.PageRequest.Direction;
import cn.com.rebirth.commons.PageRequest.Order;
import cn.com.rebirth.commons.PageRequest.Sort;
import cn.com.rebirth.persistence.LockMetadataProvider;
import cn.com.rebirth.persistence.Specification;
import cn.com.rebirth.persistence.query.PersistenceProvider;
import cn.com.rebirth.persistence.utils.QueryUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class BaseDao.
 *
 * @author l.xue.nong
 */
@Repository
public class BaseJpaDao {

	/** The logger. */
	protected Logger logger = LoggerFactory.getLogger(getClass());

	/** The em. */
	protected EntityManager em;

	/** The provider. */
	private PersistenceProvider provider;

	/** The lock metadata provider. */
	private LockMetadataProvider lockMetadataProvider;

	/** The informations. */
	private Map<Class<?>, JpaEntityInformation> informations = new HashMap<Class<?>, BaseJpaDao.JpaEntityInformation>();

	/**
	 * Sets the lock metadata provider.
	 *
	 * @param lockMetadataProvider the new lock metadata provider
	 */
	@Autowired(required = false)
	public void setLockMetadataProvider(LockMetadataProvider lockMetadataProvider) {
		this.lockMetadataProvider = lockMetadataProvider;
	}

	/**
	 * Sets the em.
	 *
	 * @param em the new em
	 */
	@PersistenceContext
	public void setEm(EntityManager em) {
		this.em = em;
		this.provider = PersistenceProvider.fromEntityManager(em);
	}

	/**
	 * Gets the em.
	 *
	 * @return the em
	 */
	public EntityManager getEm() {
		return em;
	}

	/**
	 * Gets the delete all query string.
	 *
	 * @param entityClass the entity class
	 * @return the delete all query string
	 */
	private String getDeleteAllQueryString(Class<?> entityClass) {
		return QueryUtils.getQueryString(QueryUtils.DELETE_ALL_QUERY_STRING, getEntityName(entityClass));
	}

	/**
	 * Gets the entity name.
	 *
	 * @param entityClass the entity class
	 * @return the entity name
	 */
	private String getEntityName(Class<?> entityClass) {
		JpaEntityInformation entityInformation = getEntityInformation(entityClass);
		return entityInformation.getEntityName();
	}

	/**
	 * Gets the count query string.
	 *
	 * @param entityClass the entity class
	 * @return the count query string
	 */
	private String getCountQueryString(Class<?> entityClass) {

		String countQuery = String.format(QueryUtils.COUNT_QUERY_STRING, provider.getCountQueryPlaceholder(), "%s");
		return QueryUtils.getQueryString(countQuery, getEntityName(entityClass));
	}

	/**
	 * Delete.
	 *
	 * @param <T> the generic type
	 * @param <ID> the generic type
	 * @param entityClass the entity class
	 * @param id the id
	 */
	public <T, ID extends Serializable> void delete(Class<T> entityClass, ID id) {

		Validate.notNull(id, "The given id must not be null!");
		delete(findOne(entityClass, id));
	}

	/**
	 * Delete.
	 *
	 * @param <T> the generic type
	 * @param entity the entity
	 */
	public <T> void delete(T entity) {

		Validate.notNull(entity, "The entity must not be null!");
		em.remove(em.contains(entity) ? entity : em.merge(entity));
	}

	/**
	 * Delete.
	 *
	 * @param <T> the generic type
	 * @param entities the entities
	 */
	public <T> void delete(Iterable<? extends T> entities) {

		Validate.notNull(entities, "The given Iterable of entities not be null!");

		for (T entity : entities) {
			delete(entity);
		}
	}

	/**
	 * Delete in batch.
	 *
	 * @param <T> the generic type
	 * @param entities the entities
	 */
	public <T> void deleteInBatch(Iterable<T> entities) {

		Validate.notNull(entities, "The given Iterable of entities not be null!");
		Iterator<T> it = entities.iterator();
		if (!it.hasNext()) {
			return;
		}
		Class<?> domainClass = it.next().getClass();
		JpaEntityInformation entity = getEntityInformation(domainClass);
		QueryUtils.applyAndBind(QueryUtils.getQueryString(QueryUtils.DELETE_ALL_QUERY_STRING, entity.getEntityName()),
				entities, em).executeUpdate();
	}

	/**
	 * Delete all.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 */
	public <T> void deleteAll(Class<T> entityClass) {

		for (T element : findAll(entityClass)) {
			delete(element);
		}
	}

	/**
	 * Delete all in batch.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 */
	public <T> void deleteAllInBatch(Class<T> entityClass) {
		em.createQuery(getDeleteAllQueryString(entityClass)).executeUpdate();
	}

	/**
	 * Find one.
	 *
	 * @param <T> the generic type
	 * @param <ID> the generic type
	 * @param entityClass the entity class
	 * @param id the id
	 * @return the t
	 */
	public <T, ID extends Serializable> T findOne(Class<T> entityClass, ID id) {
		Validate.notNull(id, "The given id must not be null!");
		return em.find(entityClass, id);
	}

	/**
	 * Gets the entity information.
	 *
	 * @param entityClass the entity class
	 * @return the entity information
	 */
	private JpaEntityInformation getEntityInformation(Class<?> entityClass) {
		JpaEntityInformation entityInformation = this.informations.get(entityClass);
		if (entityInformation == null)
			entityInformation = new JpaEntityInformation(em, entityClass);
		this.informations.put(entityClass, entityInformation);
		return entityInformation;
	}

	/**
	 * Exists.
	 *
	 * @param <T> the generic type
	 * @param <ID> the generic type
	 * @param entityClass the entity class
	 * @param id the id
	 * @return true, if successful
	 */
	public <T, ID extends Serializable> boolean exists(Class<T> entityClass, ID id) {

		Validate.notNull(id, "The given id must not be null!");
		JpaEntityInformation entityInformation = getEntityInformation(entityClass);
		if (entityInformation.getIdAttribute() != null) {

			String placeholder = provider.getCountQueryPlaceholder();
			String entityName = entityInformation.getEntityName();
			String idAttributeName = entityInformation.getIdAttribute().getName();
			String existsQuery = String
					.format(QueryUtils.EXISTS_QUERY_STRING, placeholder, entityName, idAttributeName);

			TypedQuery<Long> query = em.createQuery(existsQuery, Long.class);
			query.setParameter("id", id);

			return query.getSingleResult() == 1;
		} else {
			return findOne(entityClass, id) != null;
		}
	}

	/**
	 * Find all.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 * @return the list
	 */
	public <T> List<T> findAll(Class<T> entityClass) {
		return getQuery(entityClass, null, (Sort) null).getResultList();
	}

	/**
	 * Find all.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 * @param orderByProperty the order by property
	 * @param isAsc the is asc
	 * @return the list
	 */
	public <T> List<T> findAll(Class<T> entityClass, String orderByProperty, boolean isAsc) {
		return getQuery(entityClass, null, new Sort(new Order(isAsc ? Direction.ASC : Direction.DESC, orderByProperty)))
				.getResultList();
	}

	/**
	 * Find by.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 * @param propertyName the property name
	 * @param value the value
	 * @return the list
	 */
	public <T> List<T> findBy(final Class<T> entityClass, final String propertyName, final Object value) {
		return getQuery(entityClass, new Specification<T>() {

			@Override
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Path<?> path = root.get(propertyName);
				return cb.equal(path, value);
			}
		}, (Sort) null).getResultList();
	}

	/**
	 * Find unique by.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 * @param propertyName the property name
	 * @param value the value
	 * @return the t
	 */
	public <T> T findUniqueBy(final Class<T> entityClass, final String propertyName, final Object value) {
		return getQuery(entityClass, new Specification<T>() {

			@Override
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Path<?> path = root.get(propertyName);
				return cb.equal(path, value);
			}
		}, (Sort) null).getSingleResult();
	}

	/**
	 * Find all.
	 *
	 * @param <T> the generic type
	 * @param <ID> the generic type
	 * @param entityClass the entity class
	 * @param ids the ids
	 * @return the list
	 */
	public <T, ID extends Serializable> List<T> findAll(Class<T> entityClass, Iterable<ID> ids) {
		final JpaEntityInformation entityInformation = getEntityInformation(entityClass);
		return getQuery(entityClass, new Specification<T>() {
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Path<?> path = root.get(entityInformation.getIdAttribute());
				return path.in(cb.parameter(List.class, "ids"));
			}
		}, (Sort) null).setParameter("ids", ids).getResultList();
	}

	/**
	 * Find all.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 * @param sort the sort
	 * @return the list
	 */
	public <T> List<T> findAll(Class<T> entityClass, Sort sort) {
		return getQuery(entityClass, null, sort).getResultList();
	}

	/**
	 * Find all.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 * @param pageRequest the page request
	 * @return the page
	 */
	public <T> Page<T> findAll(Class<T> entityClass, PageRequest pageRequest) {

		if (null == pageRequest) {
			pageRequest = new PageRequest();
		}

		return findAll(entityClass, null, pageRequest);
	}

	/**
	 * Find one.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 * @param spec the spec
	 * @return the t
	 */
	public <T> T findOne(Class<T> entityClass, Specification<T> spec) {

		try {
			return getQuery(entityClass, spec, (Sort) null).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	/**
	 * Find all.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 * @param spec the spec
	 * @return the list
	 */
	public <T> List<T> findAll(Class<T> entityClass, Specification<T> spec) {
		return getQuery(entityClass, spec, (Sort) null).getResultList();
	}

	/**
	 * Find all.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 * @param spec the spec
	 * @param pageRequest the page request
	 * @return the page
	 */
	public <T> Page<T> findAll(Class<T> entityClass, Specification<T> spec, PageRequest pageRequest) {
		pageRequest = pageRequest == null ? new PageRequest() : pageRequest;
		TypedQuery<T> query = getQuery(entityClass, spec, pageRequest);
		return readPage(entityClass, query, pageRequest, spec);
	}

	/**
	 * Find all.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 * @param spec the spec
	 * @param sort the sort
	 * @return the list
	 */
	public <T> List<T> findAll(Class<T> entityClass, Specification<T> spec, Sort sort) {

		return getQuery(entityClass, spec, sort).getResultList();
	}

	/**
	 * Count.
	 *
	 * @param entityClass the entity class
	 * @return the long
	 */
	public long count(Class<?> entityClass) {
		return em.createQuery(getCountQueryString(entityClass), Long.class).getSingleResult();
	}

	/**
	 * Count.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 * @param spec the spec
	 * @return the long
	 */
	public <T> long count(Class<T> entityClass, Specification<T> spec) {

		return getCountQuery(entityClass, spec).getSingleResult();
	}

	/**
	 * Save.
	 *
	 * @param <T> the generic type
	 * @param entity the entity
	 * @return the t
	 */
	public <T> T save(T entity) {
		JpaEntityInformation information = getEntityInformation(entity.getClass());
		if (information.isNew(entity)) {
			em.persist(entity);
			return entity;
		} else {
			return em.merge(entity);
		}
	}

	/**
	 * Save and flush.
	 *
	 * @param <T> the generic type
	 * @param entity the entity
	 * @return the t
	 */
	public <T> T saveAndFlush(T entity) {

		T result = save(entity);
		flush();

		return result;
	}

	/**
	 * Save.
	 *
	 * @param <T> the generic type
	 * @param entities the entities
	 * @return the list
	 */
	public <T> List<T> save(Iterable<? extends T> entities) {

		List<T> result = new ArrayList<T>();

		if (entities == null) {
			return result;
		}

		for (T entity : entities) {
			result.add(save(entity));
		}

		return result;
	}

	/**
	 * Flush.
	 */
	public void flush() {
		em.flush();
	}

	/**
	 * Read page.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 * @param query the query
	 * @param pageRequest the page request
	 * @param spec the spec
	 * @return the page
	 */
	private <T> Page<T> readPage(Class<T> entityClass, TypedQuery<T> query, PageRequest pageRequest,
			Specification<T> spec) {
		Page<T> page = new Page<T>();
		query.setFirstResult(pageRequest.getOffset());
		query.setMaxResults(pageRequest.getPageSize());
		if (pageRequest.isCountTotal()) {
			Long total = getCountQuery(entityClass, spec).getSingleResult();
			page.setTotalItems(total);
		}
		List<T> content = pageRequest.isCountTotal() ? (page.getTotalItems() > pageRequest.getOffset() ? query
				.getResultList() : Collections.<T> emptyList()) : query.getResultList();
		page.setResult(content);
		return page;
	}

	/**
	 * Gets the query.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 * @param spec the spec
	 * @param pageRequest the page request
	 * @return the query
	 */
	private <T> TypedQuery<T> getQuery(Class<T> entityClass, Specification<T> spec, PageRequest pageRequest) {
		Sort sort = null;
		if (pageRequest.getSort() != null) {
			sort = pageRequest.getSort();
		}
		return getQuery(entityClass, spec, sort);
	}

	/**
	 * Gets the query.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 * @param spec the spec
	 * @param sort the sort
	 * @return the query
	 */
	private <T> TypedQuery<T> getQuery(Class<T> entityClass, Specification<T> spec, Sort sort) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> query = builder.createQuery(entityClass);

		Root<T> root = applySpecificationToCriteria(entityClass, spec, query);
		query.select(root);

		if (sort != null) {
			query.orderBy(QueryUtils.toOrders(sort, root, builder));
		}
		return applyLockMode(em.createQuery(query));
	}

	/**
	 * Gets the count query.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 * @param spec the spec
	 * @return the count query
	 */
	private <T> TypedQuery<Long> getCountQuery(Class<T> entityClass, Specification<T> spec) {

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<T> root = applySpecificationToCriteria(entityClass, spec, query);
		query.select(builder.count(root));

		return em.createQuery(query);
	}

	/**
	 * Apply specification to criteria.
	 *
	 * @param <S> the generic type
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 * @param spec the spec
	 * @param query the query
	 * @return the root
	 */
	private <S, T> Root<T> applySpecificationToCriteria(Class<T> entityClass, Specification<T> spec,
			CriteriaQuery<S> query) {

		Validate.notNull(query);
		Root<T> root = query.from(entityClass);

		if (spec == null) {
			return root;
		}

		CriteriaBuilder builder = em.getCriteriaBuilder();
		Predicate predicate = spec.toPredicate(root, query, builder);

		if (predicate != null) {
			query.where(predicate);
		}

		return root;
	}

	/**
	 * Apply lock mode.
	 *
	 * @param <T> the generic type
	 * @param query the query
	 * @return the typed query
	 */
	private <T> TypedQuery<T> applyLockMode(TypedQuery<T> query) {

		LockModeType type = lockMetadataProvider == null ? null : lockMetadataProvider.getLockModeType();
		return type == null ? query : query.setLockMode(type);
	}

	/**
	 * The Class JpaEntityInformation.
	 *
	 * @author l.xue.nong
	 */
	class JpaEntityInformation {

		/** The java type. */
		private Class<?> javaType;

		/** The entity name. */
		private String entityName;

		/** The id attribute. */
		private SingularAttribute<? super Object, ?> idAttribute;

		/** The type. */
		private IdentifiableType<?> type;

		/** The attributes. */
		private Set<SingularAttribute<?, ?>> attributes;

		/**
		 * Instantiates a new jpa entity information.
		 *
		 * @param em the em
		 * @param entityClass the entity class
		 */
		@SuppressWarnings("unchecked")
		public JpaEntityInformation(EntityManager em, Class<?> entityClass) {
			super();
			Metamodel metamodel = em.getMetamodel();
			ManagedType<?> type = metamodel.managedType(entityClass);
			if (type == null) {
				throw new IllegalArgumentException("The given domain class can not be found in the given Metamodel!");
			}

			if (!(type instanceof IdentifiableType)) {
				throw new IllegalArgumentException("The given domain class does not contain an id attribute!");
			}
			this.javaType = entityClass;
			Entity entity = entityClass.getAnnotation(Entity.class);
			boolean hasName = null != entity && StringUtils.hasText(entity.name());

			this.entityName = hasName ? entity.name() : entityClass.getSimpleName();
			this.type = (IdentifiableType<?>) type;
			this.attributes = (Set<SingularAttribute<?, ?>>) (this.type.hasSingleIdAttribute() ? Collections
					.singleton(this.type.getId(this.type.getIdType().getJavaType())) : this.type.getIdClassAttributes());
			this.idAttribute = (SingularAttribute<? super Object, ?>) this.attributes.iterator().next();
		}

		/**
		 * The Class DirectFieldAccessFallbackBeanWrapper.
		 *
		 * @author l.xue.nong
		 */
		private class DirectFieldAccessFallbackBeanWrapper extends BeanWrapperImpl {

			/**
			 * Instantiates a new direct field access fallback bean wrapper.
			 *
			 * @param entity the entity
			 */
			public DirectFieldAccessFallbackBeanWrapper(Object entity) {
				super(entity);
			}

			/**
			 * Instantiates a new direct field access fallback bean wrapper.
			 *
			 * @param type the type
			 */
			public DirectFieldAccessFallbackBeanWrapper(Class<?> type) {
				super(type);
			}

			/* 
			 * (non-Javadoc)
			 * @see org.springframework.beans.BeanWrapperImpl#getPropertyValue(java.lang.String)
			 */
			@Override
			public Object getPropertyValue(String propertyName) throws BeansException {
				try {
					return super.getPropertyValue(propertyName);
				} catch (NotReadablePropertyException e) {
					Field field = ReflectionUtils.findField(getWrappedClass(), propertyName);
					ReflectionUtils.makeAccessible(field);
					return ReflectionUtils.getField(field, getWrappedInstance());
				}
			}

			/* 
			 * (non-Javadoc)
			 * @see org.springframework.beans.BeanWrapperImpl#setPropertyValue(java.lang.String, java.lang.Object)
			 */
			@Override
			public void setPropertyValue(String propertyName, Object value) throws BeansException {
				try {
					super.setPropertyValue(propertyName, value);
				} catch (NotWritablePropertyException e) {
					Field field = ReflectionUtils.findField(getWrappedClass(), propertyName);
					ReflectionUtils.makeAccessible(field);
					ReflectionUtils.setField(field, getWrappedInstance(), value);
				}
			}
		}

		/**
		 * Checks if is new.
		 *
		 * @param <T> the generic type
		 * @param entity the entity
		 * @return true, if is new
		 */
		public <T> boolean isNew(T entity) {
			BeanWrapper entityWrapper = new DirectFieldAccessFallbackBeanWrapper(entity);
			if (this.attributes.size() == 1) {
				return entityWrapper.getPropertyValue(attributes.iterator().next().getName()) == null;
			}
			BeanWrapper idWrapper = new DirectFieldAccessFallbackBeanWrapper(this.type.getIdType().getJavaType());
			boolean partialIdValueFound = false;
			for (SingularAttribute<?, ?> attribute : this.attributes) {
				Object propertyValue = entityWrapper.getPropertyValue(attribute.getName());

				if (propertyValue != null) {
					partialIdValueFound = true;
				}

				idWrapper.setPropertyValue(attribute.getName(), propertyValue);
			}
			return (partialIdValueFound ? idWrapper.getWrappedInstance() : null) == null;
		}

		/**
		 * Gets the java type.
		 *
		 * @return the java type
		 */
		public Class<?> getJavaType() {
			return javaType;
		}

		/**
		 * Sets the java type.
		 *
		 * @param javaType the new java type
		 */
		public void setJavaType(Class<?> javaType) {
			this.javaType = javaType;
		}

		/**
		 * Gets the entity name.
		 *
		 * @return the entity name
		 */
		public String getEntityName() {
			return entityName;
		}

		/**
		 * Sets the entity name.
		 *
		 * @param entityName the new entity name
		 */
		public void setEntityName(String entityName) {
			this.entityName = entityName;
		}

		/**
		 * Gets the id attribute.
		 *
		 * @param <Y> the generic type
		 * @return the id attribute
		 */
		public <Y> SingularAttribute<? super Y, ?> getIdAttribute() {
			return idAttribute;
		}

		/**
		 * Sets the id attribute.
		 *
		 * @param idAttribute the id attribute
		 */
		public void setIdAttribute(SingularAttribute<? super Object, ?> idAttribute) {
			this.idAttribute = idAttribute;
		}

		/**
		 * Gets the type.
		 *
		 * @return the type
		 */
		public IdentifiableType<?> getType() {
			return type;
		}

		/**
		 * Sets the type.
		 *
		 * @param type the new type
		 */
		public void setType(IdentifiableType<?> type) {
			this.type = type;
		}

		/**
		 * Gets the attributes.
		 *
		 * @return the attributes
		 */
		public Set<SingularAttribute<?, ?>> getAttributes() {
			return attributes;
		}

		/**
		 * Sets the attributes.
		 *
		 * @param attributes the attributes
		 */
		public void setAttributes(Set<SingularAttribute<?, ?>> attributes) {
			this.attributes = attributes;
		}

	}
}
