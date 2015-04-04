package com.hearthsim.test.groovy.card.weapon

import com.hearthsim.card.weapon.concrete.Doomhammer
import com.hearthsim.card.weapon.concrete.FieryWarAxe
import com.hearthsim.model.BoardModel
import com.hearthsim.test.groovy.card.CardSpec
import com.hearthsim.test.helpers.BoardModelBuilder
import com.hearthsim.util.tree.HearthTreeNode
import spock.lang.Ignore

import static com.hearthsim.model.PlayerSide.CURRENT_PLAYER

class DoomhammerSpec extends CardSpec{

    HearthTreeNode root
    BoardModel startingBoard

    def setup() {

        startingBoard = new BoardModelBuilder().make {
            currentPlayer {
                hand([Doomhammer, FieryWarAxe])
                mana(7)
            }
        }

        root = new HearthTreeNode(startingBoard)
    }

    def 'gives windfury and overload'(){
        def copiedBoard = startingBoard.deepCopy()
        def copiedRoot = new HearthTreeNode(copiedBoard)
        def theCard = copiedBoard.getCurrentPlayer().getHand().get(0);
        def ret = theCard.useOn(CURRENT_PLAYER, 0, copiedRoot);

        expect:
        ret != null
        assertBoardDelta(startingBoard, copiedBoard) {
            currentPlayer {
                mana(2)
                overload(2)
                windFury(true)
                weapon(Doomhammer) {
                    weaponDamage(2)
                    weaponCharge(8)
                }
                removeCardFromHand(Doomhammer)
                numCardsUsed(1)
            }
        }
    }

    @Ignore("Existing bug")
    def 'windfury goes away after weapon is destroyed'(){
        def copiedBoard = startingBoard.deepCopy()
        def copiedRoot = new HearthTreeNode(copiedBoard)
        def theCard = copiedBoard.getCurrentPlayer().getHand().get(0);
        theCard.useOn(CURRENT_PLAYER, 0, copiedRoot);

        theCard = copiedBoard.getCurrentPlayer().getHand().get(0);
        def ret = theCard.useOn(CURRENT_PLAYER, 0, copiedRoot);

        expect:
        ret != null
        assertBoardDelta(startingBoard, copiedBoard) {
            currentPlayer {
                mana(0)
                overload(2)
                weapon(FieryWarAxe) {
                    weaponDamage(3)
                    weaponCharge(2)
                }
                removeCardFromHand(Doomhammer)
                removeCardFromHand(FieryWarAxe)
            }
        }
    }
}
