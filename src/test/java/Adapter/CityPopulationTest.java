package Adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;
import org.mockito.MockedStatic.Verification;
import org.mockito.Mockito;

class CityPopulationTest {
	private static final String STR_FORMAT          = "%s,%s,%d%n";
	private static final String URI_FORMAT          = "?city=%s&state=%s";

	private static final String STR_NEW_YORK_NY     = String.format( STR_FORMAT, "New York",    "NY", 8_336_697 );
	private static final String STR_LOS_ANGELES_CA  = String.format( STR_FORMAT, "Los Angeles", "CA", 3_857_799 );
	private static final String STR_ALBANY_NY       = String.format( STR_FORMAT, "Albany",      "NY",    97_904 );
	private static final String STR_ALBANY_GA       = String.format( STR_FORMAT, "Albany",      "GA",    77_431 );
	private static final String STR_ALBANY_OR       = String.format( STR_FORMAT, "Albany",      "OR",    51_322 );
	private static final String STR_AURORA_CO       = String.format( STR_FORMAT, "Aurora",      "CO",   339_030 );
	private static final String STR_AURORA_IL       = String.format( STR_FORMAT, "Aurora",      "IL",   199_932 );
	private static final String STR_BLOOMINGTON_MN  = String.format( STR_FORMAT, "Bloomington", "MN",    86_033 );
	private static final String STR_BLOOMINGTON_IN  = String.format( STR_FORMAT, "Bloomington", "IN",    81_963 );
	private static final String STR_BLOOMINGTON_IL  = String.format( STR_FORMAT, "Bloomington", "IL",    77_733 );
	private static final String STR_CHICAGO_IL      = String.format( STR_FORMAT, "Chicago",     "IL", 2_714_856 );
	private static final String STR_HOUSTON_TX      = String.format( STR_FORMAT, "Houston",     "TX", 2_160_821 );
	private static final String STR_PHILADELPHIA_PA = String.format( STR_FORMAT, "Philadelphia","PA", 1_547_607 );
	private static final String STR_PHOENIX_AZ      = String.format( STR_FORMAT, "Phoenix",     "AZ", 1_488_750 );
	private static final String STR_SAN_ANTONIO_TX  = String.format( STR_FORMAT, "San Antonio", "TX", 1_382_951 );
	private static final String STR_SAN_DIEGO_CA    = String.format( STR_FORMAT, "San Diego",   "CA", 1_338_348 );
	
	private static final String URI_NEW_YORK_NY     = String.format( URI_FORMAT, "new+york",    "ny" );
	private static final String URI_LOS_ANGELES_CA  = String.format( URI_FORMAT, "los+angeles", "ca" );
	private static final String URI_ALBANY_GA       = String.format( URI_FORMAT, "albany",      "ga" );
	private static final String URI_BLOOMINGTON_MN  = String.format( URI_FORMAT, "bloomington", "mn" );
	private static final String URI_BLOOMINGTON_IN  = String.format( URI_FORMAT, "bloomington", "in" );
	private static final String URI_PHOENIX_AZ      = String.format( URI_FORMAT, "phoenix",     "az" );
	private static final String URI_SAN_DIEGO_CA    = String.format( URI_FORMAT, "san+diego",   "ca" );

