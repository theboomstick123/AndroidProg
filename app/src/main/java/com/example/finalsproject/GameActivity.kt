package com.example.finalsproject

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.finalsproject.databinding.ActivityGameBinding


class GameActivity : AppCompatActivity() {
    enum class PlayerTurn{
        NOUGHT,
        CROSS
    }

    //Cell status map
    enum class CellStatus{
        EMPTY,
        LOCKED,
        UNLOCKED
    }

    //'firstTurn' represents the player who goes first, 'currentTurn' represents the current player.
    private var firstTurn = PlayerTurn.CROSS
    private var currentTurn = PlayerTurn.CROSS

    private var cellStatusMap: MutableMap<Pair<Int, Int>, CellStatus> = mutableMapOf()

    //POWER-UP STATUS MAPS

    //Variables to track if the Clear power-up has been used by each player
    private var clearPowerUpUsedMap: MutableMap<PlayerTurn, Boolean> = mutableMapOf(
        PlayerTurn.NOUGHT to false,
        PlayerTurn.CROSS to false
    )
    //Variables to track if the Undo power-up has been used by each player
    private var undoPowerUpUsedMap: MutableMap<PlayerTurn, Boolean> = mutableMapOf(
        PlayerTurn.NOUGHT to false,
        PlayerTurn.CROSS to false
    )

    // Variables to track the last move of each player
    private var lastMoveByNought: Move? = null
    private var lastMoveByCross: Move? = null

    //FLAG ACTIVE INDICATORS

    //Flag to indicate if the "Clear" power-up is activated
    private var clearPowerUpActivated = false
    // Flag to indicate if the "Undo" power-up is activated
    private var undoPowerUpActivated = false

    //Board Initialization:

    //'boardList' is a list of buttons representing the Tic-Tac-Toe game board.
    private var boardList = mutableListOf<Button>()

    private lateinit var binding : ActivityGameBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initBoard()

