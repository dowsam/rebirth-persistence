package cn.com.rebirth.persistence.query;

import javax.persistence.Query;

public interface QueryExtractor {
	/**
	 * Reverse engineers the query string from the {@link Query} object. This requires provider specific API as JPA does
	 * not provide access to the underlying query string as soon as one has created a {@link Query} instance of it.
	 * 
	 * @param query
	 * @return the query string representing the query or {@literal null} if resolving is not possible.
	 */
	String extractQueryString(Query query);

	/**
	 * Returns whether the extractor is able to extract the original query string from a given {@link Query}.
	 * 
	 * @return
	 */
	boolean canExtractQuery();
}
