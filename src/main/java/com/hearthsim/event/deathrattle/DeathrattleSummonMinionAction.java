package com.hearthsim.event.deathrattle;

import com.hearthsim.card.Card;
import com.hearthsim.card.minion.Minion;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.util.tree.HearthTreeNode;

public class DeathrattleSummonMinionAction extends DeathrattleAction {

<<<<<<< HEAD
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
=======
    private final int numMinions_;
    private final Class<? extends Minion> minionClass_;

    public DeathrattleSummonMinionAction(Class<? extends Minion> minionClass, int numMnions) {
        numMinions_ = numMnions;
        minionClass_ = minionClass;
    }

    @Override
    public HearthTreeNode performAction(Card origin,
                                        PlayerSide playerSide,
                                        HearthTreeNode boardState,
                                        boolean singleRealizationOnly) {

        HearthTreeNode toRet = super.performAction(origin, playerSide, boardState, singleRealizationOnly);
        PlayerModel targetPlayer = toRet.data_.modelForSide(playerSide);

        int targetIndex = targetPlayer.getNumMinions();
        if (origin instanceof Minion) {
            targetIndex = targetPlayer.getMinions().indexOf(origin);
            toRet.data_.removeMinion((Minion) origin);
        }

        int numMinionsToActuallySummon = numMinions_;
        if (targetPlayer.getMinions().size() + numMinions_ > 7)
            numMinionsToActuallySummon = 7 - targetPlayer.getMinions().size();

        for (int index = 0; index < numMinionsToActuallySummon; ++index) {
            try {
                Minion newMinion = minionClass_.newInstance();
                toRet = newMinion.summonMinion(playerSide, targetIndex, toRet, false, true);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Unable to instantiate card.");
            }
        }
        return toRet;
    }
>>>>>>> 0879d456082206ad6cf9a55b903d6321bf76f7dd
}
