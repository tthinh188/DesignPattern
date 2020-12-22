package Command;

import java.util.ArrayList;
import java.util.List;

public final class Stack<T> {
	private List<T> array = new ArrayList<T>();

	public T peek() {
		return array.get( getSize()-1 );
	}
	public T pop() {
		return array.remove( getSize()-1 );
	}
	public boolean push(T o) {
		return array.add( o );
	}
	public boolean isEmpty() {
		return array.isEmpty();
	}
	public int getSize() {
		return array.size();
	}
	public void clear() {
		array.clear();
	}
	@Override
	public String toString() {
		return "Stack" + array.toString();
	}
}