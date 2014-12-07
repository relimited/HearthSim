package com.hearthsim.results;

import org.json.JSONArray;
import org.json.JSONObject;

public class GameResultSummary {

	GameResult result_;
	
	public GameResultSummary(GameResult result) {
		result_ = result;
	}
	
	public JSONObject toImprovedJSON(){
		JSONObject json = new JSONObject();
		
		JSONArray p0 = new JSONArray();
		for(int i = 0; i < result_.record_.getRecordLength(0); ++i){
			JSONObject tj = new JSONObject();
			tj.put("turn " + i, result_.record_.getTurn(0, i));
			p0.put(tj);
		}
		
		JSONArray p1 = new JSONArray();
		for(int i = 0; i < result_.record_.getRecordLength(0); ++i){
			JSONObject tj = new JSONObject();
			tj.put("turn " + i, result_.record_.getTurn(1, i));
			p1.put(tj);
		}
		
		json.put("p0", p0);
		json.put("p1", p1);
		return json;
	}
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
		json.put("first_player", result_.firstPlayerIndex_);
		json.put("winner", result_.winnerPlayerIndex_);
		json.put("duration", result_.gameDuration_);
		
		JSONObject record = new JSONObject();
		JSONArray p0 = new JSONArray();
		for (int i = 0; i < result_.record_.getRecordLength(0); ++i) {
			JSONObject tj = new JSONObject();
			tj.put("p0_m", result_.record_.getNumMinions(0, i, 0));
			tj.put("p1_m", result_.record_.getNumMinions(1, i, 0));
			tj.put("p0_c", result_.record_.getNumCardsInHand(0, i, 0));
			tj.put("p1_c", result_.record_.getNumCardsInHand(1, i, 0));
			tj.put("p0_h", result_.record_.getHeroHealth(0, i, 0));
			tj.put("p1_h", result_.record_.getHeroHealth(1, i, 0));
			
			//For new board logging
			JSONObject side = result_.record_.getBoardSide(0, i, 0);
			if(side != null){
				tj.put("p0_board",side);
			}
			side = result_.record_.getBoardSide(1, i, 0);
			if(side != null){
				tj.put("p1_board", side);
			}
			
			//Action logging
			JSONObject actions = result_.record_.getActions(0, i, 0);
			if(actions != null){
				tj.put("p0_actions", actions);
			}
			actions = result_.record_.getActions(1, i, 0);
			if(actions != null){
				tj.put("p1_actions", actions);
			}
			p0.put(tj);
		}
		
		JSONArray p1 = new JSONArray();
		for (int i = 0; i < result_.record_.getRecordLength(1); ++i) {
			JSONObject tj = new JSONObject();
			tj.put("p0_m", result_.record_.getNumMinions(0, i, 1));
			tj.put("p1_m", result_.record_.getNumMinions(1, i, 1));
			tj.put("p0_c", result_.record_.getNumCardsInHand(0, i, 1));
			tj.put("p1_c", result_.record_.getNumCardsInHand(1, i, 1));
			tj.put("p0_h", result_.record_.getHeroHealth(0, i, 1));
			tj.put("p1_h", result_.record_.getHeroHealth(1, i, 1));
			
			//For new board logging
			JSONObject side = result_.record_.getBoardSide(0, i, 1);
			if(side != null){
				tj.put("p0_board",side);
			}
			side = result_.record_.getBoardSide(1, i, 1);
			if(side != null){
				tj.put("p1_board", side);
			}
			
			//Action logging
			JSONObject actions = result_.record_.getActions(0, i, 1);
			if(actions != null){
				tj.put("p0_actions", actions);
			}
			actions = result_.record_.getActions(1, i, 1);
			if(actions != null){
				tj.put("p1_actions", actions);
			}
			p1.put(tj);
		}

		record.put("p0", p0);
		record.put("p1", p1);
		
		json.put("record", record);
		return json;
	}
}
