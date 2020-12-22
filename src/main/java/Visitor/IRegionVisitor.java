package Visitor;

public interface IRegionVisitor {
	public default void visit(Region region) { }
	public default void visit(State state) { }
	
	public default void visit(City city) { }
	
	public default void visit(SmallCity city) { }
	
	public default void visit(LargeCity city) { }
}
