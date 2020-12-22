package Flyweight;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuoteManager {
	private Set<Quote> quoteSet = new HashSet<>();

	public Quote addQuote(String author, String quote) {
		if (author == null) {
			throw new IllegalArgumentException("author cannot be null");
		}
		if (quote == null) {
			throw new IllegalArgumentException("citation cannot be null");
		}
		
		List<String> authorList = StringFlyweightFactory.add(author);
		List<String> quoteList = StringFlyweightFactory.add(quote);
		
		Quote aQuote = new Quote(authorList, quoteList);
		quoteSet.add(aQuote);
		
		return aQuote;
	}
	
	public Set<Quote> getQuotes() {
		return quoteSet;
	}

	public void removeQuote(Quote quote) {
		if (quote == null) {
			throw new IllegalArgumentException("quote cannot be null");
		}
		
		StringFlyweightFactory.release(quote.getAuthor());		
		StringFlyweightFactory.release(quote.getCitation());
		
		quoteSet.remove(quote);
	}
	
	class Quote {
		private List<String> author = new ArrayList<>();
		private List<String> citation = new ArrayList<>();
		
		private Quote(List<String> author, List<String> citation) {
			if (author == null) {
				throw new IllegalArgumentException("author cannot be null");
			}
			
			if (citation == null) {
				throw new IllegalArgumentException("citation cannot be null");
			}
			
			this.author = author;
			this.citation = citation;
		}
		
		public List<String> getCitation() {
			return citation;
		}
		
		public List<String> getAuthor() {
			return author;
		}
		
		public String toString() {
			String quote = "";
			String anAuthor = "";
			
			for(String word: citation) {
				quote = quote.concat(word).concat(" ");
			}
			
			for(String word: author) {
				anAuthor = anAuthor.concat(word).concat(" ");
			}
			
			quote = quote.substring(0, quote.length()-1);
			anAuthor = anAuthor.substring(0, anAuthor.length()-1);

			return String.format("\"%s\" - %s", quote, anAuthor);
		}
	}
}
