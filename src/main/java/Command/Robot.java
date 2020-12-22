package Command;
import java.awt.Point;

public class Robot {
    enum Direction {S,N,E,W};
    private int x,y;
    private Direction direction;
    
    private Stack<Command> undo = new Stack<>();
    private Stack<Command> redo = new Stack<>();

    public Robot (int x, int y, Direction d) {
        if (x <0) {
            throw new IllegalArgumentException(String.format("x cannot be negative [%d]", x));
        }
        else if(y <0) {
            throw new IllegalArgumentException(String.format("y cannot be negative [%d]", y));
        }
        else if (d == null){
            throw new IllegalArgumentException("direction cannot be null");
        }
        else {
            this.x = x;
            this.y = y;
            this.direction = d;
        }
    }
    
    public Point getPosition() {
        return new Point(this.x, this.y);
    }
    
    public Direction getDirection() {
        return this.direction;
    }
    
    public boolean canUndo() {
        return !undo.isEmpty();
    }
    
    public boolean canRedo() {
        return !redo.isEmpty();
    }
    

    public void undo() {
    	if(!canUndo()) {
    		throw new IllegalStateException("no command to undo");
    	}
        Command cmd = undo.pop();
        cmd.undo();
        redo.push(cmd);
    }
    
    public void redo() {
    	if(!canRedo()) {
    		throw new IllegalStateException("no command to redo");
    	}
        Command cmd = redo.pop();
        cmd.execute();
        undo.push(cmd);
    }
    
    public void execute(Command cmd) {
        undo.push(cmd.execute());
        redo.clear();
    }
    

    public class Move implements Command {
        private Stack<Integer> undoX = new Stack<>();
        private Stack<Integer> undoY = new Stack<>();

        private int s;
        
        public Move(int s) {
            this.s = s;    
        }
        
        @Override
        public Command execute() {
        	undoX.push(Robot.this.x);
        	undoY.push(Robot.this.y);
            if (Robot.this.direction == Robot.Direction.N) {
                if(Robot.this.y - s >= 0) 
                    Robot.this.y = Robot.this.y - s;
                else
                    Robot.this.y = 0;

            }
            else if (Robot.this.direction == Robot.Direction.S) {
                if (Robot.this.y + s >= 0)
                    Robot.this.y = Robot.this.y + s;
                else
                    Robot.this.y = 0;                    
            }
            else if (Robot.this.direction == Robot.Direction.W) {
                if (Robot.this.x - s >= 0)
                    Robot.this.x = Robot.this.x - s;
                else
                    Robot.this.x = 0;
            }
            else {
                if (Robot.this.x + s >= 0)
                    Robot.this.x= Robot.this.x + s;
                else
                    Robot.this.y = 0;
            }
            return this;
        }
        
        @Override
        public Command undo() {
        	Robot.this.x = undoX.pop();
        	Robot.this.y = undoY.pop();
             return this;
        }
        
    }
    public class Turn implements Command {
        private Stack<Robot.Direction> undoDirection = new Stack<>();
        private Direction d;
        
        public Turn(Direction d) {
            this.d = d;    
        }
        
        @Override
        public Command execute() {
            undoDirection.push(Robot.this.getDirection());
            Robot.this.direction = d;
            return this;
        }
        
        @Override
        public Command undo() {
            Robot.this.direction = undoDirection.pop();
            return this;
        }
        
    }

}
