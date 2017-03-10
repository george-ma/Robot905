package Summative;

import java.awt.*;
import becker.robots.*;
import java.util.*;

/**
 * Fighter Robot that makes use of Robot905OppData and win-rates to determine it's algorithm for the turn.
 * @author George
 * @date 01/20/2016
 */
public class Robot905 extends FighterRobot {

	//Constant Variables (stats)
	private final static int ATTACK = 5;
	private final static int DEFENSE = 4;
	private final static int SPEED = 1;

	//Instance Variables
	private int health;
	private int myID;
	private int turnNum=0;

	//My Extended Opponent Data Which Keeps Track of Wins, Losses and Ties
	private Robot905OppData opponents [];

	/**
	 * Constructor for the Robot905.
	 * @param c - Starting City
	 * @param a - Starting Avenue
	 * @param s - Starting Street
	 * @param d - Starting Direction
	 * @param id - The Robot's unique ID
	 * @param health - The Robot's starting Health
	 */
	public Robot905(City c, int a, int s, Direction d, int id, int health) {
		super(c, a, s, d, id, ATTACK, DEFENSE, SPEED);

		//Initializing the Health and ID
		this.health=health;
		this.myID = id;

		this.setLabel(); //Calls upon the set label method
	}

	/**
	 * This method sets the robot's colour and ID.
	 * Also sets the Robot's colour to black once the robot is dead. (health <= 0) 
	 */
	public void setLabel(){
		//Sets the colour and label of the robot.
		this.setColor(Color.magenta);
		this.setLabel(this.getID()+" "+this.health);

		//Checks for when the robot's health is less than or equal to 0.
		if (this.health <= 0){
			this.health=0; //Sets the robot's health to 0.
			this.setColor(Color.BLACK); //Sets the robot's colour to black.
		}
	}

	/**
	 * Method which receives the desired (valid) avenue and street from the battle manager (which is passed from the takeTurn method)
	 * and calls upon sub methods to move the robot.
	 *
	 */
	public void goToLocation(int a, int s) {
		this.moveX(a);
		this.moveY(s);
	}

	/**
	 * Sub method which moves the robot to the desired avenue
	 * @param a - The desired avenue coordinate of the robot
	 */
	private void moveX(int a) {
		int cAvenue = this.getAvenue(); //current avenue

		//If the desired 'x' value is west of the robot's current position
		if (a < cAvenue){

			//Turns the robot in the correct direction (west)
			while (this.getDirection() != Direction.WEST){
				this.turnLeft();
			}

			this.move(cAvenue-a); //Moves the robot
		}

		//If the desired 'x' value is east of the robot's current position
		if (a > cAvenue){

			//Turns the robot in the correct direction (east)
			while (this.getDirection() != Direction.EAST){
				this.turnLeft();
			}

			this.move(a-cAvenue); //Moves the robot
		}
	}

	/**
	 * Sub method which moves the robot to the desired (valid) street
	 * @param s - The desired street coordinate of the robot
	 */
	private void moveY(int s) {
		int cStreet = this.getStreet(); //current street

		//If the 'y' value is above the robot's current position
		if (s < cStreet){

			//Turns the robot in the correct direction (north)
			while (this.getDirection() != Direction.NORTH){
				this.turnLeft();
			}

			this.move(cStreet-s); //Moves the robot
		}	

		//If the 'y' value is below the robot's current position
		if (s > cStreet){

			//Turns the robot in the correct direction (south)
			while (this.getDirection() != Direction.SOUTH){
				this.turnLeft();
			}

			this.move(s-cStreet); //Moves the robot
		}
	}

