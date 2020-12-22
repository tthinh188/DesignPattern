package Facade;
import java.util.List;

public class Tray {
	
	private List<Item> items;
	private double price;
	
	public Tray(Item... items) {
		for (Item i: items){
			price += i.getPrice();
		}
		this.items = List.of(items);
	}
	
	public List<Item> getItems(){
		return items;
	}
	
	public double getPrice(){
		return price;
	}
	
	public String toString() {
		StringBuilder bld = new StringBuilder();
		for (Item i: items) {
			bld.append(String.format("%s,", i.toString()));
		}
		String str = bld.toString();
		str = str.substring(0,str.length() -1);
		return str;
	}
}
