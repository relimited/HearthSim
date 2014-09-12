package com.hearthsim.test.minion;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.hearthsim.card.Card;
import com.hearthsim.card.Deck;
import com.hearthsim.card.minion.Minion;
import com.hearthsim.card.minion.concrete.Abomination;
import com.hearthsim.card.minion.concrete.BoulderfistOgre;
import com.hearthsim.card.minion.concrete.HarvestGolem;
import com.hearthsim.card.minion.concrete.IronbeakOwl;
import com.hearthsim.card.minion.concrete.LootHoarder;
import com.hearthsim.card.minion.concrete.RaidLeader;
import com.hearthsim.card.minion.concrete.StormwindChampion;
import com.hearthsim.card.spellcard.concrete.TheCoin;
import com.hearthsim.exception.HSException;
import com.hearthsim.util.boardstate.BoardState;
import com.hearthsim.util.tree.HearthTreeNode;

public class TestIronbeakOwl {

	private HearthTreeNode board;
	private Deck deck;

	@Before
	public void setup() {
		board = new HearthTreeNode(new BoardState());

		Minion minion0_0 = new StormwindChampion();
		Minion minion0_1 = new RaidLeader();
		Minion minion0_2 = new HarvestGolem();
	
		Minion minion1_0 = new BoulderfistOgre();
		Minion minion1_1 = new RaidLeader();
		Minion minion1_2 = new Abomination();
		Minion minion1_3 = new LootHoarder();
		
		board.data_.placeCard_hand_p0(minion0_0);
		board.data_.placeCard_hand_p0(minion0_1);
		board.data_.placeCard_hand_p0(minion0_2);
				
		board.data_.placeCard_hand_p1(minion1_0);
		board.data_.placeCard_hand_p1(minion1_1);
		board.data_.placeCard_hand_p1(minion1_2);
		board.data_.placeCard_hand_p1(minion1_3);

		Card cards[] = new Card[10];
		for (int index = 0; index < 10; ++index) {
			cards[index] = new TheCoin();
		}
	
		deck = new Deck(cards);

		board.data_.setMana_p0((byte)10);
		board.data_.setMana_p1((byte)10);
		
		board.data_.setMaxMana_p0((byte)10);
		board.data_.setMaxMana_p1((byte)10);
		
		HearthTreeNode tmpBoard = new HearthTreeNode(board.data_.flipPlayers());
		try {
			tmpBoard.data_.getCard_hand_p0(0).useOn(0, tmpBoard.data_.getHero_p0(), tmpBoard, deck, null);
			tmpBoard.data_.getCard_hand_p0(0).useOn(0, tmpBoard.data_.getHero_p0(), tmpBoard, deck, null);
			tmpBoard.data_.getCard_hand_p0(0).useOn(0, tmpBoard.data_.getHero_p0(), tmpBoard, deck, null);
			tmpBoard.data_.getCard_hand_p0(0).useOn(0, tmpBoard.data_.getHero_p0(), tmpBoard, deck, null);
		} catch (HSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		board = new HearthTreeNode(tmpBoard.data_.flipPlayers());
		try {
			board.data_.getCard_hand_p0(0).useOn(0, board.data_.getHero_p0(), board, deck, null);
			board.data_.getCard_hand_p0(0).useOn(0, board.data_.getHero_p0(), board, deck, null);
			board.data_.getCard_hand_p0(0).useOn(0, board.data_.getHero_p0(), board, deck, null);
		} catch (HSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		board.data_.resetMana();
		board.data_.resetMinions();
		
		Minion fb = new IronbeakOwl();
		board.data_.placeCard_hand_p0(fb);

	}
	
	
	
	@Test
	public void test0() throws HSException {
		
		//null case
		Minion target = board.data_.getCharacter(1, 0);
		Card theCard = board.data_.getCard_hand_p0(0);
		HearthTreeNode ret = theCard.useOn(1, target, board, deck, deck);
		
		assertTrue(ret == null);
		assertEquals(board.data_.getNumCards_hand(), 1);
		assertEquals(board.data_.getNumMinions_p0(), 3);
		assertEquals(board.data_.getNumMinions_p1(), 4);
		assertEquals(board.data_.getMana_p0(), 10);
		assertEquals(board.data_.getMana_p1(), 10);
		assertEquals(board.data_.getHero_p0().getHealth(), 30);
		assertEquals(board.data_.getHero_p1().getHealth(), 30);
		
		assertEquals(board.data_.getMinion_p0(0).getTotalHealth(), 4);
		assertEquals(board.data_.getMinion_p0(1).getTotalHealth(), 3);
		assertEquals(board.data_.getMinion_p0(2).getTotalHealth(), 6);
		
		assertEquals(board.data_.getMinion_p1(0).getTotalHealth(), 1);
		assertEquals(board.data_.getMinion_p1(1).getTotalHealth(), 4);
		assertEquals(board.data_.getMinion_p1(2).getTotalHealth(), 2);
		assertEquals(board.data_.getMinion_p1(3).getTotalHealth(), 7);

		assertEquals(board.data_.getMinion_p0(0).getTotalAttack(), 4);
		assertEquals(board.data_.getMinion_p0(1).getTotalAttack(), 3);
		assertEquals(board.data_.getMinion_p0(2).getTotalAttack(), 7);
		
		assertEquals(board.data_.getMinion_p1(0).getTotalAttack(), 3);
		assertEquals(board.data_.getMinion_p1(1).getTotalAttack(), 5);
		assertEquals(board.data_.getMinion_p1(2).getTotalAttack(), 2);
		assertEquals(board.data_.getMinion_p1(3).getTotalAttack(), 7);
	}
	
	@Test
	public void test1() throws HSException {
		
		//null case
		Minion target = board.data_.getCharacter(0, 3);
		Card theCard = board.data_.getCard_hand_p0(0);
		HearthTreeNode ret = theCard.useOn(0, target, board, deck, deck);
		
		assertFalse(ret == null);
		assertEquals(board.data_.getNumCards_hand(), 0);
		assertEquals(board.data_.getNumMinions_p0(), 4);
		assertEquals(board.data_.getNumMinions_p1(), 4);
		assertEquals(board.data_.getMana_p0(), 8);
		assertEquals(board.data_.getMana_p1(), 10);
		assertEquals(board.data_.getHero_p0().getHealth(), 30);
		assertEquals(board.data_.getHero_p1().getHealth(), 30);
		
		assertEquals(board.data_.getMinion_p0(0).getTotalHealth(), 4);
		assertEquals(board.data_.getMinion_p0(1).getTotalHealth(), 3);
		assertEquals(board.data_.getMinion_p0(2).getTotalHealth(), 6);
		assertEquals(board.data_.getMinion_p0(3).getTotalHealth(), 2);
		
		assertEquals(board.data_.getMinion_p1(0).getTotalHealth(), 1);
		assertEquals(board.data_.getMinion_p1(1).getTotalHealth(), 4);
		assertEquals(board.data_.getMinion_p1(2).getTotalHealth(), 2);
		assertEquals(board.data_.getMinion_p1(3).getTotalHealth(), 7);

		assertEquals(board.data_.getMinion_p0(0).getTotalAttack(), 4);
		assertEquals(board.data_.getMinion_p0(1).getTotalAttack(), 3);
		assertEquals(board.data_.getMinion_p0(2).getTotalAttack(), 7);
		assertEquals(board.data_.getMinion_p0(3).getTotalAttack(), 4);
		
		assertEquals(board.data_.getMinion_p1(0).getTotalAttack(), 3);
		assertEquals(board.data_.getMinion_p1(1).getTotalAttack(), 5);
		assertEquals(board.data_.getMinion_p1(2).getTotalAttack(), 2);
		assertEquals(board.data_.getMinion_p1(3).getTotalAttack(), 7);
		
		assertEquals(ret.numChildren(), 7);

		
		//------------------------------------------------------------------
		//------------------------------------------------------------------

		HearthTreeNode ret0 = ret.getChildren().get(0);
		assertEquals(ret0.data_.getNumCards_hand(), 0);
		assertEquals(ret0.data_.getNumMinions_p0(), 4);
		assertEquals(ret0.data_.getNumMinions_p1(), 4);
		assertEquals(ret0.data_.getMana_p0(), 8);
		assertEquals(ret0.data_.getMana_p1(), 10);
		assertEquals(ret0.data_.getHero_p0().getHealth(), 30);
		assertEquals(ret0.data_.getHero_p1().getHealth(), 30);
		
		assertEquals(ret0.data_.getMinion_p0(0).getTotalHealth(), 4);
		assertEquals(ret0.data_.getMinion_p0(1).getTotalHealth(), 3);
		assertEquals(ret0.data_.getMinion_p0(2).getTotalHealth(), 6);
		assertEquals(ret0.data_.getMinion_p0(3).getTotalHealth(), 2);
		
		assertEquals(ret0.data_.getMinion_p1(0).getTotalHealth(), 1);
		assertEquals(ret0.data_.getMinion_p1(1).getTotalHealth(), 4);
		assertEquals(ret0.data_.getMinion_p1(2).getTotalHealth(), 2);
		assertEquals(ret0.data_.getMinion_p1(3).getTotalHealth(), 7);

		assertEquals(ret0.data_.getMinion_p0(0).getTotalAttack(), 4);
		assertEquals(ret0.data_.getMinion_p0(1).getTotalAttack(), 3);
		assertEquals(ret0.data_.getMinion_p0(2).getTotalAttack(), 7);
		assertEquals(ret0.data_.getMinion_p0(3).getTotalAttack(), 4);
		
		assertEquals(ret0.data_.getMinion_p1(0).getTotalAttack(), 3);
		assertEquals(ret0.data_.getMinion_p1(1).getTotalAttack(), 5);
		assertEquals(ret0.data_.getMinion_p1(2).getTotalAttack(), 2);
		assertEquals(ret0.data_.getMinion_p1(3).getTotalAttack(), 7);

		//------------------------------------------------------------------
		//------------------------------------------------------------------

		HearthTreeNode ret1 = ret.getChildren().get(1);
		assertEquals(ret1.data_.getNumCards_hand(), 0);
		assertEquals(ret1.data_.getNumMinions_p0(), 4);
		assertEquals(ret1.data_.getNumMinions_p1(), 4);
		assertEquals(ret1.data_.getMana_p0(), 8);
		assertEquals(ret1.data_.getMana_p1(), 10);
		assertEquals(ret1.data_.getHero_p0().getHealth(), 30);
		assertEquals(ret1.data_.getHero_p1().getHealth(), 30);
		
		assertEquals(ret1.data_.getMinion_p0(0).getTotalHealth(), 4);
		assertEquals(ret1.data_.getMinion_p0(1).getTotalHealth(), 3);
		assertEquals(ret1.data_.getMinion_p0(2).getTotalHealth(), 6);
		assertEquals(ret1.data_.getMinion_p0(3).getTotalHealth(), 2);
		
		assertEquals(ret1.data_.getMinion_p1(0).getTotalHealth(), 1);
		assertEquals(ret1.data_.getMinion_p1(1).getTotalHealth(), 4);
		assertEquals(ret1.data_.getMinion_p1(2).getTotalHealth(), 2);
		assertEquals(ret1.data_.getMinion_p1(3).getTotalHealth(), 7);

		assertEquals(ret1.data_.getMinion_p0(0).getTotalAttack(), 3);
		assertEquals(ret1.data_.getMinion_p0(1).getTotalAttack(), 3);
		assertEquals(ret1.data_.getMinion_p0(2).getTotalAttack(), 6);
		assertEquals(ret1.data_.getMinion_p0(3).getTotalAttack(), 3);
		
		assertEquals(ret1.data_.getMinion_p1(0).getTotalAttack(), 3);
		assertEquals(ret1.data_.getMinion_p1(1).getTotalAttack(), 5);
		assertEquals(ret1.data_.getMinion_p1(2).getTotalAttack(), 2);
		assertEquals(ret1.data_.getMinion_p1(3).getTotalAttack(), 7);

		//------------------------------------------------------------------
		//------------------------------------------------------------------

		HearthTreeNode ret2 = ret.getChildren().get(2);
		assertEquals(ret2.data_.getNumCards_hand(), 0);
		assertEquals(ret2.data_.getNumMinions_p0(), 4);
		assertEquals(ret2.data_.getNumMinions_p1(), 4);
		assertEquals(ret2.data_.getMana_p0(), 8);
		assertEquals(ret2.data_.getMana_p1(), 10);
		assertEquals(ret2.data_.getHero_p0().getHealth(), 30);
		assertEquals(ret2.data_.getHero_p1().getHealth(), 30);
		
		assertEquals(ret2.data_.getMinion_p0(0).getTotalHealth(), 3);
		assertEquals(ret2.data_.getMinion_p0(1).getTotalHealth(), 2);
		assertEquals(ret2.data_.getMinion_p0(2).getTotalHealth(), 6);
		assertEquals(ret2.data_.getMinion_p0(3).getTotalHealth(), 1);
		
		assertEquals(ret2.data_.getMinion_p1(0).getTotalHealth(), 1);
		assertEquals(ret2.data_.getMinion_p1(1).getTotalHealth(), 4);
		assertEquals(ret2.data_.getMinion_p1(2).getTotalHealth(), 2);
		assertEquals(ret2.data_.getMinion_p1(3).getTotalHealth(), 7);

		assertEquals(ret2.data_.getMinion_p0(0).getTotalAttack(), 3);
		assertEquals(ret2.data_.getMinion_p0(1).getTotalAttack(), 2);
		assertEquals(ret2.data_.getMinion_p0(2).getTotalAttack(), 7);
		assertEquals(ret2.data_.getMinion_p0(3).getTotalAttack(), 3);
		
		assertEquals(ret2.data_.getMinion_p1(0).getTotalAttack(), 3);
		assertEquals(ret2.data_.getMinion_p1(1).getTotalAttack(), 5);
		assertEquals(ret2.data_.getMinion_p1(2).getTotalAttack(), 2);
		assertEquals(ret2.data_.getMinion_p1(3).getTotalAttack(), 7);

		//------------------------------------------------------------------
		//------------------------------------------------------------------
	}
}