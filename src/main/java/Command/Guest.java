package Command;
import java.util.Objects;

public class Guest {
	private String name;
	private boolean rsvp = false;
	
	public Guest(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("name cannot be null or blank");
		}
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean hasRSVP() {
		return rsvp;
	}
	
	public void setRSVP(boolean rsvp) {
		this.rsvp = rsvp;
	}
	
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj != null && this.getClass() == obj.getClass() &&
				((Guest)obj).getName().equals(this.getName()));
	}
	
	@Override
	public String toString() {
		if(rsvp) {
			return String.format("Guest [name=%s,rsvp=yes]", name);
		}
		else {
			return String.format("Guest [name=%s,rsvp=no]", name);
		}
	}
}
