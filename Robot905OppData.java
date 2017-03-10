package Summative;

/**
 * Extends OppData (opponent's data) and adds extra functionality
 * @author George
 * @date 01/23/2016
 */
public class Robot905OppData extends OppData {

	//Initializes variables
	private int id;
	private int avenue;
	private int street;
	private int health;
	
	private int numberOfWins;
	private int numberOfLoss;
	private int numberOfTies;
	private int numFightsTotal;
	
	/**
	 * Constructor for the 905OppData which keeps track of the number of wins, loss and ties
	 * @param id - The id of the opponent
	 * @param a - Avenue of the opponent
	 * @param s - Street of the opponent
	 * @param health - Health of the opponent
	 */
	public Robot905OppData(int id, int a, int s, int health) {
		super(id, a, s, health);
		
		this.numberOfWins = 0;
		this.numberOfLoss = 0;
		this.numberOfTies = 0;
		
	}
	
	/**
	 * Method which determines whether my robot is effective at fighting this certain opponent
	 * @return - Returns true or false. Whether it is a high % robot.
	 */
	public boolean optimalNumRounds(){
		//Calculates the total number of rounds
		double totalRounds = numberOfWins+numberOfTies+numberOfLoss;
		
		//Checks if total rounds with this robot is greater than 10
		if (totalRounds >= 10){
			
			//Calculates whether my opponent has a win rate above 60%.
			if ((numberOfLoss/totalRounds)*100>=60){
				return true;
				
			}
			//Determines whether my opponent is not good as facing this robot
			else{
				
				return false;
			}
			
		}
		//Or, if there are not enoughs rounds with this opponent to decide.
		else{
			
			return false;
		}
	}
	
	/**
	 * Method which increases the number of wins my robot has against this opponent
	 */
	public void increaseNumWins() {

		this.numberOfWins += 1;
	}

	/**
	 *  Method which increases the number of losses my robot has against this opponent
	 */
	public void increaseNumLoss() {

		this.numberOfLoss += 1;

	}
	
	/**
	 *  Method which increases the number of ties my robot has against this opponent
	 */
	public void increaseNumTies() {

		this.numberOfTies += 1;

	}
	
}