	/**
	 * Method which uses algorithms to return a valid turn request to the battle manager.
	 */
	public TurnRequest takeTurn(int energy, OppData[] data) {

		//Declaring and Initializing variables
		TurnRequest turn = null;
		int maxSteps,maxRounds;
		this.turnNum += 1;

		this.opponents = getOpponentData(data); //Initializes the 905OppData opponents array
		int fightID = bestOpponent(data); //The opponent which is highest on the priority list. 

		//Determining the max steps and max rounds
		maxSteps = calculateMaxSteps(energy);
		maxRounds = calculateMaxRounds(energy);

		//Determines the avenue and street coordinates of the desired opponent, based on the max number of steps per round
		int tempAvenue = this.getAvenue(); //used to find the number of steps which will be used to move the street coordinate
		int avenueFinal = calcAvenueFinal(opponents[fightID].getAvenue(),maxSteps);
		int streetFinal = calcStreetFinal(opponents[fightID].getStreet(),maxSteps-Math.abs(avenueFinal-tempAvenue));

		//		//Testing
		//		System.out.println(opponents[fightID].getAvenue() + " " + opponents[fightID].getStreet());
		//		System.out.println("fightID: " + fightID);

		//Decides on a move. Using the opponents position and current energy levels, and extended Opp Data.
		String decidedMove = determineMove(avenueFinal,streetFinal,fightID,energy);

		//If the desired move is a high % fight, it will fight for the maximum number of rounds it can
		if (decidedMove == "HIGH_%_FIGHT"){
			turn = new TurnRequest(avenueFinal,streetFinal,fightID,maxRounds);
		}

		//If the robot has low energy, it will try and fight for the maximum number of rounds. (to try and kill the opponent)
		else if (decidedMove == "LOW_ENERGY_FIGHT"){
			turn = new TurnRequest(avenueFinal,streetFinal,fightID,maxRounds);
		}

		//If the robot has not fought the robot yet, it will try and 'test' fight it, not fighting the maximum number of rounds.
		else if (decidedMove == "TEST_FIGHT"){
			turn = new TurnRequest(avenueFinal,streetFinal,fightID,maxRounds/2);
		}

		//If the robot is not on top of the target, it will try to move towards it.
		else if (decidedMove == "MOVE_TO_TARGET"){
			turn = new TurnRequest(avenueFinal,streetFinal,-1,0);
		}

		return turn; //Returns the turn
	}

	/** 
	 * Method which returns the max number of steps
	 * @param energy - Current energy level
	 * @return - Returns the maximum number of steps
	 */
	private int calculateMaxSteps(int energy) {
		int maxSteps;

		//Calculates how many steps are allowed based on energy and speed

		//Determines if the robot's speed*energy per step is less than it's current energy
		if (SPEED*5<energy){
			maxSteps=SPEED;	
		}

		//Or it sets the max steps it can take to it's energy level / 5.
		else{
			maxSteps=energy/5;
		}

		return maxSteps;

	}

	/** 
	 * Method which returns the max number of rounds it can fight
	 * @param energy - Current energy level
	 * @return - Returns the maximum number of rounds the robot can fight.
	 */
	private int calculateMaxRounds(int energy) {

		//Sets the max rounds initially to its energy/20.
		int maxRounds = energy/20; 

		//Limits the max rounds it can fight based on the robot's attack
		if (maxRounds >= ATTACK) {
			maxRounds = ATTACK;
		}

		return maxRounds;
	}

	/**
	 * 
	 * @param avenueFinal - The avenue that the robot will be at the end of the turn
	 * @param streetFinal - The street that the robot will be at the end of the turn
	 * @param fightID - The desired ID it wishes to fight
	 * @param energy - The energy level of the robot
	 * @return - Returns the optimal move
	 */
	private String determineMove(int avenueFinal, int streetFinal, int fightID, int energy) {

		//Determines if the robot will be at the same street and avenue as the target opponent
		if (avenueFinal==this.opponents[fightID].getAvenue() && streetFinal==this.opponents[fightID].getStreet()){

			//If the energy is less than 20, the robot will try and fight it's maximum number of rounds
			if (energy<20){
				return "LOW_ENERGY_FIGHT";

			}

			//If the energy is greater than 20
			else{

				//It will use Robot905OppData to determine whether the robot is successful at attacking this opponent. (win rate > 60%)
				if (this.opponents[fightID].optimalNumRounds()){
					return "HIGH_%_FIGHT";
				}

				//Or, the robot will use half the number of rounds it can attack, 
				//and determine whether or not it is good at attacking this robot
				else{
					return "TEST_FIGHT";
				}
			}

		}
		//Or else if not on top of the opponent, the robot attempts to move to the target
		else{
			return "MOVE_TO_TARGET";
		}

	}


