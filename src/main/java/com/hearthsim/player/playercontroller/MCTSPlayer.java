package com.hearthsim.player.playercontroller;

import com.hearthsim.card.minion.Minion;
import com.hearthsim.exception.HSException;
import com.hearthsim.exception.HSInvalidCardException;
import com.hearthsim.exception.HSInvalidParamFileException;
import com.hearthsim.exception.HSParamNotFoundException;
import com.hearthsim.io.ParamFile;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.util.CardFactory;
import com.hearthsim.util.HearthActionBoardPair;
import com.hearthsim.util.factory.BoardStateFactoryBase;
import com.hearthsim.util.factory.DepthBoardStateFactory;
import com.hearthsim.util.factory.SparseBoardStateFactory;
import com.hearthsim.util.tree.HearthTreeNode;
import com.hearthsim.util.tree.StopNode;
import com.hearthsim.exception.HSException;
import com.hearthsim.model.BoardModel;
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

    private MCTSTreeNode baseNode = null;
    
    private Path[] generatorParams;   //FIXME: a temp solution where we can provide the MCTS tree a way to play turns
    public MCTSPlayer() {}

    /**
     * Using the provided AI param files we made for the class project to get several board generators (to get different board states)
     * @param aiParamFile
     * @throws IOException
     * @throws HSInvalidParamFileException
     */
    public MCTSPlayer(Path aiParamFile) throws IOException, HSInvalidParamFileException {
    	generatorParams = new Path[3];
    	generatorParams[0] = FileSystems.getDefault().getPath(aiParamFile.toString(), "aggroAi.hsai");
    	generatorParams[1] = FileSystems.getDefault().getPath(aiParamFile.toString(), "controlAi.hsai");
    	generatorParams[2] = FileSystems.getDefault().getPath(aiParamFile.toString(), "tempoAi.hsai");
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
    	if(baseNode == null || (!baseNode.boardState.equals(board) && baseNode.turnNum != turn)){
    		baseNode = new MCTSTreeNode(board, turn);
    		//Before using the node, make sure it has board generators
        	baseNode.createBoardGenerators(generatorParams);
    	}
    	
    	
    	//MCTS! MCTS! MCTS!
    	MCTSTreeNode retNode = baseNode.selectAction();
    	
    	return retNode.getTurnResults();
    }

    @Override
    public ArtificialPlayer deepCopy() {
        MCTSPlayer copied = new MCTSPlayer();
        copied.scorer = this.scorer.deepCopy();
        copied.useSparseBoardStateFactory_ = useSparseBoardStateFactory_;
        copied.useDuplicateNodePruning = useDuplicateNodePruning;
        return copied;
    }
    
    @Override
    public int getMaxThinkTime() {
      return MAX_THINK_TIME;
    }
}