        // Set click listener for the "Clear" power-up button
        binding.imageButton1.setOnClickListener {
            onClearPowerUpButtonClick()
        }
        // Set click listener for the "Undo" power-up button
        binding.imageButton2.setOnClickListener {
            onUndoPowerUpButtonClick()
        }

    }

    companion object{
        const val NOUGHT = "O"
        const val CROSS = "X"
    }

    //'initBoard()' initializes this list with references to the buttons in your layout.
    private fun initBoard() {
        // Initializing the list of buttons representing the game board
        boardList.add(binding.a1)
        boardList.add(binding.a2)
        boardList.add(binding.a3)
        boardList.add(binding.a4)

        boardList.add(binding.b1)
        boardList.add(binding.b2)
        boardList.add(binding.b3)
        boardList.add(binding.b4)

        boardList.add(binding.c1)
        boardList.add(binding.c2)
        boardList.add(binding.c3)
        boardList.add(binding.c4)

        boardList.add(binding.d1)
        boardList.add(binding.d2)
        boardList.add(binding.d3)
        boardList.add(binding.d4)

    }

    //BOARD TAPPING EVENT

    //This function is triggered when a button on the game board is tapped.
    fun boardTapped(view: View){
        if(view !is Button)
            return

        // Check if the "Clear" power-up is activated
        if (clearPowerUpActivated) {
            // Process "Clear" power-up logic
            clearPowerUp(view)

            // Continue with the regular gameplay
            continueRegularGameplay()

            // Reset the "Clear" power-up activation flag
            clearPowerUpActivated = false
            return
        }

        //Regular gameplay logic
        //'addToBoard(view)' adds the current player's symbol to the tapped button.
        addToBoard(view)

        //'checkPlayerVictory()' is called to check if the current player has won.
        if(checkPlayerVictory()) {
            if (match(view, NOUGHT)) {
                result("Nought Wins!")
            } else if (match(view, CROSS)) {
                result("Cross Wins!")
            }
        }

        //'fullBoard()' is called to check if the game is a draw.
        if(fullBoard()){
            result("Draw")
        }
    }

    //ADDING SYMBOL TO THE BOARD:

    //This function adds the current player's symbol to the tapped button if it's empty.
    //It also switches the turn to the next player.
    private fun addToBoard(button: Button) {
        if(button.text != "") {
            // Cell already occupied, do nothing
            return
        }

        val row = boardList.indexOf(button) / 4
        val col = boardList.indexOf(button) % 4

        // Check if the cell is marked as locked
        if (cellStatusMap[Pair(row, col)] == CellStatus.LOCKED) {
            // Cell is locked, display a message or take appropriate action
            AlertDialog.Builder(this)
                .setTitle("Locked Cell")
                .setMessage("You cannot place a symbol on this cell.")
                .setPositiveButton("OK") { _, _ ->
                    // Do nothing or provide additional actions as needed
                }
                .setCancelable(false)
                .show()
            return
        }

        // Set the symbol (X or O) to the tapped button
        button.text = if (currentTurn == PlayerTurn.NOUGHT) NOUGHT else CROSS

        // Update the last move for the current player
        if (currentTurn == PlayerTurn.NOUGHT) {
            lastMoveByNought = Move(row, col, PlayerTurn.NOUGHT)
        } else {
            lastMoveByCross = Move(row, col, PlayerTurn.CROSS)
        }

        // Unlock cells after the opponent's move
        if (currentTurn == PlayerTurn.CROSS) {
            markCellAsUnlocked()
        }

        // Switch turn after placing the symbol
        switchTurn()
    }

    private fun switchTurn() {
        currentTurn = if (currentTurn == PlayerTurn.NOUGHT) PlayerTurn.CROSS else PlayerTurn.NOUGHT
        setTurnLabel()
    }

    private fun onUndoPowerUpButtonClick() {
        // Check if the Undo power-up is available for the current player
        if (!undoPowerUpUsed()) {
            // Display a dialog to ask the player if they want to use the Undo power-up
            AlertDialog.Builder(this)
                .setTitle("Use Undo Power-Up?")
                .setPositiveButton("Yes") { _, _ ->

                    // Set the flag to indicate that the Undo power-up is activated
                    undoPowerUpActivated = true

                    // Update the power-up usage for the current player
                    undoPowerUpUsedMap[currentTurn] = true

                    // Undo the last move of the opponent and update game state
                    undoPowerUp()

                }
                .setNegativeButton("No") { _, _ ->
                    // Do nothing if the player chooses not to use the power-up
                }
                .setCancelable(false)
                .show()
        } else {
            // Display a message indicating that the Undo power-up has already been used
            AlertDialog.Builder(this)
                .setTitle("Undo Power-Up Already Used")
                .setMessage("You have already used the Undo power-up in this game.")
                .setPositiveButton("OK") { _, _ ->
                    // Do nothing or provide additional actions as needed
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun undoPowerUpUsed(): Boolean {
        return undoPowerUpUsedMap[currentTurn]!!
    }

    private fun undoPowerUp() {
        // Find the last move made by the opponent
        val lastMove = if (currentTurn == PlayerTurn.NOUGHT) lastMoveByCross else lastMoveByNought

        if (lastMove != null) {
            // Clear the symbol in the selected cell
            val button = boardList[lastMove.row * 4 + lastMove.col]

            // Update the status to EMPTY
            button.text = ""
            cellStatusMap[Pair(lastMove.row, lastMove.col)] = CellStatus.EMPTY

            // Mark the cell as locked to prevent placing the same move again
            markCellAsLocked(lastMove.row, lastMove.col, false)

            // Print statements for debugging
            println("UndoPowerUp Last Move: $lastMove")
            println("UndoPowerUp Updated Board:")
            printBoardState()

            // Move switchTurn to the end of undoPowerUp to allow the opponent to make a move
            switchTurn()

        } else {
            println("UndoPowerUp Last Move is null.")
        }

        // Set the flag to indicate that the Undo power-up is inactivate
        undoPowerUpActivated = false
    }

    private fun printBoardState() {
        // Helper function to print the current state of the board for debugging
        for (i in 0 until 4) {
            for (j in 0 until 4) {
                print("${boardList[i * 4 + j].text} ")
            }
            println()
        }
    }


    private data class Move(val row: Int, val col: Int, val player: PlayerTurn)

    private fun markCellAsLocked(row: Int, col: Int, lock: Boolean) {
        // Implement logic to mark the cell as locked / unlocked
        cellStatusMap[Pair(row, col)] = if (lock) CellStatus.LOCKED else CellStatus.EMPTY
    }

    private fun markCellAsUnlocked() {
        // Iterate over all cells and unlock only those that are marked as locked
        for ((row, col) in cellStatusMap.keys) {
            if (cellStatusMap[Pair(row, col)] == CellStatus.LOCKED) {
                cellStatusMap[Pair(row, col)] = CellStatus.UNLOCKED
            }
        }
    }

    private fun onClearPowerUpButtonClick() {
        // Check if the "Clear" power-up is available for the current player
        if (!clearPowerUpUsed()) {
            // Display a dialog to ask the player if they want to use the "Clear" power-up
            AlertDialog.Builder(this)
                .setTitle("Use Clear Power-Up?")
                .setPositiveButton("Yes") { _, _ ->

                    // Set the flag to indicate that the "Clear" power-up is activated
                    clearPowerUpActivated = true

                    // Update the power-up usage for the current player
                    clearPowerUpUsedMap[currentTurn] = true

                }
                .setNegativeButton("No") { _, _ ->
                    // Do nothing if the player chooses not to use the power-up
                }
                .setCancelable(false)
                .show()
        } else {
            // Display a message indicating that the "Clear" power-up has already been used
            AlertDialog.Builder(this)
                .setTitle("Clear Power-Up Already Used")
                .setMessage("You have already used the Clear power-up in this game.")
                .setPositiveButton("OK") { _, _ ->
                    // Do nothing or provide additional actions as needed
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun clearPowerUpUsed(): Boolean {
        return clearPowerUpUsedMap[currentTurn]!!
    }

    private fun clearPowerUp(selectedCell: Button) {
        // Find the row and column of the selected cell
        val row = boardList.indexOf(selectedCell) / 4
        val col = boardList.indexOf(selectedCell) % 4

        // Clear the selected cell and its adjacent tiles
        for (i in row - 1..row + 1) {
            if (i in 0 until 4) {
                val buttonInRow = boardList[i * 4 + col]
                buttonInRow.text = ""
            }
        }

        for (j in col - 1..col + 1) {
            if (j in 0 until 4 && j != col) {
                val buttonInCol = boardList[row * 4 + j]
                buttonInCol.text = ""
            }
        }
    }


    private fun refreshPowerUpUsage() {
        // Refresh power-up usage
        clearPowerUpUsedMap[PlayerTurn.CROSS] = false
        clearPowerUpUsedMap[PlayerTurn.NOUGHT] = false
        undoPowerUpUsedMap[PlayerTurn.CROSS] = false
        undoPowerUpUsedMap[PlayerTurn.NOUGHT] = false
    }

    private fun continueRegularGameplay() {
        // Check for player victory
        if (checkPlayerVictory()) {
            if (currentTurn == PlayerTurn.NOUGHT) {
                result("Nought Wins!")
            } else if (currentTurn == PlayerTurn.CROSS) {
                result("Cross Wins!")
            }
        }

        // Check for a draw
        if (fullBoard()) {
            result("Draw")
        }

        switchTurn()
    }

    //Checking Player Victory:

    private fun checkPlayerVictory(): Boolean {
        // Create a 2D array to represent the current state of the buttons
        val board = Array(4) { _ -> Array(4) { "" } }

        // Fill the 2D array with the current state of the buttons
        for (i in 0 until 4) {
            for (j in 0 until 4) {
                board[i][j] = boardList[i * 4 + j].text.toString()
            }
        }

        // List of symbols to check for victory
        val symbolsToCheck = listOf(CROSS, NOUGHT)

        // Iterate over each symbol to check for victories
        for (symbol in symbolsToCheck) {
            // Check rows
            for (i in 0 until 4) {
                if ((0 until 3).all { match(boardList[i * 4 + it], symbol) } || // Check row
                    (1 until 4).all { match(boardList[i * 4 + it], symbol) }) {
                    return true
                }
            }

            // Check columns
            for (i in 0 until 4) {
                if ((0 until 3).all { match(boardList[it * 4 + i], symbol) } || // Check column
                    (1 until 4).all { match(boardList[it * 4 + i], symbol) }) {
                    return true
                }
            }

            // Check main diagonal
            if ((0 until 3).all { match(boardList[it * 4 + it], symbol) } ||   // Check main diagonal
                (1 until 4).all { match(boardList[it * 4 + it], symbol) }) {
                return true
            }

            // Check anti-diagonal
            if ((0 until 3).all { match(boardList[it * 4 + (3 - it)], symbol) } || // Check anti-diagonal
                (1 until 4).all { match(boardList[it * 4 + (3 - it)], symbol) }) {
                return true
            }

            // Check additional diagonals
            if ((0 until 3).all { match(boardList[it * 4 + (it + 1)], symbol) } ||  // Check diagonal {(0,1), (1,2), (2,3)}
                (0 until 3).all { match(boardList[it * 4 + (2 - it)], symbol) } ||  // Check diagonal {(0,2), (1,1), (2,0)}
                (1 until 4).all { match(boardList[it * 4 + (it - 1)], symbol) } ||  // Check diagonal {(1,0), (2,1), (3,2)}
                (1 until 4).all { match(boardList[it * 4 + (4 - it)], symbol) }) {  // Check diagonal {(1,3), (2,2), (3,1)}
                return true
            }

        }

        // If no victory is found, return false
        return false
    }

    private fun match(button: Button, symbol: String) = button.text == symbol

    //FULL BOARD CHECK:

    //This function checks if the game board is full, indicating a draw.
    private fun fullBoard(): Boolean {
        // Checking if any button is empty
        for(button in boardList){
            if(button.text == "")
                return false
        }
        // If no empty button is found, the board is full
        return true
    }

    //SETTING TURN LABEL:

    //This function updates the turn label based on the current player's turn.
    private fun setTurnLabel() {
        // Setting the turn label based on the current player's turn
        var turnText = ""
        if(currentTurn == PlayerTurn.CROSS)
            turnText = "$CROSS's Turn"
        else if(currentTurn == PlayerTurn.NOUGHT)
            turnText = "$NOUGHT's Turn"

        binding.txtvTurnView.text = turnText
    }

    //DISPLAY RESULT DIALOG:

    //This function displays a dialog with the result of the game (win, draw).
    //The "Reset" button in the dialog calls the resetBoard() function.
    private fun result(title: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setPositiveButton("Reset"){
                    _,_ ->
                // Reset the game board
                resetBoard()
            }
            .setCancelable(false)
            .show()
    }

    //RESETTING THE BOARD:

    //This function resets the game board by clearing the text of all buttons.
    //It also switches the first turn for the next game and sets the current turn to the first turn.
    private fun resetBoard() {
        // Clearing the text of all buttons on the board
        for(button in boardList){
            button.text = ""
        }

        //Clear cell stats for both players
        clearCellStatus()

        //Reset power-up usage for both players
        refreshPowerUpUsage()

        //Switching the first turn for the next game
        if(firstTurn == PlayerTurn.NOUGHT)
            firstTurn = PlayerTurn.CROSS
        else if(firstTurn == PlayerTurn.CROSS)
            firstTurn = PlayerTurn.NOUGHT

        // Setting the current turn to the first turn for the next game
        currentTurn = firstTurn

        //'setTurnLabel()' is then called to update the turn label.
        setTurnLabel()
    }

    private fun clearCellStatus() {
        cellStatusMap.clear()
    }
}