	/**
	 * Finds the most optimal target based on priority (health + distance) to attack
	 * @param data - Obtains opponent data
	 * @return opponentID - Returns the Best Target's ID
	 */
	private int bestOpponent(OppData[] data) {

		//Initializing the targetID and bestScore
		int targetID=0;
		double bestScore=100000; //lowest score = the best target

		OppData [] priorityList = sortPriority(data); //Creates a sorted array of the opponent data based on its priority (health+dist)

		//Loop which runs through the sorted array of opp data and finds the opponent with the lowest score
		for (int i=0;i<priorityList.length;i++){

			//Makes sure it is not looking for itself and that the opponent is alive
			if (priorityList[i].getID()!=this.myID && priorityList[i].getHealth() > 0){

				//Calculates the score of this opponent
				double score = calcPriority(priorityList[i]); 

				//Checks the score against the current best score, updates the score and the targetID if needed.
				if (calcPriority(priorityList[i])<bestScore){
					bestScore = score;
					targetID = priorityList[i].getID();
				}
			}
		}
		return targetID;
	}

	/**
	 * Method which sorts the data based on priority using a selection sort.
	 * @param data - The given oppdata
	 * @return - Returns the sorted OppData array
	 */
	private OppData[] sortPriority(OppData[] data) {
		OppData[] tempData = data;

		//Iterates through sorted list
		for (int i=0;i<tempData.length-1;i++){

			//Iterates through the unsorted list
			for (int x=i+1;x<tempData.length;x++){

				//Checks if the priority of the object in the unsorted list is less than the priority of the sorted list
				if (calcPriority(tempData[i])>calcPriority(tempData[x])){

					//Swaps the objects around
					OppData temp = tempData[x];
					tempData[x] = tempData[i];
					tempData[i] = temp;
				}
			}
		}

		return tempData; //returns the opponent data sorted by priority
	}

	/**
	 * Method which calculates the priority (health+dist) of an object, used for sorting
	 * @param opponent - The opponent which the priority is calculating for
	 * @return - Returns its priority (a double)
	 */
	private double calcPriority(OppData opponent) {

		//Initializes the variable
		double priority;

		priority = (opponent.getHealth()/100)*2+(calculateDist(opponent)/32);

		return priority; //Returns the priority of the opponent
	}

	/**
	 * Method which stores the opponent data into the instance variable: opponents.
	 * @param data - Opponent Data
	 * @return - Returns the array of opponents
	 */
	private Robot905OppData[] getOpponentData(OppData[] data) {

		//Initializes length of the array
		this.opponents = new Robot905OppData[data.length];

		//Stores the opponent's data
		for (int i=0;i<data.length;i++){
			this.opponents[i] = new Robot905OppData(data[i].getID(),data[i].getAvenue(),data[i].getStreet(),data[i].getHealth());
		}

		return this.opponents;
	}

	/**
	 * Method which calculates the distance the opponent is from the robot by finding the absolute difference of its
	 * avenue added by the absolute distance of the street.
	 * @param opp - The opponent's data
	 * @return - Returns the distance of the opponent
	 */
	private double calculateDist(OppData opp) {

		//Gets the absolute value of the difference of the opponent and the robot's street & avenue.
		double absX = Math.abs(this.getAvenue()-opp.getAvenue());
		double absY = Math.abs(this.getStreet()-opp.getStreet());

		return absX+absY;
	}

