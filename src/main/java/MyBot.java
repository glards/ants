import java.io.IOException;

/**
 * Starter bot implementation.
 */
public class MyBot
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
        _ctx = new GameContext();
        
        _ctx.readSystemInput();
    }
}
