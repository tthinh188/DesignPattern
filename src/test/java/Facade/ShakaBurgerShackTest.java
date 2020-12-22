package Facade;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ShakaBurgerShackTest {
	private <T> void assertEqualSets(Collection<T> expected, Collection<T> actual) {
		assertNotNull( actual, "Result was null" );
		Set<T> aSet = new HashSet<>( actual );
		Set<T> eSet = new HashSet<>( expected );
		for (T e : eSet) {
			if (aSet.isEmpty()) {
				fail( String.format( "Unexpected end of results: expected '%s' but results ended.", e ));
			} else {
				assertTrue( aSet.remove( e ), ()->String.format( "Expected entry not found: %s in %s", e, aSet ));
			}
		}
		if (!aSet.isEmpty()) {
			fail( String.format( "Other results not found %s", aSet ));
		}
	}
	@Test
	public void testItemReflection() {
		Class<?>   iClass  = Item.class;
		Class<?>   iParent = iClass.getSuperclass();
		assertEquals( "java.lang.Object", iParent.getName() );

		Class<?>[] iInterfaces = iClass.getInterfaces();
		if (iInterfaces.length > 0) {
			fail( "class '"+iClass.getSimpleName()+"' shouldn't implement an interface" );
		}			
		Field[]  iFields = iClass.getDeclaredFields();
		for (Field f : iFields) {
			if (!f.isSynthetic()) {
				int mods = f.getModifiers();
				assertTrue ( Modifier.isPrivate( mods ), ()->String.format("Field '%s' should be private", f.getName() ));
				if (Modifier.isStatic ( mods ) && !Modifier.isFinal( mods )) {
					fail( String.format("Field '%s' can be static if it's final", f.getName() ));
				}
			}
		}
	}
	@Test
	public void testTrayReflection() {
		Class<?>   iClass  = Tray.class;
		Class<?>   iParent = iClass.getSuperclass();
		assertEquals( "java.lang.Object", iParent.getName() );

		Class<?>[] iInterfaces = iClass.getInterfaces();
		if (iInterfaces.length > 0) {
			fail( "class '"+iClass.getSimpleName()+"' shouldn't implement an interface" );
		}			
		Field[]  iFields = iClass.getDeclaredFields();
		for (Field f : iFields) {
			if (!f.isSynthetic()) {
				int mods = f.getModifiers();
				assertTrue ( Modifier.isPrivate( mods ), ()->String.format("Field '%s' should be private", f.getName() ));
				if (Modifier.isStatic ( mods ) && !Modifier.isFinal( mods )) {
					fail( String.format("Field '%s' can be static if it's final", f.getName() ));
				}
			}
		}
	}
	@Test
	void testMorningEvening() {
		Restaurant a = new ShakaBurgerShack();
		assertTrue ( a.isMorning() );
		a.setMorning(false);
		assertFalse( a.isMorning() );
		a.setMorning(true);
		assertTrue ( a.isMorning() );
	}
	@Test
	void testHasPrivateInnerClasses() {
		Class<?>[]   inner    = ShakaBurgerShack.class.getDeclaredClasses();
		List<String> actual   = Stream.of( inner ).map( Class::getSimpleName ).collect(Collectors.toList() );
		List<String> expected = List.of("Lemonade","PaiaBurger","Pancake","RootBeer","WaileaCake","Yogurt");
		assertEqualSets( expected, actual );
		
		for (Class<?> i : inner) {
			int m = i.getModifiers();
			assertTrue( Modifier.isPrivate(m), ()->String.format( "inner class %s is not private", i.getSimpleName() ));
		}
	}
	@Nested
	class Morning {
		@Test
		void testDrink() {
			var a     = new ShakaBurgerShack();
			a.setMorning( true );
			var b     = a.getDrink();
			var items = b.getItems();
			assertEquals( 1, items.size() );
			var c     = items.get( 0 );
			assertEquals( "Lemonade ($1.50)", c.toString() );
		}		
		@Test
		void testMeal() {
			var a     = new ShakaBurgerShack();
			a.setMorning( true );
			var b     = a.getMeal();
			var items = b.getItems();
			assertEquals( 1, items.size() );
			var c     = items.get( 0 );
			assertEquals( "Pancake ($5.50)", c.toString() );
		}		
		@Test
		void testDessert() {
			var a     = new ShakaBurgerShack();
			a.setMorning( true );
			var b     = a.getDessert();
			var items = b.getItems();
			assertEquals( 1, items.size() );
			var c     = items.get( 0 );
			assertEquals( "Yogurt ($1.75)", c.toString() );
		}		
		@Test
		void testCombo() {
			var a      = new ShakaBurgerShack();
			a.setMorning( true );
			var b      = a.getCombo();
			var items  = b.getItems();
			assertEquals( 3, items.size() );
			assertEquals( 8.75, b.getPrice() );
			var actual = items.stream().map( Object::toString ).collect(Collectors.toList()); 
			assertEqualSets( List.of("Lemonade ($1.50)","Pancake ($5.50)","Yogurt ($1.75)"), actual );
		}
	}
	@Nested
	class Afternoon {
		@Test
		void testDrink() {
			var a     = new ShakaBurgerShack();
			a.setMorning( false );
			var b     = a.getDrink();
			var items = b.getItems();
			assertEquals( 1, items.size() );
			var c     = items.get( 0 );
			assertEquals( "RootBeer ($2.50)", c.toString() );
		}		
		@Test
		void testMeal() {
			var a     = new ShakaBurgerShack();
			a.setMorning( false );
			var b     = a.getMeal();
			var items = b.getItems();
			assertEquals( 1, items.size() );
			var c     = items.get( 0 );
			assertEquals( "PaiaBurger ($8.25)", c.toString() );
		}		
		@Test
		void testDessert() {
			var a     = new ShakaBurgerShack();
			a.setMorning( false );
			var b     = a.getDessert();
			var items = b.getItems();
			assertEquals( 1, items.size() );
			var c     = items.get( 0 );
			assertEquals( "WaileaCake ($3.75)", c.toString() );
		}
		@Test
		void testCombo() {
			var a     = new ShakaBurgerShack();
			a.setMorning( false );
			var b     = a.getCombo();
			var items = b.getItems();
			assertEquals( 3, items.size() );
			assertEquals( 14.5, b.getPrice() );
			List<String> actual = items.stream().map( Object::toString ).collect(Collectors.toList()); 
			assertEqualSets( List.of("RootBeer ($2.50)","PaiaBurger ($8.25)","WaileaCake ($3.75)"), actual );
		}		
		@Test
		void testTrayToString() {
			var a = new ShakaBurgerShack();
			{
				a.setMorning( false );
				var b      = a.getCombo();
				var actual = b.toString();
				var fail   = true;
				for (String expected : List.of(
						"RootBeer ($2.50),PaiaBurger ($8.25),WaileaCake ($3.75)", 
						"RootBeer ($2.50),WaileaCake ($3.75),PaiaBurger ($8.25)", 
						"PaiaBurger ($8.25),WaileaCake ($3.75),RootBeer ($2.50)", 
						"PaiaBurger ($8.25),RootBeer ($2.50),WaileaCake ($3.75)", 
						"WaileaCake ($3.75),RootBeer ($2.50),PaiaBurger ($8.25)",
						"WaileaCake ($3.75),PaiaBurger ($8.25),RootBeer ($2.50)")) {
					if (expected.equals( actual )) {
						fail = false;
						break;
					}
				}
				assertFalse( fail, "Tray.toString() failed:'" + actual +"'"  );
			}{
				a.setMorning( true );
				var b      = a.getCombo();
				var actual = b.toString();
				var fail   = true;
				for (String expected : List.of(
						"Lemonade ($1.50),Pancake ($5.50),Yogurt ($1.75)", 
						"Lemonade ($1.50),Yogurt ($1.75),Pancake ($5.50)", 
						"Yogurt ($1.75),Lemonade ($1.50),Pancake ($5.50)", 
						"Yogurt ($1.75),Pancake ($5.50),Lemonade ($1.50)", 
						"Lemonade ($1.50),Pancake ($5.50),Yogurt ($1.75)",
						"Lemonade ($1.50),Yogurt ($1.75),Pancake ($5.50)")) {
					if (expected.equals( actual )) {
						fail = false;
						break;
					}
				}
				assertFalse( fail, "Tray.toString() failed:'" + actual +"'"  );
			}
		}		
	}
}