package Flyweight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic.Verification;
import org.mockito.Mockito;

class QuoteManagerTest {
	private static final Class<?> QUOTE_MANAGER            = QuoteManager          .class;
	private static final Class<?> QUOTE                    = QuoteManager.Quote    .class;
	private static final Class<?> STRING_FLYWEIGHT_FACTORY = StringFlyweightFactory.class;

	private static <T> void assertEqualSets(Collection<T> expected, Collection<T> actual) {
		assertNotNull( actual );
		List<T> aList = new ArrayList<>( actual );
		List<T> eList = new ArrayList<>( expected );
		while (!eList.isEmpty()) {
			T e = eList.remove(0);
			if (aList.isEmpty()) {
				fail( String.format( "missing results %s%nexpected %s%nactual %s%n", eList, expected, actual ));
			} else {
				assertTrue( aList.remove( e ), ()->String.format( "'%s' expected but not found.%nexpected %s%nactual %s%n"+
						"remaining expected %s%nremaining actual %s",
						e, expected, actual, eList, aList ));
			}
		}
		if (!aList.isEmpty()) {
			fail( String.format( "excess results %s%nexpected %s%nactual %s", aList, expected, actual ));
		}
	}
	private static <T> void assertEqualLists(Collection<T> expected, Collection<T> actual) {
		assertNotNull( actual );
		List<T> aList = new ArrayList<>( actual );
		List<T> eList = new ArrayList<>( expected );
		int     index = 0;
		while (!eList.isEmpty()) {
			T e = eList.remove( 0 );
			if (aList.isEmpty()) {
				fail( String.format( "missing results %s%nexpected %s%nactual %s%n",
					 	              eList, expected, actual ));
			} else {
				T a = aList.remove( 0 );
				int i = index;
				assertEquals( e, a, () ->
				           String.format( "Unexpected result @ index %d%nexpected %s%nactual %s%n",
						   i, expected, actual ));
			}
			index++;
		}
		if (!aList.isEmpty()) {
			fail( String.format( "excess results %s%nexpected %s%nactual %s",
							      aList, expected, actual ));
		}
	}
	private <T,U> void assertEqualMaps(Map<T,U> expected, Map<T,U> actual) {
		assertNotNull( actual, "Result was null" );
		Map<T,U> aMap = new HashMap<>( actual );
		for (Map.Entry<T,U> e : expected.entrySet()) {
			if (aMap.isEmpty()) {
				fail( String.format( "Unexpected end of results: expected (%s,%s) but results ended.", e.getKey(), e.getValue() ));
			} else {
				T eKey = e.getKey();
				if (aMap.containsKey( eKey )) {
					U eValue = e.getValue();
					U aValue = aMap.get( eKey );
					assertTrue( aMap.remove( eKey, eValue ), ()->String.format( "Map key '%s' has unexpected value: expected  '%s' but was '%s'", eKey, eValue, aValue ));
				} else {
					fail( "Expected map key not found: "+ eKey );
				}
			}
		}
		if (!aMap.isEmpty()) {
			fail( String.format( "unexpected results %s", aMap ));
		}
	}
	
