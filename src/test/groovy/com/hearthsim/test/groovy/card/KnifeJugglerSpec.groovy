<<<<<<< HEAD
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


/**
 * Knife Juggler unit tests
 * TODO: write this better-- it's not testing to make sure that cards are removed, or that the bloodfen raptor was
 * correctly played, etc.
 * 
 * @author Johnathan
 */
public class KnifeJugglerSpec extends CardSpec {
	HearthTreeNode root
	BoardModel startingBoard

	def setup() {
		
		def minionMana = 2;
		def attack = 5;
		def health0 = 3;
		def health1 = 7;
		
		def commonField = [
				[mana: minionMana, attack: attack, health: health0, maxHealth: health0], //todo: attack may be irrelevant here
				[mana: minionMana, attack: attack, health: health1 - 1, maxHealth: health1]
		]
		

		startingBoard = new BoardModelBuilder().make {
			currentPlayer {
				hand([KnifeJuggler, BloodfenRaptor])
				mana(7)
			}
			waitingPlayer {
				field(commonField)
				mana(4)
			}
		}

		root = new HearthTreeNode(startingBoard)
	}
	
	def "playing for current player returns expected child states"() {
		def minionPlayedBoard = startingBoard.deepCopy()
		def copiedRoot = new HearthTreeNode(minionPlayedBoard)
		def knifeJugglerCard = minionPlayedBoard.getCurrentPlayerCardHand(0);
		def ret = knifeJugglerCard.useOn(CURRENT_PLAYER, 0, copiedRoot, null, null);
		
		def knifeJugglerPlayed = ret.data_.deepCopy();
		def nextCard = ret.data_.getCurrentPlayerCardHand(0);
		def finalRet = nextCard.useOn(CURRENT_PLAYER, 0, ret, null, null);
		
		expect:
		assertFalse(ret == null)
		assertFalse(finalRet == null)
		assertEquals(finalRet.numChildren(), 3)
		
		assertBoardDelta(startingBoard, knifeJugglerPlayed) {
			currentPlayer {
				playMinion(KnifeJuggler)
				mana(5)
			}
		}
	
		HearthTreeNode child2 = finalRet.getChildren().get(2);
		assertBoardDelta(finalRet.data_, child2.data_) {
			waitingPlayer {
				heroHealth(29)
			}
		}
		
		HearthTreeNode child0 = finalRet.getChildren().get(0);
		assertBoardDelta(finalRet.data_, child0.data_){
			waitingPlayer{
				updateMinion(0, [deltaHealth: -1])
			}
		}
		
		HearthTreeNode child1 = finalRet.getChildren().get(1);
		assertBoardDelta(finalRet.data_, child1.data_){
			waitingPlayer{
				updateMinion(1, [deltaHealth: -1])
			}
		}
		
	}
/*
		def "cannot play for waiting player's side"() {
		def copiedBoard = startingBoard.deepCopy()
		def target = root.data_.getCharacter(WAITING_PLAYER, 0)
		def theCard = root.data_.getCurrentPlayerCardHand(0)
		def ret = theCard.useOn(WAITING_PLAYER, target, root, null, null)

		expect:

		assertTrue(ret == null)
		assertEquals(copiedBoard, startingBoard)
	}

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
=======
package com.hearthsim.test.groovy.card.weapon

import com.hearthsim.card.minion.concrete.*
import com.hearthsim.card.minion.concrete.BombLobber
import com.hearthsim.card.weapon.concrete.Coghammer
import com.hearthsim.card.weapon.concrete.Glaivezooka
import com.hearthsim.model.BoardModel
import com.hearthsim.test.groovy.card.CardSpec
import com.hearthsim.test.helpers.BoardModelBuilder
import com.hearthsim.util.tree.HearthTreeNode
import com.hearthsim.util.tree.RandomEffectNode

import static com.hearthsim.model.PlayerSide.CURRENT_PLAYER
import static com.hearthsim.model.PlayerSide.WAITING_PLAYER

class KnifeJugglerSpec extends CardSpec {

    HearthTreeNode root
    BoardModel startingBoard

    def setup() {
        startingBoard = new BoardModelBuilder().make {
            currentPlayer {
                hand([BloodfenRaptor])
                field([[minion: KnifeJuggler], [minion: WarGolem]])
                mana(10)
            }
            waitingPlayer {
                hand([BloodfenRaptor])
                field([[minion: FaerieDragon],[minion: StranglethornTiger]])
            }
        }

        root = new HearthTreeNode(startingBoard)
    }

    def "returned node is normal for only one target"() {
        startingBoard.removeMinion(WAITING_PLAYER, 0);
        startingBoard.removeMinion(WAITING_PLAYER, 0);

        def copiedBoard = startingBoard.deepCopy()
        def theCard = root.data_.getCurrentPlayer().getHand().get(0)
        def ret = theCard.useOn(CURRENT_PLAYER, 0, root)

        expect:
        ret != null
        ret instanceof HearthTreeNode
        !(ret instanceof RandomEffectNode)

        assertBoardDelta(copiedBoard, ret.data_) {
            currentPlayer {
                playMinion(BloodfenRaptor, 0)
                mana(8)
                numCardsUsed(1)
            }
            waitingPlayer {
                heroHealth(29)
            }
        }
    }

    def "does not trigger on enemy play"() {
        startingBoard.removeMinion(CURRENT_PLAYER, 0);
        startingBoard.placeMinion(WAITING_PLAYER, new KnifeJuggler())

        def copiedBoard = startingBoard.deepCopy()
        def theCard = root.data_.getCurrentPlayer().getHand().get(0)
        def ret = theCard.useOn(CURRENT_PLAYER, 0, root)

        expect:
        ret != null
        ret instanceof HearthTreeNode
        !(ret instanceof RandomEffectNode)

        assertBoardDelta(copiedBoard, ret.data_) {
            currentPlayer {
                playMinion(BloodfenRaptor, 0)
                mana(8)
                numCardsUsed(1)
            }
        }
    }

    def "returned node is RNG for two or more targets"() {
        def copiedBoard = startingBoard.deepCopy()
        def theCard = root.data_.getCurrentPlayer().getHand().get(0)
        def ret = theCard.useOn(CURRENT_PLAYER, 0, root)

        expect:
        ret != null
        ret instanceof RandomEffectNode
        ret.numChildren() == 3

        assertBoardDelta(copiedBoard, ret.data_) {
            currentPlayer {
                playMinion(BloodfenRaptor, 0)
                numCardsUsed(1)
                mana(8)
            }
        }
    }

    def "hits all enemies"() {
        def theCard = root.data_.getCurrentPlayer().getHand().get(0)
        def ret = theCard.useOn(CURRENT_PLAYER, 0, root)

        expect:
        ret != null
        ret instanceof RandomEffectNode
        ret.numChildren() == 3

        def copiedBoard = ret.data_.deepCopy()

        HearthTreeNode child0 = ret.getChildren().get(0);
        assertBoardDelta(copiedBoard, child0.data_) {
            waitingPlayer {
                heroHealth(29)
            }
        }

        HearthTreeNode child1 = ret.getChildren().get(1);
        assertBoardDelta(copiedBoard, child1.data_) {
            waitingPlayer {
                updateMinion(0, [deltaHealth: -1])
            }
        }

        HearthTreeNode child2 = ret.getChildren().get(2);
        assertBoardDelta(copiedBoard, child2.data_) {
            waitingPlayer {
                updateMinion(1, [deltaHealth: -1])
            }
        }
    }
>>>>>>> cf7168a347c4fcfb274e8a8b1b2280ef3b71cafe
}
