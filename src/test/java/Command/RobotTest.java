package Command;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.Point;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

public class RobotTest {
	private Class<?> getClass(String className) {
		Class<?> result = null;
		try {
			Package pkg  = getClass().getPackage();
			String  path = (pkg == null || pkg.getName().isEmpty()) ? "" : pkg.getName()+".";
			result = Class.forName( path + className );
		} catch (ClassNotFoundException e) {
			fail( String.format("class '%s' not found", className ));
		}
		return result;
	}
	@Test
	public void testHasInnerClasses() {	
		getClass("Robot$Turn");
		getClass("Robot$Move");
		assertTrue( getClass("Robot$Direction").isEnum(), "class 'Robot.Direction' is not an enumerated type" );
	}
	@Test
	public void testNoStaticNonPrivateFields() {
		BiConsumer<Class<?>,Predicate<Field>> test = (c,p) -> Arrays.stream (c.getFields())
			      .filter (f->p.test(f))
			      .forEach(f->{
				    	  int modifier = f.getModifiers();
				    	  assertTrue  ( Modifier.isPrivate( modifier ), ()->String.format( "field '%s' can't be private", f.getName()) );
				    	  assertFalse ( Modifier.isStatic ( modifier ), ()->String.format( "field '%s' can't be static",  f.getName()) );
			      });
		test.accept(getClass( "Robot$Direction" ), f->!f.isSynthetic() && !f.isEnumConstant());
		test.accept(getClass( "Robot$Turn" ),      f->!f.isSynthetic());
		test.accept(getClass( "Robot$Move" ),      f->!f.isSynthetic());
		test.accept(getClass( "Robot" ),           f->!f.isSynthetic());
	}
	@Test
	public void testConstants() {
		Class<?>     c     = getClass( "Robot$Direction" );
		List<String> names = Arrays.stream (c.getFields())
								   .filter (Field::isEnumConstant)
								   .map    (Field::getName)
								   .collect(Collectors.toList());
		List<String> dirs  = List.of("N","S","E","W");
		for (String s : dirs) { 
			assertTrue( names.contains(s), ()->String.format("no constant '%s' found in '%s'",s,"Robot.Direction"));
		}
		for (String n : names) {
			assertTrue( dirs .contains(n), ()->String.format("unknown constant '%s' in '%s'",n,"Robot.Direction"));
		}
	}

	@Test
	public void testNewRobotWrongX() {
		IntStream.generate(()->new Random().nextInt()).filter(i->i < 0).limit(3).forEach(x -> {
			Throwable t = assertThrows( IllegalArgumentException.class, ()->new Robot(x, 0, Robot.Direction.E));
			assertEquals( String.format("x cannot be negative [%d]", x), t.getMessage() );
		}); 
	}
	@Test
	public void testNewRobotWrongY() {
		IntStream.generate(()->new Random().nextInt()).filter(i->i < 0).limit(3).forEach(y -> {
			Throwable t = assertThrows( IllegalArgumentException.class, ()->new Robot(0, y, Robot.Direction.N));
			assertEquals( String.format("y cannot be negative [%d]", y), t.getMessage() );
		}); 
	}
	@Test
	public void testNewRobotNullDirection() {
		Throwable t = assertThrows( IllegalArgumentException.class, ()->new Robot(0, 0, null));
		assertEquals( "direction cannot be null", t.getMessage() );
	}
	@Test
	public void testUndoWhenCantUndo() {
		Throwable t = assertThrows( IllegalStateException.class, ()->new Robot(0, 0, Robot.Direction.S).undo());
		assertEquals( "no command to undo", t.getMessage() );
	}
	@Test
	public void testRedoWhenCantRedo() {
		Throwable t = assertThrows( IllegalStateException.class, ()->new Robot(0, 0, Robot.Direction.E).redo());
		assertEquals( "no command to redo", t.getMessage() );
	}