	@Test
	void testFields() {
		Consumer<Class<?>> fieldsNotStatic = 
				c -> Arrays.stream  ( c.getDeclaredFields() )
				           .filter  ( f->!f.isSynthetic() )
				           .forEach ( f->assertFalse( Modifier.isStatic ( f.getModifiers() ), 
				        		                      () -> String.format("field '%s.%s' is static",      c.getSimpleName(), f.getName() )));
		Consumer<Class<?>> fieldsPrivate   = 
				c -> Arrays.stream  ( c.getDeclaredFields() )
				           .filter  ( f->!f.isSynthetic() )
				           .forEach ( f->assertTrue ( Modifier.isPrivate( f.getModifiers() ), 
				        		                      () -> String.format("field '%s.%s' is not private", c.getSimpleName(), f.getName() )));
		Consumer<Class<?>> fieldsNone   = 
				c -> Arrays.stream  ( c.getDeclaredFields() )
				           .filter  ( f->!f.isSynthetic() )
				           .anyMatch( f->fail       (       String.format("class '%s' should have no fields: found '%s'", c.getSimpleName(), f.getName() )));
		fieldsNone     .accept( CityPopulationReader .class );
		fieldsPrivate  .accept( CityPopulationAdapter.class );
		fieldsNotStatic.accept( CityPopulationAdapter.class );
	}
	@Nested
	@ExtendWith(TempDirectory.class)
	class TestingReader {
		@Test
		void testInvalidURIThrowsException(@TempDir Path folder) throws URISyntaxException {
			var path = folder.resolve( "foo.txt" );
			// null uri
			var t = assertThrows(
					IllegalArgumentException.class, 
					() -> {
						CityPopulationReader.readPopulation( null );
					});
			assertEquals( "uri cannot be null", t.getMessage() );
			
			// missing query
			t = assertThrows( 
					IllegalArgumentException.class, 
					() -> {
						var str = String.format( "%s", path.toUri() );
						var uri = new URI( str );
						CityPopulationReader.readPopulation( uri );
					});
			assertEquals( "missing query", t.getMessage() );

			t = assertThrows( 
					IllegalArgumentException.class, 
					() -> {
						var str = String.format( "%s?", path.toUri() );
						var uri = new URI( str );
						CityPopulationReader.readPopulation( uri );
					});
			assertEquals( "missing query", t.getMessage() );

			// missing city
			t = assertThrows( 
					IllegalArgumentException.class, 
					() -> {
						var str = String.format( "%s?state=ny", path.toUri() );
						var uri = new URI( str );
						CityPopulationReader.readPopulation( uri );
					});
			assertEquals( "missing city", t.getMessage() );
			
			t = assertThrows( 
					IllegalArgumentException.class, 
					() -> {
						var str = String.format( "%s?state=ny&state=ca", path.toUri() );
						var uri = new URI( str );
						CityPopulationReader.readPopulation( uri );
					});
			assertEquals( "missing city", t.getMessage() );

			// missing state
			t = assertThrows( 
					IllegalArgumentException.class, 
					() -> {
						var str = String.format( "%s?city=new+york", path.toUri() );
						var uri = new URI( str );
						CityPopulationReader.readPopulation( uri );
					});
			assertEquals( "missing state", t.getMessage() );
			
			t = assertThrows( 
					IllegalArgumentException.class, 
					() -> {
						var str = String.format( "%s?city=los+angeles+city=new+york", path.toUri() );
						var uri = new URI( str );
						CityPopulationReader.readPopulation( uri );
					});
			assertEquals( "missing state", t.getMessage() );
		}
		@Test
		void testCityNotFound(@TempDir Path folder) throws IOException, URISyntaxException {
			// create file
			Path path = folder.resolve( "foo.txt" );
			// add initial input file content
			try (var write = new PrintWriter( path.toFile() )) {
				for (var s : List.of( STR_NEW_YORK_NY, STR_LOS_ANGELES_CA, STR_CHICAGO_IL )) {
					write.printf( s );
				}
			}
			// create uri + query
			var str = String.format( "%s%s", path.toUri(), URI_SAN_DIEGO_CA );
			var uri = new URI( str ); 
			
			// invoke program
			int actual   = CityPopulationReader.readPopulation( uri );
			int expected = -1;
			assertEquals( expected, actual );
		}
		@Test
		void testReadingOnce(@TempDir Path folder) throws IOException, URISyntaxException {
			// create file
			Path path = folder.resolve( "foo.txt" );
			// add initial input file content
			try (var write = new PrintWriter( path.toFile() )) {
				write.printf( STR_NEW_YORK_NY );
			}
			// create uri + query
			var str = String.format( "%s%s", path.toUri(), URI_NEW_YORK_NY );
			var uri = new URI( str ); 
			
			// invoke program
			int actual   = CityPopulationReader.readPopulation( uri );
			int expected = 8_336_697;
			assertEquals( expected, actual );
		}
		@Test
		void testReadingTwice(@TempDir Path folder) throws IOException, URISyntaxException {
			// create file
			Path path = folder.resolve( "foo.txt" );
			{
				// add file content
				try (var write = new PrintWriter( path.toFile() )) {
					for (var s : List.of( STR_HOUSTON_TX, STR_PHILADELPHIA_PA, STR_PHOENIX_AZ, STR_SAN_ANTONIO_TX )) {
						write.printf( s );
					}
				}
				// create uri + query
				var str = String.format( "%s%s", path.toUri(), URI_PHOENIX_AZ );
				var uri = new URI( str ); 

				// invoke program
				int actual   = CityPopulationReader.readPopulation( uri );
				int expected = 1_488_750;
				assertEquals( expected, actual );
			}{
				// add file content
				try (var write = new PrintWriter( path.toFile() )) {
					for (var s : List.of( STR_ALBANY_NY, STR_ALBANY_GA, STR_ALBANY_OR, STR_AURORA_CO, STR_AURORA_IL )) {
						write.printf( s );
					}
				}
				// create uri + query
				var str = String.format( "%s%s", path.toUri(), URI_ALBANY_GA );
				var uri = new URI( str ); 
				
				// invoke program
				int actual   = CityPopulationReader.readPopulation( uri );
				int expected = 77_431;
				assertEquals( expected, actual );
			}
		}
		@Test
		void testReadingThrice(@TempDir Path folder) throws IOException, URISyntaxException {
			// create file
			Path path = folder.resolve( "foo.txt" );
			{
				// add file content
				try (var write = new PrintWriter( path.toFile() )) {
					for (var s : List.of( STR_BLOOMINGTON_MN, STR_BLOOMINGTON_IN, STR_BLOOMINGTON_IL, STR_SAN_DIEGO_CA )) {
						write.printf( s );
					}
				}
				// create uri + query
				var str = String.format( "%s%s", path.toUri(), URI_BLOOMINGTON_IN );
				var uri = new URI( str ); 

				// invoke program
				int actual   = CityPopulationReader.readPopulation( uri );
				int expected = 81_963;
				assertEquals( expected, actual );
			}{
				// add file content
				try (var write = new PrintWriter( path.toFile() )) {
					for (var s : List.of( STR_PHOENIX_AZ, STR_SAN_ANTONIO_TX )) {
						write.printf( s );
					}
				}
				// create uri + query
				var str = String.format( "%s%s", path.toUri(), URI_BLOOMINGTON_MN );
				var uri = new URI( str ); 
				
				// invoke program
				int actual   = CityPopulationReader.readPopulation( uri );
				int expected = -1;
				assertEquals( expected, actual );
			}{
				// add file content
				try (var write = new PrintWriter( path.toFile() )) {
					for (var s : List.of( STR_NEW_YORK_NY, STR_LOS_ANGELES_CA, STR_ALBANY_NY, STR_ALBANY_GA, STR_ALBANY_OR,
							STR_AURORA_CO, STR_AURORA_IL, STR_BLOOMINGTON_MN, STR_BLOOMINGTON_IN, STR_BLOOMINGTON_IL, 
							STR_CHICAGO_IL, STR_HOUSTON_TX, STR_PHILADELPHIA_PA, STR_PHOENIX_AZ, STR_SAN_ANTONIO_TX, STR_SAN_DIEGO_CA )) {
						write.printf( s );
					}
				}
				// create uri + query
				var str = String.format( "%s%s", path.toUri(), URI_LOS_ANGELES_CA );
				var uri = new URI( str ); 
				
				// invoke program
				int actual   = CityPopulationReader.readPopulation( uri );
				int expected = 3_857_799;
				assertEquals( expected, actual );
			}
		}
	}
	@Nested
	@ExtendWith(TempDirectory.class)
	class TestingAdapter {
		@Test
		void testNullParametersThrowsException(@TempDir Path folder) throws URISyntaxException {
			var path    = folder.resolve( "bar.txt" );
			var adapter = new CityPopulationAdapter( path.toFile() );
			// null parameters
			var t = assertThrows(
					IllegalArgumentException.class, 
					() -> {
						new CityPopulationAdapter( null );
					});
			assertEquals( "file cannot be null", t.getMessage() );

			t = assertThrows(
					IllegalArgumentException.class, 
					() -> {
						adapter.getPopulation( null, "va" );
					});
			assertEquals( "city cannot be null", t.getMessage() );
			
			t = assertThrows(
					IllegalArgumentException.class, 
					() -> {
						adapter.getPopulation( "albany", null );
					});
			assertEquals( "state cannot be null", t.getMessage() );			
		}
		@Test
		void testAdapterCallsReader(@TempDir Path folder) throws IOException, URISyntaxException {
			try (var reader = Mockito.mockStatic( CityPopulationReader.class )) {
				// init file, adapter, uri & mock
				var path    = folder.resolve( "bar.txt" );
				var adapter = new CityPopulationAdapter( path.toFile() );
				var str     = String.format( "%s%s", path.toUri(), URI_ALBANY_GA );
				var uri     = new URI( str ); 

				Verification readUri = () -> CityPopulationReader.readPopulation( uri );
				reader.when( readUri ).thenReturn( -1 );

				// invoke adapter
				int actual   = adapter.getPopulation( "albany", "ga" );
				int expected = -1;
				assertEquals( expected, actual );
				
				reader.verify( readUri );
			}
		}
		@Test
		void testAdapterCallsReaderOnceCachesNext(@TempDir Path folder) throws IOException, URISyntaxException {
			try (var reader = Mockito.mockStatic( CityPopulationReader.class )) {
				// init file, adapter, uri & mock
				var path    = folder.resolve( "bar.txt" );
				var adapter = new CityPopulationAdapter( path.toFile() );
				var str     = String.format( "%s%s", path.toUri(), URI_ALBANY_GA );
				var uri     = new URI( str ); 

				Verification readUri = () -> CityPopulationReader.readPopulation( uri );
				reader.when( readUri ).thenReturn( 42_000 );

				// invoke adapter (first time: calls reader & caches result)
				int actual   = adapter.getPopulation( "albany", "ga" );
				int expected = 42_000;
				assertEquals( expected, actual );
				
				reader.verify( readUri );

				// invoke adapter (second time: does not call reader, returns cached result)
				actual       = adapter.getPopulation( "Albany", "GA" );
				expected     = 42_000;
				assertEquals( expected, actual );
				
				reader.verifyNoMoreInteractions();
			}
		}
		@Test
		void testFlyingSoloTake0(@TempDir Path folder) throws IOException, URISyntaxException {
			try (var reader = Mockito.mockStatic( CityPopulationReader.class )) {
				// init file, adapter, uri & mock
				var path    = folder.resolve( "foo.txt" );
				var adapter = new CityPopulationAdapter( path.toFile() );

				Verification readUri = () -> CityPopulationReader.readPopulation( Mockito.any() );
				reader.when( readUri ).thenReturn( 42_000, 42, 4_200, -1, 420 );

				// 1) adapter calls reader & caches result
				int actual   = adapter.getPopulation( "Los Angeles", "CA" );
				int expected = 42_000;
				assertEquals( expected, actual );
				
				reader.verify( Mockito.times( 1 ), readUri );

				// 2) adapter calls reader & caches result
				actual   = adapter.getPopulation( "New York", "NY" );
				expected = 42;
				assertEquals( expected, actual );
				
				reader.verify( Mockito.times( 2 ), readUri );

				// 3) adapter does not call reader, returns cached value
				actual   = adapter.getPopulation( "los angeles", "ca" );
				expected = 42_000;
				assertEquals( expected, actual );
				
				reader.verify( Mockito.times( 2 ), readUri );

				// 4) adapter calls reader & caches result
				actual   = adapter.getPopulation( "los alamos", "nm" );
				expected = 4_200;
				assertEquals( expected, actual );
				
				reader.verify( Mockito.times( 3 ), readUri );

				// 5) adapter calls reader, does not cache result
				actual       = adapter.getPopulation( "new york", "ny" );
				expected     = 42;
				assertEquals( expected, actual );
				
				reader.verifyNoMoreInteractions();
			}
		}
		@Test
		void testFlyingSoloTake1(@TempDir Path folder) throws IOException, URISyntaxException {
			// init file & adapter
			var path    = folder.resolve( "bar.txt" );
			var adapter = new CityPopulationAdapter( path.toFile() );

			// add file content
			try (var write = new PrintWriter( path.toFile() )) {
				for (var s : List.of( STR_NEW_YORK_NY, STR_LOS_ANGELES_CA, STR_ALBANY_NY, STR_ALBANY_GA, STR_ALBANY_OR,
						STR_AURORA_CO, STR_AURORA_IL, STR_BLOOMINGTON_MN, STR_BLOOMINGTON_IN, STR_BLOOMINGTON_IL, 
						STR_CHICAGO_IL, STR_HOUSTON_TX, STR_PHILADELPHIA_PA, STR_PHOENIX_AZ, STR_SAN_ANTONIO_TX, STR_SAN_DIEGO_CA )) {
					write.printf( s );
				}
			}
			// invoke adapter (first time: calls reader & caches result)
			int actual   = adapter.getPopulation( "Houston", "TX" );
			int expected = 2_160_821;
			assertEquals( expected, actual );
		}
		@Test
		void testFlyingSoloTake2(@TempDir Path folder) throws IOException, URISyntaxException {
			// init file & adapter
			var path    = folder.resolve( "foo.txt" );
			var adapter = new CityPopulationAdapter( path.toFile() );

			// add file content
			try (var write = new PrintWriter( path.toFile() )) {
				for (var s : List.of( STR_NEW_YORK_NY, STR_LOS_ANGELES_CA, STR_ALBANY_NY, STR_ALBANY_GA, STR_ALBANY_OR,
						STR_AURORA_CO, STR_AURORA_IL, STR_BLOOMINGTON_MN, STR_BLOOMINGTON_IN, STR_BLOOMINGTON_IL, 
						STR_CHICAGO_IL, STR_HOUSTON_TX, STR_PHILADELPHIA_PA, STR_PHOENIX_AZ, STR_SAN_ANTONIO_TX, STR_SAN_DIEGO_CA )) {
					write.printf( s );
				}
			}
			// invoke adapter (first time: calls reader & caches result)
			int actual   = adapter.getPopulation( "Houston", "TX" );
			int expected = 2_160_821;
			assertEquals( expected, actual );

			// wipe file content
			try (var write = new PrintWriter( path.toFile() )) {
			}
			// invoke adapter (second time: does not call reader, returns cached result)
			actual   = adapter.getPopulation( "Houston", "TX" );
			expected = 2_160_821;
			assertEquals( expected, actual );
		}
		@Test
		void testFlyingSoloTake3(@TempDir Path folder) throws IOException, URISyntaxException {
			// init file & adapter
			var path    = folder.resolve( "foo.txt" );
			var adapter = new CityPopulationAdapter( path.toFile() );

			// add file content
			try (var write = new PrintWriter( path.toFile() )) {
				for (var s : List.of( STR_PHOENIX_AZ, STR_SAN_ANTONIO_TX, STR_SAN_DIEGO_CA )) {
					write.printf( s );
				}
			}
			// invoke adapter (first time: calls reader & nothing to cache)
			int actual   = adapter.getPopulation( "Philadelphia", "PA" );
			int expected = -1;
			assertEquals( expected, actual );

			// reset file content
			try (var write = new PrintWriter( path.toFile() )) {
				for (var s : List.of( STR_PHOENIX_AZ, STR_SAN_ANTONIO_TX, STR_PHILADELPHIA_PA, STR_SAN_DIEGO_CA )) {
					write.printf( s );
				}
			}
			// invoke adapter (second time: calls reader, caches result)
			actual   = adapter.getPopulation( "Philadelphia", "PA" );
			expected = 1_547_607;
			assertEquals( expected, actual );
		}
		@Test
		void testFlyingSoloTake4(@TempDir Path folder) throws IOException, URISyntaxException {
			// init file & adapter
			var path    = folder.resolve( "bar.txt" );
			var adapter = new CityPopulationAdapter( path.toFile() );

			// add file content
			try (var write = new PrintWriter( path.toFile() )) {
				for (var s : List.of( STR_NEW_YORK_NY, STR_LOS_ANGELES_CA, STR_ALBANY_NY, STR_ALBANY_GA, STR_ALBANY_OR,
						STR_AURORA_CO, STR_AURORA_IL, STR_BLOOMINGTON_MN, STR_BLOOMINGTON_IN, STR_BLOOMINGTON_IL, 
						STR_CHICAGO_IL, STR_HOUSTON_TX, STR_PHILADELPHIA_PA, STR_PHOENIX_AZ, STR_SAN_ANTONIO_TX, STR_SAN_DIEGO_CA )) {
					write.printf( s );
				}
			}
			// invoke adapter
			int actual   = adapter.getPopulation( "Newport News", "VA" );
			int expected = -1;
			assertEquals( expected, actual );

			// invoke adapter
			actual   = adapter.getPopulation( "San Antonio", "TX" );
			expected = 1_382_951;
			assertEquals( expected, actual );

			// invoke adapter
			actual   = adapter.getPopulation( "San Diego", "CA" );
			expected = 1_338_348;
			assertEquals( expected, actual );

			// invoke adapter
			actual   = adapter.getPopulation( "San Antonio", "TX" );
			expected = 1_382_951;
			assertEquals( expected, actual );
		}
	}
}