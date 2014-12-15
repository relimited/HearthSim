package com.hearthsim.results;

import java.util.ArrayList;
import java.util.Collection;
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
	int firstPlayer;
	
	byte[][][] numMinions_;
	byte[][][] numCards_;
	byte[][][] heroHealth_;
	byte[][][] heroArmor_;
	List<Map<Integer, List<HearthActionBoardPair>>> state_;
	List<BoardModel> boardStateOnTurn;
	
	BoardModel turn0Model;		//special place to store information on turn 0, which normally gets ignored.
	
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
		boardStateOnTurn = new ArrayList<BoardModel>();
	}
	
	@Override
	public void put(int turn, PlayerSide activePlayerSide, BoardModel board, List<HearthActionBoardPair> plays) {
        PlayerModel playerModel = board.modelForSide(activePlayerSide);

        int currentPlayerId = playerModel.getPlayerId();
        int waitingPlayerId = board.modelForSide(activePlayerSide.getOtherPlayer()).getPlayerId();
        
        //New and improved state logging
        if(turn == 0){
        	turn0Model = board; //we need to capture the state at turn 0 to have a snowballs chance in hell of being able to log turn 1 correctly
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
	
	public JSONObject getTurn(int playerId, int turn){
		JSONObject enclosingObj = new JSONObject();
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
		
		if(turn != 0 && opponentPreviousTurn != null && !opponentPreviousTurn.isEmpty()){
			HearthActionBoardPair lastActionBoardPair = opponentPreviousTurn.get(opponentPreviousTurn.size() - 1);
			enclosingObj.put("startBoard", getBoard(lastActionBoardPair.board, true));
		}else{
			enclosingObj.put("startBoard", new JSONObject());
		}
		
		enclosingObj.put("endCurrentPlayerHealth", heroHealth_[playerId][turn][playerId]);
		enclosingObj.put("endCurrentPlayerArmor", heroArmor_[playerId][turn][playerId]);
		if(playerId == 0){
			enclosingObj.put("endOpponentPlayerHealth", heroHealth_[1][turn][1]);
			enclosingObj.put("endOpponentPlayerArmor", heroArmor_[1][turn][1]);
		}else{
			enclosingObj.put("endOpponentPlayerHealth", heroHealth_[0][turn][0]);
			enclosingObj.put("endOpponentPlayerArmor", heroArmor_[0][turn][0]);
		}
		
		JSONArray statesJSON = new JSONArray();
		
		for(int i = 0; i < states.size(); i++){
			JSONObject json = new JSONObject();
			HearthActionBoardPair state = states.get(i);
			
			HearthAction act = state.action;
			BoardModel board = state.board;
			
			JSONObject actJSON = new JSONObject();			
			if(act != null){
				actJSON.put("verb", act.verb_.toString());
				try {
					if(act.verb_ == Verb.ATTACK){
						if(turn != 0 && i != 0){
							if(act.targetCharacterIndex_ < states.get(i-1).board.getMinions(act.targetPlayerSide).size() + 1){
								actJSON.put("target", states.get(i-1).board.getCharacter(act.targetPlayerSide, 
										act.targetCharacterIndex_).getName() + "-" + act.targetCharacterIndex_);
							}else{
								actJSON.put("target_index", act.targetCharacterIndex_);
							}
							if(act.cardOrCharacterIndex_ < states.get(i-1).board.getMinions(act.actionPerformerPlayerSide).size() + 1){
								actJSON.put("performer", states.get(i-1).board.getCharacter(act.actionPerformerPlayerSide, 
										act.cardOrCharacterIndex_).getName() + "-" + act.cardOrCharacterIndex_);
							}else{
								actJSON.put("performer_index", act.cardOrCharacterIndex_);
							}
						}else if(turn != 0 && i == 0){
							HearthActionBoardPair previousTurnState = previousTurn.get(previousTurn.size() - 1);
							if(act.targetCharacterIndex_ < previousTurnState.board.getMinions(act.targetPlayerSide).size() + 1){
								actJSON.put("target", previousTurnState.board.getCharacter(act.targetPlayerSide,  
										act.targetCharacterIndex_).getName() + "-" + act.targetCharacterIndex_);
							}else{
								actJSON.put("target_index", act.targetCharacterIndex_);
							}
						
							if(act.cardOrCharacterIndex_ < previousTurnState.board.getMinions(act.actionPerformerPlayerSide).size() + 1){
								actJSON.put("performer", previousTurnState.board.getCharacter(act.actionPerformerPlayerSide, 
										act.cardOrCharacterIndex_).getName() + "-" + act.cardOrCharacterIndex_);
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
								actJSON.put("target", states.get(i-1).board.getCharacter(act.targetPlayerSide, 
										act.targetCharacterIndex_).getName() + "-" + states.get(i-1).board.getIndexOfPlayer(act.targetPlayerSide));
							}else{
								actJSON.put("target_index", act.targetCharacterIndex_);
							}
							if(act.cardOrCharacterIndex_ < states.get(i-1).board.getNumCards_hand(act.actionPerformerPlayerSide)){
								actJSON.put("performer", states.get(i-1).board.getCard_hand(act.actionPerformerPlayerSide, 
										act.cardOrCharacterIndex_).getName() + "-" + states.get(i-1).board.getIndexOfPlayer(act.actionPerformerPlayerSide));
							}else{
								actJSON.put("performer_index", act.cardOrCharacterIndex_);
							}
						}else if(turn != 0 && i == 0){
							boolean skip_log = false;
							
							HearthActionBoardPair previousTurnState = null;
							//if this is turn 1, we need to try and get indexes from the special turn 0 var
							if(turn == 1){
								skip_log = true;
								
								if(act.targetCharacterIndex_ < this.turn0Model.getMinions(act.targetPlayerSide).size() + 1){
									actJSON.put("target", this.turn0Model.getCharacter(act.targetPlayerSide,  act.targetCharacterIndex_).getName() + 
											"-" + this.turn0Model.getIndexOfPlayer(act.targetPlayerSide));
								}else{
									actJSON.put("target_index", act.targetCharacterIndex_);
								}
						
								if(act.cardOrCharacterIndex_ < this.turn0Model.getNumCards_hand(act.actionPerformerPlayerSide)){
									actJSON.put("performer", this.turn0Model.getCard_hand(act.actionPerformerPlayerSide, act.cardOrCharacterIndex_).getName() + 
											"-" + this.turn0Model.getIndexOfPlayer(act.actionPerformerPlayerSide));
								}else{
									actJSON.put("performer_index", act.cardOrCharacterIndex_);
								}
							}
							
							//otherwise try to get indexes from previous logging
							if(!skip_log){
								if(previousTurn == null){
									previousTurn = state_.get(turn).get(playerId);
									if(previousTurn != null){
										previousTurnState = previousTurn.get(previousTurn.size() - 1);
									}else{
										//was completely unable to get previous turn information, which includes subbing out the current turn
										skip_log = true;
										actJSON.put("target_index", act.targetCharacterIndex_);
										actJSON.put("performer_index", act.cardOrCharacterIndex_);
									}
								}else{
									previousTurnState = previousTurn.get(previousTurn.size() - 1);
								}
							}
							
							if(!skip_log){
								if(act.targetCharacterIndex_ < previousTurnState.board.getMinions(act.targetPlayerSide).size() + 1){
									actJSON.put("target", previousTurnState.board.getCharacter(act.targetPlayerSide,  act.targetCharacterIndex_).getName() + 
											"-" + previousTurnState.board.getIndexOfPlayer(act.targetPlayerSide));
								}else{
									actJSON.put("target_index", act.targetCharacterIndex_);
								}
						
								if(act.cardOrCharacterIndex_ < previousTurnState.board.getNumCards_hand(act.actionPerformerPlayerSide)){
									actJSON.put("performer", previousTurnState.board.getCard_hand(act.actionPerformerPlayerSide, act.cardOrCharacterIndex_).getName() + 
											"-" + previousTurnState.board.getIndexOfPlayer(act.actionPerformerPlayerSide));
								}else{
									actJSON.put("performer_index", act.cardOrCharacterIndex_);
								}
							}
						}
					}else if(act.verb_ == Verb.PLAY_MINION){
						actJSON.put("target_index", act.targetCharacterIndex_ + 1);
						if(turn != 0 && i != 0){
							if(act.cardOrCharacterIndex_ < states.get(i-1).board.getNumCards_hand(act.actionPerformerPlayerSide)){
								actJSON.put("performer", states.get(i-1).board.getCard_hand(act.actionPerformerPlayerSide, 
										act.cardOrCharacterIndex_).getName() + "-" + states.get(i-1).board.getIndexOfPlayer(act.actionPerformerPlayerSide));
							}else{
								actJSON.put("performer_index", act.cardOrCharacterIndex_);
							}
						}else if(turn != 0 && i == 0){
							boolean skip_log = false;
							
							//Turn 1 is stupid
							if(turn == 1){
								skip_log = true;
								actJSON.put("performer", board.getMinion(act.targetPlayerSide, act.targetCharacterIndex_).getName() + 
										"-" + board.getIndexOfPlayer(act.actionPerformerPlayerSide));
							}
							HearthActionBoardPair previousTurnState = null;
							
							if(!skip_log){
								if(previousTurn == null){
									previousTurn = state_.get(turn).get(playerId);
									if(previousTurn != null){
										previousTurnState = previousTurn.get(previousTurn.size() - 1);
									}else{
										skip_log = true;
										//was completely unable to get previous turn information, which includes subbing out the current turn
										actJSON.put("performer_index", act.cardOrCharacterIndex_);
									}
								}else{
									previousTurnState = previousTurn.get(previousTurn.size() - 1);
								}
							}
							
							if(!skip_log){
								if(act.cardOrCharacterIndex_ < previousTurnState.board.getNumCards_hand(act.actionPerformerPlayerSide)){
									actJSON.put("performer", previousTurnState.board.getCard_hand(act.actionPerformerPlayerSide, act.cardOrCharacterIndex_).getName() + 
											"-" + previousTurnState.board.getIndexOfPlayer(act.actionPerformerPlayerSide));
								}else{
									actJSON.put("performer_index", act.cardOrCharacterIndex_);
								}
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
				json.put("board", getBoard(board, false));
				
			}else{
				json.put("board", "null");
			}
			
			statesJSON.put(json);
		}
		enclosingObj.put("states", statesJSON);
		return enclosingObj;
	}
	
	/**
	 * Convert a board into a JSON object that gives both the board for both players
	 * @param board the board state object to record
	 * @param flip record the result flipped 
	 * @return
	 */
	private JSONObject getBoard(BoardModel board, boolean flip) {
		JSONObject ret = new JSONObject();
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
			
			if(!flip){
				ret.put("currentPlayerMinions", performerMinionsJSON);
				ret.put("opposingPlayerMinions", targetMinionsJSON);
			}else{
				ret.put("currentPlayerMinions", targetMinionsJSON);
				ret.put("opposingPlayerMinions", performerMinionsJSON);
			}
		} catch (HSInvalidPlayerIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
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
