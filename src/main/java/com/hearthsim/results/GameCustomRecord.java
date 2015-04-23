package com.hearthsim.results;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hearthsim.card.Card;
import com.hearthsim.card.minion.Hero;
import com.hearthsim.card.minion.Minion;
import com.hearthsim.exception.HSInvalidPlayerIndexException;
import com.hearthsim.model.BoardModel;
import com.hearthsim.model.BoardModel.MinionPlayerPair;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.util.AbstractPair;
import com.hearthsim.util.HearthAction;
import com.hearthsim.util.HearthAction.Verb;
import com.hearthsim.util.HearthActionBoardPair;
import com.hearthsim.util.IdentityLinkedList;
import com.hearthsim.util.record.HearthActionRecord;

import org.json.JSONObject;

import java.util.List;

public class GameCustomRecord implements GameRecord {

	final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GameCustomRecord.class);
	
	int maxTurns_;
	int firstPlayer;
	
	byte[][][] numMinions_;
	byte[][][] numCards_;
	byte[][][] heroHealth_;
	byte[][][] heroArmor_;
	List<Map<Integer, AbstractPair<List<HearthActionBoardPair>, List<HearthActionRecord>>>> state_;
	List<BoardModel> boardStateOnTurn;
	
	BoardModel turn0Model;		//special place to store information on turn 0, which normally gets ignored.
	
	public GameCustomRecord() {
		this(50);
	}
	
	//TODO: update this to have no maximum on turns
	public GameCustomRecord(int maxTurns) {
		maxTurns_ = maxTurns;
		numMinions_ = new byte[2][maxTurns][2];
		numCards_ = new byte[2][maxTurns][2];
		heroHealth_ = new byte[2][maxTurns][2];
		heroArmor_ = new byte[2][maxTurns][2];
		
		state_ = new ArrayList<Map<Integer, AbstractPair<List<HearthActionBoardPair>, List<HearthActionRecord>>>>();
		boardStateOnTurn = new ArrayList<BoardModel>();
	}
	
	
	/**
	 * Now with extra information!
	 * The record has actual card information
	 */
	@Override
	public void put(int turn, PlayerSide activePlayerSide, BoardModel board,
			List<HearthActionBoardPair> plays, List<HearthActionRecord> record) {
		PlayerModel playerModel = board.modelForSide(activePlayerSide);

        int currentPlayerId = playerModel.getPlayerId();
        int waitingPlayerId = board.modelForSide(activePlayerSide.getOtherPlayer()).getPlayerId();
        
        //New and improved state logging
        if(turn == 0){
        	turn0Model = board; 
        }
        
        //standard logging strategy
        Map<Integer, AbstractPair<List<HearthActionBoardPair>, List<HearthActionRecord>>> turnInformation;
        if(state_.size() <= turn || state_.get(turn) == null){
        	turnInformation = new HashMap<Integer, AbstractPair<List<HearthActionBoardPair>, List<HearthActionRecord>>>();
        }else{
        	turnInformation = state_.get(turn);
        }
        
        List<HearthActionBoardPair> actionLog;
        List<HearthActionRecord> actionRecord;
        
        //there is already an element for this turn.  Add any additional info to this element
        if(turnInformation.containsKey(currentPlayerId)){
        	actionLog = turnInformation.get(currentPlayerId).getFirst();
        	if(actionLog != null){
        		actionLog.addAll(plays);
        	}
        	
        	actionRecord = turnInformation.get(currentPlayerId).getSecond();
        	
        	if(actionRecord != null){
        		actionRecord.addAll(record);
        	}
        	
        	if(actionLog != null || actionRecord != null){
        		turnInformation.put(currentPlayerId, new AbstractPair(actionLog, actionRecord));
        	}
        }else{
        	actionLog = plays;
        	actionRecord = record;
        	turnInformation.put(currentPlayerId, new AbstractPair(actionLog, actionRecord));
        }
        
        if(state_.size() != 0 && state_.size() > turn){
        	//replace
        	state_.remove(turn);
        	state_.add(turn, turnInformation);
        }else{
        	state_.add(turn, turnInformation);
        }
        
        recordResourceValues(currentPlayerId, waitingPlayerId, turn, board);
	}
	
	@Override
	public void put(int turn, PlayerSide activePlayerSide, BoardModel board, List<HearthActionBoardPair> plays) {
		/*/FIXME: logging
		if(turn > 1){
			if(!plays.isEmpty()){
				for(HearthActionBoardPair play : plays){
					if(play.action.verb_ == Verb.USE_CARD){
						log.debug("Verb: USE_CARD");
						//check this action for any cards in hand
						ActionDescription actData = ActionDescription.fromJSON(play.action.toJSON());
						log.debug("Card Index: " + actData.cardOrCharacterIndex);
						if(!play.board.getCurrentPlayer().getHand().isEmpty()){
							log.debug("Hand from board state associated with this action");
							for(Card card : play.board.getCurrentPlayer().getHand()){
								log.debug("Name: " + card.getName());
							}
						}else{
							log.debug("The hand associated with this action is empty!");
							//we need to check the last hand
							int opponentId = (play.board.getCurrentPlayer().getPlayerId() == 0) ? 1 : 0; 
							 List<HearthActionBoardPair> opponentPreviousTurn = state_.get(turn - 1).get(opponentId);
							 BoardModel opponentLastState = opponentPreviousTurn.get(opponentPreviousTurn.size() - 1).board.flipPlayers();
							 if(!opponentLastState.getCurrentPlayer().getHand().isEmpty()){
								 log.debug("Hand from board state associated with this player on the opponent's last action");
								 for(Card card : opponentLastState.getCurrentPlayer().getHand()){
									 log.debug("Name: " + card.getName());
								 }
							 }else{
								 int playerId = (opponentId == 0) ? 1 : 0;
								 List<HearthActionBoardPair> playerPreviousTurn = state_.get(turn - 1).get(playerId);
								 BoardModel playerLastState = playerPreviousTurn.get(playerPreviousTurn.size() - 1).board;
								 if(!playerLastState.getCurrentPlayer().getHand().isEmpty()){
									 log.debug("Hand from board state associated with this player on this player's last action");
									 for(Card card : playerLastState.getCurrentPlayer().getHand()){
										 log.debug("Name: " + card.getName());
									 }
								 }else{
									 log.debug("Fuck.  Just Fuck Me Sideways.");
									 log.debug("I can't find this index in any nearby previous states");
									 log.debug("We'll have to see if the discard pile is indexed, or do some really, really gross knowledge propigation, where we look to the"
									 		+ "future to figure this out");
								 }
							 }
						}
					}
				}
			}
		}
		//End logging*/
		PlayerModel playerModel = board.modelForSide(activePlayerSide);

        int currentPlayerId = playerModel.getPlayerId();
        int waitingPlayerId = board.modelForSide(activePlayerSide.getOtherPlayer()).getPlayerId();
        
        //New and improved state logging
        if(turn == 0){
        	turn0Model = board; 
        }
        
        //standard logging strategy
        Map<Integer, AbstractPair<List<HearthActionBoardPair>, List<HearthActionRecord>>> turnInformation;
        if(state_.size() <= turn || state_.get(turn) == null){
        	turnInformation = new HashMap<Integer, AbstractPair<List<HearthActionBoardPair>, List<HearthActionRecord>>>();
        }else{
        	turnInformation = state_.get(turn);
        }
        
        List<HearthActionBoardPair> actionLog;
        
        if(turnInformation.containsKey(currentPlayerId)){
        	actionLog = turnInformation.get(currentPlayerId).getFirst();
        	actionLog.addAll(plays);
        	turnInformation.put(currentPlayerId, new AbstractPair(actionLog, null));
        }else{
        	actionLog = plays;
        	turnInformation.put(currentPlayerId, new AbstractPair(actionLog, null));
        }
        
        if(state_.size() != 0 && state_.size() > turn){
        	//replace
        	state_.remove(turn);
        	state_.add(turn, turnInformation);
        }else{
        	state_.add(turn, turnInformation);
        }
        
        recordResourceValues(currentPlayerId, waitingPlayerId, turn, board);
       
    }

	private void recordResourceValues(int currentPlayerId, int waitingPlayerId,
			int turn, BoardModel board) {
		 numMinions_[currentPlayerId][turn][currentPlayerId] = (byte)board.getCurrentPlayer().getNumMinions();
	     numMinions_[currentPlayerId][turn][waitingPlayerId] = (byte)board.getWaitingPlayer().getNumMinions();

	     numCards_[currentPlayerId][turn][currentPlayerId] = (byte)board.getCurrentPlayer().getHand().size();
	     numCards_[currentPlayerId][turn][waitingPlayerId] = (byte)board.getWaitingPlayer().getHand().size();

	     Hero currentPlayerHero = board.getCurrentPlayer().getHero();
	     heroHealth_[currentPlayerId][turn][currentPlayerId] = currentPlayerHero.getHealth();
	     Hero waitingPlayerHero = board.getWaitingPlayer().getHero();
	     heroHealth_[currentPlayerId][turn][waitingPlayerId] = waitingPlayerHero.getHealth();

	     heroArmor_[currentPlayerId][turn][currentPlayerId] = currentPlayerHero.getArmor();
	     heroArmor_[currentPlayerId][turn][waitingPlayerId] = waitingPlayerHero.getArmor();
	}

	@Override
	public int getRecordLength(int playerId) {
		return state_.size();
	}

	@Override
	public int getNumMinions(int playerId, int turn, int currentPlayerId) {
		return numMinions_[currentPlayerId][turn][playerId];
	}

	@Override
	public int getNumCardsInHand(int playerId, int turn, int currentPlayerId) {
		return numCards_[currentPlayerId][turn][playerId];
	}

	@Override
	public int getHeroHealth(int playerId, int turn, int currentPlayerId) {
		return heroHealth_[currentPlayerId][turn][playerId];
	}

	@Override
	public int getHeroArmor(int playerId, int turn, int currentPlayerId) {
		return heroArmor_[currentPlayerId][turn][playerId];
	}
	
	/**
	 * Where most of the magic happens.  Attempt to convert a turn into a JSON file
	 * A lot of what happens in a turn is based on what happened during the previous turn
	 */
	public JSONObject getTurn(int playerId, int turn){
		JSONObject enclosingObj = new JSONObject();
		
		//get the state list for this turn
		List<HearthActionBoardPair> states = state_.get(turn).get(playerId).getFirst();
		List<HearthActionRecord> recordedActions = state_.get(turn).get(playerId).getSecond();
		
		log.debug("Recorded Actions This Turn: ");
		if(recordedActions != null){
			for(HearthActionRecord element : recordedActions){
				log.debug(element.getActionVerb().toString());
				log.debug(element.getPerformer().toString());
				log.debug(element.getTarget().toString());
			}
		}
		List<HearthActionBoardPair> previousTurn = null;
		List<HearthActionBoardPair> opponentPreviousTurn = null;
		
		if(turn != 0){
			previousTurn = state_.get(turn-1).get(playerId).getFirst();
			int opponentId = playerId == 0 ? 1 : 0;
			
			if(playerId == firstPlayer){
				opponentPreviousTurn = state_.get(turn - 1).get(opponentId).getFirst();
			}else{
				opponentPreviousTurn = state_.get(turn).get(opponentId).getFirst();
			}
		}
		
		if(states == null){
			return enclosingObj;
		}
		
		//log the starting turn values for health and armor
		enclosingObj = logTurnStartingValues(enclosingObj, heroHealth_, heroArmor_, playerId, turn);
		
		//log the starting board
		//for turn 0, the board is empty 
		HearthActionBoardPair lastActionBoardPair = null;
		if(turn != 0 && opponentPreviousTurn != null && !opponentPreviousTurn.isEmpty()){
			lastActionBoardPair = opponentPreviousTurn.get(opponentPreviousTurn.size() - 1);
			enclosingObj.put("startBoard", getBoard(lastActionBoardPair.board, true));
		}else{
			enclosingObj.put("startBoard", new JSONObject());
		}
		
		//log the final turn values for health and armor
		enclosingObj.put("endCurrentPlayerHealth", heroHealth_[playerId][turn][playerId]);
		enclosingObj.put("endCurrentPlayerArmor", heroArmor_[playerId][turn][playerId]);
		
		if(playerId == 0){
			enclosingObj.put("endOpponentPlayerHealth", heroHealth_[1][turn][1]);
			enclosingObj.put("endOpponentPlayerArmor", heroArmor_[1][turn][1]);
		}else{
			enclosingObj.put("endOpponentPlayerHealth", heroHealth_[0][turn][0]);
			enclosingObj.put("endOpponentPlayerArmor", heroArmor_[0][turn][0]);
		}
		
		//log the actual turn as states
		JSONArray statesJSON = new JSONArray();
		
		//the ofset between the state list and the recorded actions.  This happens due to the fact that we don't record _everything_ but stuff
		//should still roughly be in the same order
		int listOffset = 0;
		
		//for each state in the state array...
		for(int i = 0; i < states.size(); i++){
			JSONObject json = new JSONObject();
			HearthActionBoardPair state = states.get(i);
			HearthActionRecord actRecord = null;
			
			//get the action for this state
			HearthAction act = state.action;
			
			//if this action has a history component, get it.  Otherwise, don't.
			if(!recordedActions.isEmpty() && 
					i + listOffset < recordedActions.size() && 
					state.action.verb_ == recordedActions.get(i + listOffset).getActionVerb()){
				
				actRecord = recordedActions.get(i + listOffset);
			}else{
				//decriment the offset (the action list should always be longer than the recorded state list) to keep the lists aligned
				listOffset = listOffset - 1;
			}
			
			//get the board for this state (this is the board right after the state has occurred)
			BoardModel boardState = state.board;
			//this is the previous board model before this one
			BoardModel previousBoardState = getPreviousBoardState(i, states, turn, previousTurn, opponentPreviousTurn);
			
			JSONObject actJSON = new JSONObject();
			
			if(actRecord != null){
				//actions can now generate JSON objects that describe them, but we still need to convert the indexes that they present to 
				//names
				//Joe is picky.
				//ActionDescription actData = ActionDescription.fromJSON(act.toJSON()); //TODO: this is pretty slow.
				
				//log the verb associated with this action
				actJSON.put("verb", actRecord.getActionVerb().toString());
				try {
					
					actJSON = recordEvent(actRecord, actJSON, previousBoardState, i, turn);
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				json.put("action", actJSON);
			}else{
				json.put("action", "null");
			}
			
			if(previousBoardState != null){
				json.put("preActionBoard", getBoard(previousBoardState, false));
			}else{
				json.put("preActionBoard", "null");
			}
			
			if(boardState != null){
				json.put("postActionBoard", getBoard(boardState, false));
			}else{
				json.put("postActionBoard", "null");
			}
			
			statesJSON.put(json);
		}
		enclosingObj.put("states", statesJSON);
		
		return enclosingObj;
	}
	
	/**
	 * Get the previous state.
	 * 
	 * This... this is intense.  Get ready.
	 * TODO: this needs to get updated.  Essentally, if both players just stop doing things for more than a turn, the model will cry
	 * 		as it tries to get the last state for the pre-action logging
	 * @param i interal location in this turn
	 * @param states states that occured in this turn
	 * @param turn current turn
	 * @param previousTurn states that occured in our previous turn
	 * @param opponentPreviousTurn states that occured in the opponent's previous turn
	 * @return
	 * 
	 * @FIXME (at some point tonight, or maybe very early tomorrow morning) --> set this up to recursively look at past turns until we have turn info
	 */
	private BoardModel getPreviousBoardState(int i,
			List<HearthActionBoardPair> states, int turn,
			List<HearthActionBoardPair> previousTurn,
			List<HearthActionBoardPair> opponentPreviousTurn) {

		BoardModel returnState = null;
		if(turn == 1){
			returnState = this.turn0Model;
		}else if(i > 0 && turn > 1){
			//there is a previous state this turn we can use
			returnState = states.get(i-1).board;
		}else if( i == 0 && turn > 1){
			//we're at the first state of a turn.  We need to get the previous turn's last state
			//in most cases, that's the flipped version of the opponent's last turn
			
			if(!opponentPreviousTurn.isEmpty()){
				returnState = opponentPreviousTurn.get(opponentPreviousTurn.size() - 1).board.flipPlayers();
			}else if (!previousTurn.isEmpty()){
				//couldn't get anything from the opponent's last turn.  Try to use the current last from our last turn
				log.debug("===== OPPONENT STATE NULL ===== TRYING OUR LAST STATE ====");
				returnState = previousTurn.get(previousTurn.size() - 1).board;
			}else{
				log.debug("===== PREVIOUS STATE IS NULL ====== PREVIOUS STATE IS NULL ====== PREVIOUS STATE IS NULL ===== PREVIOUS STATE IS NULL ======");
				log.debug("Failure to assign previous state information");
				log.debug("Both our last turn and our opponent's last turn were null");
				returnState = null;
			}
		}else{
			//this is the 0th turn
			log.debug("===== PREVIOUS STATE IS NULL ====== PREVIOUS STATE IS NULL ====== PREVIOUS STATE IS NULL ===== PREVIOUS STATE IS NULL ======");
			log.debug("Failure to assign previous state information");
			log.debug("Turn " + turn);
			log.debug("i " + i);
			returnState = null;
		}
		
		return returnState;
	}

	private JSONObject recordEvent(HearthActionRecord actRecord, JSONObject actJSON, BoardModel pastBoardState, int i, int turn){
		if(actRecord.getActionVerb() == Verb.ATTACK){
			actJSON = recordAttack(actRecord, actJSON, pastBoardState);
		}else if(actRecord.getActionVerb() == Verb.USE_CARD){
			actJSON = recordUseCard(actRecord, actJSON, pastBoardState, i, turn);
		}else{
			log.debug(actRecord.getActionVerb().toString() + " currently is unlogged.");
		}
		
		return actJSON;
	}
	
	/**
	 * Record the target and performer of the USE_CARD action
	 * 
	 * @param actRecord the action description of the USE_CARD action.  Contains indexes for target and performer
	 * @param actJSON the JSON object to write the record too
	 * @param boardState the board state that maps to the indexes
	 * @param i error reporting index
	 * @param turn error reporting turn
	 * 
	 * @return actJSON, modified with the correct writes
	 * 
	 * This abuses a try-catch block to handle the fact that top decking information gets lost in this model
	 * The obvious fix is to log the actual targets (minions / cards) in logging objects, but that'll take a serious rewrite.
	 * s
	 */
	private JSONObject recordUseCard(HearthActionRecord actRecord, JSONObject actJSON, BoardModel boardState, int i, int turn) {
		actJSON.put("target", actRecord.getTarget().getName());
		actJSON.put("performer", actRecord.getPerformer().getName());
		
		return actJSON;
	}

	/**
	 * 
	 * @param actRecord
	 * @param actJSON
	 * @param pastBoardState
	 * @return
	 */
	private JSONObject recordAttack(HearthActionRecord actRecord, JSONObject actJSON, BoardModel boardState){		
		actJSON.put("target", actRecord.getTarget().getName());
		actJSON.put("performer", actRecord.getPerformer().getName());
		
		return actJSON;
	}
	
	/**
	 * Convert a board into a JSON object that gives both the board for both players
	 * @param board the board state object to record
	 * @param flip record the result flipped 
	 * @return
	 */
	private JSONObject getBoard(BoardModel board, boolean flip) {
		JSONObject ret = new JSONObject();
		
		Iterable<MinionPlayerPair> minionPlayerPairs = board.getAllMinionsFIFOList();
		List<Minion> p0Minions = new ArrayList<Minion>();
		List<Minion> p1Minions = new ArrayList<Minion>();
		
		for(MinionPlayerPair pair : minionPlayerPairs){
			if(pair.getPlayerSide() == PlayerSide.CURRENT_PLAYER){
				p0Minions.add(pair.getMinion());
			}else{
				p1Minions.add(pair.getMinion());
			}
		}
		
		JSONArray performerMinionsJSON = new JSONArray();
		for(Minion minion : p0Minions){
			JSONObject minionJSON = new JSONObject();
			minionJSON.put("name", minion.getName());
			minionJSON.put("attack", minion.getAttack());
			minionJSON.put("health", minion.getHealth());
			minionJSON.put("taunt", minion.getTaunt());
			minionJSON.put("divine shield", minion.getDivineShield());
			minionJSON.put("wind fury", minion.getWindfury());
			minionJSON.put("charge", minion.getCharge());
			minionJSON.put("frozen", minion.getFrozen());
			minionJSON.put("stealthed", minion.getStealthed());
			performerMinionsJSON.put(minionJSON);
		}
		
		JSONArray targetMinionsJSON = new JSONArray();
		for(Minion minion : p1Minions){
			JSONObject minionJSON = new JSONObject();
			minionJSON.put("name", minion.getName());
			minionJSON.put("attack", minion.getAttack());
			minionJSON.put("health", minion.getHealth());
			minionJSON.put("taunt", minion.getTaunt());
			minionJSON.put("divine shield", minion.getDivineShield());
			minionJSON.put("wind fury", minion.getWindfury());
			minionJSON.put("charge", minion.getCharge());
			minionJSON.put("frozen", minion.getFrozen());
			minionJSON.put("stealthed", minion.getStealthed());
			targetMinionsJSON.put(minionJSON);
		}
		
		if(!flip){
			ret.put("currentPlayerMinions", performerMinionsJSON);
			ret.put("opposingPlayerMinions", targetMinionsJSON);
		}else{
			ret.put("currentPlayerMinions", targetMinionsJSON);
			ret.put("opposingPlayerMinions", performerMinionsJSON);
		}
		
		return ret;
	}
	
	private JSONObject logTurnStartingValues(JSONObject enclosingObj, byte[][][] heroHealth_, byte[][][] heroArmor_, int playerId, int turn){
		if(turn != 0){
			enclosingObj.put("startCurrentPlayerHealth", heroHealth_[playerId][turn - 1][playerId]);
			enclosingObj.put("startCurrentPlayerArmor", heroArmor_[playerId][turn - 1][playerId]);
			
			if(playerId == 0){
				enclosingObj.put("startOpponentPlayerHealth", heroHealth_[1][turn - 1][1]);
				enclosingObj.put("startOpponentPlayerArmor", heroArmor_[1][turn - 1][1]);
			}else{
				enclosingObj.put("startOpponentPlayerHealth", heroHealth_[0][turn - 1][0]);
				enclosingObj.put("startOpponentPlayerArmor", heroArmor_[0][turn - 1][0]);
			}
		}else{
			enclosingObj.put("startCurrentPlayerHealth", 30);
			enclosingObj.put("startCurrentPlayerArmor", 0);
			
			if(playerId == 0){
				enclosingObj.put("startOpponentPlayerHealth", 30);
				enclosingObj.put("startOpponentPlayerArmor", 0);
			}else{
				enclosingObj.put("startOpponentPlayerHealth", 30);
				enclosingObj.put("startOpponentPlayerArmor", 0);
			}
		}
		
		return enclosingObj;
	}

	@Override
	public JSONObject toJSON() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFirstPlayer(int firstPlayerId) {
		firstPlayer = firstPlayerId;
	}
}
