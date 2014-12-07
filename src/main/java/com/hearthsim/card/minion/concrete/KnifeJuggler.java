package com.hearthsim.card.minion.concrete;

import com.hearthsim.card.minion.Minion;
import com.hearthsim.card.Deck;
import com.hearthsim.exception.HSException;
import com.hearthsim.exception.HSInvalidPlayerIndexException;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.util.tree.HearthTreeNode;

public class KnifeJuggler extends Minion {
	public KnifeJuggler() {
		super();
		
	}
	
	@Override
	public HearthTreeNode minionSummonEvent(PlayerSide thisMinionPlayerSide, PlayerSide summonedMinionPlayerSide,
			Minion summonedMinion, HearthTreeNode boardState, Deck deckPlayer0, Deck deckPlayer1)
			throws HSInvalidPlayerIndexException {
		
		HearthTreeNode toRet = boardState;
		
		if(thisMinionPlayerSide == summonedMinionPlayerSide) {
			PlayerSide otherPlayerSide = thisMinionPlayerSide.getOtherPlayer();
			int index = (int)(Math.random()*( otherPlayerSide.getPlayer(toRet).getNumCharacters() + 1));
			Minion targetCharacter = toRet.data_.getCharacter(otherPlayerSide, index);
			try {
				toRet = targetCharacter.takeDamage((byte)1, otherPlayerSide.getOtherPlayer(), otherPlayerSide, toRet, deckPlayer0, deckPlayer1, false, false);
			} catch (HSException e) {
				
			}
		}
		return super.minionSummonEvent(thisMinionPlayerSide, summonedMinionPlayerSide, summonedMinion, toRet, deckPlayer0, deckPlayer1);
	}

}
