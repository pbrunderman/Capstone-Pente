This is my Capstone project for my Bachelor's in Information Systems and Technology degree program.

For this project we were tasked with designing the logic and execution algorithms for an AI opponent for the board game "Pente".

The AI for this project was to be completely deterministic.
It does not incorporate any heuristic decision making or machine learning methodologies.
We the programmer were meant to use our understanding of the game and the strategy to design the best AI we could without implementing any external supportive data.

I was awarded "Best in Class" for this submission and received a physical copy of the game board game as a prize.

You'll find a few classes inside that drive this application, but the one containing the majority of my work is AllAIMoves.java
This class is what drives the AI decision making entirely. It evaluates all the possible moves for the AI and assigns each possible move a score. 
The best score is selected and the AI makes their move. 

In almost every game I found that there were multiple equal scoring "best" moves for my AI. So I wanted to evaluate the best of the best moves to push my AI to a higher level. 
Given that a move's score was based entirely on pattern recognition and creating a specific pattern to acheivea favorable position 
it was challenging to figure out how to determine which move was more valuable.

My solution was to try to evaluate and compare the result of each move if it were played. So I created the evaluateDuplicateMoves() method. This method plays every equal scoring "best" move and 
compares the board state of each move and selects the best one. The algorithm attempts to evaluate the resulting position of both the AI and the player for each move under consideration.
