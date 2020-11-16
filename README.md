# Connect-Four
A JAR file that trains a TDL agent to play Connect 4. The Minimax agent and the Connect 4 Framework is taken from [MarkusThill/Connect-Four](https://github.com/MarkusThill/Connect-Four).

# Running the JAR file

```
java -jar c4console.jar trainAgainstMinimax alpha epsilon
```
- trainAgainstMinimax (true/false): Determines whether the TDL agent is trained through self-play or against a Minimax agent. The default value is false.
- alpha (positive real): Determines the initial learning rate. Default is 0.004.
- epsilon (positive real): Determines the exploreation rate. Default is 0.1.

# Output

The program generates the file results.csv, which includes the score of the TDL agent measured after every 25000 training games. The score is a value between 0 and 100 that is determined over 100 evaluation games against the Minimax agent. Since the Minimax agent can force a win if it goes first, the TDL agent goes first and the Minimax agent go second. For each game, the TDL agent is awarded 1 point for a win, 0.5 points for a draw, and 0 points for a loss.

Sample results and graphs can be found under the "Results" folder.
