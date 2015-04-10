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
import com.hearthsim.util.HearthAction;
import com.hearthsim.util.HearthAction.Verb;
import com.hearthsim.util.HearthActionBoardPair;
import com.hearthsim.util.IdentityLinkedList;

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
	List<Map<Integer, List<HearthActionBoardPair>>> state_;
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
		
		state_ = new ArrayList<Map<Integer, List<HearthActionBoardPair>>>();
		boardStateOnTurn = new ArrayList<BoardModel>();
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
        Map<Integer, List<HearthActionBoardPair>> turnInformation;
        if(state_.size() <= turn || state_.get(turn) == null){
        	turnInformation = new HashMap<Integer, List<HearthActionBoardPair>>();
        }else{
        	turnInformation = state_.get(turn);
        }
        
        List<HearthActionBoardPair> actionLog;
        
        if(turnInformation.containsKey(currentPlayerId)){
        	actionLog = turnInformation.get(currentPlayerId);
        	actionLog.addAll(plays);
        	turnInformation.put(currentPlayerId, actionLog);
        }else{
        	actionLog = plays;
        	turnInformation.put(currentPlayerId, actionLog);
        }
        
        if(state_.size() != 0 && state_.size() > turn){
        	//replace
        	state_.remove(turn);
        	state_.add(turn, turnInformation);
        }else{
        	state_.add(turn, turnInformation);
        }
        
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
		List<HearthActionBoardPair> states = state_.get(turn).get(playerId);
		List<HearthActionBoardPair> previousTurn = null;
		List<HearthActionBoardPair> opponentPreviousTurn = null;
		
		if(turn != 0){
			previousTurn = state_.get(turn-1).get(playerId);
			int opponentId = playerId == 0 ? 1 : 0;
			
			if(playerId == firstPlayer){
				opponentPreviousTurn = state_.get(turn - 1).get(opponentId);
			}else{
				opponentPreviousTurn = state_.get(turn).get(opponentId);
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
		
		//for each state in the state array...
		for(int i = 0; i < states.size(); i++){
			JSONObject json = new JSONObject();
			HearthActionBoardPair state = states.get(i);
			
			//get the action for this state
			HearthAction act = state.action;
			
			//get the board for this state (this is the board right after the state has occurred)
			BoardModel boardState = state.board;
			//this is the previous board model before this one
			BoardModel previousBoardState = getPreviousBoardState(i, states, turn, previousTurn, opponentPreviousTurn);
			
			JSONObject actJSON = new JSONObject();
			
			if(act != null){
				//actions can now generate JSON objects that describe them, but we still need to convert the indexes that they present to 
				//names
				//Joe is picky.
				ActionDescription actData = ActionDescription.fromJSON(act.toJSON()); //TODO: this is pretty slow.
				
				//log the verb associated with this action
				actJSON.put("verb", actData.verb.toString());
				try {
					
					actJSON = recordEvent(actData, actJSON, previousBoardState, i, turn);
					
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
	 * @param i
	 * @param states
	 * @param turn
	 * @param previousTurn
	 * @param opponentPreviousTurn
	 * @return
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

	/**
	 * Get the minions on the board for a particular side
	 * @param board a board model to extract minions from
	 * @param side a side to extract minions from
	 * 
	 * @return a list of minions, the list can be empty
	 * 
	 * TODO I'm assuming that minion placement will be correct in the
	 
	private List<Minion> extractMinions(BoardModel board, PlayerSide side){
		IdentityLinkedList<MinionPlayerPair> minionPlayerPairs = board.getAllMinionsFIFOList();
		List<Minion> minions = new ArrayList<Minion>(); //using array lists for O(1) indexing
		for(MinionPlayerPair pair : minionPlayerPairs){
			if(pair.getPlayerSide() == side){
				minions.add(pair.getMinion());
			}
		}
		
		return minions;
	}
	*/
	
	private JSONObject recordEvent(ActionDescription actData, JSONObject actJSON, BoardModel pastBoardState, int i, int turn){
		if(actData.verb == Verb.ATTACK){
			actJSON = recordAttack(actData, actJSON, pastBoardState);
		}else if(actData.verb == Verb.USE_CARD){
			actJSON = recordUseCard(actData, actJSON, pastBoardState, i, turn);
		}else{
			log.debug(actData.verb.toString() + " currently is unlogged.");
		}
		
		return actJSON;
	}
	
	/**
	 * 
	 * @param actData
	 * @param actJSON
	 * @param pastBoardState
	 * @return
	 */
	private JSONObject recordUseCard(ActionDescription actData, JSONObject actJSON, BoardModel boardState, int i, int turn) {
		try{
			actJSON.put("target", boardState.getCharacter(actData.targetPlayerSide, 
					actData.targetCharacterIndex).getName() + "-" + actData.targetCharacterIndex);
		}catch(Exception e){
			//if that doesn't work, fall back on indexes
			log.debug(e.toString());
			log.debug(e.getLocalizedMessage());
			actJSON.put("target_index", actData.targetCharacterIndex);
		}
		
		try{
			actJSON.put("performer", boardState.getCard_hand(actData.actionPerformerPlayerSide,
					actData.cardOrCharacterIndex).getName() + "-" + actData.cardOrCharacterIndex);
		}catch(Exception e){
			//if that doesn't work, fall back on indexes
			log.debug(e.toString());
			log.debug(e.getLocalizedMessage());
			//FIXME: There is a case where, if top-decking, a card just straight up doesn't get logged in hand.  The fix is to go to the previous state
			//and draw the card.
			
			//FIXME: This is gross, and I hate it, but it works.
			boardState.drawCardFromCurrentPlayerDeck(1);
			actJSON.put("performer", boardState.getCard_hand(actData.actionPerformerPlayerSide, actData.cardOrCharacterIndex).getName() + "-" + actData.cardOrCharacterIndex);
			actJSON.put("performer_index", actData.cardOrCharacterIndex);
		}
		
		return actJSON;
	}

	/**
	 * 
	 * @param actData
	 * @param actJSON
	 * @param pastBoardState
	 * @return
	 */
	private JSONObject recordAttack(ActionDescription actData, JSONObject actJSON, BoardModel boardState){
		try{
			actJSON.put("target", boardState.getCharacter(actData.targetPlayerSide, 
					actData.targetCharacterIndex).getName() + "-" + actData.targetCharacterIndex);
		}catch(Exception e){
			e.printStackTrace();
			//if that doesn't work, fall back on indexes
			log.debug(e.toString());
			log.debug(e.getLocalizedMessage());
			actJSON.put("target_index", actData.targetCharacterIndex);
		}
		
		try{
			actJSON.put("performer", boardState.getCharacter(actData.actionPerformerPlayerSide,
					actData.cardOrCharacterIndex).getName() + "-" + actData.cardOrCharacterIndex);
		}catch(Exception e){
			e.printStackTrace();
			//if that doesn't work, fall back on indexes
			log.debug(e.toString());
			log.debug(e.getLocalizedMessage());
			actJSON.put("performer_index", actData.cardOrCharacterIndex);
		}
		
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
		
		IdentityLinkedList<MinionPlayerPair> minionPlayerPairs = board.getAllMinionsFIFOList();
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
