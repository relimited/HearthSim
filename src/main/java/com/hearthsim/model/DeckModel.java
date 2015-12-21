package com.hearthsim.model;

import com.hearthsim.card.Card;
import com.hearthsim.card.Deck;
import com.hearthsim.util.DeepCopyable;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.math3.distribution.HypergeometricDistribution;

/*
 * Eventually, this class will be used to generate a probabilistic model of the opponent's deck
 * For now, just stubs and ideas.
 */

public class DeckModel implements DeepCopyable<DeckModel> {
	private final ArrayList<Card> uniqueCards;
	private ArrayList<Card> curDeck;
	private final int[] cardCounts;

	public DeckModel(BoardModel board)
	{
		this(board.modelForSide(PlayerSide.CURRENT_PLAYER).getDeck());
	}
	
	public DeckModel(Deck deck)
	{
		//need to figure out how to incorporate Cards We've Seen Played
		//But I feel like that might just need to be Future Work
		//We know the deck list, and we can get base-line statistics from that
		this.uniqueCards = new ArrayList<>();
		this.curDeck = new ArrayList<>();
		this.cardCounts = new int[11];
		for (int i = 0; i < deck.getNumCards(); i++)
		{
			Card temp = deck.drawCard(i);
			this.curDeck.add(temp.deepCopy());
			this.cardCounts[temp.getBaseManaCost()]++;
			if (!this.uniqueCards.contains(temp))
			{
				this.uniqueCards.add(temp.deepCopy());
			}
		} 
		
		//this.cards = deck.deepCopy();
	}
	
	//Currently, returns probability of having at least one card of a particular mana cost
	public double getProb(int manaVal, int HandSize)
	{
		HypergeometricDistribution dist = new HypergeometricDistribution(this.curDeck.size(), this.cardCounts[manaVal], HandSize);
		
		return dist.upperCumulativeProbability(1);
	}
	
	public void getCounts()
	{
		System.out.println(Arrays.toString(this.cardCounts));
	}
	
	public int cardCount()
	{
		return this.curDeck.size();
	}
	
	public int numManaCards(int manaCost)
	{
		int rVal = 0;
		for (int i = 0; i < this.uniqueCards.size(); i++)
		{
			if (this.uniqueCards.get(i).getBaseManaCost() == manaCost)
			{
				rVal++;
			}
		}
		return rVal;
	}
	
	@Override
	public DeckModel deepCopy() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
