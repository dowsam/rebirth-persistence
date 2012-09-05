/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-persistence LockMetadataProvider.java 2012-2-12 12:14:55 l.xue.nong$$
 */
package cn.com.rebirth.persistence;

import javax.persistence.LockModeType;

/**
 * The Interface LockMetadataProvider.
 *
 * @author l.xue.nong
 */
public interface LockMetadataProvider {
	
	/**
	 * Gets the lock mode type.
	 *
	 * @return the lock mode type
	 */
	LockModeType getLockModeType();
}
