package com.verint.textanalytics.common.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by EZlotnik on 3/23/2016.
 */
public interface MultivaluedStringMap {

	/**
	 * Keys of map.
	 * @return set of map keys
	 */
	Set keySet();

	/**
	 * Values set.
	 * @return set of values
	 */
	Set entrySet();

	/**
	 * Keys ordered by the order of insertion.
	 * @return list of keys
	 */
	ArrayList<String> keyList();

	/**
	 * Number of values.
	 * @return number of values
	 */
	int size();

	/**
	 * Is map empty.
	 * @return is map empty
	 */
	boolean isEmpty();

	/**
	 * Removes all values in map.
	 */
	void clear();

	/**
	 * Add value with key.
	 * @param key key
	 * @param value value
	 */
	void add(String key, String value);

	/**
	 * Values associated to a key.
	 * @param key key
	 * @return list of values
	 */
	List<String> get(String key);

	/**
	 * All values in map.
	 * @return collection of all values in map
	 */
	Collection<List<String>> values();

	/**
	 * Does map contains key.
	 * @param key key to check
	 * @return list of values assigned to key
	 */
	boolean containsKey(String key);

	/**
	 * Does map contains value.
	 * @param value value to check
	 * @return indication
	 */
	boolean containsValue(String value);

	/**
	 * Removes all values associated with a key.
	 * @param key key
	 * @return list of removed values
	 */
	List<String> remove(String key);
}
