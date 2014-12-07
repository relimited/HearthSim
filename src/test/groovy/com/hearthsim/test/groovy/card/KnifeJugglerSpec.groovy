package com.hearthsim.test.groovy.card;

import com.hearthsim.card.Card
import com.hearthsim.card.Deck
import com.hearthsim.card.minion.concrete.BloodfenRaptor
import com.hearthsim.card.minion.concrete.KnifeJuggler
import com.hearthsim.card.spellcard.concrete.TheCoin
import com.hearthsim.model.BoardModel
import com.hearthsim.Game
import com.hearthsim.test.helpers.BoardModelBuilder
import com.hearthsim.util.tree.HearthTreeNode

import static com.hearthsim.model.PlayerSide.CURRENT_PLAYER
import static com.hearthsim.model.PlayerSide.WAITING_PLAYER
import static org.junit.Assert.*


public class KnifeJugglerSpec extends CardSpec {
	HearthTreeNode root
	BoardModel startingBoard

	def setup() {

		startingBoard = new BoardModelBuilder().make {
			currentPlayer {
				hand([KnifeJuggler, BloodfenRaptor])
				mana(7)
				deck([TheCoin, TheCoin])
			}
			waitingPlayer {
				mana(4)
				deck([TheCoin, TheCoin])
			}
		}

		root = new HearthTreeNode(startingBoard)
	}
	
	def "cannot play for waiting player's side"() {
		def copiedBoard = startingBoard.deepCopy()
		def target = root.data_.getCharacter(WAITING_PLAYER, 0)
		def theCard = root.data_.getCurrentPlayerCardHand(0)
		def ret = theCard.useOn(WAITING_PLAYER, target, root, null, null)

		expect:

		assertTrue(ret == null)
		assertEquals(copiedBoard, startingBoard)
	}
/*
	def "playing KnifeJuggler"() {
		def cards = [ new TheCoin(), new TheCoin() ]
		def deck = new Deck(cards)
		def copiedBoard = startingBoard.deepCopy()
		def target = root.data_.getCharacter(CURRENT_PLAYER, 0)
		
		def theCard = root.data_.getCurrentPlayerCardHand(1)
		def ret = theCard.useOn(CURRENT_PLAYER, target, root, null, null)
		
		expect:
		assertFalse(ret == null);

		assertBoardDelta(copiedBoard, ret.data_) {
			currentPlayer {
				//playMinion(KnifeJuggler)
				mana(5)
			}
		}
	}
	*/
}
