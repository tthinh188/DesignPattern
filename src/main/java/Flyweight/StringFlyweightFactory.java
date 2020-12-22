package Flyweight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringFlyweightFactory {
	private static Map<String, Integer> m = new HashMap<>();
	
	public static List<String> add(String str) {
		if (str == null) {
			throw new IllegalArgumentException("string cannot be null");
		}
		
		List<String> list = new ArrayList<>();
		String[] split = str.split("\\s+"); // split by spaces
		
		for(String token: split) {
			list.add(token);
			if(!m.containsKey(token)) {
				m.put(token, 1);
			}
			else {
				m.put(token, m.get(token) + 1);
			}
		}
		return list;
	}

	public static Map<String, Integer> getEntries() {
		return m;
	}

	public static void release(List<String> list) {
		if (list == null) {
			throw new IllegalArgumentException("list cannot be null");
		}
		
		for (String word: list) {
			if (m.containsKey(word)) { // if have word, then decrement.
				m.put(word, m.get(word) - 1);
				if (m.get(word) == 0) { // if it's 0 remove it from the map.
					m.remove(word);
				}
			}
		}
	}
	
}
