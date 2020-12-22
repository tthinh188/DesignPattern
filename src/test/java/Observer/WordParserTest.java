package Observer;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

class WordParserTest {
	@Nested
	class TestWithMockito {
		@Test
		void testSubscribe0() {
			WordParserObserver a = Mockito.mock( WordParserObserver.class );
			WordParser   p = new WordParser();
			p.subscribe( a, "Hello" );
			
			Mockito.verifyNoMoreInteractions( a );
		}
		@Test
		void testUpdate0() {
			WordParserObserver a = Mockito.mock( WordParserObserver.class );			
			WordParser   p = new WordParser();
			p.subscribe( a, "Hello" );
			p.processText("Hello World");
	
			Mockito.verify( a ).update( p, "Hello" );
			Mockito.verifyNoMoreInteractions( a );
		}
		@Test
		void testUpdate1() {
			WordParserObserver a = Mockito.mock( WordParserObserver.class );			
			WordParser   p = new WordParser();
			p.subscribe( a, "hello" );
			p.subscribe( a, "world" );
			p.processText("Hello World (or HELLO or hello or...)");
	
			Mockito.verify( a ).update( p, "Hello" );
			Mockito.verify( a ).update( p, "HELLO" );
			Mockito.verify( a ).update( p, "hello" );
			Mockito.verify( a ).update( p, "World" );
			Mockito.verifyNoMoreInteractions( a );
		}
		@Test
		void testUpdate2() {
			WordParserObserver a = Mockito.mock( WordParserObserver.class );			
			WordParserObserver b = Mockito.mock( WordParserObserver.class );			
			WordParserObserver c = Mockito.mock( WordParserObserver.class );			
			WordParser   p = new WordParser();
			p.subscribe( a, "hello" );
			p.subscribe( b, "hello" );
			p.subscribe( c, "hello" );
			p.processText("Hello World");
	
			Mockito.verify( a ).update( p, "Hello" );
			Mockito.verify( b ).update( p, "Hello" );
			Mockito.verify( c ).update( p, "Hello" );
			Mockito.verifyNoMoreInteractions( a );
			Mockito.verifyNoMoreInteractions( b );
			Mockito.verifyNoMoreInteractions( c );
		}
		@Test
		void testUpdate3() {
			WordParserObserver a = Mockito.mock( WordParserObserver.class );			
			WordParserObserver b = Mockito.mock( WordParserObserver.class );			
			WordParser   p = new WordParser();
			
			p.subscribe( a, "hello" );
			p.subscribe( a, "hello" );
			p.subscribe( a, "hello" );
			p.subscribe( b, "hello" );
			p.processText("HELLO World");
			p.processText("World Hello");
	
			Function<String,ArgumentMatcher<String>> m = s -> in -> in.equalsIgnoreCase( s );
			Mockito.verify( a, Mockito.times( 2 )).update( ArgumentMatchers.eq(p), ArgumentMatchers.argThat( m.apply( "hello" )));
			Mockito.verify( b, Mockito.times( 2 )).update( ArgumentMatchers.eq(p), ArgumentMatchers.argThat( m.apply( "hello" )));
			Mockito.verifyNoMoreInteractions( a );
			Mockito.verifyNoMoreInteractions( b );
		}
		@Test
		void testUpdateUnsubscribe0() {
			WordParserObserver a = Mockito.mock( WordParserObserver.class );			
			WordParser   p = new WordParser();
			p.subscribe( a, "hello" );
			p.subscribe( a, "world" );
			p.processText("Hello World");
	
			Mockito.verify( a ).update( p, "Hello" );
			Mockito.verify( a ).update( p, "World" );
			
			p.unsubscribe( a, "hello" );
			p.processText("Hello World");
			Mockito.verify( a, Mockito.times( 2 )).update( p, "World" );
			Mockito.verifyNoMoreInteractions( a );
		}
		@Test
		void testUnsubscribe1() {
			WordParserObserver a = Mockito.mock( WordParserObserver.class );			
			WordParser   p = new WordParser();
			p.subscribe( a, "hello" );
			p.subscribe( a, "world" );
			p.processText("Hello World");
	
			Mockito.verify( a ).update( p, "Hello" );
			Mockito.verify( a ).update( p, "World" );
			Mockito.verifyNoMoreInteractions( a );
			
			p.unsubscribe( a, "HELLO" );
			p.processText("Hello World");
			Mockito.verify( a, Mockito.times( 2 )).update( p, "World" );
			Mockito.verifyNoMoreInteractions( a );
			
			p.unsubscribe( a, "World" );
			p.processText("Hello World");
			Mockito.verifyNoMoreInteractions( a );
		}
		@Test
		void testUnsubscribe2() {
			WordParserObserver a = Mockito.mock( WordParserObserver.class );			
			WordParserObserver b = Mockito.mock( WordParserObserver.class );			
			WordParserObserver c = Mockito.mock( WordParserObserver.class );			
			WordParser   p = new WordParser();

			p.subscribe( a, "hello" );
			p.subscribe( b, "hello" );
			p.subscribe( b, "world" );
			p.subscribe( c, "world" );
			p.processText("Hello World");

			p.unsubscribe( b );
			p.processText("World Hello");
			
			Mockito.verify( a, Mockito.times( 2 )).update( p, "Hello" );
			Mockito.verify( b, Mockito.times( 1 )).update( p, "Hello" );
			Mockito.verify( b, Mockito.times( 1 )).update( p, "World" );
			Mockito.verify( c, Mockito.times( 2 )).update( p, "World" );
			Mockito.verifyNoMoreInteractions( a );
			Mockito.verifyNoMoreInteractions( b );
			Mockito.verifyNoMoreInteractions( c );
		}
		@Test
		void testUnsubscribe3() {
			WordParserObserver a = Mockito.mock( WordParserObserver.class );			
			WordParser   p = new WordParser();

			p.subscribe  ( a, "hello" );
			p.unsubscribe( a, "world" );
			p.processText("hello world");

			Mockito.verify( a ).update( p, "hello" );
			Mockito.verifyNoMoreInteractions( a );
		}
	}
	@Nested
	class TestWordParserExceptions {
		@Test
		void testSubscribeNullObserverThrowsException() {
			WordParser p = new WordParser();
			Throwable  t = assertThrows( IllegalArgumentException.class, 
					() -> p.subscribe( null, "Hello" )
			);
			assertEquals( "observer cannot be null", t.getMessage() );
		}
		@Test
		void testSubscribeInvalidWordThrowsException() {
			WordParser   p = new WordParser();
			WordParserObserver a = Mockito.mock( WordParserObserver.class );
			for (String word : new String[] { null, "", " ", "Hello ", " Hello", "Hello World" }) {
			   Throwable t = assertThrows( IllegalArgumentException.class, 
					() -> p.subscribe( a, word ),
					() -> String.format( "with word: '%s'", word )
			   );
			   assertEquals( "word cannot be null, empty or have spaces", t.getMessage(), 
					   () -> String.format( "with word: '%s'", word ));
			}
		}
		@Test
		void testUnsubscribeNullObserverThrowsException() {
			WordParser p = new WordParser();
			Throwable  t = assertThrows( IllegalArgumentException.class, 
					() -> p.unsubscribe( null, "Hello" )
			);
			assertEquals( "observer cannot be null", t.getMessage() );
		}
		@Test
		void testUnsubscribeAllNullObserverThrowsException() {
			WordParser p = new WordParser();
			Throwable  t = assertThrows( IllegalArgumentException.class, 
					() -> p.unsubscribe( null )
			);
			assertEquals( "observer cannot be null", t.getMessage() );
		}
		@Test
		void testUnsubscribeInvalidWordThrowsException() {
			WordParser   p = new WordParser();
			WordParserObserver a = Mockito.mock( WordParserObserver.class );
			for (String word : new String[] { null, "", " ", "Hello ", " Hello", "Hello World" }) {
			   Throwable t = assertThrows( IllegalArgumentException.class, 
					() -> p.unsubscribe( a, word ),
					() -> String.format( "with word: '%s'", word )
			   );
			   assertEquals( "word cannot be null, empty or have spaces", t.getMessage(), 
					   () -> String.format( "with word: '%s'", word ));
			}
		}
		@Test
		void testProcessTextNullThrowsException() {
			WordParser p = new WordParser();
			Throwable  t = assertThrows( IllegalArgumentException.class, 
					() -> p.processText( null )
			);
			assertEquals( "text cannot be null", t.getMessage() );
		}
	}
	@Nested
	class TestMostRepeatedLetter {
		@Test
		void test0() {
			MostRepeatedWord a = new MostRepeatedWord();
			{
				Optional<String> actual = a.getWord();
				assertNotNull( actual );
				assertEquals ( Optional.empty(), a.getWord() );
				assertTrue   ( actual.isEmpty() );
			}{
				int actual = a.getTimes();
				assertEquals( 0, actual );
			}
		}
		@Test
		void test1() {
			WordParser       p = new WordParser();
			MostRepeatedWord a = new MostRepeatedWord();
			p.subscribe( a, "hello" );
			p.processText( "hello" );
			{
				Optional<String> actual = a.getWord();
				assertNotNull( actual );
				assertTrue   ( actual.isPresent() );
				assertEquals ( "hello", a.getWord().get() );
			}{
				int actual = a.getTimes();
				assertEquals( 1, actual );
			}
		}
		@Test
		void test2() {
			WordParser       p = new WordParser();
			MostRepeatedWord a = new MostRepeatedWord();
			p.subscribe( a, "hello" );
			p.subscribe( a, "world" );
			p.processText( "hello world HELLO hello world" );
			{
				Optional<String> actual = a.getWord();
				assertNotNull( actual );
				assertTrue   ( actual.isPresent() );
				assertEquals ( "hello", a.getWord().get() );
			}{
				int actual = a.getTimes();
				assertEquals( 3, actual );
			}
		}
		@Test
		void test3() {
			WordParser       p = new WordParser();
			WordParser       q = new WordParser();
			MostRepeatedWord a = new MostRepeatedWord();
			p.subscribe( a, "hello" );
			q.subscribe( a, "world" );
			p.processText( "hello hello world" );
			{
				Optional<String> actual = a.getWord();
				assertNotNull( actual );
				assertTrue   ( actual.isPresent() );
				assertEquals ( "hello", a.getWord().get() );
			}{
				int actual = a.getTimes();
				assertEquals( 2, actual );
			}
			q.processText( "world world hello world hello" );
			{
				Optional<String> actual = a.getWord();
				assertNotNull( actual );
				assertTrue   ( actual.isPresent() );
				assertEquals ( "world", a.getWord().get() );
			}{
				int actual = a.getTimes();
				assertEquals( 3, actual );
			}
		}
		@Test
		void testUpdateNullParserThrowsException() {
			WordParserObserver a = new MostRepeatedWord();
			Throwable    t = assertThrows( IllegalArgumentException.class, 
					() -> a.update( null, "hello" )
			);
			assertEquals( "parser cannot be null", t.getMessage());
		}
		@Test
		void testUpdateInvalidWordThrowsException() {
			WordParser   p = new WordParser();
			WordParserObserver a = new MostRepeatedWord();
			for (String word : new String[] { null, "", " ", "Hello ", " Hello", "Hello World" }) {
			   Throwable t = assertThrows( IllegalArgumentException.class, 
					() -> a.update( p, word ),
					() -> String.format( "with word: '%s'", word )
			   );
			   assertEquals( "word cannot be null, empty or have spaces", t.getMessage(), 
					   () -> String.format( "with word: '%s'", word ));
			}
		}
	}
}