	/**
	 * Calculates the desired 'X' value of the robot and moves the robot accordingly (based on remaining steps)
	 * @param desiredAvenue - The desired avenue of the robot
	 * @param remainingSteps - Steps left that the robot can take
	 * @return - Returns the avenue which the robot can legally move to
	 */
	private int calcAvenueFinal(int desiredAvenue, int remainingSteps) {
		int cAvenue = this.getAvenue(); //Current avenue

		int difference = (desiredAvenue-cAvenue);

		//The desired avenue is to the right of the current avenue
		if (difference > 0){

			//If the different is greater than the remaining steps, it adds the remaining steps to the current avenue
			if (difference>remainingSteps){
				return (cAvenue+remainingSteps);
			}
			
			//Or if the difference is less than the remaining steps, it adds the difference of the avenues
			else{
				return (cAvenue+difference);
			}

		}
		//The desired avenue is to the left of the current avenue
		else if (difference < 0){
			difference = Math.abs(difference);

			//If the abs difference is greater than the remaining steps, it subtracts the remaining steps from the current avenue
			if (difference>remainingSteps){
				return (cAvenue-remainingSteps);
			}
			//If the abs difference is less than the remaining steps, it subtract the difference from the current avenue
			else{
				return (cAvenue-difference);
			}

			//Or it returns the current position
		}else{
			return cAvenue;
		}
	}

	/**
	 * Calculates the desired 'Y' value of the robot and moves the robot accordingly (based on remaining steps)
	 * @param desiredStreet - The desired street ('y') of the robot
	 * @param remainingSteps - Steps left that the robot can take
	 * @return - Returns the street which the robot can legally move to
	 */
	private int calcStreetFinal(int desiredStreet, int remainingSteps) {
		int cStreet = this.getStreet(); //Current street

		int difference = (desiredStreet-cStreet);

		//The desired street is below that of the current street
		if (difference > 0){

			//If the difference is greater than the remaining steps, it will add the remaining steps and the current street
			if (difference>remainingSteps){
				return (cStreet+remainingSteps);
			}
			//Or, it will add the difference
			else{
				return (cStreet+difference);
			}
		}
		//The desired street is above that of the current street
		else if (difference < 0){
			difference = Math.abs(difference);

			//If the abs difference is greater than the remaining steps, it will subtract the remaining steps and the current street
			if (difference>remainingSteps){
				return (cStreet-remainingSteps);
			}
			//Or it will subtract the difference
			else{
				return (cStreet-difference);
			}
		}
		//Or it returns the current position
		else{
			return cStreet;
		}
	}


	/**
	 * Method which calls upon the overridden battleResult method
	 */
	public void battleResult(int healthLost, int oppID, int oppHealthLost,int numRoundsFought) {

		if (turnNum > 0){
		// Call the overrode method
		this.battleResult(healthLost, oppID, oppHealthLost, numRoundsFought, this.opponents);
		}
		else{
			this.health-=1;
		}
	}

	/**
	 * Extended Battle Results which stores the statistics of a battle into MappOppData
	 * @param healthLost - Health lost by this robot
	 * @param oppID - Opponent's ID
	 * @param oppHealthLost - Health lost by the opponent
	 * @param numRoundsFought - not used
	 * @param data - Opponent data which is sent by battle manager
	 */
	public void battleResult(int healthLost, int oppID, int oppHealthLost,int numRoundsFought, Robot905OppData [] data) {

		if (oppID != -1 && oppID >=0){

			//My Robot Won -> increase the number of losses for the opposing robot
			if (oppHealthLost > healthLost){
				this.opponents[oppID].increaseNumLoss();

				//Opponent Robot Won --> increase the number of wins for the opposing robot
			}else if (healthLost > oppHealthLost){
				this.opponents[oppID].increaseNumWins();

				//Tie
			}else if (healthLost == oppHealthLost && numRoundsFought > 0){
				this.opponents[oppID].increaseNumTies();
			}

		}

		this.health -= healthLost; //Subtracts the health lost from current health accordingly
	}


}