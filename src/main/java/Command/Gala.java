package Command;

import java.util.ArrayList;
import java.util.List;

public class Gala {
	private Stack<Command> undo = new Stack<>();
	private Stack<Command> redo = new Stack<>();
	private List<Guest>  guests = new ArrayList<>();
	
	public void execute(Command cmd) {
		undo.push(cmd.execute());
        redo.clear();
	}
	
	public void undo() {
		if(undoSize() == 0) {
			throw new IndexOutOfBoundsException("Index -1 out of bounds for length 0");
		}
		Command cmd = undo.pop();
        cmd.undo();
        redo.push(cmd);	
    }
    
    public void redo() {
    	if (redoSize() == 0) {
			throw new IndexOutOfBoundsException("Index -1 out of bounds for length 0");
    	}
    	Command cmd = redo.pop();
        cmd.execute();
        undo.push(cmd);	
        
    }
    
    public int undoSize() {
    	return undo.getSize();
    }
    
    public int redoSize() {
    	return redo.getSize();
    }
    
    public boolean hasGuest(Guest g) {
    	return guests.contains(g);
    }
    
    public List<Guest> getGuests() {
    	List<Guest> copy = new ArrayList<>();
    	for (Guest guest: guests) {
    		copy.add(guest);
    	}
    	return copy;
    }
    
    public List<Guest> getPending() {
    	List<Guest> pending = new ArrayList<>();
    	for (Guest guest: guests) {
    		if (!guest.hasRSVP()) {
    			pending.add(guest);
    		}
    	}
    	return pending;
    }
    
    public List<Guest> getRSVP() {
    	List<Guest> rsvp = new ArrayList<>();
    	for (Guest guest: guests) {
    		if (guest.hasRSVP()) {
    			rsvp.add(guest);
    		}
    	}
    	return rsvp;
    }

	public final class Add implements Command {
		private List<Guest> addedGuest = new ArrayList<>();
		private Guest g;
		
		public Add(Guest g) {
			this.g = g;
		}
		
		@Override
		public Command execute() {
			if (Gala.this.hasGuest(g)) {
				throw new IllegalStateException("guest exists already");
			}
			else {
				Gala.this.guests.add(g);
				addedGuest.add(g);
			}
			return this;
		}

		@Override
		public Command undo() {
			Gala.this.guests.remove(addedGuest.get(addedGuest.size() - 1));
			addedGuest.remove(addedGuest.size() - 1);
			return this;
		}
		
	}
	
	public final class Delete implements Command {
		private List<Guest> deletedGuest = new ArrayList<>();
		private Guest g;
		
		public Delete(Guest g) {
			this.g = g;
		}
		
		@Override
		public Command execute() {
			if (!Gala.this.hasGuest(g)) {
				throw new IllegalStateException("guest doesn't exist");
			}
			else {
				Gala.this.guests.remove(g);
				deletedGuest.add(g);
			}
			return this;
		}

		@Override
		public Command undo() {
			Gala.this.guests.add(deletedGuest.get(deletedGuest.size() - 1));
			deletedGuest.remove(deletedGuest.size() - 1);
			return this;
		}
		
	}
	
	public final class RSVP implements Command {
		private Guest g;
		private boolean theRSVP;
		private boolean undoRSVP;
		
		public RSVP(Guest g, boolean rsvp) {
			this.g = g;
			theRSVP = rsvp;
		}
		
		@Override
		public Command execute() {
			if (!Gala.this.hasGuest(g)) {
				throw new IllegalStateException("guest doesn't exist");
			}
			else {
				undoRSVP = g.hasRSVP();
				g.setRSVP(theRSVP);
			}
			return this;
		}

		@Override
		public Command undo() {
			g.setRSVP(undoRSVP);
			return this;
		}
		
	}
}
