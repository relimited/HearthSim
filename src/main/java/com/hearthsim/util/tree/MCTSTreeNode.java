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
 * @author Johnathan
 *
 */
public class MCTSTreeNode {
	protected final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
    static Random r = new Random();
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
	int numberOfTurnsToPlay;
	int MCTSloops;
	int numChildren;
	
	 
    /**
     * Create a new MCTS node with arguments setting all parameters, and the path to a file that has weights for a board scorer
     * 
     * @param board board state for this node
     * @param turn turn number
     * @param numMCTSIterations number of iterations for the MCTS loop
     * @param numSimulateTurns number of turns to simulate during the simulation step of MCTS
     * @param generatorParams paths to parameter files for each board generator
     * @param scorerWeights path to parameters for a scorer for the MCTS algorithm
     * 
     * @throws IOException 
     * @throws HSInvalidParamFileException 
     */
    public MCTSTreeNode(BoardModel board, int turn, int numMCTSIterations, int numSimulateTurns, Path[] generatorParams, Path scorerWeights) throws HSInvalidParamFileException, IOException{
    	this(board, turn, numMCTSIterations, numSimulateTurns, createBoardGenerators(generatorParams),  new BruteForceSearchAI(scorerWeights).getScorer());
    	
    }

    /**
     * Creates an MCTS node with several board generators
     * Intermediate constructor, not for outside use
     * 
     * @param board board state for this node
     * @param turn turn number
     * @param numMCTSIterations number of iterations for the MCTS loop
     * @param numSimulateTurns number of turns to simulate during the simulation step of MCTS
     * @param createBoardGenerators several ArtificalPlayers to generate various board states
     * @param scorer
     */
	private MCTSTreeNode(BoardModel board, int turn, int numMCTSIterations, int numSimulateTurns, ArtificialPlayer[] createBoardGenerators, BoardScorer scorer) {
		this(board, turn, numMCTSIterations, numSimulateTurns, createBoardGenerators.length, createBoardGenerators, scorer);
	}
	
	/**
	 * Creates an MCTS node with a random board generator
	 * 
	 * @param board
	 * @param turn
	 * @param numMCTSIterations
	 * @param numSimulateTurns
	 * @param numChildrenPerGeneration
	 * @param scorerWeights
	 * @throws HSInvalidParamFileException
	 * @throws IOException
	 */
	public MCTSTreeNode(BoardModel board, int turn, int numMCTSIterations, int numSimulateTurns, int numChildrenPerGeneration, Path scorerWeights) throws HSInvalidParamFileException, IOException{
		this(board, turn, numMCTSIterations, numSimulateTurns, numChildrenPerGeneration, new BruteForceSearchAI(scorerWeights).getScorer());
	}

	/**
	 * Creates an MCTS node with a random board generator
	 *  Intermediate constructor, not for outside use
	 *  
	 * @param board
	 * @param turn
	 * @param numMCTSIterations
	 * @param numSimulateTurns
	 * @param numChildrenPerGeneration
	 * @param scorer
	 */
	private MCTSTreeNode(BoardModel board, int turn, int numMCTSIterations, int numSimulateTurns, int numChildrenPerGeneration,BoardScorer scorer) {
		this(board, turn, numMCTSIterations, numSimulateTurns, numChildrenPerGeneration, convertTolist(RandomAI.buildRandomAI()), scorer);
	}

	/**
	 * Creates an MCTS node with all parameters
	 *
	 * Used by parents to make children exact copies of themselves
	 * 
	 * @param board
	 * @param turn
	 * @param numMCTSIterations
	 * @param numSimulateTurns
	 * @param numChildrenPerGeneration
	 * @param createBoardGenerators
	 * @param scorer
	 */
	private MCTSTreeNode(BoardModel board, int turn, int numMCTSIterations,
			int numSimulateTurns, int numChildrenPerGeneration,
			ArtificialPlayer[] createBoardGenerators, BoardScorer scorer) {
		
		this.turnNum = turn;
		this.scorer = scorer;
		this.boardState = board;
		this.numberOfTurnsToPlay = numSimulateTurns;
		this.MCTSloops = numMCTSIterations;
		this.boardGenerators = createBoardGenerators;
		this.numChildren = numChildrenPerGeneration;
		
		this.opponentModel = RandomAI.buildRandomAI();
		
		//and score the node
		this.nodeValue = this.scorer.boardScore(this.boardState);
		//log.info(this.toString() + " init score: " + this.nodeValue);
	}

