package com.hearthsim.player.playercontroller;

import com.hearthsim.card.minion.Minion;
import com.hearthsim.exception.HSException;
import com.hearthsim.exception.HSInvalidCardException;
import com.hearthsim.exception.HSInvalidParamFileException;
import com.hearthsim.exception.HSParamNotFoundException;
import com.hearthsim.io.ParamFile;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.util.CardFactory;
import com.hearthsim.util.HearthActionBoardPair;
import com.hearthsim.util.factory.BoardStateFactoryBase;
import com.hearthsim.util.factory.DepthBoardStateFactory;
import com.hearthsim.util.factory.SparseBoardStateFactory;
import com.hearthsim.util.tree.HearthTreeNode;
import com.hearthsim.util.tree.StopNode;
import com.hearthsim.exception.HSException;
import com.hearthsim.model.BoardModel;
import com.hearthsim.model.DeckModel;
import com.hearthsim.util.DeepCopyable;
import com.hearthsim.util.HearthActionBoardPair;
import com.hearthsim.util.tree.MCTSTreeNode;
import com.hearthsim.util.factory.BoardStateFactoryBase;

import java.util.List;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.io.IOException;

/**
 * An MCTS AI for hearthsim
 * @author Johnathan
 * @author Dylan
 *
 */
public class MCTSPlayer implements ArtificialPlayer {
    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
    private final static int MAX_THINK_TIME = 20000;

    private boolean useSparseBoardStateFactory_ = true;
    private boolean useDuplicateNodePruning = true;

    public WeightedScorer scorer = new WeightedScorer();

    //MCTS parameters.  Read in from a param file
    //defaults!
    private int numMCTSIterations = 1;
    private int numChildrenPerGeneration = 1;
    private int numSimulateTurns = 1;
    private Path[] generatorParams = null;
    private Path paramFile = null;
    
    private MCTSTreeNode baseNode = null;
    protected MCTSPlayer() {}

    /**
     * Using the provided AI param files we made for the class project to get several board generators (to get different board states)
     * This also gets the tunable constants for MCTS:
     * 		C value (Not Implemented)
     * 		Number of times to perform MCTS loop (numMCTSIterations)
     * 		Number of child nodes ot create (numChildrenPerGeneration)
     * 			Not used if generators are used
     * 		Number of turns to play in the future (numSimulateTurns)
     * 		Generator file paths (generatorFiles)
     * @param aiParamFile the param file with the above information
     * 
     * @throws IOException
     * @throws HSInvalidParamFileException
     */
    public MCTSPlayer(Path aiParamFile) throws IOException, HSInvalidParamFileException {
    	this.paramFile = aiParamFile;
    	this.createPlayer();
    }
    
    /**
     * Private helper method to synchronize the creation of new MCTS players.
     * @throws IOException 
     * @throws HSInvalidParamFileException 
     */
    private synchronized void createPlayer() throws HSInvalidParamFileException, IOException{
    	try{
    		ParamFile pFile = new ParamFile(this.paramFile);
    		numMCTSIterations = pFile.getInt("numMCTSIterations");
    		numSimulateTurns = pFile.getInt("numSimulateTurns");
    		
    		//optional parameters
    		if(!pFile.getKeysContaining("numChildrenPerGeneration").isEmpty()){
    			numChildrenPerGeneration = pFile.getInt("numChildrenPerGeneration");
    		}
    		if(!pFile.getKeysContaining("generatorFiles").isEmpty()){
    			String[] generatorPaths = pFile.getString("generatorFiles").split(",");
    			generatorParams = new Path[generatorPaths.length];
    			for(int i = 0; i < generatorPaths.length; i++){
    				generatorParams[i] = FileSystems.getDefault().getPath(this.paramFile.getParent().toString(), generatorPaths[i]);
    			}
    		}
    		
    		//if nothing is specified for the optional params, a randomAI is used, that creates one child
    		
    	} catch(HSParamNotFoundException e) {
            log.error(e.getMessage());
            System.exit(1);
        }
    	
    	String MCTS_info = "\nMCTS PLAYER INFO\nNumber of MCTS cycles: " + numMCTSIterations + "\nNumber of simulation turns: " + numSimulateTurns + "\n";
    	if(generatorParams != null){
    		MCTS_info = MCTS_info + "generators: \n";
    		for(Path genPath : generatorParams){
    			MCTS_info = MCTS_info + genPath.toString() + "\n";
    		}
    	}else{
    		MCTS_info = MCTS_info + "Number of children to generate: " + numChildrenPerGeneration + "\n";
    	}
    	
    	//log.info(MCTS_info);
    }

