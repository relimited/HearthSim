package com.hearthsim.card.minion.concrete;

import java.util.ArrayList;
import java.util.List;

import com.hearthsim.card.minion.Hero;
import com.hearthsim.card.minion.Minion;
import com.hearthsim.card.Card;
import com.hearthsim.card.CardPlayAfterInterface;
import com.hearthsim.card.CardPlayBeginInterface;
import com.hearthsim.card.Deck;
import com.hearthsim.exception.HSException;
import com.hearthsim.exception.HSInvalidPlayerIndexException;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.util.HearthAction;
import com.hearthsim.util.tree.HearthTreeNode;
import com.hearthsim.util.tree.RandomEffectNode;

public class KnifeJuggler extends Minion implements CardPlayAfterInterface {
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

	public KnifeJuggler() {
		super();	
	}

	@Override
	public HearthTreeNode onCardPlayResolve(PlayerSide thisCardPlayerSide,
			PlayerSide cardUserPlayerSide, Card usedCard,
			HearthTreeNode boardState, Deck deckPlayer0, Deck deckPlayer1,
			boolean singleRealizationOnly) {
		
		HearthTreeNode toRet = boardState;
		PlayerSide otherPlayerSide = cardUserPlayerSide.getOtherPlayer();
		
		if(usedCard == this){
			return toRet;
		}
		
		if(singleRealizationOnly){
			if(toRet != null){
				int targetIndex = (int)(Math.random()*( otherPlayerSide.getPlayer(toRet).getNumCharacters() ));
				Minion targetCharacter;
				try {
					targetCharacter = toRet.data_.getCharacter(otherPlayerSide, targetIndex);
					targetCharacter.takeDamage((byte)1, otherPlayerSide.getOtherPlayer(), otherPlayerSide, toRet, deckPlayer0, deckPlayer1, false, false);
				} catch (HSInvalidPlayerIndexException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (HSException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else{
			List<Minion> possibleTargets = new ArrayList<Minion>();
			possibleTargets = PlayerSide.WAITING_PLAYER.getPlayer(toRet).getMinions();
			possibleTargets.add(PlayerSide.WAITING_PLAYER.getPlayer(toRet).getHero());
			
			if(possibleTargets.size() > 0){
				toRet = new RandomEffectNode(toRet, new HearthAction(HearthAction.Verb.RNG)); //TODO: not proud of this implementation, and it might not work.  This needs to be an RNG action
																							  //	  but I'm not entirely sure how to get the right params for the action
				PlayerModel targetPlayer = PlayerSide.WAITING_PLAYER.getPlayer(toRet);
				for(Minion possibleTarget : possibleTargets){
					HearthTreeNode newState = new HearthTreeNode(toRet.data_.deepCopy());
					Minion targetMinion = (possibleTarget instanceof Hero) ? PlayerSide.WAITING_PLAYER.getPlayer(newState).getHero() : PlayerSide.WAITING_PLAYER.getPlayer(newState).getMinions().get(targetPlayer.getMinions().indexOf(possibleTarget));
					try {
						targetMinion.takeDamage((byte)1, PlayerSide.CURRENT_PLAYER, PlayerSide.CURRENT_PLAYER, newState, deckPlayer0, deckPlayer1, false, false);
					} catch (HSException e) {
						e.printStackTrace();
					}
					toRet.addChild(newState);
				}
			}
			
		}
		
		return toRet;
	}

}
