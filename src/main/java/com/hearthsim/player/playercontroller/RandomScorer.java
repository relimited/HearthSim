/**
 * 
 */
package com.hearthsim.player.playercontroller;

import java.util.Random;

import com.hearthsim.card.Card;
import com.hearthsim.card.minion.Minion;
import com.hearthsim.model.BoardModel;
import com.hearthsim.model.PlayerSide;

/**
 * A scorer that supplies a random score to a board state
 * 
 * All scores are uniformly (or psudo-uniformly, I didn't check how bad Java's standard RNG is) generated 
 * from -10 to 10, which is a range I just pulled out of my ass.
 * @author Johnathan
 *
 */
public class RandomScorer implements BoardScorer {

	public Random randomGenerator = new Random();
	public double lowerBound = -10;
	public double upperBound = 10;
	
	/* (non-Javadoc)
	 * @see com.hearthsim.player.playercontroller.BoardScorer#boardScore(com.hearthsim.model.BoardModel)
	 */
	@Override
	public double boardScore(BoardModel board) {
		double range = upperBound - lowerBound + 1;
		double fraction = range * randomGenerator.nextDouble();
		return lowerBound + fraction;
	}

	/* (non-Javadoc)
	 * @see com.hearthsim.player.playercontroller.BoardScorer#cardInHandScore(com.hearthsim.card.Card, com.hearthsim.model.BoardModel)
	 */
	@Override
	@Deprecated
	public double cardInHandScore(Card card, BoardModel board) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.hearthsim.player.playercontroller.BoardScorer#minionOnBoardScore(com.hearthsim.card.minion.Minion, com.hearthsim.model.PlayerSide, com.hearthsim.model.BoardModel)
	 */
	@Override
	@Deprecated
	public double minionOnBoardScore(Minion minion, PlayerSide side,
			BoardModel board) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.hearthsim.player.playercontroller.BoardScorer#heroHealthScore_p0(double, double)
	 */
	@Override
	@Deprecated
	public double heroHealthScore_p0(double heroHealth, double heroArmor) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.hearthsim.player.playercontroller.BoardScorer#heroHealthScore_p1(double, double)
	 */
	@Override
	@Deprecated
	public double heroHealthScore_p1(double heroHealth, double heroArmor) {
		// TODO Auto-generated method stub
		return 0;
	}

	public RandomScorer deepCopy() {
		RandomScorer copied = new RandomScorer();
		copied.randomGenerator = this.randomGenerator;
		copied.lowerBound = this.lowerBound;
		copied.upperBound = this.upperBound;
		
		return copied;
	}

}