	@Test
	void testFields() {
		Consumer<Class<?>> fieldsNotStatic = 
				c -> Arrays.stream ( c.getDeclaredFields() )
				           .filter ( f->!f.isSynthetic() )
				           .forEach( f->assertFalse( Modifier.isStatic ( f.getModifiers() ), () -> String.format("field '%s.%s' is static",      c.getSimpleName(), f.getName() )));
		Consumer<Class<?>> fieldsPrivate   = 
				c -> Arrays.stream ( c.getDeclaredFields() )
				           .filter ( f->!f.isSynthetic() )
				           .forEach( f->assertTrue ( Modifier.isPrivate( f.getModifiers() ), () -> String.format("field '%s.%s' is not private", c.getSimpleName(), f.getName() )));
		Consumer<Class<?>> fieldsStatic    = 
				c -> Arrays.stream ( c.getDeclaredFields() )
				           .filter ( f->!f.isSynthetic() )
				           .forEach( f->assertTrue ( Modifier.isStatic ( f.getModifiers() ), () -> String.format("field '%s.%s' is not static",  c.getSimpleName(), f.getName() )));
		Consumer<Class<?>> fieldsCollections = 
				c -> Arrays.stream ( c.getDeclaredFields() )
				           .filter ( f->!f.isSynthetic() )
				           .forEach( f->{
				        	   var     type         = f.getType();
				        	   boolean isCollection = 
				        			   Collection .class.isAssignableFrom( type ) ||
						        	   Dictionary .class.isAssignableFrom( type ) ||
						        	   AbstractMap.class.isAssignableFrom( type ) ||
						        	   type.isArray();
				        	   assertTrue( isCollection, () -> String.format( "Field '%s.%s' should be an array or collection", c.getSimpleName(), f.getName() ));
		});
		fieldsNotStatic  .accept( QUOTE_MANAGER );
		fieldsPrivate    .accept( QUOTE_MANAGER );
		fieldsNotStatic  .accept( QUOTE );
		fieldsPrivate    .accept( QUOTE );
		fieldsCollections.accept( QUOTE );
		fieldsStatic     .accept( STRING_FLYWEIGHT_FACTORY );
		fieldsPrivate    .accept( STRING_FLYWEIGHT_FACTORY );		
	}
	@Nested
	class TestingManager {
		@Test
		void testAddAndReleaseWithNullThrowException() {
			var manager = new QuoteManager();

			var t = assertThrows( IllegalArgumentException.class, () -> manager.addQuote( null, "" ));
			assertEquals( "author cannot be null", t.getMessage());

			var u = assertThrows( IllegalArgumentException.class, () -> manager.addQuote( "", null ));
			assertEquals( "citation cannot be null",  u.getMessage());

			var v = assertThrows( IllegalArgumentException.class, () -> manager.removeQuote( null ));
			assertEquals( "quote cannot be null",  v.getMessage());
		}
		@Test
		void testNewManagerDoesNotCallFactory() {
			try (var factory = Mockito.mockStatic( StringFlyweightFactory.class )) {
				var manager  = new QuoteManager();
				var actual   = manager.getQuotes();
				var expected = new HashSet<QuoteManager.Quote>();
				assertEqualSets( expected, actual );
				factory.verifyNoInteractions();
			}
		}
		@Test
		void testAddQuoteCallsFactoryAdd() {
			try (var factory = Mockito.mockStatic( StringFlyweightFactory.class )) {
				var name     = "F.A. Clark";
				var cite     = "It's hard to detect good luck because it looks so much like something you have earned";
				Verification addAuthor = () -> StringFlyweightFactory.add( name );
				Verification addQuote  = () -> StringFlyweightFactory.add( cite );

				var manager  = new QuoteManager();
				var quote    = manager.addQuote( name, cite );

				factory.verify( addAuthor );
				factory.verify( addQuote  );

				var quotes   = manager.getQuotes();
				assertNotNull( quote );
				assertNotNull( quotes );
				var expected = Set.of( quote );
				assertEqualSets( expected, quotes );

				factory.verifyNoMoreInteractions();
			}
		}
		@Test
		void testDeleteQuoteCallsFactoryRelease() {
			try (var factory = Mockito.mockStatic( StringFlyweightFactory.class )) {
				var name     = "Life magazine in The Secret Life of Walter Mitty";
				var cite     = "to see the world, things dangerous to come to, to see behind walls, draw closer, to find each other, and to feel. that is the purpose of life";

				var manager  = new QuoteManager();
				var quote    = manager.addQuote( name, cite );
				
				manager.removeQuote( quote );

				Verification release  = () -> StringFlyweightFactory.release( ArgumentMatchers.anyList() );
				factory.verify( Mockito.times(2), release );

				var quotes   = manager.getQuotes();
				assertNotNull( quotes );
				assertTrue   ( quotes.isEmpty() );
			}
		}
		@Test
		void testAddingRemovingSeveralQuotes() {
			try (var factory = Mockito.mockStatic( StringFlyweightFactory.class )) {
				var authors  = List.of(
						"Adam Lindsay Gordon",
						"Nicolas Boileau-Despreaux",
						"Eleanor Roosevelt"
						);
				var cites    = List.of(
						"Life is mostly froth and bubble, Two things stand like stone, Kindness in another's trouble, Courage in your own",
						"Who is content with nothing possesses all things",
						"Beautiful young people are accidents of nature, but beautiful old people are works of art"
						);
				var manager  = new QuoteManager();
				
				List<QuoteManager.Quote> expected = new ArrayList<>();
				Set <QuoteManager.Quote> actual;
				QuoteManager.Quote       quote;
				// adding
				// 0
				quote = manager.addQuote( authors.get( 0 ), cites.get( 0 ));
				assertNotNull( quote );
				expected.add ( quote );

				actual = manager.getQuotes();
				assertNotNull  ( actual );
				assertEqualSets( expected, actual );
				// 1
				quote = manager.addQuote( authors.get( 1 ), cites.get( 1 ));
				assertNotNull( quote );
				expected.add ( quote );

				actual = manager.getQuotes();
				assertNotNull  ( actual );
				assertEqualSets( expected, actual );
				// 2
				quote = manager.addQuote( authors.get( 2 ), cites.get( 2 ));
				assertNotNull( quote );
				expected.add ( quote );

				actual = manager.getQuotes();
				assertNotNull  ( actual );
				assertEqualSets( expected, actual );
				
				// removing
				// 0
				quote  = expected.remove( 0 );
				manager.removeQuote( quote );
				
				actual = manager.getQuotes();
				assertNotNull  ( actual );
				assertEqualSets( expected, actual );
				// 1
				quote  = expected.remove( 0 );
				manager.removeQuote( quote );
				
				actual = manager.getQuotes();
				assertNotNull  ( actual );
				assertEqualSets( expected, actual );
				// 2
				quote  = expected.remove( 0 );
				manager.removeQuote( quote );
				
				actual = manager.getQuotes();
				assertNotNull( actual );
				assertTrue   ( actual.isEmpty() );
			}
		}
	}
	@Nested
	class TestingQuote {
		@Test
		void testHasOnePrivateConstructor() {
			Constructor<?>[] constructors = QUOTE.getDeclaredConstructors();
			assertEquals( 1, constructors.length );
			
			int modifier = constructors[0].getModifiers();
			assertTrue( Modifier.isPrivate( modifier ),  "constructors hould be private " );
		}
		@Test
		void testQuote() {
			try (var factory = Mockito.mockStatic( StringFlyweightFactory.class )) {
				var author     = "Walt Kelly";
				var cite       = "We have met the enemy and he is us";
				var authorList = List.of( "Walt","Kelly" );
				var citeList   = List.of( "We","have","met","the","enemy","and","he","is","us" );

				Verification  addAuthor = () -> StringFlyweightFactory.add( author );
				factory.when( addAuthor ).thenReturn( authorList );
				
				Verification  addCite   = () -> StringFlyweightFactory.add( cite );
				factory.when( addCite ).thenReturn( citeList );

				var manager  = new QuoteManager();
				var quote    = manager.addQuote( author, cite );

				var authorActual   = quote.getAuthor();
				assertEqualLists( authorList, authorActual );

				var citeActual = quote.getCitation();
				assertEqualLists( citeList, citeActual );
				
				var toExpected = "\"We have met the enemy and he is us\" - Walt Kelly";
				var toActual   = quote.toString();
				assertEquals( toExpected, toActual );
			}			
		}
		@Test
		void testNewQuoteWithNullThrowsExceptiopn() {
			try (var factory = Mockito.mockStatic( StringFlyweightFactory.class )) {
				var name     = "Mark Twain";
				var cite     = "We despise all reverences and all objects of reverence which are outside the pale of our list of sacred things. and yet, with strange inconsistency, we are shocked when other people despise and defile the things which are holy to us";
				Verification addAuthor = () -> StringFlyweightFactory.add( name );
				Verification addQuote  = () -> StringFlyweightFactory.add( cite );

				factory.when( addAuthor ).thenReturn( null      ).thenReturn( List.of() );
				factory.when( addQuote  ).thenReturn( List.of() ).thenReturn( null      );
				
				Set<QuoteManager.Quote> quotes;
				Throwable               t;
				var                     manager  = new QuoteManager();
				// author
				t      = assertThrows( IllegalArgumentException.class, () -> manager.addQuote( name, cite ));
				assertEquals( "author cannot be null", t.getMessage() );
				
				quotes = manager.getQuotes();
				assertNotNull( quotes );
				assertTrue   ( quotes.isEmpty() );
				// citation
				t      = assertThrows( IllegalArgumentException.class, () -> manager.addQuote( name, cite ));
				assertEquals( "citation cannot be null", t.getMessage() );

				quotes = manager.getQuotes();
				assertNotNull( quotes );
				assertTrue   ( quotes.isEmpty() );
			}
		}
	}
	private static class MapBuilder<T,U> {
		private Map<T,U> map = new HashMap<>();
		
