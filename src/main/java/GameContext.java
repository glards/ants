import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

/**
 * This class is the context of the game. 
 * 
 * It extends AbstractSystemInputParser that read from the input and call the method in the manner defined below:
 * 1. (game started)
 * 2. setup()
 * 3. doTurn()
 * 4. beforeUpdate()
 * 5. Multiple calls to addAnt()/addFood()/addHill()/addWater()
 * 6. afterUpdate()
 * 7. doTurn()
 * 8. (go to step 4 until the end of the game)
 * 
 * @author glards
 *
 */
public class GameContext extends AbstractSystemInputParser {
	// Static
	public static boolean SetupCalled = false;
	
	// Some constants
	private final int MAX_HILL = 2;
	private final int MAX_PLAYER = 8;
	
	// Public 
	public int loadTime;
	public int turnTime;
	public int rows;
	public int cols;
	public int turns;
	public int viewRadiusSquared;
	public int attackRadiusSquared;
	public int spawnRadiusSquared;
	
	// Mechanic variables
	private AgentSpawner _spawner;
	
	// Game variables
	// The current turn
	private int _turn = 0;
	
	// Number of tiles on the map
	private int _nbTiles = 0;
	
	// The bitset that represent the water tiles
	private BitSet _waters;
	
	// The bitset for the visible portion of the map
	private BitSet _visible;
	
	// The bitset for the discovered portion of the map
	private BitSet _discovered;
	
	// The bitset for the area discovered in this turn
	private BitSet _newWorld;
	
	private int[] _distances;
	
	// Our hills
	private Vector<Position> _ownHills = new Vector<Position>(MAX_HILL);
	
	// Enemy hills
	private Vector<Position> _enemyHills = new Vector<Position>(MAX_HILL * MAX_PLAYER);
	
	// Our ants
	private Vector<Position> _ownAnts = new Vector<Position>(15);
	private Vector<Position> _ownDeadAnts = new Vector<Position>(10);
	private Vector<Position> _enemyAnts = new Vector<Position>(25);
	
	private Vector<Agent> _agents = new Vector<Agent>(15);
	
	
	@Override
	public void setup(int loadTime, int turnTime, int rows, int cols,
			int turns, int viewRadius2, int attackRadius2, int spawnRadius2) {
		Position.maxRows = rows;
		Position.maxCols = cols;
		
		GameContext.SetupCalled = true;
		
		this.loadTime = loadTime;
		this.turnTime = turnTime;
		this.rows = rows;
		this.cols = cols;
		this.turns = turns;
		this.viewRadiusSquared = viewRadius2;
		this.attackRadiusSquared = attackRadius2;
		this.spawnRadiusSquared = spawnRadius2;
		
		// Calculate the number of tiles
		_nbTiles = cols*rows;
		
		// Create the map data structures
		_waters = new BitSet(_nbTiles);
		_visible = new BitSet(_nbTiles);
		_discovered = new BitSet(_nbTiles);
		_newWorld = new BitSet(_nbTiles);
		
		_distances = new int[_nbTiles];
		
		Arrays.fill(_distances, 0);
	}
	
	public void setSpawner(AgentSpawner spawner) {
		this._spawner = spawner;
	}
	
	private void setSquare(BitSet s, int row, int col, int radius2) {
		int r = (int) Math.ceil(Math.sqrt(radius2));
		
		for (int x = 0; x <= r; x++) {
			int x2 = x*x;
			for (int y = 0; y <= r; y++) {
				int y2=y*y;
				if (x2+y2 < radius2) {
					s.set(Position.getIndex(row+x, col+y));
					s.set(Position.getIndex(row-x, col+y));
					s.set(Position.getIndex(row+x, col-y));
					s.set(Position.getIndex(row-x, col-y));
				}
			}
		}
	}
	
    /**
     * Calculates distance between two locations on the game map.
     * 
     * @param t1 one location on the game map
     * @param t2 another location on the game map
     * 
     * @return distance between <code>t1</code> and <code>t2</code>
     */
    public int getDistance(Position t1, Position t2) {
        int rowDelta = Math.abs(t1.getRow() - t2.getRow());
        int colDelta = Math.abs(t1.getCol() - t2.getCol());
        rowDelta = Math.min(rowDelta, rows - rowDelta);
        colDelta = Math.min(colDelta, cols - colDelta);
        return rowDelta * rowDelta + colDelta * colDelta;
    }
    
    /**
     * Returns one or two orthogonal directions from one location to the another.
     * 
     * @param t1 one location on the game map
     * @param t2 another location on the game map
     * 
     * @return orthogonal directions from <code>t1</code> to <code>t2</code>
     */
    public List<Aim> getDirections(Position t1, Position t2) {
        List<Aim> directions = new ArrayList<Aim>();
        if (t1.getRow() < t2.getRow()) {
            if (t2.getRow() - t1.getRow() >= rows / 2) {
                directions.add(Aim.NORTH);
            } else {
                directions.add(Aim.SOUTH);
            }
        } else if (t1.getRow() > t2.getRow()) {
            if (t1.getRow() - t2.getRow() >= rows / 2) {
                directions.add(Aim.SOUTH);
            } else {
                directions.add(Aim.NORTH);
            }
        }
        if (t1.getCol() < t2.getCol()) {
            if (t2.getCol() - t1.getCol() >= cols / 2) {
                directions.add(Aim.WEST);
            } else {
                directions.add(Aim.EAST);
            }
        } else if (t1.getCol() > t2.getCol()) {
            if (t1.getCol() - t2.getCol() >= cols / 2) {
                directions.add(Aim.EAST);
            } else {
                directions.add(Aim.WEST);
            }
        }
        return directions;
    }

