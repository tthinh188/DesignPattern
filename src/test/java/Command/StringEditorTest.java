package Command;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

public class StringEditorTest {
	@Test
	public void classHasPrivateNonStaticFields() {
		for (Field field : StringEditor.class.getDeclaredFields()) {
			if (!field.isSynthetic()) {
				int modifier = field.getModifiers();
				assertTrue ( Modifier.isPrivate( modifier ), () -> String.format( "field \"%s\" should be private", field.getName()) );
				assertFalse( Modifier.isStatic ( modifier ), () -> String.format( "field \"%s\" cannot be static",  field.getName()) );
			}
		}
	}
	@Test
	public void testNewStringEditor() {
		StringEditor editor = new StringEditor();
		assertTrue  ( editor.get().isEmpty() );
		assertFalse ( editor.canUndo() );
		assertFalse ( editor.canRedo() );
	}
	@Test
	public void testAddUndoAndRedo() {
		StringEditor editor = new StringEditor();
		{
			Command         add = editor.new Insert( 0, "Hello" );
			editor.execute( add );
			assertEquals( "Hello", editor.get() );
			assertTrue  ( editor.canUndo() );
			assertFalse ( editor.canRedo() );
		} {
			Command         add = editor.new Insert( 5, " " );
			editor.execute( add );
			assertEquals( "Hello ", editor.get() );
			assertTrue  ( editor.canUndo() );
			assertFalse ( editor.canRedo() );
		} {
			Command         add = editor.new Insert( 6, "World" );
			editor.execute( add );
			assertEquals( "Hello World", editor.get() );
			assertTrue  ( editor.canUndo() );
			assertFalse ( editor.canRedo() );
		} {
			editor.undo();
			assertEquals( "Hello ", editor.get() );
			assertTrue  ( editor.canUndo() );
			assertTrue  ( editor.canRedo() );
		} {
			editor.undo();
			assertEquals( "Hello", editor.get() );
			assertTrue  ( editor.canUndo() );
			assertTrue  ( editor.canRedo() );
		} {
			editor.undo();
			assertEquals( "", editor.get() );
			assertFalse ( editor.canUndo() );
			assertTrue  ( editor.canRedo() );
		} {
			editor.redo();
			assertEquals( "Hello", editor.get() );
			assertTrue  ( editor.canUndo() );
			assertTrue  ( editor.canRedo() );
		} {
			editor.redo();
			assertEquals( "Hello ", editor.get() );
			assertTrue  ( editor.canUndo() );
			assertTrue  ( editor.canRedo() );
		} {
			editor.redo();
			assertEquals( "Hello World", editor.get() );
			assertTrue  ( editor.canUndo() );
			assertFalse ( editor.canRedo() );
		}
	}
	@Test
	public void testAddAndRemove() {
		StringEditor editor = new StringEditor();
		{
			Command         add = editor.new Insert( 0, "aaaaa" );
			editor.execute( add );
			assertEquals( "aaaaa", editor.get() );
		} {
			Command         add = editor.new Insert( 1, "br" );
			editor.execute( add );
			assertEquals( "abraaaa", editor.get() );
		} {
			Command         add = editor.new Insert( 6, "br" );
			editor.execute( add );
			assertEquals( "abraaabra", editor.get() );
		} {
			Command         add = editor.new Insert( 4, "c" );
			editor.execute( add );
			assertEquals( "abracaabra", editor.get() );
		} {
			Command         add = editor.new Insert( 6, "d" );
			editor.execute( add );
			assertEquals( "abracadabra", editor.get() );
		} {
			Command         del = editor.new Delete( 2, 6 );
			editor.execute( del );
			assertEquals( "abbra", editor.get() );
		} {
			Command         del = editor.new Delete( 3, 1 );
			editor.execute( del );
			assertEquals( "abba", editor.get() );
		} {
			editor.undo();
			assertEquals( "abbra", editor.get() );
		} {
			editor.undo();
			assertEquals( "abracadabra", editor.get() );
		}
	}
	@Test
	public void testNewCommandClearsRedo() {
		StringEditor editor = new StringEditor();
		{
			Command         add = editor.new Insert( 0, "acca" );
			editor.execute( add );
			assertFalse ( editor.canRedo() );
		} {
			Command         add = editor.new Insert( 1, "bxb" );
			editor.execute( add );
			assertFalse ( editor.canRedo() );
		} {
			editor.undo();
			assertTrue  ( editor.canRedo() );
		} {
			Command         del = editor.new Delete( 2, 1 );
			editor.execute( del );
			assertFalse ( editor.canRedo() );
		}
	}
}