		MapBuilder<T,U> of(T key,U value) {
			map.put( key, value );
			return this;
		}	
		Map<T,U> build() {
			return map;
		}
	}
	@Nested
	class FactoryTesting {
		@Test
		void testAddAndReleaseWithNullThrowException() {
			var t = assertThrows( IllegalArgumentException.class, () -> StringFlyweightFactory.add( null ));
			assertEquals( "string cannot be null", t.getMessage());

			var u = assertThrows( IllegalArgumentException.class, () -> StringFlyweightFactory.release( null ));
			assertEquals( "list cannot be null", u.getMessage());
		}
		@Test
		void testFactoryParsesAndStoresWords0() {
			var            preActual = StringFlyweightFactory.getEntries();
			assertNotNull( preActual );
			assertTrue   ( preActual.isEmpty() );

			var cite     = "a a";
			var added    = StringFlyweightFactory.add( cite );
			var expected = List.of( "a","a" ); 
			assertEqualSets( expected, added );

			var inActual   = StringFlyweightFactory.getEntries();
			var inExpected = new MapBuilder<String,Integer>()
					.of( "a",    2 ).build();
			assertEqualMaps( inExpected, inActual );		

			StringFlyweightFactory.release( added );

			var            postActual = StringFlyweightFactory.getEntries();
			assertNotNull( postActual );
			assertTrue   ( postActual.isEmpty() );
		}
		@Test
		void testFactoryParsesAndStoresWords1() {
			var            preActual = StringFlyweightFactory.getEntries();
			assertNotNull( preActual );
			assertTrue   ( preActual.isEmpty() );

			var cite     = String.format( "a  aa  aaa bbb   bb_b  aaa aaa%naaa .c. .c c. b_bb%nbbb bb b bb%n%na%n" );
			var added    = StringFlyweightFactory.add( cite );
			var expected = List.of( "a","aa","aaa","bbb","bb_b","aaa","aaa","aaa",".c.",".c","c.","b_bb","bbb","bb","b","bb","a" ); 
			assertEqualSets( expected, added );

			var inActual   = StringFlyweightFactory.getEntries();
			var inExpected = new MapBuilder<String,Integer>()
					.of( "a",    2 )
					.of( "aa",   1 )
					.of( "aaa",  4 )
					.of( "bbb",  2 )
					.of( "bb_b", 1 )
					.of( ".c.",  1 )
					.of( ".c",   1 )
					.of( "c.",   1 )
					.of( "bb",   2 )
					.of( "b",    1 )
					.of( "b_bb", 1 ).build();
			assertEqualMaps( inExpected, inActual );		

			StringFlyweightFactory.release( added );

			var            postActual = StringFlyweightFactory.getEntries();
			assertNotNull( postActual );
			assertTrue   ( postActual.isEmpty() );
		}
		@Test
		void testFactoryParsesAndStoresWords2() {
			var            preActual = StringFlyweightFactory.getEntries();
			assertNotNull( preActual );
			assertTrue   ( preActual.isEmpty() );

			var cite     = "and the fox said to the little prince: men have forgotten this truth, but you must not forget it. you become responsible, forever, for what you have tamed";
			var added    = StringFlyweightFactory.add( cite );
			var expected = List.of( "and","the","fox","said","to","the","little","prince:","men","have","forgotten","this","truth,","but","you","must","not","forget","it.","you","become","responsible,","forever,","for","what","you","have","tamed" ); 
			assertEqualSets( expected, added );

			var inActual   = StringFlyweightFactory.getEntries();
			var inExpected = new MapBuilder<String,Integer>()
					.of( "and",          1 )
					.of( "the",          2 )
					.of( "fox",          1 )
					.of( "said",         1 )
					.of( "to",           1 )
					.of( "little",       1 )
					.of( "prince:",      1 )
					.of( "men",          1 )
					.of( "have",         2 )
					.of( "forgotten",    1 )
					.of( "this",         1 )
					.of( "truth,",       1 )
					.of( "but",          1 )
					.of( "you",          3 )
					.of( "must",         1 )
					.of( "not",          1 )
					.of( "forget",       1 )
					.of( "it.",          1 )
					.of( "become",       1 )
					.of( "responsible,", 1 )
					.of( "forever,",     1 )
					.of( "for",          1 )
					.of( "what",         1 )
					.of( "tamed",        1 ).build();
			assertEqualMaps( inExpected, inActual );		

			StringFlyweightFactory.release( added );

			var            postActual = StringFlyweightFactory.getEntries();
			assertNotNull( postActual );
			assertTrue   ( postActual.isEmpty() );
		}
		@Test
		void testSeveralStrings() {
			var            preActual = StringFlyweightFactory.getEntries();
			assertNotNull( preActual );
			assertTrue   ( preActual.isEmpty() );

			var cites    = List.of(
					"he that wrestles with us strengthens our nerves, and sharpens our skill. our antagonist is our helper",
					"never idealize others. they will never live up to your expectations",
					"never bear more than one trouble at a time some people bear three kinds: all they have had, all they have now, and all they expect to have"
			);
			var expected = List.of(
					List.of( "he","that","wrestles","with","us","strengthens","our","nerves,","and","sharpens","our","skill.","our","antagonist","is","our","helper" ),
					List.of( "never","idealize","others.","they","will","never","live","up","to","your","expectations" ),
					List.of( "never","bear","more","than","one","trouble","at","a","time","some","people","bear","three","kinds:","all","they","have","had,","all","they","have","now,","and","all","they","expect","to","have" )
			);
			List<List<String>>  actual   = new ArrayList<>();
			Map<String,Integer> inActual;
			Map<String,Integer> inExpected;
			// 0
			actual.add( StringFlyweightFactory.add( cites.get( 0 )));
			assertEqualSets( expected.get( 0 ), actual.get( 0 ));

			inActual   = StringFlyweightFactory.getEntries();
			inExpected = new MapBuilder<String,Integer>()
					.of( "he",           1 )
					.of( "that",         1 )
					.of( "wrestles",     1 )
					.of( "with",         1 )
					.of( "us",           1 )
					.of( "strengthens",  1 )
					.of( "our",          4 )
					.of( "nerves,",      1 )
					.of( "and",          1 )
					.of( "sharpens",     1 )
					.of( "skill.",       1 )
					.of( "antagonist",   1 )
					.of( "is",           1 )
					.of( "helper",       1 )
					.build();
			assertEqualMaps( inExpected, inActual );
			// 1
			actual.add( StringFlyweightFactory.add( cites.get( 1 )));
			assertEqualSets( expected.get( 1 ), actual.get( 1 ));

			inActual   = StringFlyweightFactory.getEntries();
			inExpected = new MapBuilder<String,Integer>()
					.of( "he",           1 )
					.of( "that",         1 )
					.of( "wrestles",     1 )
					.of( "with",         1 )
					.of( "us",           1 )
					.of( "strengthens",  1 )
					.of( "our",          4 )
					.of( "nerves,",      1 )
					.of( "and",          1 )
					.of( "sharpens",     1 )
					.of( "skill.",       1 )
					.of( "antagonist",   1 )
					.of( "is",           1 )
					.of( "helper",       1 )
					.of( "never",        2 )
					.of( "idealize",     1 )
					.of( "others.",      1 )
					.of( "they",         1 )
					.of( "will",         1 )
					.of( "live",         1 )
					.of( "up",           1 )
					.of( "to",           1 )
					.of( "your",         1 )
					.of( "expectations", 1 )
					.build();
			assertEqualMaps( inExpected, inActual );		
			// 2
			actual.add( StringFlyweightFactory.add( cites.get( 2 )));
			assertEqualSets( expected.get( 2 ), actual.get( 2 ));

			inActual   = StringFlyweightFactory.getEntries();
			inExpected = new MapBuilder<String,Integer>()
					.of( "he",           1 )
					.of( "that",         1 )
					.of( "wrestles",     1 )
					.of( "with",         1 )
					.of( "us",           1 )
					.of( "strengthens",  1 )
					.of( "our",          4 )
					.of( "nerves,",      1 )
					.of( "and",          2 )
					.of( "sharpens",     1 )
					.of( "skill.",       1 )
					.of( "antagonist",   1 )
					.of( "is",           1 )
					.of( "helper",       1 )
					.of( "never",        3 )
					.of( "idealize",     1 )
					.of( "others.",      1 )
					.of( "they",         4 )
					.of( "will",         1 )
					.of( "live",         1 )
					.of( "up",           1 )
					.of( "to",           2 )
					.of( "your",         1 )
					.of( "expectations", 1 )
					.of( "bear",         2 )
					.of( "more",         1 )
					.of( "than",         1 )
					.of( "one",          1 )
					.of( "trouble",      1 )
					.of( "at",           1 )
					.of( "a",            1 )
					.of( "time",         1 )
					.of( "some",         1 )
					.of( "people",       1 )
					.of( "three",        1 )
					.of( "kinds:",       1 )
					.of( "all",          3 )
					.of( "have",         3 )
					.of( "had,",         1 )
					.of( "now,",         1 )
					.of( "expect",       1 )
					.build();
			assertEqualMaps( inExpected, inActual );		

			StringFlyweightFactory.release( actual.get( 0 ));
			StringFlyweightFactory.release( actual.get( 1 ));
			StringFlyweightFactory.release( actual.get( 2 ));

			var            postActual = StringFlyweightFactory.getEntries();
			assertNotNull( postActual );
			assertTrue   ( postActual.isEmpty() );
		}
	}
}
