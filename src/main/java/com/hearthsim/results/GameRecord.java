package com.hearthsim.results;

import com.hearthsim.model.BoardModel;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.util.HearthActionBoardPair;
import com.hearthsim.util.record.HearthActionRecord;

import org.json.JSONObject;

import java.util.List;

public interface GameRecord {

    /**
     * Put a record
     * @param turn The turn number
     * @param activePlayerSide Index of the player that just played a turn
     * @param board
     */
	public void put(int turn, PlayerSide activePlayerSide, BoardModel board, List<HearthActionBoardPair> plays);
	
	/**
	 * put a record in with an additional history object
	 * @param turn turn number
	 * @param activePlayerSide player who just took a turn
	 * @param board board state
	 * @param plays list of plays a player took.  This has been indexed
	 * @param choiceRecord record of actions taken.  This list does not map to state, but should, roughly, map to the state object
	 */
	public void put(int turn, PlayerSide activePlayerSide, BoardModel board,
			List<HearthActionBoardPair> plays,
			List<HearthActionRecord> record);
	
	/**
	 * Return the number of turns recorded for each player
	 * 
	 * @param playerIndex
	 * @return
	 */
	public int getRecordLength(int playerIndex);
	
	
	/**
	 * Returns the number of minions on a player's board
	 * 
	 * @param playerId The id of the player for which to return the data
	 * @param turn Turn number
	 * @param currentPlayerId The id of the player that just played a turn
	 * @return
	 */
	public int getNumMinions(int playerId, int turn, int currentPlayerId);
	
	/**
	 * Returns the number of cards in the hand of a given player
	 * 
	 * @param playerId The id of the player for which to return the data
	 * @param turn Turn number
	 * @param currentPlayerId The id of the player that just played a turn
	 * @return
	 */
	public int getNumCardsInHand(int playerId, int turn, int currentPlayerId);
	
	/**
	 * Get the health of a given player's hero
	 * 
	 * @param playerId The id of the player for which to return the data
	 * @param turn Turn number
	 * @param currentPlayerId The id of the player that just played a turn
	 * @return
	 */
	public int getHeroHealth(int playerId, int turn, int currentPlayerId);
	
	/**
	 * Get the armor of a given player's hero
	 * 
	 * @param playerId The id of the player for which to return the data
	 * @param turn Turn number
	 * @param currentPlayerId The id of the player that just played a turn
	 * @return
	 */
	public int getHeroArmor(int playerId, int turn, int currentPlayerId);
	
	/**
	 * Get a straight log of all things on a turn 
	 */
	public JSONObject getTurn(int playerId, int turn);
	
	/**
	 * Set which player went first
	 */
	public void setFirstPlayer(int firstPlayerId);
	
	/**
	 * Returns the JSON representation of this record
	 * @return
	 */
	public JSONObject toJSON();
}
