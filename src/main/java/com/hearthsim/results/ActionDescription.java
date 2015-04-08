/**
 * Class to take the JSON report from an action and programmatically access it.
 * 
 * This is a little bit cheating.  We expect that the JSON reports will always contain both public and private data, which they might not
 * in the future of hearthsim.
 * 
 * @author Johnathan Pagnutti
 */
package com.hearthsim.results;

import org.json.JSONObject;

import com.hearthsim.model.PlayerSide;
import com.hearthsim.util.HearthAction.Verb;

public class ActionDescription {
	final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionDescription.class);
	
	public final Verb verb;

	public final PlayerSide actionPerformerPlayerSide;
	public final int cardOrCharacterIndex;

	public final PlayerSide targetPlayerSide;
	public final int targetCharacterIndex; 
	
	public ActionDescription(String verb,  String actionPerformerPlayerSide, String cardOrCharacterIndex, String targetPlayerSide, 
			String targetCharacterIndex){
		this.verb = (verb != "") ? Verb.valueOf(verb) : null;
		this.actionPerformerPlayerSide = (actionPerformerPlayerSide != "") ? PlayerSide.valueOf(actionPerformerPlayerSide) : null;
		this.cardOrCharacterIndex = (cardOrCharacterIndex != "") ? Integer.valueOf(cardOrCharacterIndex).intValue() : null; //FIXME: slow
		
		this.targetPlayerSide = (targetPlayerSide != "") ? PlayerSide.valueOf(targetPlayerSide) : null;
		this.targetCharacterIndex = (targetCharacterIndex != "") ?  Integer.valueOf(targetCharacterIndex).intValue() : null; //FIXME: slow
	}
	
	/**
	 * Get an action description from a JSON object with the correct fields
	 * @param json the json object to convert to a programmatic action description
	 * @requires TODO
	 * @ensures TODO
	 * @return an ActionDescription object that has programmatic access to the various enums and targets of an action
	 */
	public static ActionDescription fromJSON(JSONObject json){
		String verbStr = json.has("verb_") ? json.get("verb_").toString() : "";
		String actionPerformerPlayerSideStr = json.has("actionPerformerPlayerSide") ? json.get("actionPerformerPlayerSide").toString() : "";
		String cardOrCharacterIndexStr = json.has("cardOrCharacterIndex_") ? json.get("cardOrCharacterIndex_").toString() : "";
		String targetPlayerSideStr = json.has("targetPlayerSide") ? json.get("targetPlayerSide").toString() : "";
		String targetCharacterIndexStr = json.has("targetCharacterIndex_") ? json.get("targetCharacterIndex_").toString() : "";
		
		return new ActionDescription(verbStr, actionPerformerPlayerSideStr, cardOrCharacterIndexStr, targetPlayerSideStr, targetCharacterIndexStr);
	}
}
