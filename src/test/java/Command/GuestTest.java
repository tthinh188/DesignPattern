package Command;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import Command.Guest;

public class GuestTest {
	private Class<?> getClass(String name) {
		try {
			Package pkg  = getClass().getPackage();
			String  path = (pkg == null || pkg.getName().isEmpty()) ? "" : pkg.getName()+".";
			return Class.forName( path + name );
		} catch (ClassNotFoundException e) {
			fail( String.format( "Class '%s' doesn't exist", name ));
		}
		return null;
	}

	@Test
	void testAllFieldsPrivateNonStatic() {
		Class<?> iClass  = getClass("Guest");
		Field[]  iFields = iClass.getDeclaredFields();
		
		for (Field f : iFields) {
			if (!f.isSynthetic()) {
				assertTrue ( Modifier.isPrivate( f.getModifiers() ), () -> "Field \""+f.getName()+"\" should be private" );
				assertFalse( Modifier.isStatic ( f.getModifiers() ), () -> "Field \""+f.getName()+"\" can't be static" );
			}
		}
	}
	@Nested
	class TestNewObject {
		@Test
		void testNameNullOrBlank() {
			{
				Throwable exception = assertThrows( IllegalArgumentException.class, () -> new Guest( null ));
				assertEquals( "name cannot be null or blank", exception.getMessage(), "unexpected result" );
			}{
				for(String name : new String[] {""," ","   ","     ","                           "}) {
				    Throwable exception = assertThrows( IllegalArgumentException.class, () -> new Guest( name ));
				    assertEquals( "name cannot be null or blank", exception.getMessage(), "unexpected result" );
				}
			}
		}
		@Test
		void testGetName() {
			Guest a        = new Guest( "Helen" );
			String expected = "Helen";
			String actual   = a.getName();  
			assertEquals( expected, actual, "unexpected getName() result" );
		}
		@Test
		void testHasRSVP() {
			Guest  a      = new Guest( "Albert" );
			boolean actual = a.hasRSVP();  
			assertFalse( actual, "unexpected hasRSVP() result" );
		}
	}
	
	@Nested
	class TestRSVPAndCompanion {
		@Test
		void testRSVP() {
			Guest a = new Guest( "Bob" );
			assertFalse( a.hasRSVP(), "unexpected result" );
			a.setRSVP(true);
			assertTrue ( a.hasRSVP(), "unexpected result" );
			a.setRSVP(false);
			assertFalse( a.hasRSVP(), "unexpected result" );
		}
	}
	
	@Nested
	class TestEquals {
		@Test
		void testToNull() {
			Object one = new Guest( new String( "Evan" ));
			assertNotEquals( one, null,  "unexpected result: null" );
		}
		@Test
		void testReflectivity() {
			Object one = new Guest( new String( "Foo" ));
			assertEquals( one, one, "unexpected result: reflexivity" );
		}
		@Test
		void testSymmetryEqualState() {
			Object one = new Guest( new String( "Gerardo" ));
			Object two = new Guest( new String( "Gerardo" ));
			assertEquals( one, two, "unexpected result: symmetry [equal state]" );
		}
		@Test
		void testSymmetryDifferentState() {
			Object one = new Guest( new String( "Ximena" ));
			Object two = new Guest( new String( "Yoyo" ));
			assertNotEquals( one, two, "unexpected result: symmetry [different state]" );
		}
		@Test
		void testSymmetryDifferentType() {
			Object one = new Guest( new String( "Jude" ));
			assertNotEquals( one, "Jude", "unexpected result: symmetry [different type]" );
			assertNotEquals( one, Integer.valueOf( 1 ), "unexpected result: symmetry [different type]" );
		}
		@Test
		void testSymmetrySubclass() {
			Object one = new Guest( new String( "Kelvin" ));
			Object two = new Guest( new String( "Kelvin" )) { };
			assertNotEquals( one, two, "unexpected result: symmetry [subclass]" );
		}
	}
	
	@Nested
	class TestHashCode {
		@Test
		void testNewObject() {
			for (String name : List.of( "Lola", "Manuel", "Neville", "Oscar" )) {
				int expected  = Objects.hash ( name );
				int actual    = new Guest( name ).hashCode();
				assertEquals( expected, actual, "unexpected result" );
			}
		}
	}
	
	@Nested
	class TestToString {
		@Test
		void test0() {
			String expected = "Guest [name=Pedro,rsvp=no]";
			String actual   = new Guest( "Pedro" ).toString();
			assertEquals( expected, actual, "unexpected result" );
		}
		@Test
		void test1() {
			Guest a     = new Guest( "Quincy" );
			a.setRSVP(true);
			String expected = "Guest [name=Quincy,rsvp=yes]";
			String    actual   = a.toString();
			assertEquals( expected, actual, "unexpected result" );
		}
	}
}