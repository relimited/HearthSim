package com.hearthsim.util.tree;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.hearthsim.Game;
import com.hearthsim.exception.HSException;
import com.hearthsim.exception.HSInvalidParamFileException;
import com.hearthsim.model.BoardModel;
import com.hearthsim.player.playercontroller.ArtificialPlayer;
import com.hearthsim.player.playercontroller.BoardScorer;
import com.hearthsim.player.playercontroller.BruteForceSearchAI;
import com.hearthsim.player.playercontroller.RandomAI;
import com.hearthsim.util.HearthActionBoardPair;

/**
 * This is a node for an MCTS tree
 * 
 * @todo: Add in some real simulation logic
 * 			Some sort of opponent modeling so we can MCTS loop for more than one chunk at a time
 * 							
 * @author Johnathan
 *
 */
public class MCTSTreeNode {
	protected final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
    static Random r = new Random();
    //static int numPotentialTurns = 5;  this is now dynamically dependent on the number of different ways we can generate new boards
    static double epsilon = 1e-6;

    public HearthTreeNode turn;  //the hearthnode tree of how to play out this turn.  
    
    MCTSTreeNode[] children;
    double nVisits, nodeValue;
    
    //FIXME: this is a little gross, but I think it'll work for the short term
    //I'm not super into the tree node having a way to create nodes.
    //essentally, we need to mirror what the BFS AI would have so we can play out a turn
    private ArtificialPlayer[] boardGenerators;
    private ArtificialPlayer opponentModel;
    
	private List<HearthActionBoardPair> turnResults;
	public int turnNum;
	private BoardScorer scorer;
	public BoardModel boardState;
	
	/**
	 * Creates a new MCTS Tree Node.
	 * 
	 * @param turn a BoardModel that is the board state this MCTS node wraps
	 * @param turnNum the turn number.
	 * 				@FIXME: this is a hold-over from using ArificialPlayers to generate new board states from the provided one.
	 */
    public MCTSTreeNode(BoardModel boardState, int turnNum){
    	this(boardState, turnNum, BruteForceSearchAI.buildStandardAI1().getScorer()); //haxs!  Because I didn't want to write my own, just use one already configured in the system somewhere.
    }
    
    public MCTSTreeNode(BoardModel boardState, int turnNum, BoardScorer scorer){
    	this.boardState = boardState;
    	this.scorer = scorer;
    	nodeValue = this.scorer.boardScore(this.boardState);  //initialize new MCTS tree scores to the internal score from the HearthTreeNodes
    	nVisits = 0;						//initialize visits to 0
    	this.turnNum = turnNum;
    	
    	this.boardGenerators = new ArtificialPlayer[3]; //FIXME: this should really be a call to 'createGenerators'
    													//		To be really good: this should be wrapped in an alternate constructor so a parent
    													//		node can just provide it's children with generator references
    	
    	this.opponentModel = RandomAI.buildRandomAI(); // one of the standard AIs that hearthsim comes with
    																//as we'll be explicitly passing a random scorer to it's	
    																//play turn function, the weights don't really matter
    }
    
