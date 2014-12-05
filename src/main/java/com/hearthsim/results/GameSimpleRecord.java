package com.hearthsim.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hearthsim.card.minion.Hero;
import com.hearthsim.card.minion.Minion;
import com.hearthsim.exception.HSInvalidPlayerIndexException;
import com.hearthsim.model.BoardModel;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.util.HearthActionBoardPair;
import com.hearthsim.util.MinionList;

import org.json.JSONObject;

public class GameSimpleRecord implements GameRecord {

	int maxTurns_;
	byte[][][] numMinions_;
	byte[][][] numCards_;
	byte[][][] heroHealth_;
	byte[][][] heroArmor_;
	Map<Integer, List<Map<Integer, MinionList>>> board_;
	
	public GameSimpleRecord() {
		this(50);
	}
	
	public GameSimpleRecord(int maxTurns) {
		maxTurns_ = maxTurns;
		numMinions_ = new byte[2][maxTurns][2];
		numCards_ = new byte[2][maxTurns][2];
		heroHealth_ = new byte[2][maxTurns][2];
		heroArmor_ = new byte[2][maxTurns][2];
		
		board_ = new HashMap<Integer, List<Map<Integer, MinionList>>>();
	}
	
	@Override
	public void put(int turn, PlayerSide activePlayerSide, BoardModel board, List<HearthActionBoardPair> plays) {
        PlayerModel playerModel = board.modelForSide(activePlayerSide);

        int currentPlayerId = playerModel.getPlayerId();
        int waitingPlayerId = board.modelForSide(activePlayerSide.getOtherPlayer()).getPlayerId();
        
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
        	//add a blank element
        	fullSideInformation.add(null);
        }
        
        fullSideInformation.add(currentPlayerId, boardSides);
        board_.put(turn, fullSideInformation);
        
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
		
		if(board_.containsKey(turn) && board_.get(turn).size() > currentPlayerId && board_.get(turn).get(currentPlayerId).containsKey(playerId)){
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
	@Override
	public JSONObject toJSON() {
		// TODO Auto-generated method stub
		return null;
	}

}