    /**
     * Play a turn
     *
     * This function is called by GameMaster, and it should return a BoardState resulting from the AI playing its turn.
     *
     * @param turn Turn number, 1-based
     * @param board The board state at the beginning of the turn (after all card draws and minion deaths)
     *
     * @return A list of HearthActionBoardPair that the AI has performed, starting from the earliest play to the last.
     * @throws HSException
     */
    @Override
    public List<HearthActionBoardPair> playTurn(int turn, BoardModel board) throws HSException {
    	//we need some way to create potential board states (aka a board state factory)
    	//copied from BruteForceSearchAI.java
    	
    	//Under the current implementation, almost all of this gets ignored.  Which is probably not what we want eventually.
    	 PlayerModel playerModel0 = board.getCurrentPlayer();
         PlayerModel playerModel1 = board.getWaitingPlayer();
         BoardStateFactoryBase factory;
         if (useSparseBoardStateFactory_) {
             factory = new SparseBoardStateFactory(playerModel0.getDeck(), playerModel1.getDeck(), MAX_THINK_TIME, useDuplicateNodePruning);
         } else {
             factory = new DepthBoardStateFactory(playerModel0.getDeck(), playerModel1.getDeck(), MAX_THINK_TIME, useDuplicateNodePruning);
         }
         return this.playTurn(turn, board, factory);
    }


    /**
     * Play a turn
     *
     * This function is called by GameMaster, and it should return a BoardState resulting from the AI playing its turn.
     *
     * @param turn Turn number, 1-based
     * @param board The board state at the beginning of the turn (after all card draws and minion deaths)
     * @param factory The factory to use for node generation
     *
     * @return A list of HearthActionBoardPair that the AI has performed, starting from the earliest play to the last.
     * @throws HSException
     */
    @Override
    public List<HearthActionBoardPair> playTurn(int turn, BoardModel board, BoardStateFactoryBase factory) throws HSException {
    	//if the baseNode is null, use this board as a base
    	//or, if the baseNode's board model doesn't match the board state, then start a new tree with this board
    	
    	if(baseNode == null){
    		try {
    			//if we don't have any generator params (that line of the param file is blank), then we're using a random board generator
    			//with a specified number of children
    			if(generatorParams != null){
    				//baseNode = new MCTSTreeNode(board, turn, numMCTSIterations, numSimulateTurns, generatorParams, paramFile);
    				baseNode = new MCTSTreeNode(board, turn, numMCTSIterations, numSimulateTurns, numChildrenPerGeneration, generatorParams, paramFile);
    			}else{
    				baseNode = new MCTSTreeNode(board, turn, numMCTSIterations, numSimulateTurns, numChildrenPerGeneration, paramFile);
    			}
    		} catch (IOException e) {
				e.printStackTrace();
			}
    	//we have a base node, it's just not the right board state.  Make a new base node with all of this base node's parameters
    	//cuts down on file reads
    	}else if(!baseNode.boardState.equals(board) && baseNode.turnNum != turn){
    		baseNode = new MCTSTreeNode(board, turn, baseNode);
    	}
    	
    	//MCTS! MCTS! MCTS!
    	baseNode = baseNode.selectAction();
    	
    	return baseNode.getTurnResults();
    }

    @Override
    public ArtificialPlayer deepCopy() {
        MCTSPlayer copied = new MCTSPlayer();
        copied.scorer = this.scorer.deepCopy();
        copied.useSparseBoardStateFactory_ = useSparseBoardStateFactory_;
        copied.useDuplicateNodePruning = useDuplicateNodePruning;
        copied.numMCTSIterations = this.numMCTSIterations;
        copied.numChildrenPerGeneration = this.numChildrenPerGeneration;
        copied.numSimulateTurns = this.numSimulateTurns;
        copied.generatorParams = this.generatorParams;
        copied.paramFile = this.paramFile;
        copied.baseNode = this.baseNode;
        
        return copied;
    }
    
    @Override
    public int getMaxThinkTime() {
      return MAX_THINK_TIME;
    }
}
