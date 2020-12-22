package Facade;

public abstract class Item {
	private double price;
	
	public Item (double price) {
		this.price = price;
	}
	
	public double getPrice() {
		return price;
	}
}
