package com.hearthsim;

import com.hearthsim.card.Deck;
import com.hearthsim.card.minion.Hero;
import com.hearthsim.event.HSGameEndEventListener;
import com.hearthsim.exception.HSException;
import com.hearthsim.exception.HSInvalidParamFileException;
import com.hearthsim.exception.HSParamNotFoundException;
import com.hearthsim.io.ParamFile;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.player.playercontroller.ArtificialPlayer;
import com.hearthsim.results.GameResult;
import com.hearthsim.results.GameResultSummary;
import com.hearthsim.util.ThreadQueue;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class HearthSimBase {

    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
	
	int numSims_;
	int numThreads_;
	String gameResultFileName_;
	protected Path rootPath_;
	
	protected Path aiParamFilePath0_;
	protected Path aiParamFilePath1_;
	
	boolean isRunning_;
	
	private List<HSGameEndEventListener> gameEndListeners_;
	
	private Object lockObject = new Object();

	/**
	 * Constructor
	 * 
	 * @param setupFilePath
	 * @throws HSInvalidParamFileException
	 * @throws HSParamNotFoundException
	 * @throws IOException
	 */
	HearthSimBase(Path setupFilePath) throws HSInvalidParamFileException, HSParamNotFoundException, IOException {
		rootPath_ = setupFilePath.getParent();
		ParamFile masterParam = new ParamFile(setupFilePath);
		numSims_ = masterParam.getInt("num_simulations", 40000);
		numThreads_ = masterParam.getInt("num_threads", 1);
		aiParamFilePath0_ = FileSystems.getDefault().getPath(rootPath_.toString(), masterParam.getString("aiParamFilePath0"));
		aiParamFilePath1_ = FileSystems.getDefault().getPath(rootPath_.toString(), masterParam.getString("aiParamFilePath1"));
		gameResultFileName_ = masterParam.getString("output_file", "gameres.txt");
		gameEndListeners_ = new ArrayList<HSGameEndEventListener>();
		isRunning_ = false;
	}

	HearthSimBase(int numSimulations, int numThreads) {
		numSims_ = numSimulations;
		numThreads_ = numThreads;
		gameEndListeners_ = new ArrayList<HSGameEndEventListener>();
		isRunning_ = false;
	}
	
	/**
	 * Run a single game.
	 * 
	 * Must be overridden by a concrete subclass.  The subclass's job is to set up the decks and the AIs and to 
	 * call runSigleGame(ArtificialPlayer, Hero, Deck, ArtificialPlayer, Hero, Deck).
	 * 
	 * @return
	 * @throws HSException
	 * @throws IOException 
	 */
	public abstract GameResult runSingleGame(int gameId) throws HSException, IOException;

	protected GameResult runSingleGame(ArtificialPlayer ai0, Hero hero0, Deck deck0, ArtificialPlayer ai1, Hero hero1, Deck deck1) throws HSException {
		return this.runSingleGame(ai0, hero0, deck0, ai1, hero1, deck1, 0);
	}
	
	/**
	 * Run a single game
	 * 
	 * @param ai0 AI for player 0
	 * @param hero0 Hero class for player 0
	 * @param deck0 Deck for player 0
	 * @param ai1 AI for player 1
	 * @param hero1 Hero class for player 1
	 * @param deck1 Deck for player 1
	 * @param shufflePlayOrder Randomizes the play order if set to true
	 * @return
	 * @throws HSException
	 */
	protected GameResult runSingleGame(ArtificialPlayer ai0, Hero hero0, Deck deck0, ArtificialPlayer ai1, Hero hero1, Deck deck1, boolean shufflePlayOrder) throws HSException {

		//Shuffle the decks!
		deck0.shuffle();
		deck1.shuffle();
		
		PlayerModel playerModel0 = new PlayerModel(0, "player0", hero0, deck0);
		PlayerModel playerModel1 = new PlayerModel(1, "player1", hero1, deck1);

		//DEBUGGING
		Game game = new Game(playerModel0, playerModel1, ai0, ai1, shufflePlayOrder);
		return game.runGame();
	}
	
	protected GameResult runSingleGame(ArtificialPlayer ai0, Hero hero0, Deck deck0, ArtificialPlayer ai1, Hero hero1, Deck deck1, int firstPlayerId) throws HSException {
		//Shuffle the decks!
		deck0.shuffle();
		deck1.shuffle();
		
		PlayerModel playerModel0 = new PlayerModel(0, "player0", hero0, deck0);
		PlayerModel playerModel1 = new PlayerModel(1, "player1", hero1, deck1);

		Game game = new Game(playerModel0, playerModel1, ai0, ai1, firstPlayerId);
		return game.runGame();
	}
	
	
	public void run() throws HSException, IOException, InterruptedException {
        long simStartTime = System.currentTimeMillis();
		Path outputFilePath = FileSystems.getDefault().getPath(rootPath_.toString(), gameResultFileName_);
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFilePath.toString()), "utf-8"));
		ThreadQueue tQueue = new ThreadQueue(numThreads_);
		for (int i = 0; i < numSims_; ++i) {
			GameThread gThread = new GameThread(i, writer);
			tQueue.queue(gThread);
		}

		tQueue.runQueue();

		writer.close();
        long simEndTime = System.currentTimeMillis();
        double  simDeltaTimeSeconds = (simEndTime - simStartTime) / 1000.0;
        String prettyDeltaTimeSeconds = String.format("%.2f", simDeltaTimeSeconds);
        double secondsPerGame = simDeltaTimeSeconds / numSims_;
        String prettySecondsPerGame = String.format("%.2f", secondsPerGame);

        log.info("completed simulation of {} games in {} seconds on {} thread(s)", numSims_, prettyDeltaTimeSeconds, numThreads_);
        log.info("average time per game: {} seconds", prettySecondsPerGame);
    }
	
	
	class GameThread implements Runnable {

		
		final int gameId_;
		Writer writer_;
		
		public GameThread(int gameId, Writer writer) {
			gameId_ = gameId;
			writer_ = writer;
		}
		
		@Override
		public void run() {
			try {
				GameResult res = runSingleGame(gameId_);
				
				if (writer_ != null) {
					//This doesn't need to be in a synchronized block, as each thread has it's own writer now.
					synchronized(writer_) {
						GameResultSummary grs = new GameResultSummary(res);
						writer_.write("{\"game\":" + gameId_ + ",\"winner\":" + res.winnerPlayerIndex_ + ", \"wentFirst\":" + res.firstPlayerIndex_ + ", \"rounds\":");
						writer_.write(grs.toImprovedJSON().toString(4) + "\n");
						writer_.write("\n}");
						writer_.flush();
					}
				}
				synchronized(lockObject) {
					log.info("game " + gameId_ + ", player " + res.winnerPlayerIndex_ + " wins");
					for (HSGameEndEventListener listener : gameEndListeners_) {
						listener.gameEnded(res);
					}
				}
			} catch (HSException | IOException e) {
				log.error("Error! " + e);
			}
		}
		
	}
	
    public void addGameEndListener(HSGameEndEventListener toAdd) {
    	gameEndListeners_.add(toAdd);
    }

    public void removeGameEndListener(HSGameEndEventListener toAdd) {
    	gameEndListeners_.remove(toAdd);
    }

}
