package com.hearthsim.util.tree;

import com.hearthsim.card.Card;
import com.hearthsim.card.minion.*;
import com.hearthsim.exception.HSException;
import com.hearthsim.model.BoardModel;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.player.playercontroller.BoardScorer;
import com.hearthsim.player.playercontroller.ArtificialPlayer;
import com.hearthsim.player.playercontroller.BruteForceSearchAI;
import com.hearthsim.util.HearthAction;
import com.hearthsim.util.HearthActionBoardPair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * This is a node for an MCTS tree
 * 
 * @TODO: current annoyence: the onboard AIs don't return trees, they return modified board states
 * 							and also require a turn number
 * 							look up when the first relevant turn is (1 or 0), and work from there
 * 							stupid implementation will end up with a lot of copies, but that'll just have to be OK.
 * 
 * 							
 * @author Johnathan
 *
 */
public class MCTSTreeNode {
	protected final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
    static Random r = new Random();
    static int numPotentialTurns = 5;
    static double epsilon = 1e-6;

    public HearthTreeNode turn;  //the hearthnode tree of how to play out this turn.  
    
    MCTSTreeNode[] children;
    double nVisits, nodeValue;
    
    //FIXME: this is a little gross, but I think it'll work for the short term
    //essentally, we need to mirror what the BFS AI would have so we can play out a turn
    private ArtificialPlayer ai;
	private List<HearthActionBoardPair> turnResults;
	public int turnNum;
	
    public MCTSTreeNode(HearthTreeNode turn, int turnNum){
    	this(turn, turnNum, BruteForceSearchAI.buildStandardAI1());
    }
    
    public MCTSTreeNode(HearthTreeNode turn, int turnNum, ArtificialPlayer ai){
    	this.turn = turn;
    	nodeValue = ((BruteForceSearchAI)ai).getScorer().boardScore(this.turn.data_);  //initialize new MCTS tree scores to the internal score from the HearthTreeNodes
    	nVisits = 0;						//initialize visits to 0
    	this.ai = ai;
    	this.turnNum = turnNum;
    }
    
    public MCTSTreeNode selectAction() {	//updated selectAction to return the high scoring hearthsim tree
    	
        List<MCTSTreeNode> visited = new LinkedList<MCTSTreeNode>();
        MCTSTreeNode cur = this;
        visited.add(this);
        //also, this is one round of the MCTS loop
        while (!cur.isLeaf()) {
            cur = cur.select();
            // System.out.println("Adding: " + cur);
            visited.add(cur);
        }
        cur.expand();
        MCTSTreeNode newNode = cur.select();
        visited.add(newNode);
        double value = rollOut(newNode);
        for (MCTSTreeNode node : visited) {
            // would need extra logic for n-player game
            // System.out.println(node);
            node.updateStats(value);
        }
        //end MCTS loop
        
        //select final node to use
        if(this.children.length > 0){
        	//get best child node
        	MCTSTreeNode bestChild = null;	//I feel like such a shity person when I work on trees
        	double bestScore = Double.NEGATIVE_INFINITY;
        	for(MCTSTreeNode child : this.children){
        		if(child.nodeValue > bestScore){
        			bestChild = child;
        			bestScore = child.nodeValue;
        		}
        	}
        	
        	return bestChild;
        }else{
        	//assume that this turn ends in lethal or fuck it yolo, etc.
        	return this;
        }
    }

    public void expand() {
        children = new MCTSTreeNode[numPotentialTurns];
        //right now, all children will be the same (because they're a BFS AI turn away from their parent)
    	HearthTreeNode nextTurn = null;
    	BoardModel state = this.turn.data_.deepCopy(); //an AI taking its turn will destroy the current board, so we need to pass it a copy
    	
    	log.info("MCTS turn num {}", turnNum);
    	log.info("MCTS start state {}", state);
    	
        for (int i=0; i<numPotentialTurns; i++) {
        	try {
        		//This is where some logic would go to play a turn ou in some varying manner
				turnResults = ai.playTurn(turnNum, state);
				
				if(turnResults.size() > 0){
					nextTurn = new HearthTreeNode(turnResults.get(turnResults.size() - 1).board);
					children[i] = new MCTSTreeNode(nextTurn, turnNum + 1, ai);
					children[i].turnResults = turnResults;	
				}else{
					//we didn't change the board state at all, so just use this board again.
					children[i] = new MCTSTreeNode(new HearthTreeNode(state), turnNum + 1, ai);
					children[i].turnResults = null; //explicitly setting what should be implictly set-- there were no actions performed to get to this board  state
				}
			} catch (HSException e) {
				e.printStackTrace();
			}  
        }
    }

    private MCTSTreeNode select() {
        MCTSTreeNode selected = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        for (MCTSTreeNode c : children) {
            double uctValue =
                    	c.nodeValue / (c.nVisits + epsilon) +				
                        Math.sqrt(Math.log(nVisits+1) / (c.nVisits + epsilon)) +
                        r.nextDouble() * epsilon;
            // small random number to break ties randomly in unexpanded nodes
            //log.info("UCT value = " + uctValue);
            if (uctValue > bestValue) {
                selected = c;
                bestValue = uctValue;
            }
        }
        //log.info("Returning: " + selected);
        return selected;
    }

    public boolean isLeaf() {
        return children == null;
    }

    public double rollOut(MCTSTreeNode tn) {
        // ultimately a roll out will end in some value
        // assume for now that it ends in a win or a loss
        // and just return this at random
    	
    	//this is where some form of simulation will go.  Simulate out to a win/loss (probably impossible), so simulate
    	//out to some future state and backprop the score
        return r.nextInt(2);
    }

    public void updateStats(double value) {
        nVisits++;
        nodeValue += value;
    }

    public int arity() {
        return children == null ? 0 : children.length;
    }

    /**
     * Turn results is a bit of a misnomer and needs to be renamed.  Turn results is a set of action / board state pairs that are how we got
     * to this particular node.
     * 
     * @return turnResults or an empty list if the parameter is null
     */
	public List<HearthActionBoardPair> getTurnResults() {
		if(turnResults == null){
			log.info("Returning a null value for what an MCTS AI did for a turn!");
			return new ArrayList<HearthActionBoardPair>();
		}else{
			return turnResults;
		}
	}
}