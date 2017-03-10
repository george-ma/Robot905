package Summative;

import java.awt.*;
import becker.robots.*;
import java.util.*;

/**
 * Fighter Robot
 * @author George
 *
 */
public class TestFighterRunner extends FighterRobot {

	private int health;
	private int myID;

	private static int ATTACK = 1;
	private static int DEFENSE = 5;
	private static int SPEED = 4;
	
	public static final int WIDTH = 20;
	public static final int HEIGHT = 12;
	private boolean swap=false;


	private OppData opponents [];

	public TestFighterRunner(City c, int a, int s, Direction d, int id, int health) {
		super(c, a, s, d, id, ATTACK, DEFENSE, SPEED);
		this.health=health;
		this.myID = id;

		this.setLabel();
	}

	public void setLabel(){
		this.setColor(Color.YELLOW);
		this.setLabel(this.getID()+" "+this.health);

		if (this.health <= 0){
			this.health=0;
			this.setColor(Color.BLACK);
		}
	}

	@Override
	public void goToLocation(int a, int s) {
		this.moveX(a);
		this.moveY(s);
		//int avenue = this.getAvenue();
	}

	private void moveX(int a) {
		int cAvenue = this.getAvenue(); //current avenue

		//If the desired 'x' value is west of the robot's current position
		if (a < cAvenue){
			while (this.getDirection() != Direction.WEST){
				this.turnLeft();
			}
			this.move(cAvenue-a); //Moves the robot
		}

		//If the desired 'x' value is east of the robot's current position
		if (a > cAvenue){
			while (this.getDirection() != Direction.EAST){
				this.turnLeft();
			}
			this.move(a-cAvenue); //Moves the robot
		}
	}

	private void moveY(int s) {
		int cStreet = this.getStreet(); //current street

		//If the 'y' value is above the robot's current position
		if (s < cStreet){
			while (this.getDirection() != Direction.NORTH){
				this.turnLeft();
			}
			this.move(cStreet-s); //Moves the robot
		}	

		//If the 'y' value is below the robot's current position
		if (s > cStreet){
			while (this.getDirection() != Direction.SOUTH){
				this.turnLeft();
			}
			this.move(s-cStreet); //Moves the robot
		}
	}

	public TurnRequest takeTurn(int energy, OppData[] data) {

		TurnRequest turn = null;
		int maxSteps,maxRounds;
		int x=0,y=0;


		this.opponents = getOpponentData(data);
		int fightID = bestOpponent(data);

		maxSteps = calculateMaxSteps(energy);
		maxRounds = calculateMaxRounds(energy);
		
//		if (this.getAvenue()>0){
//			x = 0;
//		}
		
		if (swap==false){
			if (this.getStreet() > -1){
				y = 0;
			}
			if (this.getStreet() == 0){
				swap = true;
			}
		}
		
		if (swap==true){
			if (this.getStreet() < HEIGHT){
				y = HEIGHT-1;
			}
			if (this.getStreet() == HEIGHT-1){
				swap = false;
			}
		}
		
		int tempAvenue = this.getAvenue(); //used to find the number of steps which remains to move the street coordinate
		int avenueFinal = calcAvenueFinal(this.getAvenue(),maxSteps);
		int streetFinal = calcStreetFinal(y,maxSteps-Math.abs(avenueFinal-tempAvenue));

//		//Testing
//		System.out.println(opponents[fightID].getAvenue() + " " + opponents[fightID].getStreet());
//		System.out.println("fightID: " + fightID);

		turn = new TurnRequest(avenueFinal,streetFinal,-1,0);
		
//		if (avenueFinal==this.opponents[fightID].getAvenue() && streetFinal==this.opponents[fightID].getStreet()){
//			turn = new TurnRequest(avenueFinal,streetFinal,fightID,maxRounds);
//		}else{
//			
//		}

		return turn;
	}

	private int calculateMaxSteps(int energy) {
		int maxSteps;
		
//		//Calculates how many steps are allowed based on energy and speed
//		
//		if (SPEED*5>energy){
//			maxSteps = (SPEED*5-energy)/energy;
//		}else if (SPEED*5<energy){
//			maxSteps = SPEED;
//		}else{
//			maxSteps = SPEED-1;
//		}
//		
//		if (maxSteps < 1 && energy != 0){
//			maxSteps = energy/5;
//		}
		
		//Calculates how many steps are allowed based on energy and speed
		if (SPEED*5<energy){
			maxSteps=SPEED;
		}else{
			maxSteps=energy/5;
		}
		
		return maxSteps;

	}
	
	private int calculateMaxRounds(int energy) {
		int maxRounds = energy/20;
		
		//Calculates the max number of rounds based on energy
		if (maxRounds >= ATTACK) {
			maxRounds = ATTACK;
		}
		
		return maxRounds;
	}

	private String determineMove(int avenueFinal, int streetFinal, int fightID, int energy) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Finds the most optimal target based on priority (health + distance) to attack
	 * @param data - Obtains opponent data
	 * @return opponentID - Returns the Best Target's ID
	 */
	private int bestOpponent(OppData[] data) {
		int targetID=0;
		double bestScore=100000; //lowest score = highest on the priority list (closest to a priority of 1.0)

		OppData [] priorityList = sortPriority(data);

		for (int i=0;i<priorityList.length;i++){

			if (priorityList[i].getID()!=this.myID && priorityList[i].getHealth() > 0){

				double score = calcPriority(priorityList[i]);

				if (calcPriority(priorityList[i])<bestScore){
					bestScore = score;
					targetID = priorityList[i].getID();
				}
			}
		}
		return targetID;
	}

	private OppData[] sortPriority(OppData[] data) {
		OppData[] tempData = data;

		//Selection sort
		for (int i=0;i<tempData.length - 1;i++){
			
			for (int x=i+1;x<tempData.length;x++){
				
				if (calcPriority(tempData[i])>calcPriority(tempData[x])){
					
					//swap
					OppData temp = tempData[x];
					tempData[x] = tempData[i];
					tempData[i] = temp;
				}
			}
		}

		return tempData; //returns the opponent data sorted by array
	}


	private double calcPriority(OppData opponent) {
		double priority;
		priority = (opponent.getHealth()/100)+(calculateDist(opponent)/32);
		return priority;
	}

	private OppData[] getOpponentData(OppData[] data) {

		this.opponents = new OppData[data.length];

		for (int i=0;i<data.length;i++){
			this.opponents[i] = new OppData(data[i].getID(),data[i].getAvenue(),data[i].getStreet(),data[i].getHealth());
		}

		return this.opponents;
	}

	private double calculateDist(OppData opp) {
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

			if (difference>remainingSteps)
				return (cAvenue+remainingSteps);
			else
				return (cAvenue+difference);

			//The desired avenue is to the left of the current avenue
		}else if (difference < 0){
			difference = Math.abs(difference);

			if (difference>remainingSteps)
				return (cAvenue-remainingSteps);
			else
				return (cAvenue-difference);

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

			if (difference>remainingSteps)
				return (cStreet+remainingSteps);
			else
				return (cStreet+difference);

			//The desired street is above that of the current street
		}else if (difference < 0){
			difference = Math.abs(difference);

			if (difference>remainingSteps)
				return (cStreet-remainingSteps);
			else
				return (cStreet-difference);

			//Or it returns the current position
		}else{
			return cStreet;
		}
	}

	@Override
	public void battleResult(int healthLost, int oppID, int oppHealthLost,int numRoundsFought) {

		this.health-=healthLost;

	}

}