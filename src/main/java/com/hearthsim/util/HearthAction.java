package com.hearthsim.util;

import com.hearthsim.Game;
import com.hearthsim.card.Card;
import com.hearthsim.card.Deck;
import com.hearthsim.card.minion.Hero;
import com.hearthsim.card.minion.Minion;
import com.hearthsim.exception.HSException;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.util.tree.HearthTreeNode;
import org.json.JSONObject;


/**
 * A class representing an action that a player can take
 *
 *
 */
public class HearthAction {
<<<<<<< HEAD
	
	// TODO the DO_NOT_ verbs are used for history tracking but we can probably optimize them away in the future.
	public enum Verb {
		USE_CARD, HERO_ABILITY, ATTACK, UNTARGETABLE_BATTLECRY, TARGETABLE_BATTLECRY, START_TURN, END_TURN, DO_NOT_USE_CARD, DO_NOT_ATTACK, DO_NOT_USE_HEROPOWER, RNG, DRAW_CARDS, PLAY_MINION
	}
		
	public final Verb verb_;
	
	public final PlayerSide actionPerformerPlayerSide;
	public final int cardOrCharacterIndex_;
	
	public final PlayerSide targetPlayerSide;
	public final int targetCharacterIndex_;
	
	public HearthAction(Verb verb) {
		this(verb, PlayerSide.CURRENT_PLAYER, -1, null, -1);
	}

	public HearthAction(Verb verb, PlayerSide actionPerformerPlayerSide, int cardOrCharacterIndex) {
		this(verb, actionPerformerPlayerSide, cardOrCharacterIndex, null, -1);
	}

	public HearthAction(Verb verb, PlayerSide actionPerformerPlayerSide, int cardOrCharacterIndex, PlayerSide targetPlayerSide, int targetCharacterIndex) {
		verb_ = verb;
		this.actionPerformerPlayerSide = actionPerformerPlayerSide;
		cardOrCharacterIndex_ = cardOrCharacterIndex;

		this.targetPlayerSide = targetPlayerSide;
		targetCharacterIndex_ = targetCharacterIndex;
	}

	public HearthTreeNode perform(HearthTreeNode boardState, Deck deckPlayer0, Deck deckPlayer1) throws HSException {
		return this.perform(boardState, deckPlayer0, deckPlayer1, true);
	}

