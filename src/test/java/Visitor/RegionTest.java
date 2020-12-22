package Visitor;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.ObjLongConsumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import Visitor.City;
import Visitor.CountRegionVisitor;
import Visitor.IRegionVisitor;
import Visitor.LargeCity;
import Visitor.Map;
import Visitor.PopulationRangeVisitor;
import Visitor.Region;
import Visitor.SmallCity;
import Visitor.State;

class RegionTest {
	private static final Class<?> CITY                     = City                  .class;
	private static final Class<?> COUNT_REGION_VISITOR     = CountRegionVisitor    .class;
	private static final Class<?> IREGION_VISITOR          = IRegionVisitor        .class;
	private static final Class<?> LARGE_CITY               = LargeCity             .class;
	private static final Class<?> MAP                      = Map                   .class;
	private static final Class<?> POPULATION_RANGE_VISITOR = PopulationRangeVisitor.class;
	private static final Class<?> REGION                   = Region                .class;
	private static final Class<?> SMALL_CITY               = SmallCity             .class;
	private static final Class<?> STATE                    = State                 .class;
	private static final Class<?> OBJECT                   = Object                .class;
	private static <T> void assertEqualSets(Collection<T> expected, Collection<T> actual) {
		assertNotNull( actual );
		List<T> aList = new ArrayList<>( actual );
		List<T> eList = new ArrayList<>( expected );
		while (!eList.isEmpty()) {
			T e = eList.remove(0);
			if (aList.isEmpty()) {
				fail( String.format( "missing results %s%nexpected %s%nactual %s%n", eList, expected, actual ));
			} else {
				assertTrue( aList.remove( e ), ()->String.format( "'%s' not found.%nexpected %s%nactual %s%n"+
						"remaining expected %s%nremaining actual %s",
						e, expected, actual, eList, aList ));
			}
		}
		if (!aList.isEmpty()) {
			fail( String.format( "excess results %s%nexpected %s%nactual %s", aList, expected, actual ));
		}
	}
	
	@Test
	public void testNoStaticNonPrivateFields() {
		Consumer<Class<?>> testFields = c -> Arrays.stream( c.getDeclaredFields() ).filter( f->!f.isSynthetic() ).forEach( f->{
			int mod = f.getModifiers();
			assertTrue ( Modifier.isPrivate( mod ), () -> String.format("field '%s' is not private", f.getName()) );
			assertFalse( Modifier.isStatic ( mod ), () -> String.format("field '%s' is static",      f.getName()) );
		});
		testFields.accept( CITY );
		testFields.accept( COUNT_REGION_VISITOR );
		testFields.accept( IREGION_VISITOR );
		testFields.accept( LARGE_CITY );
		testFields.accept( MAP );
		testFields.accept( POPULATION_RANGE_VISITOR );
		testFields.accept( REGION );
		testFields.accept( SMALL_CITY );
		testFields.accept( STATE );
	}
	@Test
	public void testClassesHaveSuper() {
		Consumer<Class<?>> isInterface = a ->
			assertTrue ( a.isInterface(), () -> String.format("type '%s' is not an interface", a.getName()) );
		Consumer<Class<?>> isAbstract = a -> {
			int mod = a.getModifiers();
			assertTrue ( Modifier.isAbstract( mod ), () -> String.format("class '%s' is not abstract", a.getName()) );
		};
		BiConsumer<Class<?>,Class<?>> isSuperType = (a,b) -> 
			assertTrue( a .isAssignableFrom( b ),
					() -> String.format( "'%s' is not the supertype of '%s'", a.getSimpleName(), b.getSimpleName() ));
		BiConsumer<Class<?>,Class<?>> isSuperClass = (a,b) -> {
			Class<?> expected = a;
			Class<?> actual   = b.getSuperclass();
			assertEquals( expected, actual,
					() -> String.format( "'%s' is not the superclass of '%s'", a.getSimpleName(), b.getSimpleName() ));
		};
		isInterface .accept( IREGION_VISITOR );

		isAbstract  .accept( REGION );
		isAbstract  .accept( CITY   );
		
		isSuperType .accept( IREGION_VISITOR, COUNT_REGION_VISITOR     );
		isSuperType .accept( IREGION_VISITOR, POPULATION_RANGE_VISITOR );

		isSuperClass.accept( OBJECT, REGION                   );
		isSuperClass.accept( REGION, STATE                    );
		isSuperClass.accept( REGION, CITY                     );
		isSuperClass.accept( OBJECT, MAP                      );
		isSuperClass.accept( OBJECT, COUNT_REGION_VISITOR     );
		isSuperClass.accept( OBJECT, POPULATION_RANGE_VISITOR );
		isSuperClass.accept( CITY,   SMALL_CITY               );
		isSuperClass.accept( CITY,   LARGE_CITY               );
	}
	@Test
	public void testClassesHaveFields() {
		ObjLongConsumer<Class<?>> hasFields = (c,expected) -> {
			long actual = Arrays.stream ( c.getDeclaredFields() )
				                           .filter ( f->!f.isSynthetic() )
				                           .collect( Collectors.counting() );
			assertEquals( expected, actual, 
					      ()->String.format( "Incorrect number of fields in class '%s'", c.getSimpleName() ));
		};
		hasFields.accept( REGION,     1L );
		hasFields.accept( STATE,      1L );
		hasFields.accept( CITY,       1L );
		hasFields.accept( SMALL_CITY, 0L );
		hasFields.accept( LARGE_CITY, 0L );
	}
	private static final String CITY_1 = "St. Petersburg";
	private static final String CITY_2 = "Orlando";
	private static final String CITY_3 = "Baton Rouge";
	private static final String CITY_4 = "Winston-Salem";
	private static final String CITY_5 = "San Francisco";

