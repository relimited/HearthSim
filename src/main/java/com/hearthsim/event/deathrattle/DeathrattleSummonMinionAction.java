package com.hearthsim.event.deathrattle;

import com.hearthsim.card.Deck;
import com.hearthsim.card.minion.Minion;
import com.hearthsim.exception.HSException;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.util.tree.HearthTreeNode;

public class DeathrattleSummonMinionAction extends DeathrattleAction {

	private final int numMinions_;
    private final Class<?> minionClass_;
    
	public DeathrattleSummonMinionAction(Class<?> minionClass, int numMnions) {
		numMinions_ = numMnions;
		minionClass_ = minionClass;
	}
	
	@Override
	public HearthTreeNode performAction(
			Minion minion,
			PlayerSide playerSide,
			HearthTreeNode boardState,
			Deck deckPlayer0,
			Deck deckPlayer1) 
		throws HSException
	{
		int numMinions = PlayerSide.CURRENT_PLAYER.getPlayer(boardState).getNumMinions();

		HearthTreeNode toRet = super.performAction(minion, playerSide, boardState, deckPlayer0, deckPlayer1);
		for (int index = 0; index < numMinions_; ++index) {
            try {
            	Minion newMinion = (Minion)minionClass_.newInstance();
            	if(toRet.data_.getMinions(playerSide).indexOf(minion) < 0){
            		//the original minion has already been removed.
            		//try to place the next summoned minion next to the first summoned minion
            		if (numMinions < 6) {
            			Minion placementTarget = toRet.data_.getCharacter(playerSide, toRet.data_.getMinions(playerSide).indexOf(newMinion) + 1);
            			toRet = newMinion.summonMinion(playerSide, placementTarget, toRet, deckPlayer0, deckPlayer1, false, true);
            		}
            	}else{
            		//original minion needs to be removed
            		Minion placementTarget = toRet.data_.getCharacter(playerSide, toRet.data_.getMinions(playerSide).indexOf(minion)); //this minion can't be a hero
            		toRet.data_.removeMinion(minion);
					toRet = newMinion.summonMinion(playerSide, placementTarget, toRet, deckPlayer0, deckPlayer1, false, true);
            	}
			} catch (InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				throw new HSException();
			}
		}

		return toRet;
	}
}