	/**
	 * Creates a new MCTS node using the parameters from another MCTS node, and a provided board state and turn
	 * 
	 * @param board
	 * @param turn
	 * @param baseNode
	 */
	public MCTSTreeNode(BoardModel board, int turn, MCTSTreeNode baseNode) {
		this.turnNum = turn;
		
		this.scorer = baseNode.scorer;
		this.boardState = board;
		this.numberOfTurnsToPlay = baseNode.numberOfTurnsToPlay;
		this.MCTSloops = baseNode.MCTSloops;
		this.boardGenerators = baseNode.boardGenerators;
		this.numChildren = baseNode.numChildren;
		
		this.opponentModel = baseNode.opponentModel;
		
		//and score the node
		this.nodeValue = this.scorer.boardScore(this.boardState);
		//log.info(this.toString() + " initial score: " + this.nodeValue); 
	}

	/**
     * private static method creates an array of board generators that take a turn.
     * A board generator takes a starting board state and plays a turn to transform it to an ending board state
     * 
     * Right now, these generators are implemented as HearthSim's BFS AIs with different weights.
     * @param generatorParams -- the paths to get the config files for the generators.
     * @return a list of generators
     */
    private static synchronized BruteForceSearchAI[] createBoardGenerators(Path[] generatorParams) {
		BruteForceSearchAI[] generators = new BruteForceSearchAI[generatorParams.length];
		for(int i = 0; i < generatorParams.length; i++){
			//set up some differing AIs
			try {
				generators[i] = new BruteForceSearchAI(generatorParams[i]);
			
			} catch (HSInvalidParamFileException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return generators;
	}

    /**
     * Private conversion method to take a RandomAI and return a list of one element of a random AI
     * @param ai
     * @return
     */
    private static RandomAI[] convertTolist(RandomAI ai){
    	RandomAI[] list = new RandomAI[1];
    	list[0] = ai;
    	return list;
    }
    
    /**
     * Go through the MCTS action loop to get the best child, and then return it.
     * 
     * @return the highest scored child, according to the MCTS algorithm
     */
	public MCTSTreeNode selectAction() {
		//log.info("----- START MCTS TURN -----");
		//log.info("===========================");
        List<MCTSTreeNode> visited = new LinkedList<MCTSTreeNode>();
        visited.add(this);
        
        //START MCTS LOOP
        for(int i = 0; i < MCTSloops; i++){
        	MCTSTreeNode cur = this;
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
        }
        //END MCTS LOOP
        //this.print();
        //select final node to use
        //log.info("----- FINAL NODE SCORES -----");
        if(this.children.length > 0){
        	//get best child node
        	MCTSTreeNode bestChild = null;	//I feel like such a shitty person when I work on trees
        	double bestScore = Double.NEGATIVE_INFINITY;
        	for(MCTSTreeNode child : this.children){
        		//log.info(child + " value = " + child.nodeValue);
        		if(child.nodeValue > bestScore){
        			bestChild = child;
        			bestScore = child.nodeValue;
        		}
        	}
        	
        	//log.info("Returning: ");
        	//log.info(bestChild.toString() + " score: " + bestChild.nodeValue);
        	//log.info("Plays: ");
        	for(HearthActionBoardPair action : bestChild.getTurnResults()){
        		//log.info(action.action.verb_.toString());
        	}
        	//log.info("=========================");
        	//log.info("----- END MCTS TURN -----");
        	return bestChild;
        }else{
        	//log.info("Constantly returning the same node, you twat");
        	//assume that this turn ends in lethal or fuck it yolo, etc.
        	return this;
        }
    }

    public void expand() {
        children = new MCTSTreeNode[numChildren];
    	BoardModel nextTurn = null; 
    	
		for(int i = 0; i < this.numChildren; i++){
			ArtificialPlayer generator;
			//if we have less generators than the number of children we need to generate, just use the last generator again
			if(i >= boardGenerators.length){
				generator = boardGenerators[boardGenerators.length - 1];
			}else{
				generator = boardGenerators[i];
			}
			
        	try {
				List<HearthActionBoardPair> localResults = generator.playTurn(turnNum, boardState);
				if(localResults.size() > 0){
					nextTurn = localResults.get(localResults.size() - 1).board;
				}else{
					nextTurn = boardState;
				}
				
				//end the turn and flip the players
				nextTurn = Game.endTurn(nextTurn);
				
				//new plan: score the board here.
				double childInitScore = this.scorer.boardScore(nextTurn);
				
				///*
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
				nextTurn = Game.beginTurn(nextTurn);
				
				//now children are a full opponent turn away from their parents
				children[i] = new MCTSTreeNode(nextTurn, turnNum + 1, this.MCTSloops, this.numberOfTurnsToPlay, this.numChildren, this.boardGenerators, this.scorer);
				children[i].turnResults = localResults;	//and this is how we got here, so that when we bounce back out to the game, we can modify the board appropriately
				children[i].nodeValue = childInitScore;  //override the child score with this score
															//FIXME: there might be a smarter way to do this
				
			} catch (HSException e) {
				e.printStackTrace();
			} 
        }
    }

    //Look at this node's children and select the one with the best UCB
    private MCTSTreeNode select() {
    	//log.info("----- SELECTING A NODE -----");
        MCTSTreeNode selected = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        for (MCTSTreeNode c : children) {
            double uctValue =
                    	c.nodeValue / (c.nVisits + epsilon) +				
                        Math.sqrt(Math.log(nVisits+1) / (c.nVisits + epsilon)) +
                        r.nextDouble() * epsilon; // small random number to break ties randomly in unexpanded nodes
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
    	//return tn.nodeValue;
    	
      //check-- start by copying the tn node so rollout doesn't get 'double counted'
    	//also, this means we need to copy numberOfTurnsToPlay
    	int turns = this.numberOfTurnsToPlay;
    	//log.info("----- FUTURE STATS ----");
    	MCTSTreeNode future = new MCTSTreeNode(tn.boardState, tn.turnNum, tn.MCTSloops, tn.numberOfTurnsToPlay, tn.numChildren, tn.boardGenerators, tn.scorer);
    	while(turns > 0){
    		//log.info("Simulated turns left: " + turns);
    		future.expand();
    		future = future.select();
    		turns--;
    	}
    	//log.info("----- END FUTURE STATS ----");
    	//log.info("Node used for future propigation: " + tn.toString());
    	//log.info("Score to propagate back: ");
    	//log.info(String.valueOf(future.nodeValue));
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
			//log.info("Returning a null value for what an MCTS AI did for a turn!");
			return new ArrayList<HearthActionBoardPair>();
		}else{
			return turnResults;
		}
	}
	
	
	
	/**
	 * Now we're getting to the weird shit.
	 * This prints an ASCII representation of the tree.  I need to see the goddamn thing to try and understand how we're picking / using
	 * nodes.  Because something is fucked up, I just goddamn know it.
	 */
	public void print() {
        print("", true);
    }

	/**
	 * Prints the MCTS tree
	 * @param prefix the current graph
	 * @param isTail is this a tail?
	 */
    private void print(String prefix, boolean isTail) {
        log.info(prefix + (isTail ? "└── " : "├── ") + this.hashCode() + ": " + this.nodeValue);
        for (int i = 0; i < this.arity() - 1; i++) {
            children[i].print(prefix + (isTail ? "    " : "│   "), false);
        }
        if (this.arity() > 0) {
            children[children.length - 1].print(prefix + (isTail ?"    " : "│   "), true);
        }
    }
}