    /**
     * This method creates an array of board generators that take a turn.
     * A board generator takes a starting board state and plays a turn to transform it to an ending board state
     * 
     * Right now, these genators are implmented as HearthSim's BFS AIs with different weights.
     * @param generatorParams -- the paths to get the config files for the generators.  This, again, isn't the best way to do things
     */
    public void createBoardGenerators(Path[] generatorParams) {
		
		//set up some differing AIs
		try {
			this.boardGenerators[0] = new BruteForceSearchAI(generatorParams[0]);
			this.boardGenerators[1] = new BruteForceSearchAI(generatorParams[1]);
			this.boardGenerators[2] = new BruteForceSearchAI(generatorParams[2]);
		} catch (HSInvalidParamFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    /**
     * Go through the MCTS action loop to get the best child, and then return it.
     * 
     * @return the highest scored child, according to the MCTS algorithm
     */
	public MCTSTreeNode selectAction() {
        List<MCTSTreeNode> visited = new LinkedList<MCTSTreeNode>();
        MCTSTreeNode cur = this;
        visited.add(this);
        
        //START MCTS LOOP
        //which currently doesn't loop, but this is where the loop code would go
        //selection
        while (!cur.isLeaf()) {
            cur = cur.select();
            // System.out.println("Adding: " + cur);
            visited.add(cur);
        }
        //expansion
        cur.expand();
        MCTSTreeNode newNode = cur.select();
        visited.add(newNode);
        //simulation
        double value = rollOut(newNode);
        //back-prop
        for (MCTSTreeNode node : visited) {
            // would need extra logic for n-player game
            // System.out.println(node);
            node.updateStats(value);
        }
        //END MCTS LOOP
        
        //select final node to use
        if(this.children.length > 0){
        	//get best child node
        	MCTSTreeNode bestChild = null;	//I feel like such a shitty person when I work on trees
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
        children = new MCTSTreeNode[this.boardGenerators.length];
    	BoardModel nextTurn = null; 
    	
    	int i = 0;
		//This is where some logic would go to play a turn out in some varying manner
    	for(ArtificialPlayer generator : this.boardGenerators){
        	try {
				List<HearthActionBoardPair> localResults = generator.playTurn(turnNum, boardState);
				if(localResults.size() > 0){
					nextTurn = localResults.get(localResults.size() - 1).board;
				}else{
					nextTurn = boardState;
				}
				//end the turn and flip the players
				nextTurn = Game.endTurn(nextTurn);
				nextTurn = nextTurn.flipPlayers();
				//pass the state off to our opponent model
				nextTurn = Game.beginTurn(nextTurn);
				//don't bother checking for a game over state.
				List<HearthActionBoardPair> opponentTurnResults = opponentModel.playTurn(turnNum, nextTurn);
				
				if(opponentTurnResults.size() > 0){
					//update the 'next turn' to include changes from your opponent
					nextTurn = opponentTurnResults.get(opponentTurnResults.size() - 1).board;
				}
				
				//and clean the board if it needs it
				nextTurn = Game.endTurn(nextTurn);
				//and flip the players one more time
				nextTurn = nextTurn.flipPlayers();
				
				//now children are a full opponent turn away from their parents
				children[i] = new MCTSTreeNode(nextTurn, turnNum + 1, this.scorer);
				children[i].boardGenerators = this.boardGenerators;
				children[i].turnResults = localResults;	//and this is how we got here, so that when we bounce back out to the game, we can modify the board appropriately
				
			} catch (HSException e) {
				e.printStackTrace();
			} 
        	i++;
        }
    }

    //Look at this node's children and select the one with the best UCB
    private MCTSTreeNode select() {
        MCTSTreeNode selected = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        for (MCTSTreeNode c : children) {
            double uctValue =
                    	c.nodeValue / (c.nVisits + epsilon) +				
                        Math.sqrt(Math.log(nVisits+1) / (c.nVisits + epsilon)) +
                        r.nextDouble() * epsilon;
            // small random number to break ties randomly in unexpanded nodes
            log.info("UCT value = " + uctValue);
            if (uctValue > bestValue) {
                selected = c;
                bestValue = uctValue;
            }
        }
        log.info("Returning: " + selected);
        return selected;
    }

    public boolean isLeaf() {
        return children == null;
    }

    public double rollOut(MCTSTreeNode tn) {
      //check-- start by copying the tn node so rollout doesn't get 'double counted'
    	MCTSTreeNode future = new MCTSTreeNode(tn.boardState, tn.turnNum);
    	future.boardGenerators = this.boardGenerators; //FIXME THIS REALLY NEEDS TO BE IN A CONSTRUCTOR, YOU HALFWIT
    	int numberOfTurnsToPlay = 1;
    	log.info("----- FUTURE STATS ----");
    	while(numberOfTurnsToPlay > 0){
    		future.expand();
    		future = future.select();
    		numberOfTurnsToPlay--;
    	}
    	
    	return future.nodeValue;
    }

    //back prop-- go through and update this node's stats
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
