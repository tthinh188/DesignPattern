package Facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.Point;
import java.util.Arrays;
import java.util.function.Consumer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EuclideanFacadeTest {
	@Nested
	class TestDistanceWithMockito {
		@Test
		void testAdditionWasCalled() {
			try (MockedStatic<Addition> add = Mockito.mockStatic( Addition.class )) {
				Verification addOf = () -> Addition.of( Mockito.anyDouble(), Mockito.anyDouble() );

		        Point one = new Point( 0, 0 );
		        Point two = new Point( 5, 5 );
		        EuclideanFacade.distance( one, two );

		        add.verify( addOf );
			}
		}
		@Test
		void testSubtractionWasCalled() {
			try (MockedStatic<Subtraction> subtract = Mockito.mockStatic( Subtraction.class )) {
				Verification subtractOf = () -> Subtraction.of( Mockito.anyDouble(), Mockito.anyDouble() );

		        Point one = new Point( 0, 0 );
		        Point two = new Point( 5, 5 );
		        EuclideanFacade.distance( one, two );

		        subtract.verify( Mockito.times( 2 ), subtractOf );
			}
		}
		@Test
		void testPowerWasCalled() {
			try (MockedStatic<Power> power = Mockito.mockStatic( Power.class )) {
				Verification powerOf = () -> Power.of( Mockito.anyDouble(), Mockito.anyDouble() );

		        Point one = new Point( 0, 0 );
		        Point two = new Point( 5, 5 );
		        EuclideanFacade.distance( one, two );

		        power.verify( Mockito.times( 2 ), powerOf );
			}
		}
		@Test
		void testSquareRootWasCalled() {
			try (MockedStatic<SquareRoot> squareRoot = Mockito.mockStatic( SquareRoot.class )) {
				Verification squareRootOf = () -> SquareRoot.of( Mockito.anyDouble() );

		        Point one = new Point( 0, 0 );
		        Point two = new Point( 5, 5 );
		        EuclideanFacade.distance( one, two );

		        squareRoot.verify( squareRootOf );
			}
		}
		@Test
		void testDistanceCalculatedUsingMethods() {
			try (MockedStatic<SquareRoot>  squareRoot = Mockito.mockStatic( SquareRoot.class );
				 MockedStatic<Addition>    addition   = Mockito.mockStatic( Addition.class );
				 MockedStatic<Subtraction> subtract   = Mockito.mockStatic( Subtraction.class );
				 MockedStatic<Power>       power      = Mockito.mockStatic( Power.class )) {
				Verification squareRootOf  = () -> SquareRoot .of( Mockito.anyDouble() );
				Verification subtractionOf = () -> Subtraction.of( Mockito.anyDouble(), Mockito.anyDouble() );
				Verification powerOf       = () -> Power      .of( Mockito.anyDouble(), Mockito.anyDouble() );
				Verification additionOf    = () -> Addition   .of( Mockito.anyDouble(), Mockito.anyDouble() );
				squareRoot.when( squareRootOf  ).thenReturn( 5.0   );
				power     .when( powerOf       ).thenReturn( 25d, 0d );
				subtract  .when( subtractionOf ).thenReturn(  5d, 0d );
				addition  .when( additionOf    ).thenReturn( 25d );

		        Point one = new Point( 0, 0 );
		        Point two = new Point( 0, 0 );
		        double actual   = EuclideanFacade.distance( one, two );
		        double expected = 5.0;
		        assertEquals( expected, actual, 0.01 );

		        squareRoot.verify(                     squareRootOf  );
		        power     .verify( Mockito.times( 2 ), powerOf       );
		        subtract  .verify( Mockito.times( 2 ), subtractionOf );
		        addition  .verify(                     additionOf    );
			}
		}
	}
	@Nested
	class TestDistance {
		@Test
		void test0() {
	        Point one = new Point( 0, 0 );
	        Point two = new Point( 0, 0 );
	        double actual   = EuclideanFacade.distance( one, two );
	        double expected = 0.0;
	        assertEquals( expected, actual, 0.01 );
		}
		@Test
		void test1() {
	        Point one = new Point( 5, 0 );
	        Point two = new Point( 0, 5 );
	        double actual   = EuclideanFacade.distance( one, two );
	        double expected = 7.0710678118654755;
	        assertEquals( expected, actual, 0.01 );
		}
		@Test
		void test2() {
	        Point one = new Point( 5,  4 );
	        Point two = new Point( 2, -1 );
	        double actual   = EuclideanFacade.distance( one, two );
	        double expected = 5.830951894845301;
	        assertEquals( expected, actual, 0.01 );
		}
		@Test
		void test3() {
	        Point one = new Point( -1,  2 );
	        Point two = new Point(  2, -1 );
	        double actual   = EuclideanFacade.distance( one, two );
	        double expected = 4.242640687119285;
	        assertEquals( expected, actual, 0.01 );
		}
		@Test
		void testNullParametersThrowExceptionPlusNoFields() {
			{
				var two = new Point(  2, -1 );
				var t   = assertThrows( IllegalArgumentException.class,
						() -> EuclideanFacade.distance( null, two ));
				assertEquals( "point one cannot be null", t.getMessage() );
			}{
				var one = new Point( -1,  2 );
				var t   = assertThrows( IllegalArgumentException.class,
						() -> EuclideanFacade.distance( one, null ));
				assertEquals( "point two cannot be null", t.getMessage() );
			}
			Consumer<Class<?>> fieldsNone   = 
					c -> Arrays.stream  ( c.getDeclaredFields() )
					           .filter  ( f->!f.isSynthetic() )
					           .anyMatch( f->fail       (       String.format("class '%s' should have no fields: found '%s'", c.getSimpleName(), f.getName() )));
			fieldsNone.accept( Addition       .class );
			fieldsNone.accept( Power          .class );
			fieldsNone.accept( SquareRoot     .class );
			fieldsNone.accept( Subtraction    .class );
			fieldsNone.accept( EuclideanFacade.class );
		}
	}
}