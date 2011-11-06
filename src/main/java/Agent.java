/**
 * Base class representing an agent
 * @author glards
 *
 */

public abstract class Agent {
	private static int NextAgentId = 1;
	
	protected final GameContext context;
	protected final int id;
	
	private Position currentPosition;
	private Position nextPosition;
	
	public Agent(GameContext ctx) {
		this.context = ctx;
		this.id = Agent.NextAgentId++;
	}
	
	public Position getNextPosition() {
		return this.nextPosition;
	}
	
	public void clearNextPosition() {
		this.nextPosition = null;
	}
	
	public Position getCurrentPosition() {
		return this.currentPosition;
	}
	
	public void setCurrentPosition(Position p) {
		//TODO : Sanity check to prevent setting invalid positions ?
		this.currentPosition = p;
	}
	
	public abstract void init();
	
	public abstract void think();
}
