package Command;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import Command.Gala;
import Command.Guest;

public class GalaTest {
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
	private <T> void assertListEqualsUnordered(List<T> expected, List<T> actual) {
		assertNotNull( actual, "actual was null" );
		List<T> aList = new ArrayList<>( actual );
		List<T> eList = new ArrayList<>( expected );
		while (!eList.isEmpty()) {
			T e = eList.remove(0);
			if (aList.isEmpty()) {
				String msg = String.format( 
						"missing results %s%n"+
								"expected %s%n"+
								"actual %s%n",
								eList,
								expected,
								actual );
				fail( msg );
			} else {
				String msg = String.format(
						"'%s' not found.%n"+
								"expected %s%n"+
								"actual %s%n" +
								"remaining expected %s%n"+
								"remaining actual %s",
								e,
								expected,
								actual,
								eList,
								aList );
				assertTrue( aList.remove( e ), msg );
			}
		}
		if (!aList.isEmpty()) {
			String msg = String.format(
					"excess results %s%n"+
							"expected %s%n"+
							"actual %s",
							aList,
							expected,
							actual );
			fail( msg );
		}
	}

	@Test
	void testFieldsPrivateNonStatic() {
		Class<?> iClass  = getClass("Gala");
		Field[]  iFields = iClass.getDeclaredFields();
		
		for (Field f : iFields) {
			if (!f.isSynthetic()) {
				assertTrue ( Modifier.isPrivate( f.getModifiers() ), () -> "Field \""+f.getName()+"\" should be private" );
				assertFalse( Modifier.isStatic ( f.getModifiers() ), () -> "Field \""+f.getName()+"\" can't be static" );
			}
		}
	}
	@Test
	void testFieldsArePrivateNonStatic() {
		{
			String   name = "Gala";
			Class<?> c    = GalaTest.this.getClass( name );
			Arrays.stream (c.getFields())
                  .filter (f->!f.isSynthetic())
                  .forEach(f->{
                     int mods = f.getModifiers();
                     assertTrue ( Modifier.isPrivate( mods ), ()->String.format( "'%s.%s' can't be private", name, f.getName()) );
                     assertFalse( Modifier.isStatic ( mods ), ()->String.format( "'%s.%s' can't be static",  name, f.getName()) );
			});
		}
		for (String s : new String[] {"Gala$Add","Gala$Delete","Gala$RSVP"}) {
			Class<?> c = GalaTest.this.getClass( s );
	    	int      m = c.getModifiers();
	    	assertTrue ( Modifier.isFinal( m ), ()->String.format( "'%s' must be final", s ));
			Arrays.stream (c.getFields())
			      .filter (f->!f.isSynthetic())
			      .forEach(f->{
			    	  int mods = f.getModifiers();
			    	  assertTrue ( Modifier.isPrivate( mods ), ()->String.format( "'%s.%s' can't be private", s, f.getName()) );
			    	  assertFalse( Modifier.isStatic ( mods ), ()->String.format( "'%s.%s' can't be static",  s, f.getName()) );
			      });
		}
	}
	@Test
	void testNewGala() {
		Gala  gala = new Gala();
		Guest a    = new Guest("Brad Pitt");
		
		assertFalse ( gala.hasGuest( a )  );

		assertListEqualsUnordered( List.of(), gala.getGuests()  );
		assertListEqualsUnordered( List.of(), gala.getPending() );
		assertListEqualsUnordered( List.of(), gala.getRSVP() );

		assertEquals( 0, gala.redoSize() );
		assertEquals( 0, gala.undoSize() );
	}
	@Test
	void testOneGuest() {
		Gala  gala = new Gala();
		Guest a    = new Guest("Carlos Santana");

		gala.execute( gala.new Add( a ));

		assertTrue( gala.hasGuest( a )  );

		assertListEqualsUnordered( List.of( a ), gala.getGuests()  );
		assertListEqualsUnordered( List.of( a ), gala.getPending() );
		assertListEqualsUnordered( List.of(   ), gala.getRSVP() );
		
		assertEquals( 0, gala.redoSize() );
		assertEquals( 1, gala.undoSize() );
	}
	@Test
	void testOneRSPV() {
		Gala  gala = new Gala();
		Guest a    = new Guest("Ric Ocasek");

		gala.execute( gala.new Add ( a ));
		gala.execute( gala.new RSVP( a, true ));

		assertTrue( gala.hasGuest( a )  );

		assertListEqualsUnordered( List.of( a ), gala.getGuests()  );
		assertListEqualsUnordered( List.of(   ), gala.getPending() );
		assertListEqualsUnordered( List.of( a ), gala.getRSVP() );
		
		assertEquals( 0, gala.redoSize() );
		assertEquals( 2, gala.undoSize() );
	}
	@Test
	void testOneDelete() {
		Gala  gala = new Gala();
		Guest a    = new Guest("Eddie Van Halen");
		// add
		gala.execute( gala.new Add( a ));

		assertTrue  ( gala.hasGuest( a )  );

		assertListEqualsUnordered( List.of( a ), gala.getGuests()  );
		assertListEqualsUnordered( List.of( a ), gala.getPending() );
		assertListEqualsUnordered( List.of(   ), gala.getRSVP() );
		
		assertEquals( 0, gala.redoSize() );
		assertEquals( 1, gala.undoSize() );
		// delete
		gala.execute( gala.new Delete( a ));

		assertFalse ( gala.hasGuest( a )  );

		assertListEqualsUnordered( List.of(   ), gala.getGuests()  );
		assertListEqualsUnordered( List.of(   ), gala.getPending() );
		assertListEqualsUnordered( List.of(   ), gala.getRSVP() );
		
		assertEquals( 0, gala.redoSize() );
		assertEquals( 2, gala.undoSize() );
	}
	@Test
	void testRedoUndoOneGuest() {
		Gala  gala = new Gala();
		Guest a    = new Guest("Paul McCartney");

		gala.execute( gala.new Add   ( a ));
		gala.execute( gala.new RSVP  ( a, true ));
		gala.execute( gala.new Delete( a ));

		assertFalse ( gala.hasGuest( a )  );

		assertListEqualsUnordered( List.of(   ), gala.getGuests()  );
		assertListEqualsUnordered( List.of(   ), gala.getPending() );
		assertListEqualsUnordered( List.of(   ), gala.getRSVP() );
		
		assertEquals( 0, gala.redoSize() );
		assertEquals( 3, gala.undoSize() );
		// undo (from deleted to RSVPed)
		gala.undo();

		assertTrue  ( gala.hasGuest( a )  );
		
		assertEquals( 1, gala.redoSize() );
		assertEquals( 2, gala.undoSize() );

		assertListEqualsUnordered( List.of( a ), gala.getGuests()  );
		assertListEqualsUnordered( List.of(   ), gala.getPending() );
		assertListEqualsUnordered( List.of( a ), gala.getRSVP() );
		// undo (from RSVPed to invited)
		gala.undo();

		assertTrue  ( gala.hasGuest( a )  );
		
		assertEquals( 2, gala.redoSize() );
		assertEquals( 1, gala.undoSize() );

		assertListEqualsUnordered( List.of( a ), gala.getGuests()  );
		assertListEqualsUnordered( List.of( a ), gala.getPending() );
		assertListEqualsUnordered( List.of(   ), gala.getRSVP() );
		// undo (from invited to not)
		gala.undo();

		assertFalse ( gala.hasGuest( a )  );
		
		assertEquals( 3, gala.redoSize() );
		assertEquals( 0, gala.undoSize() );

		assertListEqualsUnordered( List.of(   ), gala.getGuests()  );
		assertListEqualsUnordered( List.of(   ), gala.getPending() );
		assertListEqualsUnordered( List.of(   ), gala.getRSVP() );
		// redo (from not to invited, to RSVPed)
		gala.redo();
		gala.redo();

		assertTrue  ( gala.hasGuest( a )  );
		
		assertEquals( 1, gala.redoSize() );
		assertEquals( 2, gala.undoSize() );

		assertListEqualsUnordered( List.of( a ), gala.getGuests()  );
		assertListEqualsUnordered( List.of(   ), gala.getPending() );
		assertListEqualsUnordered( List.of( a ), gala.getRSVP() );
	}
	@Test
	void testRedoUndoExecute() {
		Gala  gala = new Gala();
		Guest a    = new Guest("Shakira");
		Guest b    = new Guest("Maradona");
		Guest c    = new Guest("Pele");

		gala.execute( gala.new Add( a ));
		gala.execute( gala.new Add( b ));

		assertTrue  ( gala.hasGuest( a )  );
		assertTrue  ( gala.hasGuest( b )  );
		
		assertListEqualsUnordered( List.of( a, b ), gala.getGuests()  );
		assertListEqualsUnordered( List.of( a, b ), gala.getPending() );
		assertListEqualsUnordered( List.of(      ), gala.getRSVP() );
		
		assertEquals( 0, gala.redoSize() );
		assertEquals( 2, gala.undoSize() );

		gala.undo();
		
		assertEquals( 1, gala.redoSize() );
		assertEquals( 1, gala.undoSize() );

		gala.execute( gala.new Add( c ));

		assertListEqualsUnordered( List.of( a, c ), gala.getGuests()  );
		assertListEqualsUnordered( List.of( a, c ), gala.getPending() );
		assertListEqualsUnordered( List.of(      ), gala.getRSVP() );
		
		assertEquals( 0, gala.redoSize() );
		assertEquals( 2, gala.undoSize() );
	}
	@Test
	void testUndoRedoSeveralGuests() {
		Gala  gala = new Gala();
		Guest a    = new Guest("Cristian Castro");
		Guest b    = new Guest("Paul McCartney");
		Guest c    = new Guest("Ricardo Arjona");
		Guest d    = new Guest("David Grohl");
		// add a, b(RSPV), c
		gala.execute( gala.new Add   ( a ));
		gala.execute( gala.new Add   ( b ));
		gala.execute( gala.new RSVP  ( b, true ));
		gala.execute( gala.new Add   ( c ));
		gala.execute( gala.new RSVP  ( c, true ));
		gala.execute( gala.new Add   ( d ));
		gala.execute( gala.new RSVP  ( c, false ));

		assertListEqualsUnordered( List.of( a, b, c, d ), gala.getGuests()  );
		assertListEqualsUnordered( List.of( a,    c, d ), gala.getPending() );
		assertListEqualsUnordered( List.of(    b       ), gala.getRSVP() );
		
		assertEquals( 0, gala.redoSize() );
		assertEquals( 7, gala.undoSize() );
		// undo 3 times
		gala.undo();
		gala.undo();
		gala.undo();
		
		assertListEqualsUnordered( List.of( a, b, c ), gala.getGuests()  );
		assertListEqualsUnordered( List.of( a,    c ), gala.getPending() );
		assertListEqualsUnordered( List.of(    b    ), gala.getRSVP() );
		
		assertEquals( 3, gala.redoSize() );
		assertEquals( 4, gala.undoSize() );
		// undo 2 times
		gala.undo();
		gala.undo();
		
		assertListEqualsUnordered( List.of( a, b ), gala.getGuests()  );
		assertListEqualsUnordered( List.of( a, b ), gala.getPending() );
		assertListEqualsUnordered( List.of(      ), gala.getRSVP() );
		
		assertEquals( 5, gala.redoSize() );
		assertEquals( 2, gala.undoSize() );
		// execute command
		gala.execute( gala.new RSVP( a, true ));
		
		assertListEqualsUnordered( List.of( a, b ), gala.getGuests()  );
		assertListEqualsUnordered( List.of(    b ), gala.getPending() );
		assertListEqualsUnordered( List.of( a    ), gala.getRSVP() );
		
		assertEquals( 0, gala.redoSize() );
		assertEquals( 3, gala.undoSize() );
	}
	@Test
	void testGetListsReturnCopies() {
		Gala  gala = new Gala();
		Guest a    = new Guest("Jeff Porcaro");
		Guest b    = new Guest("John Bonham");
		Guest c    = new Guest("David Garibaldi");
		// add a, b(RSPV), c
		gala.execute( gala.new Add ( a ));
		gala.execute( gala.new Add ( b ));
		gala.execute( gala.new RSVP( a, true ));
		gala.execute( gala.new Add ( c ));
		gala.execute( gala.new RSVP( c, true ));

		gala.getGuests() .clear();
		gala.getPending().clear();
		gala.getRSVP()   .clear();

		assertListEqualsUnordered( List.of( a, b, c ), gala.getGuests()  );
		assertListEqualsUnordered( List.of(    b    ), gala.getPending() );
		assertListEqualsUnordered( List.of( a,    c ), gala.getRSVP() );
	}
	@Test
	void testExceptionAddExisting() {
		Gala  gala = new Gala();
		Guest a    = new Guest("Geddy Lee");
		Guest b    = new Guest("Neil Peart");
		Guest c    = new Guest("Alex Lifeson");

		gala.execute( gala.new Add( a ));
		gala.execute( gala.new Add( b ));
		gala.execute( gala.new Add( c ));
		
		var       add = gala.new Add ( b );
		Throwable t   = assertThrows( IllegalStateException.class,
				() -> gala.execute( add ));
		assertEquals( "guest exists already", t.getMessage() );
	}
	@Test
	void testExceptionDeleteNonExisting() {
		Gala  gala = new Gala();
		Guest a    = new Guest("Geddy Lee");
		Guest b    = new Guest("Neil Peart");
		Guest c    = new Guest("Alex Lifeson");

		gala.execute( gala.new Add( a ));
		gala.execute( gala.new Add( b ));
		
		var       del = gala.new Delete( c );
		Throwable t   = assertThrows( IllegalStateException.class,
				() -> gala.execute( del ));
		assertEquals( "guest doesn't exist", t.getMessage() );
	}
	@Test
	void testExceptionRSVPNonExisting() {
		Gala  gala = new Gala();
		Guest a    = new Guest("Geddy Lee");
		Guest b    = new Guest("Neil Peart");
		Guest c    = new Guest("Alex Lifeson");

		gala.execute( gala.new Add   ( a ));
		gala.execute( gala.new Add   ( b ));
		gala.execute( gala.new Delete( a ));
		gala.execute( gala.new Add   ( c ));

		var       rsvp = gala.new RSVP( a, true );
		Throwable t    = assertThrows( IllegalStateException.class,
				() -> gala.execute( rsvp ));
		assertEquals( "guest doesn't exist", t.getMessage() );
	}
	@Test
	void testExceptionUndo() {
		Gala  gala = new Gala();
		Guest a    = new Guest("Billy Gibbons");
		Guest b    = new Guest("Frank Beard");
		Guest c    = new Guest("Dusty Hill");

		gala.execute( gala.new Add( a ));
		gala.execute( gala.new Add( b ));
		gala.execute( gala.new Add( c ));
		
		gala.undo();
		gala.undo();
		gala.undo();

		Throwable t = assertThrows( IndexOutOfBoundsException.class,
				() -> gala.undo());
		assertEquals( "Index -1 out of bounds for length 0", t.getMessage() );
	}
	@Test
	void testExceptionRedo() {
		Gala      gala = new Gala();
		Throwable t    = assertThrows( IndexOutOfBoundsException.class,
				() -> gala.redo());
		assertEquals( "Index -1 out of bounds for length 0", t.getMessage() );
	}
}