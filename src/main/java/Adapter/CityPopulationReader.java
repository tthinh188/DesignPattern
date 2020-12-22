package Adapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.Scanner;

public class CityPopulationReader {
	public static int readPopulation(URI uri) {
		if(uri == null) {
			throw new IllegalArgumentException("uri cannot be null");
		}

		File file = new File(uri.getPath());
	
		String rawURI = uri.toString();
	
		if (!rawURI.contains("?state=") && !rawURI.contains("city=")) {
			throw new IllegalArgumentException("missing query");
		}
		
		if(!rawURI.contains("?city=")) {
			throw new IllegalArgumentException("missing city");
		}
		
		if(!rawURI.contains("&state=")) {
			throw new IllegalArgumentException("missing state");
		}
		
		String cityAndState = rawURI.substring(rawURI.indexOf("?") + 1, rawURI.length());
		
		String city = cityAndState.substring(5, cityAndState.indexOf("&"));
		city = city.replace("+", " ");
		
		String state = cityAndState.substring(cityAndState.indexOf("&")+ 7,cityAndState.length());
		
		try {
			Scanner scanner = new Scanner(file);
			while(scanner.hasNextLine()) {
				String info = scanner.nextLine();
				String[] tokens = info.split(",");
				if(tokens[0].equalsIgnoreCase(city) && tokens[1].equalsIgnoreCase(state)) {
					scanner.close();
					return Integer.parseInt(tokens[2]);
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
}
	