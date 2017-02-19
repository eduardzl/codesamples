package com.verint.textanalytics.common.collection;

/**
 * Created by EZlotnik on 3/23/2016.
 */
/*
Copyright (c) 2007, Dennis M. Sosnoski
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.
 * Neither the name of JiBX nor the names of its contributors may be used
   to endorse or promote products derived from this software without specific
   prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;

import javax.ws.rs.core.MultivaluedMap;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Map with keys iterated in insertion order. This is similar to the Java 1.4
 * java.util.LinkedHashMap class, but compatible with earlier JVM versions. It
 * also guarantees insertion ordering only for iterating through the key values,
 * not for other iterations. This implementation is optimized for insert-only
 * maps.
 */
public class InsertionOrderedMultiValueStringMap implements com.verint.textanalytics.common.collection.MultivaluedStringMap {
	private final MultivaluedStringMap baseMap;
	private final ArrayList<String> insertList;

	/**
	 * Constructor.
	 */
	public InsertionOrderedMultiValueStringMap() {
		baseMap = new MultivaluedStringMap();
		insertList = new ArrayList<>();
	}

	/**
	 * Clears the map.
	 */
	public void clear() {
		baseMap.clear();
		insertList.clear();
	}

	/**
	 * Values stored in map.
	 * @return values
	 */
	public Collection<List<String>> values() {
		return baseMap.values();
	}

	/**
	 * Contains key indication.
	 * @param key key to check for.
	 * @return boolean value
	 */
	public boolean containsKey(String key) {
		return baseMap.containsKey(key);
	}

	/**
	 * Delegates to base map.
	 * @param value value to check if it contained in map
	 * @return indication
	 */
	public boolean containsValue(String value) {
		return baseMap.containsValue(value);
	}

	/**
	 * Set of all values in map.
	 * @return values set
	 */
	public Set entrySet() {
		return baseMap.entrySet();
	}

	/**
	 * Get values by key.
	 * @param key key
	 * @return list of values assigned to the key
	 */
	public List<String> get(String key) {
		return baseMap.get(key);
	}

	/**
	 * Is map empty.
	 * @return true if map is empty
	 */
	public boolean isEmpty() {
		return baseMap.isEmpty();
	}

	/**
	 * Set of keys in map.
	 * @return set of keys in map
	 */
	public Set keySet() {
		return new ListSet(insertList);
	}

	/**
	 * Add values assigned to specific key.
	 * @param key key
	 * @param value value
	 */
	public void add(String key, String value) {
		if (!baseMap.containsKey(key)) {
			insertList.add(key);
		}

		baseMap.add(key, value);
	}

	/**
	 * Removes all values assigned to specific key.
	 * @param key key
	 * @return list of removed values
	 */
	public List<String> remove(String key) {
		if (baseMap.containsKey(key)) {
			insertList.remove(key);
			return baseMap.remove(key);
		} else {
			return null;
		}
	}

	/**
	 * Number of values stored in map.
	 * @return number of values.
	 */
	public int size() {
		return baseMap.size();
	}

	/**
	 * Get list of keys in order added. The returned list is live, and will
	 * grow or shrink as pairs are added to or removed from the map.
	 *
	 * @return key list
	 */
	public ArrayList keyList() {
		return insertList;
	}

	/**
	 * Set implementation backed by a list.
	 */
	protected static class ListSet extends AbstractSet {
		private List list = null;

		public ListSet(List listSet) {
			list = listSet;
		}

		public Iterator iterator() {
			return list.iterator();
		}

		public int size() {
			return list.size();
		}
	}
}



