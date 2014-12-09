package com.hearthsim.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hearthsim.card.minion.Hero;
import com.hearthsim.card.minion.Minion;
import com.hearthsim.exception.HSInvalidPlayerIndexException;
import com.hearthsim.model.BoardModel;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.util.HearthAction;
import com.hearthsim.util.HearthAction.Verb;
import com.hearthsim.util.HearthActionBoardPair;
import com.hearthsim.util.MinionList;

public class GameSimpleRecord implements GameRecord {

	final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GameSimpleRecord.class);
	
	int maxTurns_;
	byte[][][] numMinions_;
	byte[][][] numCards_;
	byte[][][] heroHealth_;
	byte[][][] heroArmor_;
	List<Map<Integer, List<HearthActionBoardPair>>> state_;
	
	Map<Integer, List<Map<Integer, MinionList>>> board_;
	Map<Integer, List<List<HearthAction>>> actions_;
	
	public GameSimpleRecord() {
		this(50);
	}
	
	public GameSimpleRecord(int maxTurns) {
		maxTurns_ = maxTurns;
		numMinions_ = new byte[2][maxTurns][2];
		numCards_ = new byte[2][maxTurns][2];
		heroHealth_ = new byte[2][maxTurns][2];
		heroArmor_ = new byte[2][maxTurns][2];
		
		state_ = new ArrayList<Map<Integer, List<HearthActionBoardPair>>>();
		
		board_ = new HashMap<Integer, List<Map<Integer, MinionList>>>();
		actions_ = new HashMap<Integer, List<List<HearthAction>>>();
	}
	
	@Override
	public void put(int turn, PlayerSide activePlayerSide, BoardModel board, List<HearthActionBoardPair> plays) {
        PlayerModel playerModel = board.modelForSide(activePlayerSide);

        int currentPlayerId = playerModel.getPlayerId();
        int waitingPlayerId = board.modelForSide(activePlayerSide.getOtherPlayer()).getPlayerId();
        
        //This handles getting a board side on a turn from a player perspective
        List<Map<Integer, MinionList>> fullSideInformation;
        if(board_.get(turn) != null){
        	fullSideInformation = board_.get(turn);
        }else{
        	fullSideInformation = new ArrayList<Map<Integer, MinionList>>();
        }
        HashMap<Integer, MinionList> boardSides = new HashMap<Integer, MinionList>();
        try {
			boardSides.put(currentPlayerId, board.getMinions(activePlayerSide));
			boardSides.put(waitingPlayerId, board.getMinions(activePlayerSide.getOtherPlayer()));
		} catch (HSInvalidPlayerIndexException e) {
			e.printStackTrace();
		}
        if(fullSideInformation.size() < currentPlayerId){
        	//add a blank element in this list (we only have data from the other side.)
        	fullSideInformation.add(new HashMap<Integer, MinionList>());
        }
        
        fullSideInformation.add(currentPlayerId, boardSides);
        board_.put(turn, fullSideInformation);
        
        List<List<HearthAction>> actionsTakenByPlayer = new  ArrayList<List<HearthAction>>();
        List<HearthAction> actions = new ArrayList<HearthAction>();
        
        if(plays != null){
        	for(HearthActionBoardPair act : plays){
        		actions.add(act.action);
        	}
        }
        if(actionsTakenByPlayer.size() < currentPlayerId){
        	//add a blank element in this list (no data from the other side)
        	actionsTakenByPlayer.add(new ArrayList<HearthAction>());
        }
        actionsTakenByPlayer.add(currentPlayerId, actions);
        actions_.put(turn, actionsTakenByPlayer);
        
        //New and improved state logging
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

        numCards_[currentPlayerId][turn][currentPlayerId] = (byte)board.getNumCardsHandCurrentPlayer();
        numCards_[currentPlayerId][turn][waitingPlayerId] = (byte)board.getNumCardsHandWaitingPlayer();

        Hero currentPlayerHero = board.getCurrentPlayerHero();
        heroHealth_[currentPlayerId][turn][currentPlayerId] = currentPlayerHero.getHealth();
        Hero waitingPlayerHero = board.getWaitingPlayerHero();
        heroHealth_[currentPlayerId][turn][waitingPlayerId] = waitingPlayerHero.getHealth();

        heroArmor_[currentPlayerId][turn][currentPlayerId] = currentPlayerHero.getArmor();
        heroArmor_[currentPlayerId][turn][waitingPlayerId] = waitingPlayerHero.getArmor();
    }

	@Override
	public int getRecordLength(int playerId) {
		return board_.keySet().size();
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

	public JSONObject getBoardSide(int playerId, int turn, int currentPlayerId){
		JSONObject json = new JSONObject();
		//defensive programming!
		MinionList minions = null;
		
		if(board_.containsKey(turn) && board_.get(turn).size() > currentPlayerId && 
				board_.get(turn).get(currentPlayerId) != null && 
				board_.get(turn).get(currentPlayerId).containsKey(playerId)){
		    minions = board_.get(turn).get(currentPlayerId).get(playerId);
		}
		
		if(minions == null){
			return null;
		}
		
		for(Minion minion : minions){
			json.put("name", minion.getName());
			json.put("attack", minion.getAttack());
			json.put("health", minion.getHealth());
			json.put("taunt", minion.getTaunt());
			json.put("divine shield", minion.getDivineShield());
			json.put("wind fury", minion.getWindfury());
			json.put("charge", minion.getCharge());
			json.put("frozen", minion.getFrozen());
			json.put("stealthed", minion.getStealthed());
		}
		
		return json;		
	}
	
	public JSONObject getActions(int playerId, int turn, int currentPlayerId){
		JSONObject json = new JSONObject();
		//defensive programming!
		
		List<HearthAction> actions = null;
		if(actions_.containsKey(turn) && actions_.get(turn).size() > currentPlayerId &&
				actions_.get(turn).get(currentPlayerId) != null){
			actions = actions_.get(turn).get(currentPlayerId);
		}
		
		if(actions == null){
			return null;
		}
		
		for(HearthAction act : actions){
			if(act != null){
				if(act.verb_ != null){
					json.put("verb", act.verb_.toString());
				}
				json.put("target index", act.targetCharacterIndex_);
				if(act.actionPerformerPlayerSide != null){
					json.put("performed by", act.actionPerformerPlayerSide.name());
				}
				
			}
		}
		return json;
	}
	
	public JSONObject getTurn(int playerId, int turn){
		JSONObject enclosingObj = new JSONObject();
		List<HearthActionBoardPair> states = state_.get(turn).get(playerId);
		
		if(states == null){
			return enclosingObj;
		}
		
		enclosingObj.put("currentPlayerHealth", heroHealth_[playerId][turn][playerId]);
		enclosingObj.put("currentPlayerArmor", heroArmor_[playerId][turn][playerId]);
		if(playerId == 0){
			enclosingObj.put("opponentPlayerHealth", heroHealth_[1][turn][1]);
			enclosingObj.put("opponentPlayerArmor", heroArmor_[1][turn][1]);
		}else{
			enclosingObj.put("opponentPlayerHealth", heroHealth_[0][turn][0]);
			enclosingObj.put("opponentPlayerArmor", heroArmor_[0][turn][0]);
		}
		
		JSONArray statesJSON = new JSONArray();
		for(int i = 0; i < states.size(); i++){
			JSONObject json = new JSONObject();
			HearthActionBoardPair state = states.get(i);
			
			HearthAction act = state.action;
			BoardModel board = state.board;
			
			JSONObject actJSON = new JSONObject();
			JSONObject boardJSON = new JSONObject();
			
			if(act != null){
				actJSON.put("verb", act.verb_.toString());
				try {
					if(act.verb_ == Verb.ATTACK){
						if(turn != 0 && i != 0){
							if(act.targetCharacterIndex_ < states.get(i-1).board.getMinions(act.targetPlayerSide).size() + 1){
								actJSON.put("target", states.get(i-1).board.getCharacter(act.targetPlayerSide, act.targetCharacterIndex_).getName() + "-" + act.targetCharacterIndex_);
							}else{
								actJSON.put("target_index", act.targetCharacterIndex_);
							}
							if(act.cardOrCharacterIndex_ < states.get(i-1).board.getMinions(act.actionPerformerPlayerSide).size() + 1){
								actJSON.put("performer", states.get(i-1).board.getCharacter(act.actionPerformerPlayerSide, act.cardOrCharacterIndex_).getName() + "-" + act.cardOrCharacterIndex_);
							}else{
								actJSON.put("performer_index", act.cardOrCharacterIndex_);
							}
						}else if(turn != 0 && i == 0){
							HearthActionBoardPair previousTurnState = state_.get(turn - 1).get(playerId).get(state_.get(turn - 1).get(playerId).size() - 1);
							if(act.targetCharacterIndex_ < previousTurnState.board.getMinions(act.targetPlayerSide).size() + 1){
								actJSON.put("target", previousTurnState.board.getCharacter(act.targetPlayerSide,  act.targetCharacterIndex_).getName() + "-" + act.targetCharacterIndex_);
							}else{
								actJSON.put("target_index", act.targetCharacterIndex_);
							}
						
							if(act.cardOrCharacterIndex_ < previousTurnState.board.getMinions(act.actionPerformerPlayerSide).size() + 1){
								actJSON.put("performer", previousTurnState.board.getCharacter(act.actionPerformerPlayerSide, act.cardOrCharacterIndex_).getName() + "-" + act.cardOrCharacterIndex_);
							}else{
								actJSON.put("performer_index", act.cardOrCharacterIndex_);
							}
						}else{
							actJSON.put("target_index", act.targetCharacterIndex_);
							actJSON.put("performer_index", act.cardOrCharacterIndex_);
						}
					}else if(act.verb_ == Verb.USE_CARD){
						if(turn != 0 && i != 0){
							if(act.targetCharacterIndex_ < states.get(i-1).board.getMinions(act.targetPlayerSide).size() + 1){
								actJSON.put("target", states.get(i-1).board.getCharacter(act.targetPlayerSide, act.targetCharacterIndex_).getName() + "-" + states.get(i-1).board.getIndexOfPlayer(act.targetPlayerSide));
							}else{
								actJSON.put("target_index", act.targetCharacterIndex_);
							}
							if(act.cardOrCharacterIndex_ < states.get(i-1).board.getNumCards_hand(act.actionPerformerPlayerSide)){
								actJSON.put("performer", states.get(i-1).board.getCard_hand(act.actionPerformerPlayerSide, act.cardOrCharacterIndex_).getName() + "-" + states.get(i-1).board.getIndexOfPlayer(act.actionPerformerPlayerSide));
							}else{
								actJSON.put("performer_index", act.cardOrCharacterIndex_);
							}
						}else if(turn != 0 && i == 0){
							List<HearthActionBoardPair> previousTurn = state_.get(turn-1).get(playerId);
							HearthActionBoardPair previousTurnState = null;
							if(previousTurn == null){
								previousTurn = state_.get(i).get(playerId);
								if(previousTurn != null){
									previousTurnState = previousTurn.get(previousTurn.size() - 1);
								}else{
									//was completely unable to get previous turn information, which includes subbing out the current turn
									actJSON.put("target_index", act.targetCharacterIndex_);
									actJSON.put("performer_index", act.cardOrCharacterIndex_);
									break;
								}
							}else{
								previousTurnState = previousTurn.get(previousTurn.size() - 1);
							}
							if(act.targetCharacterIndex_ < previousTurnState.board.getMinions(act.targetPlayerSide).size() + 1){
								actJSON.put("target", previousTurnState.board.getCharacter(act.targetPlayerSide,  act.targetCharacterIndex_).getName() + "-" + previousTurnState.board.getIndexOfPlayer(act.targetPlayerSide));
							}else{
								actJSON.put("target_index", act.targetCharacterIndex_);
							}
						
							if(act.cardOrCharacterIndex_ < previousTurnState.board.getNumCards_hand(act.actionPerformerPlayerSide)){
								actJSON.put("performer", previousTurnState.board.getCard_hand(act.actionPerformerPlayerSide, act.cardOrCharacterIndex_).getName() + "-" + previousTurnState.board.getIndexOfPlayer(act.actionPerformerPlayerSide));
							}else{
								actJSON.put("performer_index", act.cardOrCharacterIndex_);
							}
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (HSInvalidPlayerIndexException e) {
					e.printStackTrace();
				}
				json.put("action", actJSON);
			}else{
				json.put("action", "null");
			}
			
			
			if(board != null){
				try {
					MinionList p0Minions = board.getMinions(board.getPlayerByIndex(0));
					MinionList p1Minions = board.getMinions(board.getPlayerByIndex(1));
					
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
					
					boardJSON.put("currentPlayerMinions", performerMinionsJSON);
					boardJSON.put("opposingPlayerMinions", targetMinionsJSON);
					
					json.put("board", boardJSON);
					
				} catch (HSInvalidPlayerIndexException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				json.put("board", "null");
			}
			
			statesJSON.put(json);
		}
		enclosingObj.put("states", statesJSON);
		return enclosingObj;
	}
	@Override
	public JSONObject toJSON() {
		// TODO Auto-generated method stub
		return null;
	}

}