	@Test
	public void testNewRobot() {
		Robot r = new Robot(5, 42, Robot.Direction.S);
		Point p = r.getPosition();
		assertEquals(  5, p.x );
		assertEquals( 42, p.y );
		assertEquals( Robot.Direction.S, r.getDirection() );
		assertFalse ( r.canUndo() );
		assertFalse ( r.canRedo() );
	}
	@Test
	public void testExecuteTurn() {
		Robot      r = new Robot(5, 42, Robot.Direction.S);
		Robot.Turn t = r.new Turn( Robot.Direction.N );
		
		r.execute( t );
		Point      p = r.getPosition();
		assertEquals(  5, p.x );
		assertEquals( 42, p.y );
		assertEquals( Robot.Direction.N, r.getDirection() );
	}
	@Test
	public void testUndoTurn() {
		Robot      r = new Robot(0, 0, Robot.Direction.W );
		Robot.Turn t = r.new Turn( Robot.Direction.N );
		Point      p;

		r.execute( t );
		p = r.getPosition();
		assertEquals( 0, p.x );
		assertEquals( 0, p.y );
		assertEquals( Robot.Direction.N, r.getDirection() );
		assertTrue  ( r.canUndo() );
		assertFalse ( r.canRedo() );
		
		r.undo();
		p = r.getPosition();
		assertEquals( 0, p.x );
		assertEquals( 0, p.y );
		assertEquals( Robot.Direction.W, r.getDirection() );
		assertFalse ( r.canUndo() );
		assertTrue  ( r.canRedo() );
	}
	@Test
	public void testSeveralUndoRedoExecuteTurn() {
		Robot      r = new Robot(0, 0, Robot.Direction.N );
		Robot.Turn s = r.new Turn( Robot.Direction.S );
		Robot.Turn w = r.new Turn( Robot.Direction.W );
		Robot.Turn e = r.new Turn( Robot.Direction.E );
		// now : N
		// undo: 
		// redo: 
		r.execute( e );
		assertEquals( Robot.Direction.E, r.getDirection() );
		assertTrue  ( r.canUndo() );
		assertFalse ( r.canRedo() );
		// now : E
		// undo: (N)E
		// redo: 
		r.execute( w );
		assertEquals( Robot.Direction.W, r.getDirection() );
		assertTrue  ( r.canUndo() );
		assertFalse ( r.canRedo() );
		// now : W
		// undo: (N)E (E)W
		// redo: 
		r.undo();
		assertEquals( Robot.Direction.E, r.getDirection() );
		assertTrue  ( r.canUndo() );
		assertTrue  ( r.canRedo() );
		// now : E
		// undo: (N)E
		// redo: (E)W
		r.execute( s );
		assertEquals( Robot.Direction.S, r.getDirection() );
		assertTrue  ( r.canUndo() );
		assertFalse ( r.canRedo() );
		// now : S
		// undo: (N)E (E)S
		// redo: 
		r.undo();
		assertEquals( Robot.Direction.E, r.getDirection() );
		assertTrue  ( r.canUndo() );
		assertTrue  ( r.canRedo() );
		// now : E
		// undo: (N)E
		// redo: (E)S
		r.undo();
		assertEquals( Robot.Direction.N, r.getDirection() );
		assertFalse ( r.canUndo() );
		assertTrue  ( r.canRedo() );
		// now : N
		// undo: 
		// redo: (E)S (N)E
		r.redo();
		assertEquals( Robot.Direction.E, r.getDirection() );
		assertTrue  ( r.canUndo() );
		assertTrue  ( r.canRedo() );
		// now : E
		// undo: (N)E
		// redo: (E)S
		r.redo();
		assertEquals( Robot.Direction.S, r.getDirection() );
		assertTrue  ( r.canUndo() );
		assertFalse ( r.canRedo() );
		// now : S
		// undo: (N)E (E)S
		// redo: 
	}

	@Test
	public void testExecuteMove() {
		Robot      r = new Robot(5, 8, Robot.Direction.W);
		Robot.Move m = r.new Move( 4 );
		
		r.execute( m );
		Point      p = r.getPosition();
		assertEquals( 1, p.x );
		assertEquals( 8, p.y );
		assertEquals( Robot.Direction.W, r.getDirection() );
	}
	@Test
	public void testUndoMove() {
		Robot      r = new Robot(2, 1, Robot.Direction.N );
		Robot.Move m = r.new Move( -3 );
		Point      p;

		r.execute( m );
		p = r.getPosition();
		assertEquals( 2, p.x );
		assertEquals( 4, p.y );
		assertEquals( Robot.Direction.N, r.getDirection() );
		assertTrue  ( r.canUndo() );
		assertFalse ( r.canRedo() );
		
		r.undo();
		p = r.getPosition();
		assertEquals( 2, p.x );
		assertEquals( 1, p.y );
		assertEquals( Robot.Direction.N, r.getDirection() );
		assertFalse ( r.canUndo() );
		assertTrue  ( r.canRedo() );
	}
	@Test
	public void testMoveNegativeLocation() {
		Robot      r = new Robot(7, 1, Robot.Direction.S );
		Robot.Move m = r.new Move( -5 );
		Point      p;

		r.execute( m );
		p = r.getPosition();
		assertEquals( 7, p.x );
		assertEquals( 0, p.y );
		
		r.undo();
		p = r.getPosition();
		assertEquals( 7, p.x );
		assertEquals( 1, p.y );
	}
	@Test
	public void testSeveralCommands() {
		Robot      r = new Robot(0, 0, Robot.Direction.N );

		r.execute( r.new Turn( Robot.Direction.E ));
		assertEquals( Robot.Direction.E, r.getDirection() );
		
		r.execute( r.new Move( 6 ));
		assertEquals( 6, r.getPosition().x );
		assertEquals( 0, r.getPosition().y );

		r.execute( r.new Turn( Robot.Direction.S ));
		assertEquals( Robot.Direction.S, r.getDirection() );
		
		r.execute( r.new Move( 5 ));
		assertEquals( 6, r.getPosition().x );
		assertEquals( 5, r.getPosition().y );

		r.execute( r.new Turn( Robot.Direction.W ));
		assertEquals( Robot.Direction.W, r.getDirection() );
		
		r.execute( r.new Move( 8 ));
		assertEquals( 0, r.getPosition().x );
		assertEquals( 5, r.getPosition().y );
		
		r.execute( r.new Move( -1 ));
		assertEquals( 1, r.getPosition().x );
		assertEquals( 5, r.getPosition().y );
		
		while (r.canUndo()) {
			r.undo();
		}
		assertEquals( Robot.Direction.N, r.getDirection() );
		assertEquals( 0, r.getPosition().x );
		assertEquals( 0, r.getPosition().y );
		
		while (r.canRedo()) {
			r.redo();
		}
		assertEquals( Robot.Direction.W, r.getDirection() );
		assertEquals( 1, r.getPosition().x );
		assertEquals( 5, r.getPosition().y );
	}
}
