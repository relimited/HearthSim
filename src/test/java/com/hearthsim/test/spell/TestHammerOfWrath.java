package com.hearthsim.test.spell;

import com.hearthsim.card.Card;
import com.hearthsim.card.basic.spell.HammerOfWrath;
import com.hearthsim.card.minion.Minion;
import com.hearthsim.card.minion.MinionMock;
import com.hearthsim.exception.HSException;
import com.hearthsim.model.BoardModel;
import com.hearthsim.model.PlayerModel;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.util.tree.CardDrawNode;
import com.hearthsim.util.tree.HearthTreeNode;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestHammerOfWrath {

    private HearthTreeNode board;
    private PlayerModel currentPlayer;
    private PlayerModel waitingPlayer;

    private static final byte mana = 2;
    private static final byte attack0 = 2;
    private static final byte health0 = 3;
    private static final byte health1 = 7;

    @Before
    public void setup() throws HSException {
        board = new HearthTreeNode(new BoardModel());
        currentPlayer = board.data_.getCurrentPlayer();
        waitingPlayer = board.data_.getWaitingPlayer();

        Minion minion0_0 = new MinionMock("" + 0, mana, attack0, health0, attack0, health0, health0);
        Minion minion0_1 = new MinionMock("" + 0, mana, attack0, (byte)(health1 - 1), attack0, health1, health1);
        Minion minion1_0 = new MinionMock("" + 0, mana, attack0, health0, attack0, health0, health0);
        Minion minion1_1 = new MinionMock("" + 0, mana, attack0, (byte)(health1 - 1), attack0, health1, health1);

        board.data_.placeMinion(PlayerSide.CURRENT_PLAYER, minion0_0);
        board.data_.placeMinion(PlayerSide.CURRENT_PLAYER, minion0_1);

        board.data_.placeMinion(PlayerSide.WAITING_PLAYER, minion1_0);
        board.data_.placeMinion(PlayerSide.WAITING_PLAYER, minion1_1);

        currentPlayer.setMana((byte) 10);
        currentPlayer.setMaxMana((byte) 10);
    }

    @Test
    public void test0() throws HSException {
        HammerOfWrath fb = new HammerOfWrath();
        currentPlayer.placeCardHand(fb);

        Card theCard = currentPlayer.getHand().get(0);
        HearthTreeNode ret = theCard.useOn(PlayerSide.CURRENT_PLAYER, 0, board);
        assertFalse(ret == null);
        assertEquals(currentPlayer.getHand().size(), 0);
        assertTrue(ret instanceof CardDrawNode);
        PlayerModel currentPlayer = ret.data_.modelForSide(PlayerSide.CURRENT_PLAYER);

        assertEquals(((CardDrawNode)ret).getNumCardsToDraw(), 1);
        assertEquals(currentPlayer.getNumMinions(), 2);
        assertEquals(waitingPlayer.getNumMinions(), 2);
        assertEquals(currentPlayer.getHero().getHealth(), 27);
        assertEquals(waitingPlayer.getHero().getHealth(), 30);
        assertEquals(currentPlayer.getCharacter(1).getHealth(), health0);
        assertEquals(currentPlayer.getCharacter(2).getHealth(), health1 - 1);
        assertEquals(waitingPlayer.getCharacter(1).getHealth(), health0);
        assertEquals(waitingPlayer.getCharacter(2).getHealth(), health1 - 1);
        assertEquals(currentPlayer.getCharacter(1).getTotalAttack(), attack0);
        assertEquals(currentPlayer.getCharacter(2).getTotalAttack(), attack0);
        assertEquals(waitingPlayer.getCharacter(1).getTotalAttack(), attack0);
        assertEquals(waitingPlayer.getCharacter(2).getTotalAttack(), attack0);
        assertFalse(currentPlayer.getCharacter(1).getCharge());
        assertFalse(currentPlayer.getCharacter(2).getCharge());
        assertFalse(currentPlayer.getCharacter(1).getFrozen());
        assertFalse(waitingPlayer.getCharacter(1).getFrozen());
    }

    @Test
    public void test1() throws HSException {
        HammerOfWrath fb = new HammerOfWrath();
        currentPlayer.placeCardHand(fb);

        Card theCard = currentPlayer.getHand().get(0);
        HearthTreeNode ret = theCard.useOn(PlayerSide.WAITING_PLAYER, 1, board);
        assertFalse(ret == null);

        assertEquals(currentPlayer.getHand().size(), 0);
        assertTrue(ret instanceof CardDrawNode);
        assertEquals(((CardDrawNode)ret).getNumCardsToDraw(), 1);

        assertEquals(currentPlayer.getNumMinions(), 2);
        assertEquals(waitingPlayer.getNumMinions(), 1);
        assertEquals(currentPlayer.getHero().getHealth(), 30);
        assertEquals(waitingPlayer.getHero().getHealth(), 30);
        assertEquals(currentPlayer.getCharacter(1).getHealth(), health0);
        assertEquals(currentPlayer.getCharacter(2).getHealth(), health1 - 1);
        assertEquals(waitingPlayer.getCharacter(1).getHealth(), health1 - 1);
        assertEquals(currentPlayer.getCharacter(1).getTotalAttack(), attack0);
        assertEquals(currentPlayer.getCharacter(2).getTotalAttack(), attack0);
        assertEquals(waitingPlayer.getCharacter(1).getTotalAttack(), attack0);
    }
}
