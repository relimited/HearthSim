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
			HearthTreeNode boardState, boolean singleRealizationOnly) {
		// TODO Auto-generated method stub
		// TODO Juggles doesn't do much of anything right now.  Fix that.
		return null;
	}
}
