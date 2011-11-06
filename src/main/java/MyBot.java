import java.io.IOException;

/**
 * Starter bot implementation.
 */
public class MyBot implements AgentSpawner
{
	private static GameContext _ctx;
    /**
     * Main method executed by the game engine for starting the bot.
     * 
     * @param args command line arguments
     * 
     * @throws IOException if an I/O error occurs
     */
    public static void main(String[] args) throws IOException {
    	MyBot bot = new MyBot();
    	
        _ctx = new GameContext();
        _ctx.setSpawner(bot);
        
        _ctx.readSystemInput();
    }
	public Agent spawnNewAgent(Position pos) {
		return null;
	}
}
