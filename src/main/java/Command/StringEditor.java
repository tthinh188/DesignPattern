package Command;

public class StringEditor {
	private String content = "";
	private Stack<Command> undo = new Stack<>();
    private Stack<Command> redo = new Stack<>();
      
    public class Delete implements Command {
    	private int startPosition, endPosition;
    	private Stack<String> undoString = new Stack<>();
    	
		public Delete(int start, int end) {
			this.startPosition = start;
    		this.endPosition = end;		
    	}

		@Override
		public Command execute() {
			undoString.push(StringEditor.this.content);
		    String begin = StringEditor.this.content.substring(0,startPosition);
		    String end = StringEditor.this.content.substring(startPosition + endPosition, StringEditor.this.content.length());
		    StringEditor.this.content = begin + end;
			return this;
		}

		@Override
		public Command undo() {
			StringEditor.this.content = undoString.pop();
			return this;
		}
    	
    }
    
    public class Insert implements Command {
    	
    	private int position;
    	private String word;
        private Stack<String> undoString = new Stack<>();

    	public Insert(int position, String word) {
    		this.position = position;
    		this.word = word;
    	}
    	
		@Override
		public Command execute() {
			undoString.push(StringEditor.this.content);
		    String begin = StringEditor.this.content.substring(0,position);
		    String end = StringEditor.this.content.substring(position);
		    StringEditor.this.content = begin + word +end;
			return this;
		}

		@Override
		public Command undo() {
			StringEditor.this.content = undoString.pop();
			return this;
		}
    }
    
    public String get() {
    	return content;
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
    
    
}
