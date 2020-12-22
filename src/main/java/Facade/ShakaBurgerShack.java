package Facade;

public class ShakaBurgerShack implements Restaurant{
	private boolean isMorning = true;
	private static final String STRING_FORMAT = "%s ($%.2f)";
	
	private class Lemonade extends Item{
		public Lemonade() {
			super(1.50);
		}
		public String toString() {
			return String.format(STRING_FORMAT, this.getClass().getSimpleName(),this.getPrice());
		}	
	}
	private class Pancake extends Item {
		public Pancake() {
			super(5.50);
		}
		public String toString() {
			return String.format(STRING_FORMAT, this.getClass().getSimpleName(),this.getPrice());
		}
	}
		
	private class Yogurt extends Item {
		public Yogurt() {
			super(1.75);
		}
		public String toString() {
			return String.format(STRING_FORMAT, this.getClass().getSimpleName(),this.getPrice());
		}
	}
		
	private class RootBeer extends Item {
		public RootBeer() {
			super(2.50);
		}
		public String toString() {
			return String.format(STRING_FORMAT, this.getClass().getSimpleName(),this.getPrice());
		}
	}
		
	private class PaiaBurger extends Item {
		public PaiaBurger() {
			super(8.25);
		}
		public String toString() {
			return String.format(STRING_FORMAT, this.getClass().getSimpleName(),this.getPrice());
		}
	}
		
	private class WaileaCake extends Item {
		public WaileaCake() {
			super(3.75);
		}
		public String toString() {
			return String.format(STRING_FORMAT, this.getClass().getSimpleName(),this.getPrice());
		}
	}
	@Override
	public Tray getDrink() {
		if(isMorning) {
			return new Tray(new Lemonade());
		}
		else {
			return new Tray(new RootBeer());
		}
	}

	@Override
	public Tray getMeal() {
		if(isMorning) {
			return new Tray(new Pancake());
		}
		else {
			return new Tray(new PaiaBurger());
		}
	}

	@Override
	public Tray getDessert() {
		if(isMorning) {
			return new Tray(new Yogurt());
		}
		else {
			return new Tray(new WaileaCake());
		}
	}

	@Override
	public Tray getCombo() {
		Item[] morning = { new Lemonade(), new Pancake(), new Yogurt()};
		Item[] afternoon = { new RootBeer(), new PaiaBurger(), new WaileaCake()};
		
		if(isMorning) {
			return new Tray(morning);
		}
		else {
			return new Tray(afternoon);
		}
	}

	@Override
	public void setMorning(boolean morning) {
		isMorning = morning;
	}

	@Override
	public boolean isMorning() {
		return isMorning;
	}

	
}
