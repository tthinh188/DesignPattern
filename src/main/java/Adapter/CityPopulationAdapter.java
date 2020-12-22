package Adapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CityPopulationAdapter {
	private File file;
	private Map<String, Integer> reader = new HashMap<>();

	public CityPopulationAdapter(File file) {
		if(file == null) {
			throw new IllegalArgumentException("file cannot be null");
		}
		
		this.file = file;
	}
	
	public int getPopulation(String city, String state) {
		if(city == null) {
			throw new IllegalArgumentException("city cannot be null");
		}
		
		if(state == null) {
			throw new IllegalArgumentException("state cannot be null");
		}
		
		Path path = file.toPath();
		city = city.toLowerCase();
		state = state.toLowerCase();
		city = city.replace(" ", "+");
		String str = String.format("%s?city=%s&state=%s", path.toUri(),city,state);
		
		URI uri;
		try {	
			uri = new URI(str);
			if(!reader.containsKey(city)) {
				int population = CityPopulationReader.readPopulation(uri);
				if(population != -1)
				{
					reader.put(city, population);
					return reader.get(city);
				}
			}
			else {
				return reader.get(city);
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return -1; 
	}
}