	public HearthTreeNode perform(HearthTreeNode boardState, Deck deckPlayer0, Deck deckPlayer1, boolean singleRealization) throws HSException {
		HearthTreeNode toRet = boardState;
		
		switch(verb_) {
			case USE_CARD: {
				Card card = boardState.data_.getCard_hand(actionPerformerPlayerSide, cardOrCharacterIndex_);
				toRet = card.useOn(targetPlayerSide, targetCharacterIndex_, toRet, deckPlayer0, deckPlayer1, singleRealization);
			}
			break;
			case HERO_ABILITY: {
				Hero hero = boardState.data_.getHero(actionPerformerPlayerSide);
				Minion target = boardState.data_.getCharacter(targetPlayerSide, targetCharacterIndex_);
				toRet = hero.useHeroAbility(targetPlayerSide, target, toRet, deckPlayer0, deckPlayer1, singleRealization);
			}
			break;
			case ATTACK: {
				Minion attacker = boardState.data_.getCharacter(actionPerformerPlayerSide, cardOrCharacterIndex_);
				Minion target = boardState.data_.getCharacter(targetPlayerSide, targetCharacterIndex_);
				toRet = attacker.attack(targetPlayerSide, target, toRet, deckPlayer0, deckPlayer1);
			}
			break;
			case UNTARGETABLE_BATTLECRY: {
				Minion minion = boardState.data_.getCharacter(actionPerformerPlayerSide, cardOrCharacterIndex_);
				Minion placementTarget = boardState.data_.getCharacter(targetPlayerSide, targetCharacterIndex_);
				toRet = minion.useUntargetableBattlecry(placementTarget, toRet, deckPlayer0, deckPlayer1, singleRealization);
				break;
			}
			case TARGETABLE_BATTLECRY: {
				Minion minion = boardState.data_.getCharacter(actionPerformerPlayerSide, cardOrCharacterIndex_);
				Minion battlecryTarget = boardState.data_.getCharacter(targetPlayerSide, targetCharacterIndex_);
				toRet = minion.useTargetableBattlecry(targetPlayerSide, battlecryTarget, toRet, deckPlayer0, deckPlayer1);
				break;
			}
			case START_TURN: {
				toRet = new HearthTreeNode(Game.beginTurn(boardState.data_.deepCopy()));
				break;
			}
			case END_TURN: {
				toRet = new HearthTreeNode(Game.endTurn(boardState.data_.deepCopy()).flipPlayers());
				break;
			}
			case DO_NOT_USE_CARD: {
				for(Card c : boardState.data_.getCurrentPlayerHand()) {
					c.hasBeenUsed(true);
				}
				break;
			}
			case DO_NOT_ATTACK: {
				for(Minion minion : PlayerSide.CURRENT_PLAYER.getPlayer(boardState).getMinions()) {
					minion.hasAttacked(true);
				}
				boardState.data_.getCurrentPlayerHero().hasAttacked(true);
				break;
			}
			case DO_NOT_USE_HEROPOWER: {
				boardState.data_.getCurrentPlayerHero().hasBeenUsed(true);
				break;
			}
			case RNG: {
				// We need to perform the current state again if the children don't exist yet. This can happen in certain replay scenarios.
				// Do not do this if the previous action was *also* RNG or we will end up in an infinite loop.
				if(toRet.isLeaf() && boardState.getAction().verb_ != Verb.RNG) {
					toRet = boardState.getAction().perform(boardState, deckPlayer0, deckPlayer1, singleRealization);
				}
				// RNG has declared this child happened
				toRet = toRet.getChildren().get(cardOrCharacterIndex_);
				break;
			}
			case DRAW_CARDS: {
				// Note, this action only supports drawing cards from the deck. Cards like Ysera or Webspinner need to be implemented using RNG children.
				for (int indx = 0; indx < cardOrCharacterIndex_; ++indx) {
					toRet.data_.modelForSide(actionPerformerPlayerSide).drawNextCardFromDeck();
				}
				break;
			}
		}
		return toRet;
	}
=======

    // TODO the DO_NOT_ verbs are used for history tracking but we can probably optimize them away in the future.
    public enum Verb {
        USE_CARD, HERO_ABILITY, ATTACK, UNTARGETABLE_BATTLECRY, TARGETABLE_BATTLECRY, START_TURN, END_TURN, DO_NOT_USE_CARD, DO_NOT_ATTACK, DO_NOT_USE_HEROPOWER, RNG, DRAW_CARDS
    }

    public final Verb verb_;

    private final PlayerSide actionPerformerPlayerSide;
    private final int cardOrCharacterIndex_;

    private final PlayerSide targetPlayerSide;
    public final int targetCharacterIndex_;

    public HearthAction(Verb verb) {
        this(verb, PlayerSide.CURRENT_PLAYER, -1, null, -1);
    }

    public HearthAction(Verb verb, PlayerSide actionPerformerPlayerSide, int cardOrCharacterIndex) {
        this(verb, actionPerformerPlayerSide, cardOrCharacterIndex, null, -1);
    }

    public HearthAction(Verb verb, PlayerSide actionPerformerPlayerSide, int cardOrCharacterIndex, PlayerSide targetPlayerSide, int targetCharacterIndex) {
        verb_ = verb;
        this.actionPerformerPlayerSide = actionPerformerPlayerSide;
        cardOrCharacterIndex_ = cardOrCharacterIndex;

        this.targetPlayerSide = targetPlayerSide;
        targetCharacterIndex_ = targetCharacterIndex;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        json.put("verb_", verb_);
        json.put("actionPerformerPlayerSide", actionPerformerPlayerSide);
        json.put("cardOrCharacterIndex_", cardOrCharacterIndex_);
        json.put("targetPlayerSide", targetPlayerSide);
        json.put("targetCharacterIndex_", targetCharacterIndex_);

        return json;
    }

