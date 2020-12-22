package Visitor;

public class CountRegionVisitor implements IRegionVisitor {
	private int states;
	private int cities;
	
	@Override
	public void visit(State state) {
		states++;
	}
	
	@Override
	public void visit(SmallCity city) {
		cities++;
	}
	
	@Override
	public void visit(LargeCity city) {
		cities++;
	}
	
	public int getStates() {
		return states;
	}
	
	public int getCities() {
		return cities;
	}
}
