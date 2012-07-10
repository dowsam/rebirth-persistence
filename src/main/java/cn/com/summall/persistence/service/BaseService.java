/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-persistence BaseService.java 2012-7-10 16:04:55 l.xue.nong$$
 */
package cn.com.summall.persistence.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.com.rebirth.commons.Page;
import cn.com.rebirth.commons.PageRequest;
import cn.com.summall.persistence.Specification;
import cn.com.summall.persistence.dao.BaseDao;

/**
 * The Class BaseService.
 *
 * @author l.xue.nong
 */
@Service
@Transactional
public class BaseService {

	/** The logger. */
	protected Logger logger = LoggerFactory.getLogger(getClass());

	/** The base dao. */
	protected BaseDao baseDao;

	/**
	 * Gets the base dao.
	 *
	 * @return the base dao
	 */
	public BaseDao getBaseDao() {
		return baseDao;
	}

	/**
	 * Sets the base dao.
	 *
	 * @param baseDao the new base dao
	 */
	@Autowired(required = false)
	public void setBaseDao(BaseDao baseDao) {
		this.baseDao = baseDao;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#save(T)
	 */
	/**
	 * Save.
	 *
	 * @param <T> the generic type
	 * @param entity the entity
	 * @return the t
	 */
	public <T> T save(final T entity) {
		return baseDao.save(entity);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#saveAndFlush(T)
	 */
	/**
	 * Save and flush.
	 *
	 * @param <T> the generic type
	 * @param entity the entity
	 * @return the t
	 */
	public <T> T saveAndFlush(T entity) {
		return baseDao.saveAndFlush(entity);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#save(java.lang.Iterable)
	 */
	/**
	 * Save.
	 *
	 * @param <T> the generic type
	 * @param entities the entities
	 * @return the list
	 */
	public <T> List<T> save(Iterable<? extends T> entities) {
		return baseDao.save(entities);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#flush()
	 */
	/**
	 * Flush.
	 */
	@Transactional(readOnly = true)
	public void flush() {
		baseDao.flush();
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#delete(T)
	 */
	/**
	 * Delete.
	 *
	 * @param <T> the generic type
	 * @param entity the entity
	 */
	public <T> void delete(final T entity) {
		baseDao.delete(entity);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#delete(java.lang.Class, PK)
	 */

	/**
	 * Delete.
	 *
	 * @param <T> the generic type
	 * @param <PK> the generic type
	 * @param entityClass the entity class
	 * @param id the id
	 */
	public <T, PK extends Serializable> void delete(final Class<T> entityClass, final PK id) {
		baseDao.delete(entityClass, id);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#delete(java.lang.Iterable)
	 */

	/**
	 * Delete.
	 *
	 * @param <T> the generic type
	 * @param entities the entities
	 */
	public <T> void delete(Iterable<? extends T> entities) {
		baseDao.delete(entities);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#deleteInBatch(java.lang.Iterable)
	 */

	/**
	 * Delete in batch.
	 *
	 * @param <T> the generic type
	 * @param entities the entities
	 */
	public <T> void deleteInBatch(Iterable<T> entities) {
		baseDao.deleteInBatch(entities);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#deleteAll(java.lang.Class)
	 */

	/**
	 * Delete all.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 */
	public <T> void deleteAll(Class<T> entityClass) {
		baseDao.deleteAll(entityClass);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#deleteAllInBatch(java.lang.Class)
	 */

	/**
	 * Delete all in batch.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 */
	public <T> void deleteAllInBatch(Class<T> entityClass) {
		baseDao.deleteAllInBatch(entityClass);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#exists(java.lang.Class, ID)
	 */

	/**
	 * Exists.
	 *
	 * @param <T> the generic type
	 * @param <ID> the generic type
	 * @param entityClass the entity class
	 * @param id the id
	 * @return true, if successful
	 */
	@Transactional(readOnly = true)
	public <T, ID extends Serializable> boolean exists(Class<T> entityClass, ID id) {
		return baseDao.exists(entityClass, id);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#count(java.lang.Class)
	 */

	/**
	 * Count.
	 *
	 * @param entityClass the entity class
	 * @return the long
	 */
	@Transactional(readOnly = true)
	public long count(Class<?> entityClass) {
		return baseDao.count(entityClass);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#count(java.lang.Class, cn.com.summall.persistence.Specification)
	 */

	/**
	 * Count.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 * @param spec the spec
	 * @return the long
	 */
	@Transactional(readOnly = true)
	public <T> long count(Class<T> entityClass, Specification<T> spec) {
		return baseDao.count(entityClass, spec);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#get(java.lang.Class, PK)
	 */

	/**
	 * Gets the.
	 *
	 * @param <T> the generic type
	 * @param <PK> the generic type
	 * @param entityClass the entity class
	 * @param id the id
	 * @return the t
	 */
	@Transactional(readOnly = true)
	public <T, PK extends Serializable> T get(final Class<T> entityClass, final PK id) {
		return baseDao.findOne(entityClass, id);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#getAll(java.lang.Class)
	 */

	/**
	 * Gets the all.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 * @return the all
	 */
	@Transactional(readOnly = true)
	public <T> List<T> getAll(Class<T> entityClass) {
		return baseDao.findAll(entityClass);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#getAll(java.lang.Class, java.lang.String, boolean)
	 */

	/**
	 * Gets the all.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 * @param orderByProperty the order by property
	 * @param isAsc the is asc
	 * @return the all
	 */
	@Transactional(readOnly = true)
	public <T> List<T> getAll(Class<T> entityClass, String orderByProperty, boolean isAsc) {
		return baseDao.findAll(entityClass, orderByProperty, isAsc);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#findBy(java.lang.Class, java.lang.String, java.lang.Object)
	 */

	/**
	 * Find by.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 * @param propertyName the property name
	 * @param value the value
	 * @return the list
	 */
	@Transactional(readOnly = true)
	public <T> List<T> findBy(final Class<T> entityClass, final String propertyName, final Object value) {
		return baseDao.findBy(entityClass, propertyName, value);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#findUniqueBy(java.lang.Class, java.lang.String, java.lang.Object)
	 */

	/**
	 * Find unique by.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 * @param propertyName the property name
	 * @param value the value
	 * @return the t
	 */
	@Transactional(readOnly = true)
	public <T> T findUniqueBy(final Class<T> entityClass, final String propertyName, final Object value) {
		return baseDao.findUniqueBy(entityClass, propertyName, value);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#findByIds(java.lang.Class, java.util.List)
	 */

	/**
	 * Find by ids.
	 *
	 * @param <T> the generic type
	 * @param <PK> the generic type
	 * @param entityClass the entity class
	 * @param ids the ids
	 * @return the list
	 */
	@Transactional(readOnly = true)
	public <T, PK extends Serializable> List<T> findByIds(Class<T> entityClass, List<PK> ids) {
		return baseDao.findAll(entityClass, ids);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#find(java.lang.String, java.lang.Object)
	 */

	/**
	 * Find.
	 *
	 * @param <X> the generic type
	 * @param ql the ql
	 * @param values the values
	 * @return the list
	 */
	@Transactional(readOnly = true)
	public <X> List<X> find(final String ql, final Object... values) {
		return baseDao.find(ql, values);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#find(java.lang.String, java.util.Map)
	 */

	/**
	 * Find.
	 *
	 * @param <X> the generic type
	 * @param ql the ql
	 * @param values the values
	 * @return the list
	 */
	@Transactional(readOnly = true)
	public <X> List<X> find(final String ql, final Map<String, ?> values) {
		return baseDao.find(ql, values);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#findUnique(java.lang.String, java.lang.Object)
	 */

	/**
	 * Find unique.
	 *
	 * @param <X> the generic type
	 * @param ql the ql
	 * @param values the values
	 * @return the x
	 */
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public <X> X findUnique(final String ql, final Object... values) {
		return (X) baseDao.findUnique(ql, values);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#findUnique(java.lang.String, java.util.Map)
	 */

	/**
	 * Find unique.
	 *
	 * @param <X> the generic type
	 * @param ql the ql
	 * @param values the values
	 * @return the x
	 */
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public <X> X findUnique(final String ql, final Map<String, ?> values) {
		return (X) baseDao.findUnique(ql, values);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#batchExecute(java.lang.String, java.lang.Object)
	 */

	/**
	 * Batch execute.
	 *
	 * @param ql the ql
	 * @param values the values
	 * @return the int
	 */
	public int batchExecute(final String ql, final Object... values) {
		return baseDao.batchExecute(ql, values);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#batchExecute(java.lang.String, java.util.Map)
	 */

	/**
	 * Batch execute.
	 *
	 * @param ql the ql
	 * @param values the values
	 * @return the int
	 */
	public int batchExecute(final String ql, final Map<String, ?> values) {
		return baseDao.batchExecute(ql, values);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.persistence.service.BaseServiceInterface#getAll(java.lang.Class, cn.com.summall.commons.PageRequest)
	 */

	/**
	 * Gets the all.
	 *
	 * @param <T> the generic type
	 * @param entityClass the entity class
	 * @param pageRequest the page request
	 * @return the all
	 */
	@Transactional(readOnly = true)
	public <T> Page<T> getAll(final Class<T> entityClass, final PageRequest pageRequest) {
		return baseDao.findAll(entityClass, pageRequest);
	}

	/**
	 * Find page.
	 *
	 * @param <T> the generic type
	 * @param pageRequest the page request
	 * @param ql the ql
	 * @param values the values
	 * @return the page
	 */
	@Transactional(readOnly = true)
	public <T> Page<T> findPage(final PageRequest pageRequest, final String ql, final Object... values) {
		return baseDao.findPage(pageRequest, ql, values);
	}

	/**
	 * Find page.
	 *
	 * @param <T> the generic type
	 * @param pageRequest the page request
	 * @param ql the ql
	 * @param values the values
	 * @return the page
	 */
	@Transactional(readOnly = true)
	public <T> Page<T> findPage(final PageRequest pageRequest, final String ql, final Map<String, Object> values) {
		return baseDao.findPage(pageRequest, ql, values);
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
	@Transactional(readOnly = true)
	public <T> boolean isPropertyUnique(final Class<T> entityClass, final String propertyName, final Object newValue,
			final Object oldValue) {
		return baseDao.isPropertyUnique(entityClass, propertyName, newValue, oldValue);
	}
}