	@Override
	public void beforeUpdate() {
		// Clear stateless data
		_ownAnts.clear();
		_enemyAnts.clear();
		_ownDeadAnts.clear();
		
		// We clear the visible portion
		_visible.clear();
		
		// Clear the new world
		_newWorld.clear();
		_newWorld.or(_discovered);
	}

	@Override
	public void addWater(int row, int col) {
		_waters.set(Position.getIndex(row,col));
	}

	@Override
	public void addAnt(int row, int col, int owner) {
		if (owner == 0) {
			// Our ant
			setSquare(_visible, row, col, this.viewRadiusSquared);
			setSquare(_discovered, row, col, this.viewRadiusSquared);
		}
	}

	@Override
	public void addFood(int row, int col) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAnt(int row, int col, int owner) {
		if (owner == 0) {
			_ownDeadAnts.add(new Position(row, col));
		}
	}
	
	@Override
	public void addHill(int row, int col, int owner) {
		if (owner > 0) {
			_enemyHills.add(new Position(row, col));
			_distances[Position.getIndex(row, col)] = 0;
		} else {
			_ownHills.add(new Position(row, col));
		}
	}
	
	private boolean computeDistances(int idx) {
		return false;
	}
	
	private void computeDistancesForNewWorld() {
		Stack<Integer> s = new Stack<Integer>();
		
		 for (int i = _newWorld.nextSetBit(0); i >= 0; i = _newWorld.nextSetBit(i+1)) {
			 if (!computeDistances(i)) {
				 s.push(i);
			 }
		 }
		 
		 while (s.isEmpty()) {
			 int idx = s.pop();
			 
			 if (computeDistances(idx)) {
				 
			 } else {
				 
			 }
		 }
	}
	
	private void handleNewAgents() {
		HashMap<Integer, Agent> positionLookup = new HashMap<Integer, Agent>(_agents.size());
		Vector<Position> newAnts = new Vector<Position>();
		Vector<Agent> handledAgents = new Vector<Agent>(_ownAnts.size()); // Not used at the moment
		
		// Create the lookup to match the ants we received with our agents
		for (Agent a : _agents) {
			positionLookup.put(a.getNextPosition().getIndex(), a);
		}
		
		// Update the agents when they are found
		for (Position p : _ownAnts) {
			if (positionLookup.containsKey(p.getIndex())) {
				Agent a = positionLookup.get(p.getIndex());
				a.clearNextPosition();
				a.setCurrentPosition(p);
				handledAgents.add(a);
			} else {
				newAnts.add(p);
			}
		}
		
		// Remove dead agents
		for (Position p : _ownDeadAnts) {
			if (positionLookup.containsKey(p.getIndex())) {
				// Remove the agents from the list as it is dead
				Agent a = positionLookup.get(p.getIndex());
				handledAgents.add(a);
				_agents.remove(a);
			} else {
				// Hmmm, we got one of our ant that died in a wrong spot ?!
				//TODO: Check if we should lookup dead ants with nextPosition or currentPosition (or both)
				assert(false);
			}
		}
		
		// Spawn new ants
		for (Position p : newAnts) {
			if (_spawner != null) {
				Agent a = _spawner.spawnNewAgent(p);
				if (a != null) {
					a.setCurrentPosition(p);
					handledAgents.add(a);
					_agents.add(a);
				}
			}
		}
		
		//TODO: Check with handled agents that everything was handled
	}
	
	@Override
	public void afterUpdate() {
		/* Handle after update computation like :
		 *  - Compute the new territory		
		 *  - Compute the distance from the hills for new territory
		 *  - Create new agents
		 */
		
		// Compute the new territory
		_newWorld.xor(_discovered);
		//computeDistancesForNewWorld();
		
		//TODO: Do we have to do special handling for case with two hills
		//computeTerritory(row, col);
		
		handleNewAgents();
	}

	@Override
	public void doTurn() {
		_turn++;
		
		if (Constants.DEBUG) {
			PrintWriter debug = Debug.getDebugFile();
			debug.println(String.format("#################### TURN %04d ####################", _turn));
			debug.println();
			debug.println("Map:");
			debug.println(Debug.drawMapBitSet(_waters, rows, cols));
			debug.println();
			debug.println();
			debug.println("Discovered:");
			debug.println(Debug.drawMapBitSet(_discovered, rows, cols));
			debug.println();
			debug.println();
			debug.println("New world:");
			debug.println(Debug.drawMapBitSet(_newWorld, rows, cols));
			debug.println();
			debug.println();
		}
		
		for (Agent a : _agents) {
			a.think();
		}
	}

}
