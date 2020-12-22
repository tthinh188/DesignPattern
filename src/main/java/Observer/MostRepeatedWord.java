package Observer;
import java.util.Optional;

public class MostRepeatedWord implements WordParserObserver {
	private WordParser currentObservable;
	private String subWord;
	private int time;
	@Override
	public void update(WordParser observable, String word) {
		if (observable == null) {
			throw new IllegalArgumentException("parser cannot be null");
		}
		if(word == null ||word.equals("") || word.contains(" ")) {
			throw new IllegalArgumentException("word cannot be null, empty or have spaces");
		}
		if (currentObservable == null) {
			currentObservable = observable;
		}
		
		if(currentObservable != observable) {
			currentObservable = observable;
			time = 0;
		}
		
		subWord = observable.getWord(this).get(0);
		if(subWord.equalsIgnoreCase(word)) {
			time++;
		}
			
	}
	public Optional<String> getWord() {
		if(subWord == null)
			return Optional.empty();
		else
			return Optional.of(subWord);
	}
	public int getTimes() {
		return time;
	}

}
