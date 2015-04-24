
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
import java.nio.file.Path;
import java.io.IOException;

public class MCTSPlayer implements ArtificialPlayer {
    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
    private final static int MAX_THINK_TIME = 20000;

    private boolean useSparseBoardStateFactory_ = true;
    private boolean useDuplicateNodePruning = true;

    public WeightedScorer scorer = new WeightedScorer();

    public MCTSPlayer() {}

    public MCTSPlayer(Path aiParamFile) throws IOException, HSInvalidParamFileException {
      //Ignore param file
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
return null;

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
return null;
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