	private static final String STATE_1 = "VA";
	private static final String STATE_2 = "PA";
	private static final String STATE_3 = "NY";
	private static final String STATE_4 = "TX";
	
	private static final String ERROR_NO_CALL = "this method should not get called";

	private static final int    SMALL_LOWER = 0;
	private static final int    SMALL_UPPER = 250_000;
	private static final int    LARGE_LOWER = 250_001;
	private static final int    LARGE_UPPER = Integer.MAX_VALUE;
	@Nested
	class TestRegion {
		@Test
		void testNewRegion() {
			for (String expected : List.of( CITY_1, CITY_2, CITY_3 )) {
				var a = new Region( expected ) {
					@Override
					public void accept(IRegionVisitor visitor) {
						throw new IllegalAccessError( ERROR_NO_CALL );
					}
				};
				var actual = a.getName();
				assertEquals( expected, actual );
			}			
		}
		@Test
		void testBlankOrNullNameThrowsException() {
			for (var name : new String[]{ null,""," ","   ","         " }) {
				var t = assertThrows(
						IllegalArgumentException.class,
						() -> new Region( name ) {
							@Override
							public void accept(IRegionVisitor visitor) {
								throw new IllegalAccessError( ERROR_NO_CALL );
							}
						});
				assertEquals( String.format( "Name cannot be null nor blank ['%s']", name), t.getMessage() );
			}
		}		
	}
	@Nested
	class TestCity {
		@Test
		void testNewCity() {
			for (var name : List.of( CITY_1, CITY_2, CITY_4 ) ) {
				for (var pop : List.of( SMALL_LOWER, 42, LARGE_UPPER )) {
					var a = new City( name, pop ) {
						@Override
						public void accept(IRegionVisitor visitor) {
							throw new IllegalAccessError( ERROR_NO_CALL );
						}
					};
					assertEquals( name, a.getName() );
					assertEquals( pop,  a.getPopulation() );
				}
			}
		}
		@Test
		void testNegativePopulationThrowsException() {
			for (int pop : List.of( -1, -42, Integer.MIN_VALUE )) {
				var t = assertThrows(
						IllegalArgumentException.class,
						() -> new City( CITY_1, pop ) {
							@Override
							public void accept(IRegionVisitor visitor) {
								throw new IllegalAccessError( ERROR_NO_CALL );
							}							
						});
				assertEquals( String.format( "Population cannot be negative [%d]", pop ), t.getMessage() );
			}
		}
		@Test
		void testHashcode() {
			var a         = new SmallCity( CITY_1, SMALL_LOWER );
			var aexpected = Objects.hash ( CITY_1, SMALL_LOWER ); 
			var aactual   = a.hashCode();
			assertEquals( aexpected, aactual );
			
			var b         = new LargeCity( CITY_2, LARGE_UPPER );
			var bexpected = Objects.hash ( CITY_2, LARGE_UPPER ); 
			var bactual   = b.hashCode();
			assertEquals( bexpected, bactual );
			
			var c         = new Region( CITY_3 ) {
				@Override
				public void accept(IRegionVisitor visitor) {
					throw new IllegalAccessError( ERROR_NO_CALL );
				}				
			};
			var cexpected = Objects.hash ( CITY_3 ); 
			var cactual   = c.hashCode();
			assertEquals( cexpected, cactual );
		}
		@Test
		void testEquals() {
			/* Region */
			var x = new Region( CITY_1 ) {
				@Override
				public void accept(IRegionVisitor visitor) {
					throw new IllegalAccessError( ERROR_NO_CALL );
				}				
			};
			assertNotEquals( x, null );
			// reflexivity
			assertEquals( x, x );			

			/* City */
			var y = new Region( CITY_2 ) {
				@Override
				public void accept(IRegionVisitor visitor) {
					throw new IllegalAccessError( ERROR_NO_CALL );
				}				
			};
			assertNotEquals( y, null );
			// reflexivity
			assertEquals( y, y );			

			/* Small + Large */
			// null
			var a = new SmallCity( CITY_1, SMALL_LOWER );
			assertNotEquals( a, null );
			// reflexivity
			assertEquals( a, a );
			// symmetry
			//    equal state
			var b = new SmallCity( String.valueOf( CITY_1.toCharArray()), SMALL_LOWER ); 
			assertEquals( a, b );
			assertEquals( b, a );
			// symmetry
			//    == name, != population
			var c = new SmallCity( CITY_1, SMALL_UPPER );
			assertNotEquals( a, c );
			assertNotEquals( c, a );
			// symmetry
			//    != name, == population
			var d = new SmallCity( CITY_2, SMALL_LOWER );
			assertNotEquals( a, d );
			assertNotEquals( d, a );
			// symmetry
			//    != name, != population
			var e = new LargeCity( CITY_3, LARGE_LOWER );
			var f = new LargeCity( CITY_4, LARGE_UPPER );
			assertNotEquals( e, f );
			assertNotEquals( f, e );
			// symmetry
			//    subtype
			var g = new LargeCity( CITY_3, LARGE_LOWER ) {};
			assertNotEquals( e, g );
			// transitivity
			var h = new LargeCity( CITY_1, LARGE_LOWER );
			var i = new LargeCity( CITY_2, LARGE_LOWER );
			var j = new LargeCity( CITY_1, LARGE_UPPER );
			assertNotEquals( h, i );
			assertNotEquals( i, j );
		}
	}
	@Nested
	class TestSmallCity {
		@Test
		void testNewSmallCity() {
			for (int expected : List.of( SMALL_LOWER, 100_000, SMALL_UPPER )) {
				var a      = new SmallCity( CITY_2, expected );
				int actual = a.getPopulation();
				assertEquals( expected, actual );
			}
		}
		@Test
		void testVisitor() {
			var       v = Mockito.spy( new IRegionVisitor() {} );
			SmallCity a = new SmallCity( CITY_1, 100_000 );
			a.accept( v );
			Mockito.verify( v ).visit( a );
		}
		@Test
		void testPopulationNotInRangeThrowsException() {
			for (int pop : List.of( LARGE_LOWER, LARGE_UPPER )) {
				var t = assertThrows( IllegalArgumentException.class,
						() -> new SmallCity( CITY_2, pop ) {
							@Override
							public void accept(IRegionVisitor visitor) {
								throw new IllegalAccessError( ERROR_NO_CALL );
							}							
						});
				assertEquals( String.format( "Population not in range (%d,%d) [%d]", SMALL_LOWER, SMALL_UPPER, pop ), t.getMessage() );
			}
		}
	}
	@Nested
	class TestLargeCity {
		@Test
		void testNewLargeCity() {
			for (int expected : List.of( LARGE_LOWER, 500_000, LARGE_UPPER )) {
				var a      = new LargeCity( CITY_2, expected );
				int actual = a.getPopulation();
				assertEquals( expected, actual );
			}
		}
		@Test
		void testVisitor() {
			var       v = Mockito.spy( new IRegionVisitor() {} );
			LargeCity a = new LargeCity( CITY_1, 500_000 );
			a.accept( v );
			Mockito.verify( v ).visit( a );
		}
		@Test
		void testPopulationNotInRangeThrowsException() {
			for (int pop : List.of( SMALL_LOWER, SMALL_UPPER )) {
				var t = assertThrows( IllegalArgumentException.class,
						() -> new LargeCity( CITY_2, pop ) {
							@Override
							public void accept(IRegionVisitor visitor) {
								throw new IllegalAccessError( ERROR_NO_CALL );
							}							
						});
				assertEquals( String.format( "Population not in range (%d,%d) [%d]", LARGE_LOWER, LARGE_UPPER, pop ), t.getMessage() );
			}
		}
	}
	@Nested
	class TestState {
		@Test
		void testState() {
			var expected = STATE_1;
			var a        = new State( expected );
			var actual   = a.getName();
			assertEquals( expected, actual );
		}
		@Test
		void testAddNullCityThrowsException() {
			State a = new State( STATE_4 );
			var   t = assertThrows( IllegalArgumentException.class,
					() -> a.addCities( null, null ));
			assertEquals( "Cities cannot be null", t.getMessage() );
		}
		@Test
		void testVisitorWithNoCities() {
			var   v = Mockito.mock( IRegionVisitor.class );
			State a = new State( STATE_2 );
			a.accept( v );
			Mockito.verify( v ).visit( a );
		}
		@Test
		void testVisitorWithCities() {
			var        v = Mockito.mock( IRegionVisitor.class );
			State      s = new State    ( STATE_3 );
			SmallCity  a = new SmallCity( CITY_1, SMALL_LOWER );
			SmallCity  b = new SmallCity( CITY_2, SMALL_UPPER );
			LargeCity  c = new LargeCity( CITY_3, LARGE_UPPER );
			s.addCities( a, b, c );
			s.accept( v );
			Mockito.verify( v ).visit( s );
			Mockito.verify( v ).visit( a );
			Mockito.verify( v ).visit( b );
			Mockito.verify( v ).visit( c );
		}
	}
	@Nested
	class TestMap {
		@Test
		void testAddNullRegionThrowsException() {
			Map a = new Map();
			var t = assertThrows( IllegalArgumentException.class,
					() -> a.addRegions( null, null ));
			assertEquals( "Regions cannot be null", t.getMessage() );
		}
		@Test
		void testVisitorWithNoCities() {
			var v = Mockito.mock( IRegionVisitor.class );
			Map a = new Map();
			a.traverse( v );
			Mockito.verifyNoInteractions( v );
		}
		@Test
		void testVisitorWithRegions() {
			var       v = Mockito.mock( IRegionVisitor.class );
			Map       m = new Map();
			State     s = Mockito.spy( new State( STATE_4 ));
			SmallCity a = Mockito.spy( new SmallCity( CITY_1, SMALL_LOWER ) );
			SmallCity b = Mockito.spy( new SmallCity( CITY_2, SMALL_UPPER ));
			LargeCity c = Mockito.spy( new LargeCity( CITY_3, LARGE_UPPER ));
			s.addCities ( b );
			m.addRegions( a, s, c );
			m.traverse  ( v );
			Mockito.verify( a ).accept( v );
			Mockito.verify( b ).accept( v );
			Mockito.verify( c ).accept( v );
			Mockito.verify( s ).accept( v );
			
			Mockito.verify( v ).visit( a );
			Mockito.verify( v ).visit( s );
			Mockito.verify( v ).visit( b );
			Mockito.verify( v ).visit( c );
		}
	}
	@Nested
	class TestCountRegionVisitor {
		@Test
		void testNoRegions() {
			var v = Mockito.mock( CountRegionVisitor.class );
			var m = new Map();
			m.traverse( v );
			
			Mockito.verifyNoInteractions( v );
			
			assertEquals( 0, v.getStates() );
			assertEquals( 0, v.getCities() );
		}
		@Test
		void testOneStateNoCities() {
			var v = Mockito.spy( new CountRegionVisitor() );
			var m = new Map();
			var s = new State( STATE_1 );
			m.addRegions( s );
			m.traverse  ( v );
			
			Mockito.verify( v ).visit( s );
			Mockito.verifyNoMoreInteractions( v );
			
			assertEquals( 1, v.getStates() );
			assertEquals( 0, v.getCities() );
		}
		@Test
		void testNoStateOneCity() {
			var v = Mockito.spy( new CountRegionVisitor() );
			var m = new Map();
			var a = new SmallCity( CITY_1, 42_000 );
			m.addRegions( a );
			m.traverse  ( v );
			
			Mockito.verify( v ).visit( a );
			Mockito.verifyNoMoreInteractions( v );
			
			assertEquals( 0, v.getStates() );
			assertEquals( 1, v.getCities() );
		}
		@Test
		void testStateAndCity() {
			var v = Mockito.spy( new CountRegionVisitor() );
			var m = new Map();
			var s = new State    ( STATE_1 );
			var a = new SmallCity( CITY_1, 42_000 );
			s.addCities ( a );
			m.addRegions( s );
			m.traverse( v );
			
			Mockito.verify( v ).visit( s );
			Mockito.verify( v ).visit( a );
			Mockito.verifyNoMoreInteractions( v );
			
			assertEquals( 1, v.getStates() );
			assertEquals( 1, v.getCities() );
		}
		@Test
		void testStatesAndCities() {
			var v = Mockito.spy( new CountRegionVisitor() );
			var m = new Map();
			var s = new State    ( STATE_1 );
			var z = new State    ( STATE_2 );
			var a = new SmallCity( CITY_1,  42_000 );
			var b = new SmallCity( CITY_2, 142_000 );
			var c = new LargeCity( CITY_3, 420_000 );
			var d = new SmallCity( CITY_4,   4_200 );
			var e = new City     ( CITY_5,  77_000 ) {
				@Override
				public void accept(IRegionVisitor visitor) {
					visitor.visit( this );
				}
			};
			s.addCities ( a, c );
			z.addCities ( d );
			m.addRegions( b, s, e );
			m.addRegions( z );
			m.traverse( v );
			
			Mockito.verify( v ).visit( s );
			Mockito.verify( v ).visit( z );
			Mockito.verify( v ).visit( a );
			Mockito.verify( v ).visit( b );
			Mockito.verify( v ).visit( c );
			Mockito.verify( v ).visit( d );
			Mockito.verify( v ).visit( e );
			Mockito.verifyNoMoreInteractions( v );
			
			assertEquals( 2, v.getStates() );
			assertEquals( 4, v.getCities() );
		}
	}
	@Nested
	class TestPopulationRangeVisitor {
		@Test
		void testNegativeOrIncorrectRangeThrowsException() {
			// negative
			var a = assertThrows( IllegalArgumentException.class,
					() -> new PopulationRangeVisitor( Integer.MIN_VALUE, Integer.MAX_VALUE ));
			assertEquals( "Range must have positive values", a.getMessage() );

			var b = assertThrows( IllegalArgumentException.class,
					() -> new PopulationRangeVisitor( 42, -1 ));
			assertEquals( "Range must have positive values", b.getMessage() );
			// lower > upper
			var c = assertThrows( IllegalArgumentException.class,
					() -> new PopulationRangeVisitor( 42, 41 ));
			assertEquals( "Range must be incremental", c.getMessage() );

			var d = assertThrows( IllegalArgumentException.class,
					() -> new PopulationRangeVisitor( 11, 7 ));
			assertEquals( "Range must be incremental", d.getMessage() );
		}
		@Test
		void testNoRegions() {
			var v = Mockito.spy( new PopulationRangeVisitor( 11, 41 ));
			var m = new Map();
			m.traverse( v );
			
			Mockito.verifyNoInteractions( v );
			
			List<City> expected = List.of();
			List<City> actual   = v.getCities();
			assertEqualSets( expected, actual );
		}
		@Test
		void testOneStateNoCities() {
			var v = Mockito.spy( new PopulationRangeVisitor( 100_000, 500_000 ));
			var m = new Map();
			var s = new State( STATE_1 );
			m.addRegions( s );
			m.traverse  ( v );
			
			Mockito.verify( v ).visit( s );
			Mockito.verifyNoMoreInteractions( v );
			
			List<City> expected = List.of();
			List<City> actual   = v.getCities();
			assertEqualSets( expected, actual );
		}
		@Test
		void testStateAndCityOneInRange() {
			var v = Mockito.spy( new PopulationRangeVisitor( 42_000, 42_000 ));
			var m = new Map();
			var s = new State    ( STATE_1 );
			var a = new SmallCity( CITY_1, 42_000 );
			var b = new City     ( CITY_2, 42_000 ) {
				@Override
				public void accept(IRegionVisitor visitor) {
					visitor.visit( this );
				}				
			};
			s.addCities ( a );
			m.addRegions( s, b );
			m.traverse( v );
			
			Mockito.verify( v ).visit( s );
			Mockito.verify( v ).visit( a );
			Mockito.verify( v ).visit( b );
			Mockito.verifyNoMoreInteractions( v );
			
			List<City> expected = List.of( a );
			List<City> actual   = v.getCities();
			assertEqualSets( expected, actual );
		}
		@Test
		void testStateAndCityNoneInRange() {
			var v = Mockito.spy( new PopulationRangeVisitor( 45_000, 50_000 ));
			var m = new Map();
			var s = new State    ( STATE_1 );
			var a = new SmallCity( CITY_1, 42_000 );
			s.addCities ( a );
			m.addRegions( s );
			m.traverse( v );
			
			Mockito.verify( v ).visit( s );
			Mockito.verify( v ).visit( a );
			Mockito.verifyNoMoreInteractions( v );
			
			List<City> expected = List.of();
			List<City> actual   = v.getCities();
			assertEqualSets( expected, actual );
		}
		@Test
		void testStatesAndCitiesTwoInRange() {
			var v = Mockito.spy( new PopulationRangeVisitor( 100_000, 500_000 ));
			var m = new Map();
			var s = new State    ( STATE_1 );
			var z = new State    ( STATE_2 );
			var a = new SmallCity( CITY_1,  42_000 );
			var b = new SmallCity( CITY_2, 142_000 );
			var c = new LargeCity( CITY_3, 420_000 );
			var d = new SmallCity( CITY_4,   4_200 );
			s.addCities ( a, c );
			z.addCities ( d );
			m.addRegions( b, s );
			m.addRegions( z );
			m.traverse( v );
			
			Mockito.verify( v ).visit( s );
			Mockito.verify( v ).visit( z );
			Mockito.verify( v ).visit( a );
			Mockito.verify( v ).visit( b );
			Mockito.verify( v ).visit( c );
			Mockito.verify( v ).visit( d );
			Mockito.verifyNoMoreInteractions( v );
			
			List<City> expected = List.of( b, c );
			List<City> actual   = v.getCities();
			assertEqualSets( expected, actual );
		}
		@Test
		void testStatesAndCitiesAllRange() {
			var v = Mockito.spy( new PopulationRangeVisitor( 0, 500_000 ));
			var m = new Map();
			var s = new State    ( STATE_1 );
			var z = new State    ( STATE_2 );
			var a = new SmallCity( CITY_1,  42_000 );
			var b = new SmallCity( CITY_2, 142_000 );
			var c = new LargeCity( CITY_3, 420_000 );
			var d = new SmallCity( CITY_4,   4_200 );
			var e = new Region   ( CITY_5 ) {
				@Override
				public void accept(IRegionVisitor visitor) {
					visitor.visit( this );
				}				
			};
			s.addCities ( a, c );
			z.addCities ( d );
			m.addRegions( b, s );
			m.addRegions( z, e );
			m.traverse( v );
			
			Mockito.verify( v ).visit( s );
			Mockito.verify( v ).visit( z );
			Mockito.verify( v ).visit( a );
			Mockito.verify( v ).visit( b );
			Mockito.verify( v ).visit( c );
			Mockito.verify( v ).visit( d );
			Mockito.verify( v ).visit( e );
			Mockito.verifyNoMoreInteractions( v );
			
			List<City> expected = List.of( a, b, c, d );
			List<City> actual   = v.getCities();
			assertEqualSets( expected, actual );
		}
	}
}