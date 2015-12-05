package com.hearthsim.player.playercontroller;

import java.util.ArrayList;
import java.util.List;

import com.hearthsim.exception.HSException;
import com.hearthsim.model.BoardModel;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.util.HearthActionBoardPair;
import com.hearthsim.util.factory.BoardStateFactoryBase;
import com.hearthsim.util.factory.DepthBoardStateFactory;
import com.hearthsim.util.factory.SparseBoardStateFactory;
import com.hearthsim.util.tree.HearthTreeNode;
import com.hearthsim.util.tree.StopNode;

/**
 * An AI that uses a BFS and a random scoring function to come up with a random
 * set of actions to take for a turn
 * 
 * @author Johnathan
 */
public class RandomAI implements ArtificialPlayer {
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
	private final static int MAX_THINK_TIME = 20000;

	private boolean useSparseBoardStateFactory_ = true;
	private boolean useDuplicateNodePruning = true;
	    
	private RandomScorer scorer = new RandomScorer();
	
	protected RandomAI(){
	}
	
	public static RandomAI buildRandomAI(){
		RandomAI artificialPlayer = new RandomAI();
		
		return artificialPlayer;
	}
	
	@Override
	public ArtificialPlayer deepCopy() {
		RandomAI copied = new RandomAI();
		copied.useSparseBoardStateFactory_ = useSparseBoardStateFactory_;
	    copied.useDuplicateNodePruning = useDuplicateNodePruning;
		  
	    copied.scorer = this.scorer.deepCopy();
		return copied;
	}

	@Override
	public List<HearthActionBoardPair> playTurn(int turn, BoardModel board)
			throws HSException {
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

	@Override
	public List<HearthActionBoardPair> playTurn(int turn, BoardModel board,
			BoardStateFactoryBase factory) throws HSException {
		PlayerModel playerModel0 = board.getCurrentPlayer();
        log.debug("playing turn for " + playerModel0.getName());
        // The goal of this ai is to maximize his board score
        log.debug("start turn board state is {}", board);
        HearthTreeNode toRet = new HearthTreeNode(board);

        HearthTreeNode allMoves = factory.doMoves(toRet, this.scorer);
        ArrayList<HearthActionBoardPair> retList = new ArrayList<>();
        HearthTreeNode curMove = allMoves;

        while(curMove.getChildren() != null) {
            curMove = curMove.getChildren().get(0);
            if (curMove instanceof StopNode) {
                // Add the initial step that created the StopNode
                retList.add(new HearthActionBoardPair(curMove.getAction(), curMove.data_.deepCopy()));
                // Force the step to resolve
                HearthTreeNode allEffectsDone = ((StopNode)curMove).finishAllEffects();
                // Add the resolution to action list
                retList.add(new HearthActionBoardPair(allEffectsDone.getAction(), allEffectsDone.data_.deepCopy()));

                // Continue the turn
                List<HearthActionBoardPair> nextMoves = this.playTurn(turn, allEffectsDone.data_);
                if (nextMoves.size() > 0) {
                    for ( HearthActionBoardPair actionBoard : nextMoves) {
                        retList.add(actionBoard);
                    }
                }
                break;
            } else {
                retList.add(new HearthActionBoardPair(curMove.getAction(), curMove.data_));
            }
        }
        return retList;
	}

	@Override
	public int getMaxThinkTime() {
		 return MAX_THINK_TIME;
	}
}
