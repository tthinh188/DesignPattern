package Visitor;

import java.util.ArrayList;
import java.util.Objects;

public abstract class Region {
	private String name;
	
	public Region(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException(String.format( "Name cannot be null nor blank ['%s']", name));
		}
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj != null && this.getClass() == obj.getClass() &&
				((Region)obj).getName().equals(this.getName()));
	}
	
	public int hashCode() {
		return Objects.hash(name);
	}
	
	public void accept(IRegionVisitor visitor) {
		visitor.visit(this);
	}
	
}

abstract class City extends Region {
	private int population;
	
	public City(String name, int population) {
		super(name);
		if(population < 0) {
			throw new IllegalArgumentException(String.format("Population cannot be negative [%d]", population));
		}
		
		this.population = population;
	}
	
	public int getPopulation() {
		return population;
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj != null && this.getClass() == obj.getClass() &&
				((City)obj).getName().equals(this.getName())
				&& ((City)obj).getPopulation() == this.getPopulation());
		}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getName(), this.getPopulation());
	}
}

class Map {
	private ArrayList<Region> regions = new ArrayList<>();
	
	public void addRegions(Region... regions) {
		for(Region region: regions) {
			if(region == null) {
				throw new IllegalArgumentException("Regions cannot be null");
			}
			this.regions.add(region);
		}
	}
	
	public void traverse(IRegionVisitor visitor) {
		for(Region region: regions) {
			region.accept(visitor);
		}
	}
}

class State extends Region {
	private ArrayList<City> cities = new ArrayList<>();
	
	public State(String name) {
		super(name);
	}
	
	public void addCities(City... cities) {
		for(City city: cities) {
			if(city == null) {
				throw new IllegalArgumentException("Cities cannot be null");
			}
			this.cities.add(city);
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public void accept(IRegionVisitor visitor) {
		visitor.visit(this);
		for (City city: cities) {
			city.accept(visitor);
		}
	}
}

class LargeCity extends City {
	
	public LargeCity(String name, int population) {
		super(name, population);
		if(population <= 250000) {
			throw new IllegalArgumentException(String.format("Population not in range (%d,%d) [%d]" , 250001, Integer.MAX_VALUE, population));
		}
	}
	
	@Override
	public void accept(IRegionVisitor visitor) {
		visitor.visit(this);
	}
}

class SmallCity extends City {

	public SmallCity(String name, int population) {
		super(name, population);
		if(population > 250000) {
			throw new IllegalArgumentException(String.format("Population not in range (0,250000) [%d]" , population));
		}
	}
	
	@Override
	public void accept(IRegionVisitor visitor) {
		visitor.visit(this);
	}
}
