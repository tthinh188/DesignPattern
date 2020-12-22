package Facade;


public interface Restaurant {
	public Tray getDrink();
	public Tray getMeal();
	public Tray getDessert();
	public Tray getCombo();
	public void setMorning(boolean morning);
	public boolean isMorning();
}
