package Adapter;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class IBankAccountsTest {
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
			fail( String.format( "Other results not found %s", aMap ));
		}
	}
	// -------------------------------------------
	@ParameterizedTest
	@MethodSource("providerBankNames")
	public void testBankNames(IBankAccounts adapter, String expected) {
		String actual = adapter.getName();
		assertEquals( expected, actual );
	}
	static Stream<Arguments> providerBankNames() {
	    return Stream.of(
	    		Arguments.of( new BofAAdapter( new BofA() ), "Bank of America" ),
	    		Arguments.of( new RBCAdapter ( new RBC()  ), "Royal Bank Of Canada" )
			);
	}
	// -------------------------------------------
	@ParameterizedTest
	@MethodSource("providerBankAccountsGetDelinquent")
	public void testBankAccountsGetDelinquent(IBankAccounts adapter, int threshold, Map<Integer, Integer> expected) {
		Map<Integer, Integer> actual = adapter.getDelinquent( threshold );
		assertEqualMaps( expected, actual );
	}
	static Stream<Arguments> providerBankAccountsGetDelinquent() {
		return Stream.of(
				Arguments.of( new BofAAdapter( new BofA() ), 500, 
						Map.ofEntries(
							Map.entry( 1, 500),
							Map.entry( 5,  20)
						)),
				Arguments.of( new RBCAdapter ( new RBC()  ), 360, 
						Map.ofEntries(
							Map.entry( 6, 350),
							Map.entry( 9,   0),
							Map.entry(11,  42)
						))
				);
	}
	// -------------------------------------------
	@ParameterizedTest
	@MethodSource("providerBankAccountsGetNames")
	public void testBankAccountsGetNames(IBankAccounts adapter, Set<Integer> numbers, Map<Integer,String> expected) {
		Map<Integer, String> actual = adapter.getNames( numbers );
		assertEqualMaps( expected, actual );
	}
	static Stream<Arguments> providerBankAccountsGetNames() {
		return Stream.of(
				Arguments.of( new BofAAdapter( new BofA() ), 
						Set.of( 1, 5, 6, 10, 12 ), 
						Map.ofEntries(
							Map.entry(  1, "Starbuck"),
							Map.entry(  5, "Number Six"),
							Map.entry(  6, "President Palmer"),
							Map.entry( 10, "Commander Adama"),
							Map.entry( 12, "President Palmer")
						)),
				Arguments.of( new RBCAdapter ( new RBC()  ),
						Set.of( 6, 9, 11 ), 
						Map.ofEntries(
							Map.entry( 6, "Peter Pan"),
							Map.entry( 9, "Captain Hook"),
							Map.entry(11, "Captain Hook")
						))
				);
	}
}
