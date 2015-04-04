package com.hearthsim.card.minion.concrete;

import com.hearthsim.card.minion.Minion;
import com.hearthsim.exception.HSException;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.util.tree.HearthTreeNode;

public class Demolisher extends Minion {

    public Demolisher() {
        super();
    }

    @Override
    public HearthTreeNode startTurn(PlayerSide thisMinionPlayerIndex, HearthTreeNode boardModel) throws HSException {
        HearthTreeNode toRet = boardModel;
        if (thisMinionPlayerIndex == PlayerSide.CURRENT_PLAYER) {
            PlayerModel waitingPlayer = toRet.data_.modelForSide(PlayerSide.WAITING_PLAYER);
            Minion targetMinion = toRet.data_.modelForSide(PlayerSide.WAITING_PLAYER).getCharacter((int)(Math.random()*(waitingPlayer.getNumMinions() + 1)));
            toRet = targetMinion.takeDamageAndNotify((byte) 2, PlayerSide.CURRENT_PLAYER, PlayerSide.WAITING_PLAYER, toRet, false, false);
        }
        return super.startTurn(thisMinionPlayerIndex, toRet);
    }
}