    @Deprecated
    public HearthTreeNode perform(HearthTreeNode boardState, Deck deckPlayer0, Deck deckPlayer1) throws HSException {
        return this.perform(boardState, true);
    }

    @Deprecated
    public HearthTreeNode perform(HearthTreeNode boardState, Deck deckPlayer0, Deck deckPlayer1, boolean singleRealization) throws HSException {
        return this.perform(boardState, singleRealization);
    }

    public HearthTreeNode perform(HearthTreeNode boardState) throws HSException {
        return this.perform(boardState, true);
    }

    public HearthTreeNode perform(HearthTreeNode boardState, boolean singleRealization) throws HSException {
        HearthTreeNode toRet = boardState;
        PlayerModel actingPlayer = actionPerformerPlayerSide != null ? boardState.data_.modelForSide(actionPerformerPlayerSide) : null;
        PlayerModel targetPlayer = targetPlayerSide != null ? boardState.data_.modelForSide(targetPlayerSide) : null;

        switch(verb_) {
            case USE_CARD: {
                Card card = actingPlayer.getHand().get(cardOrCharacterIndex_);
                toRet = card.useOn(targetPlayerSide, targetCharacterIndex_, toRet, singleRealization);
            }
            break;
            case HERO_ABILITY: {
                Hero hero = actingPlayer.getHero();
                Minion target = targetPlayer.getCharacter(targetCharacterIndex_);
                toRet = hero.useHeroAbility(targetPlayerSide, target, toRet, singleRealization);
            }
            break;
            case ATTACK: {
                Minion attacker = actingPlayer.getCharacter(cardOrCharacterIndex_);
                toRet = attacker.attack(targetPlayerSide, targetCharacterIndex_, toRet, singleRealization);
            }
            break;
            case UNTARGETABLE_BATTLECRY: {
                Minion minion = actingPlayer.getCharacter(cardOrCharacterIndex_);
                toRet = minion.useUntargetableBattlecry(targetCharacterIndex_, toRet, singleRealization);
                break;
            }
            case TARGETABLE_BATTLECRY: {
                Minion minion = actingPlayer.getCharacter(cardOrCharacterIndex_);
                toRet = minion.useTargetableBattlecry(targetPlayerSide, targetCharacterIndex_, toRet, singleRealization);
                break;
            }
            case START_TURN: {
                toRet = new HearthTreeNode(Game.beginTurn(boardState.data_.deepCopy()));
                break;
            }
            case END_TURN: {
                toRet = new HearthTreeNode(Game.endTurn(boardState.data_.deepCopy()).flipPlayers());
                break;
            }
            case DO_NOT_USE_CARD: {
                for (Card c : actingPlayer.getHand()) {
                    c.hasBeenUsed(true);
                }
                break;
            }
            case DO_NOT_ATTACK: {
                for (Minion minion : actingPlayer.getMinions()) {
                    minion.hasAttacked(true);
                }
                actingPlayer.getHero().hasAttacked(true);
                break;
            }
            case DO_NOT_USE_HEROPOWER: {
                actingPlayer.getHero().hasBeenUsed(true);
                break;
            }
            case RNG: {
                // We need to perform the current state again if the children don't exist yet. This can happen in certain replay scenarios.
                // Do not do this if the previous action was *also* RNG or we will end up in an infinite loop.
                if (toRet.isLeaf() && boardState.getAction().verb_ != Verb.RNG) {
                    boardState.data_.getCurrentPlayer().addNumCardsUsed((byte)-1); //do not double count
                    toRet = boardState.getAction().perform(boardState, singleRealization);
                }
                // RNG has declared this child happened
                toRet = toRet.getChildren().get(cardOrCharacterIndex_);
                break;
            }
            case DRAW_CARDS: {
                // Note, this action only supports drawing cards from the deck. Cards like Ysera or Webspinner need to be implemented using RNG children.
                for (int indx = 0; indx < cardOrCharacterIndex_; ++indx) {
                    actingPlayer.drawNextCardFromDeck();
                }
                break;
            }
        }
        return toRet;
    }
>>>>>>> 0879d456082206ad6cf9a55b903d6321bf76f7dd
}
