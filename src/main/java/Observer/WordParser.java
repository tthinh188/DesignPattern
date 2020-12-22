package Observer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WordParser {
	private Map<WordParserObserver, List<String>> observers = new HashMap<>();

	private static final String NULL_OBSERVER_MESSAGE = "observer cannot be null";

	public void subscribe(WordParserObserver o, String word) {
		if(o == null) {
			throw new IllegalArgumentException(NULL_OBSERVER_MESSAGE);
		}
		
		if(word == null || word.equals("") || word.contains(" ")) {
			throw new IllegalArgumentException("word cannot be null, empty or have spaces");
		}
		if (!observers.containsKey(o)) {
			List<String> words = new ArrayList<>();
			words.add(word);
			observers.put(o, words);
		}
		else {
			List<String> words = observers.get(o);
			for(String str: words) {
				if(!str.equalsIgnoreCase(word)) {
					words.add(word);
					break;
				}
			}
			observers.put(o, words);
		}
		
	}
	
	public void unsubscribe(WordParserObserver o, String word) {
		if(o == null) {
			throw new IllegalArgumentException(NULL_OBSERVER_MESSAGE);
		}
		
		if(word == null || word.equals("") || word.contains(" ")) {
			throw new IllegalArgumentException("word cannot be null, empty or have spaces");
		}
		List<String> words = observers.get(o);
		for(String str: words) {
			if (str.equalsIgnoreCase(word)) {
				words.remove(str);
				break;
			}
		}
		if(words.isEmpty()) {
			observers.remove(o);
		}
		else {
			observers.put(o, words);			
		}
	}
	
	public void unsubscribe(WordParserObserver o) {
		if(o == null) {
			throw new IllegalArgumentException(NULL_OBSERVER_MESSAGE);
		}
		observers.remove(o);
	}
	
	public void processText(String text) {
		if(text == null) {
			throw new IllegalArgumentException("text cannot be null");

		}
		String[] token = text.split("\\s+");
		Set<WordParserObserver> keys = observers.keySet();
		for (String str: token) {
			for(WordParserObserver key: keys) {
				for(String subWord: observers.get(key)) {
					if(subWord.equalsIgnoreCase(str)) {
						key.update(this, str);
					}
				}
			}
		}
	}
		
	
	public List<String> getWord(MostRepeatedWord w) {
		return observers.get(w);
	}
}
