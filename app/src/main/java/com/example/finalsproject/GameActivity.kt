package com.example.finalsproject
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
        BLOCKED
    }

    private var row = -1
    private var col = -1

    private val maxBlockedTurns = 5
    private val maxBlockerUsage = 1

    //'firstTurn' represents the player who goes first, 'currentTurn' represents the current player.
    private var firstTurn = PlayerTurn.CROSS
    private var currentTurn = PlayerTurn.CROSS

    private var totalGameTimeMillis: Long = 45000 // in milliseconds
    private var currentPlayerTimer: CountDownTimer? = null
    private var opponentPlayerTimer: CountDownTimer? = null
    private var crossPlayerRemainingTime: Long = totalGameTimeMillis
    private var noughtPlayerRemainingTime: Long = totalGameTimeMillis

    private lateinit var timerView: TextView


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
    private var blockPowerUpUsedMap: MutableMap<PlayerTurn, Int> = mutableMapOf(
        PlayerTurn.NOUGHT to 0,
        PlayerTurn.CROSS to 0
    )

    private val cellLockingPlayerMap: MutableMap<Pair<Int, Int>, PlayerTurn> = mutableMapOf()
    private val blockedCellTurnsMap: MutableMap<Pair<Int, Int>, Int> = mutableMapOf()
    // Flag to store the cell where the "Blocker" power-up is placed
    private var blockedCell: Pair<Int, Int>? = null



    // Variables to track the last move of each player
    private var lastMoveByNought: Move? = null
    private var lastMoveByCross: Move? = null

    //FLAG ACTIVE INDICATORS

    //Flag to indicate if the "Clear" power-up is activated
    private var clearPowerUpActivated = false
    // Flag to indicate if the "Undo" power-up is activated
    private var undoPowerUpActivated = false
    // Flag to indicate if the "Undo" power-up is activated
    private var blockPowerUpActivated = false
    // Add this variable to your class
    private var lastMoveUndone = false

    private lateinit var binding : ActivityGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_game)

        // Set content view using View Binding
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Log to check if binding is initialized
        Log.d("GameActivity", "Binding initialized: ${::binding.isInitialized}")

        // Initialize the game board
        initBoard()
        Log.d("GameActivity", "initBoard() called")

        // Initialize timers
        initializeTimers()

        // Set click listener for the "Clear" power-up button
        binding.imageButton1.setOnClickListener {
            onClearPowerUpButtonClick()
        }
        // Set click listener for the "Undo" power-up button
        binding.imageButton2.setOnClickListener {
            onUndoPowerUpButtonClick()
        }

        // Set click listener for the "Block" power-up button
        binding.imageButton3.setOnClickListener {
            onBlockPowerUpButtonClick()
        }

        timerView = findViewById(R.id.txtvTimerView) // Replace with the actual ID of your TextView


    }

    companion object{
        const val NOUGHT = "O"
        const val CROSS = "X"
    }

    //Board Initialization:

    //'boardList' is a list of buttons representing the Tic-Tac-Toe game board.
    private var boardList = mutableListOf<ImageView>()

    //'initBoard()' initializes this list with references to the buttons in your layout.
    private fun initBoard() {
        Log.d("GameActivity", "initBoard() called")

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

        // Set click listeners for the ImageViews in the grid
        for (imageView in boardList) {
            imageView.setBackgroundResource(R.drawable.blankcell)
            imageView.tag = "" // Ensure the tag is set to an empty string
            imageView.setOnClickListener {
                boardTapped(imageView)
            }
        }

    }

    private fun isCellEmpty(imageView: ImageView): Boolean {
        // Check if the tag of the ImageView is empty
        return imageView.tag == ""
    }

    //BOARD TAPPING EVENT

    //This function is triggered when a button on the game board is tapped.
    fun boardTapped(imageView: ImageView){
        Log.d("BoardTapped", "Board tapped!")

        // Check if the "Clear" power-up is activated
        if (clearPowerUpActivated) {
            // Process "Clear" power-up logic
            clearPowerUp(imageView)

            // Continue with the regular gameplay
            continueRegularGameplay()

            // Reset the "Clear" power-up activation flag
            clearPowerUpActivated = false
            return
        }

        // Check if the "Block" power-up is activated
        if (blockPowerUpActivated) {
            // Process "Block" power-up logic
            blockPowerUp(imageView)

            // Continue with the regular gameplay
            continueRegularGameplay()

            // Reset the "Block" power-up activation flag
            blockPowerUpActivated = false
            return
        }

        if (!isCellEmpty(imageView))
            return

        //Regular gameplay logic
        //'addToBoard(view)' adds the current player's symbol to the tapped button.
        addToBoard(imageView)

        //'checkPlayerVictory()' is called to check if the current player has won.
        if (checkPlayerVictory()) {
            if (imageView.tag == NOUGHT) {
                result("Nought Wins!")
            } else if (imageView.tag == CROSS) {
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
    private fun addToBoard(imageView: ImageView) {
        if(imageView.drawable != null) {
            // Cell already occupied, do nothing
            return
        }

        val row = boardList.indexOf(imageView) / 4
        val col = boardList.indexOf(imageView) % 4

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

        // Check if the cell is marked as blocked
        if (cellStatusMap[Pair(row, col)] == CellStatus.BLOCKED) {
            // Cell is blocked, display a message or take appropriate action
            showToast("Cannot place symbol on a blocked cell.")
            return
        }

        // Set the drawable (X or O) to the tapped ImageView
        imageView.setImageResource(if (currentTurn == PlayerTurn.NOUGHT) R.drawable.selectcircle else R.drawable.selectcross)

        // Set the tag to the corresponding symbol
        imageView.tag = if (currentTurn == PlayerTurn.NOUGHT) NOUGHT else CROSS

        // Update the background to remove the blank cell appearance
        imageView.setBackgroundResource(0)

        // Stop the current player's timer
        currentPlayerTimer?.cancel()

        // Update the last move for the current player
        if (currentTurn == PlayerTurn.NOUGHT) {
            lastMoveByNought = Move(row, col, PlayerTurn.NOUGHT)
        } else {
            lastMoveByCross = Move(row, col, PlayerTurn.CROSS)
        }

        // Switch turn after placing the symbol
        switchTurn()

    }

    private fun updateLineBackground(lineIds: List<Int>, drawableResId: Int) {
        for (lineId in lineIds) {
            findViewById<View>(lineId).background = ContextCompat.getDrawable(this, drawableResId)
        }
    }

    private fun updateGridLines() {
        // Update background for vertical lines
        val verticalLineIds = listOf(
            R.id.line1, R.id.line2, R.id.line3, R.id.line4, R.id.line5,
            R.id.line6, R.id.line7, R.id.line8, R.id.line9, R.id.line10,
            R.id.line11, R.id.line12, R.id.line13, R.id.line14, R.id.line15,
            R.id.line16, R.id.line17, R.id.line18, R.id.line19, R.id.line20
        )

        // Update background for horizontal lines
        val horizontalLineIds = listOf(R.id.lineA, R.id.lineB, R.id.lineC, R.id.lineD, R.id.lineE)

        val lineDrawableResId = if (currentTurn == PlayerTurn.NOUGHT) R.drawable.columnblue else R.drawable.columnred
        val lineDrawableResIdV = if (currentTurn == PlayerTurn.NOUGHT) R.drawable.verticalblue else R.drawable.verticalred

        updateLineBackground(verticalLineIds, lineDrawableResIdV)
        updateLineBackground(horizontalLineIds, lineDrawableResId)
    }

    private fun switchTurn() {
        // Pause the current player's timer
        currentPlayerTimer?.cancel()

        // Check if the Undo power-up is activated and if the current player is the one who locked the cell
        if (undoPowerUpActivated) {
            // Retrieve the locking player for the last move
            val lockingPlayer = cellLockingPlayerMap[Pair(row, col)]

            // Check if the cell is locked and the current player is the one who locked it
            if (lockingPlayer == currentTurn) {
                // Add logging
                Log.d("SwitchTurn", "MarkCellAsUnlocked() triggered.")

                // Mark the cell as unlocked after the opponent (Player A) makes a move
                markCellAsUnlocked(row, col)

                // Reset the last move undone flag
                lastMoveUndone = false

                // Reset activated flag
                undoPowerUpActivated = false

            } else {
                Log.d("SwitchTurn", "Cell is not locked by the current player.")
            }
        } else {
            Log.d("SwitchTurn", "Undo power-up not activated.")
        }

        // Reset activated flag
        undoPowerUpActivated = false

        // Decrement the remaining turns for each blocked cell
        decrementBlockedCellTurns()

        //Switch Turns
        // Add logging
        Log.d("SwitchTurn", "Next turn")
        currentTurn = if (currentTurn == PlayerTurn.NOUGHT) PlayerTurn.CROSS else PlayerTurn.NOUGHT
        setTurnLabel()
        // Update the color of the grid lines based on the current player's turn
        updateGridLines()

        // Initialize timers for the new turn with the remaining time of the current player
        initializeTimers()
    }

    private fun initializeTimers() {
        currentPlayerTimer?.cancel() // Stop the previous player's timer

        // Deduct the remaining time of the previous player from the total game time
        val remainingTimeMillis = if (currentTurn == PlayerTurn.CROSS) {
            crossPlayerRemainingTime
        } else {
            noughtPlayerRemainingTime
        }

        // Create a timer for the current player with the deducted remaining time
        currentPlayerTimer = createTimer(remainingTimeMillis, currentTurn)
        currentPlayerTimer?.start()

        opponentPlayerTimer?.cancel()
        opponentPlayerTimer = null
    }

    private fun createTimer(initialMillis: Long, player: PlayerTurn): CountDownTimer {
        return object : CountDownTimer(initialMillis, 100) {
            override fun onTick(millisUntilFinished: Long) {
                // Update UI with remaining time
                timerView.text = formatTime(millisUntilFinished)

                // Update remaining time for the current player
                if (player == PlayerTurn.CROSS) {
                    crossPlayerRemainingTime = millisUntilFinished
                } else {
                    noughtPlayerRemainingTime = millisUntilFinished
                }
            }

            override fun onFinish() {
                // Player loses when the timer reaches zero
                result("${player.name} ran out of time. ${opponentPlayer(player).name} wins!")
            }
        }
    }

    private fun formatTime(millis: Long): String {
        val seconds = millis / 1000
        val milliseconds = millis % 1000
        return String.format("%02d.%02d", seconds, milliseconds / 10)
    }

    private fun opponentPlayer(player: PlayerTurn): PlayerTurn {
        return if (player == PlayerTurn.CROSS) PlayerTurn.NOUGHT else PlayerTurn.CROSS
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
            clearCellAt(lastMove.row, lastMove.col)

            // Mark the cell as locked to prevent placing the same move again
            markCellAsLocked(lastMove.row, lastMove.col, true)

            // Update the locking player information during the Undo operation
            cellLockingPlayerMap[Pair(lastMove.row, lastMove.col)] = currentTurn

            // Set the flag to indicate that the last move was undone
            lastMoveUndone = true

            // Log the currentTurn before switching
            Log.d("UndoPowerUp", "Current Turn before switch: $currentTurn")

            // Switch the turn
            switchTurn()

            // Log the currentTurn after switching
            Log.d("UndoPowerUp", "Current Turn after switch: $currentTurn")

        } else {
            println("UndoPowerUp Last Move is null.")
        }
    }

    private data class Move(val row: Int, val col: Int, val player: PlayerTurn)

    private fun markCellAsLocked(row: Int, col: Int, lock: Boolean) {
        // Implement logic to mark the cell as locked
        cellStatusMap[Pair(row, col)] = if (lock) CellStatus.LOCKED else CellStatus.EMPTY
        // Store the locking player information
        if (lock) {
            cellLockingPlayerMap[Pair(row, col)] = currentTurn
            Log.d("CellLocking", "Cell at ($row, $col) locked by ${currentTurn.name}")

        } else {
            cellLockingPlayerMap.remove(Pair(row, col))
            Log.d("CellLocking", "Cell at ($row, $col) unlocked")
        }
    }

    private fun markCellAsUnlocked(row: Int, col: Int) {
        // Iterate over all cells and unlock only those that were marked as locked by the current player
        for ((row, col) in cellStatusMap.keys) {
            if (cellStatusMap[Pair(row, col)] == CellStatus.LOCKED) {
                // Unlock the cell
                cellStatusMap[Pair(row, col)] = CellStatus.EMPTY
            }
        }
        // Reset the flag after processing
        lastMoveUndone = false
    }

    private fun onBlockPowerUpButtonClick() {
        // Check if the "Blocker" power-up is available for the current player
        if (!blockPowerUpUsed()) {
            // Display a dialog to ask the player if they want to use the "Blocker" power-up
            AlertDialog.Builder(this)
                .setTitle("Use Blocker Power-Up?")
                .setPositiveButton("Yes") { _, _ ->

                    // Set the flag to indicate that the "Blocker" power-up is activated
                    blockPowerUpActivated = true

                    // Update the power-up usage for the current player
                    blockPowerUpUsedMap[currentTurn] = blockPowerUpUsedMap[currentTurn]!! + 1

                }
                .setNegativeButton("No") { _, _ ->
                    // Do nothing if the player chooses not to use the power-up
                }
                .setCancelable(false)
                .show()
        } else {
            // Display a message indicating that the "Blocker" power-up has already been used
            AlertDialog.Builder(this)
                .setTitle("Blocker Power-Up Already Used")
                .setMessage("You have already used the Blocker power-up in this game.")
                .setPositiveButton("OK") { _, _ ->
                    // Do nothing or provide additional actions as needed
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun blockPowerUpUsed(): Boolean {
        return blockPowerUpUsedMap[currentTurn]!! >= maxBlockerUsage
    }

    private fun blockPowerUp(imageView: ImageView) {
        // Implement logic to mark the cell as blocked
        val row = boardList.indexOf(imageView) / 4
        val col = boardList.indexOf(imageView) % 4

        // Check if the cell is already blocked or has a symbol
        if (isCellEmpty(imageView) && cellStatusMap[Pair(row, col)] != CellStatus.BLOCKED) {
            // Mark the cell as blocked
            cellStatusMap[Pair(row, col)] = CellStatus.BLOCKED

            // Set the background or update the cell appearance to indicate it is blocked
            imageView.setBackgroundResource(R.drawable.powerupblock)

            // Store the blocked cell information
            blockedCell = Pair(row, col)

            // Set the initial number of turns the cell is blocked
            blockedCellTurnsMap[Pair(row, col)] = maxBlockedTurns // Set the desired number of turns

            // Optionally, update the UI or take other actions as needed
        } else {
            // Handle the case where the cell is already blocked or has a symbol
            // Display a message, show a toast, or take appropriate action
            showToast("Cannot place Blocker Power-Up here.")
        }
    }

    private fun decrementBlockedCellTurns() {
        val iterator = blockedCellTurnsMap.iterator()
        while (iterator.hasNext()) {
            val (cell, remainingTurns) = iterator.next()
            if (remainingTurns > 0) {
                // Decrement the remaining turns
                blockedCellTurnsMap[cell] = remainingTurns - 1
            } else {
                // Turns are zero, unblock the cell
                unblockCell(cell.first, cell.second)
                iterator.remove()
            }
        }
    }

    private fun unblockCell(row: Int, col: Int) {
        // Implement logic to unblock the cell
        cellStatusMap[Pair(row, col)] = CellStatus.EMPTY

        // Update the cell appearance to indicate it is no longer blocked
        val unblockedCell = boardList[row * 4 + col]
        unblockedCell.setBackgroundResource(R.drawable.blankcell)

        // Optionally, update the UI or take other actions as needed
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

    private fun clearPowerUp(selectedCell: ImageView) {
        // Find the row and column of the selected cell
        val row = boardList.indexOf(selectedCell) / 4
        val col = boardList.indexOf(selectedCell) % 4

        // Clear the selected cell and its adjacent tiles
        for (i in row - 1..row + 1) {
            if (i in 0 until 4) {
                clearCellAt(i, col)
                markCellAsUnlocked(i, col)  // Unlock the cell
            }
        }

        for (j in col - 1..col + 1) {
            if (j in 0 until 4 && j != col) {
                clearCellAt(row, j)
                markCellAsUnlocked(row, j)  // Unlock the cell
            }
        }
    }

    private fun clearCellAt(row: Int, col: Int) {
        // Clear the cell based on its position
        val imageView = boardList[row * 4 + col]
        // Clear the image resource
        imageView.setImageDrawable(null)
        // Clear all tags
        imageView.tag = ""

        if (clearPowerUpActivated) {
            imageView.setBackgroundResource(R.drawable.powerupdestroy)
        }
        // Set the background to the blank cell
        imageView.setBackgroundResource(R.drawable.blankcell)

        if (undoPowerUpActivated) {
            imageView.setBackgroundResource(R.drawable.powerupundo)
        }
    }

    private fun refreshPowerUpUsage() {
        // Refresh power-up usage
        clearPowerUpUsedMap[PlayerTurn.CROSS] = false
        clearPowerUpUsedMap[PlayerTurn.NOUGHT] = false
        undoPowerUpUsedMap[PlayerTurn.CROSS] = false
        undoPowerUpUsedMap[PlayerTurn.NOUGHT] = false
        blockPowerUpUsedMap[PlayerTurn.CROSS] = 0
        blockPowerUpUsedMap[PlayerTurn.NOUGHT] = 0
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
                // Check if the ImageView has a tag and set the tag as the value in the board array
                board[i][j] = boardList[i * 4 + j].tag as String? ?: ""
            }
        }

        // List of symbols to check for victory
        val symbolsToCheck = listOf(CROSS, NOUGHT)

        // Iterate over each symbol to check for victories
        for (symbol in symbolsToCheck) {
            // Check rows
            for (i in 0 until 4) {
                if ((0 until 3).all { match(board[i][it], symbol) } || // Check row
                    (1 until 4).all { match(board[i][it], symbol) }) {
                    return true
                }
            }

            // Check columns
            for (i in 0 until 4) {
                if ((0 until 3).all { match(board[it][i], symbol) } || // Check column
                    (1 until 4).all { match(board[it][i], symbol) }) {
                    return true
                }
            }

            // Check main diagonal
            if ((0 until 3).all { match(board[it][it], symbol) } ||   // Check main diagonal
                (1 until 4).all { match(board[it][it], symbol) }) {
                return true
            }

            // Check anti-diagonal
            if ((0 until 3).all { match(board[it][3 - it], symbol) } || // Check anti-diagonal
                (1 until 4).all { match(board[it][3 - it], symbol) }) {
                return true
            }

            // Check additional diagonals
            if ((0 until 3).all { match(board[it][it + 1], symbol) } ||  // Check diagonal {(0,1), (1,2), (2,3)}
                (0 until 3).all { match(board[it][2 - it], symbol) } ||  // Check diagonal {(0,2), (1,1), (2,0)}
                (1 until 4).all { match(board[it][it - 1], symbol) } ||  // Check diagonal {(1,0), (2,1), (3,2)}
                (1 until 4).all { match(board[it][4 - it], symbol) }) {  // Check diagonal {(1,3), (2,2), (3,1)}
                return true
            }
        }

        // If no victory is found, return false
        return false
    }

    private fun match(cellValue: String, symbol: String): Boolean {
        return cellValue == symbol
    }

    //FULL BOARD CHECK:

    //This function checks if the game board is full, indicating a draw.
    private fun fullBoard(): Boolean {
        // Checking if any button is empty
        for(imageView in boardList){
            if(imageView.tag == null || imageView.tag == "")
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
        currentPlayerTimer?.cancel()
        opponentPlayerTimer?.cancel()

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
        // Resetting the images of all ImageViews on the board
        for(imageView in boardList){
            // Clear the image resource
            imageView.setImageDrawable(null)
            // Clear all tags
            imageView.tag = ""
            // Set the background to the blank cell
            imageView.setBackgroundResource(R.drawable.blankcell)
        }

        // Reset the remaining time for each player
        crossPlayerRemainingTime = totalGameTimeMillis
        noughtPlayerRemainingTime = totalGameTimeMillis

        // Cancel and reset the timers
        currentPlayerTimer?.cancel()
        currentPlayerTimer = createTimer(crossPlayerRemainingTime, currentTurn)
        currentPlayerTimer?.start()

        opponentPlayerTimer?.cancel()
        opponentPlayerTimer = null

        // Update UI with initial time
        timerView.text = formatTime(totalGameTimeMillis)

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

    // Helper function to show toast messages
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}