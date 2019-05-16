package edu.hust.utils;

import java.time.LocalDateTime;
import java.util.Map;

public interface FrequentlyUtils {

	/**
	 * @param jsonMap - a Map contains some keys
	 * @param args - list of keys that need check
	 * @return true if the map contains all keys; 
	 */
	boolean checkKeysExist(Map<String, Object> jsonMap, String... args);
	
	
	String makeRollcallRecord(LocalDateTime rollcallAt);
}
