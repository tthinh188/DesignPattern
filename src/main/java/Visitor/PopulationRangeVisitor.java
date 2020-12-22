package Visitor;

import java.util.ArrayList;
import java.util.List;

public class PopulationRangeVisitor implements IRegionVisitor {
	
	private List<City> cityList = new ArrayList<>();
	private int lower;
	private int upper;
	
	public PopulationRangeVisitor(int lower, int upper) {
		if(lower < 0 || upper < 0) {
			throw new IllegalArgumentException("Range must have positive values");
		}
		
		if(lower > upper) {
			throw new IllegalArgumentException("Range must be incremental");
		}
		this.lower = lower;
		this.upper = upper;
	}
	
	@Override
	public void visit(SmallCity city) {
		if(lower <= city.getPopulation() && city.getPopulation() <= upper) {
			cityList.add(city);
		}
	}
	
	@Override
	public void visit(LargeCity city) {
		if(lower <= city.getPopulation() && city.getPopulation() <= upper) {
			cityList.add(city);
		}
	}
	
	public List<City> getCities(){
		return cityList;
	}
}
