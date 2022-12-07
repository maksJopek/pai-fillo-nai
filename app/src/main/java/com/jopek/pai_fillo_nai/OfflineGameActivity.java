package com.jopek.pai_fillo_nai;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class OfflineGameActivity extends AppCompatActivity {
    final String TAG = "maks";
    // 0player - O, 1player - X
    int whoseTurn = ThreadLocalRandom.current().nextInt(0, 1 + 1);
    int[] gameBoard = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1};
    ImageView p0Img;
    ImageView p1Img;
    TextView square0, square1, square2, square3, square4, square5, square6, square7, square8;
    TextView[] squares;
    TextView tvP0, tvP1;
    boolean bot = false;
    int playerIs = -1;
    boolean botTurn = false;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_offline_game);

        boolean playBotTurn = false;

        square0 = findViewById(R.id.square0);
        square1 = findViewById(R.id.square1);
        square2 = findViewById(R.id.square2);
        square3 = findViewById(R.id.square3);
        square4 = findViewById(R.id.square4);
        square5 = findViewById(R.id.square5);
        square6 = findViewById(R.id.square6);
        square7 = findViewById(R.id.square7);
        square8 = findViewById(R.id.square8);
        squares = new TextView[]{square0, square1, square2, square3, square4, square5, square6, square7, square8};

        tvP0 = findViewById(R.id.tvP0);
        tvP1 = findViewById(R.id.tvP1);

        if (savedInstanceState == null) {
            new AlertDialog.Builder(this)
                    .setTitle("How do you want to play?")
                    .setMessage("Do you want to play with a bot or a friend?")
                    .setCancelable(false)
                    .setPositiveButton("Bot", (dialog, which) -> {
                        bot = true;
                        playerIs = ThreadLocalRandom.current().nextInt(0, 1 + 1);
                        whoseTurn = 0;
                        if (playerIs == 0) {
                            tvP0.setText("Player (O)");
                            tvP1.setText("Bot (X)");
                        } else {
                            tvP0.setText("Bot (O)");
                            tvP1.setText("Player (X)");
                            botTurn = true;
                            botTurn();
                        }
                    })
                    .setNegativeButton("Friend", (dialog, which) -> {
                    })
                    .setIcon(R.drawable.ic_baseline_gamepad_24)
                    .show();
        } else {
            int[] maybeGameBoard = savedInstanceState.getIntArray("gameBoard");
            int maybeWhoseTurn = savedInstanceState.getInt("whoseTurn");
            boolean maybeBot = savedInstanceState.getBoolean("bot");
            int maybePlayerIs = savedInstanceState.getInt("playerIs");
            if (maybeGameBoard != null) {
                gameBoard = maybeGameBoard;
                whoseTurn = maybeWhoseTurn;
                bot = maybeBot;
                playerIs = maybePlayerIs;
                if (bot) {
                    if (playerIs == 0) {
                        tvP0.setText("Player (O)");
                        tvP1.setText("Bot (X)");
                    } else {
                        tvP0.setText("Bot (O)");
                        tvP1.setText("Player (X)");
                        playBotTurn = whoseTurn != playerIs;
//                        botTurn = true;
//                        botTurn();
                    }
                }
            }
        }

        p0Img = findViewById(R.id.p0Img);
        p1Img = findViewById(R.id.p1Img);
        if (whoseTurn == 0) {
            p0Img.setImageResource(R.mipmap.bg_you_in_army_foreground);
            p1Img.setImageResource(R.mipmap.bg_no_no_foreground);
        } else {
            p0Img.setImageResource(R.mipmap.bg_no_no_foreground);
            p1Img.setImageResource(R.mipmap.bg_you_in_army_foreground);
        }


        int i = 0;
        for (TextView square :
                new TextView[]{square0, square1, square2, square3, square4, square5, square6, square7, square8}) {
            int finalI = i;
            square.setOnClickListener(view -> onSquareClick((TextView) view, finalI, false));
            if (gameBoard[i] == -1)
                square.setText("");
            else
                square.setText(gameBoard[i] == 0 ? "O" : "X");
            i++;
        }

        if (playBotTurn || (bot && playerIs == 1 && Arrays.stream(gameBoard).filter(c -> c == -1).count() == 9)) {
            botTurn = true;
            botTurn();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray("gameBoard", gameBoard);
        outState.putInt("whoseTurn", whoseTurn);
        outState.putBoolean("bot", bot);
        outState.putInt("playerIs", playerIs);
    }

    protected void onSquareClick(TextView square, int id, boolean force) {
        if (botTurn && !force) return;
        if (gameBoard[id] != -1) return;
        gameBoard[id] = whoseTurn;
        square.setText(whoseTurn == 0 ? "O" : "X");
        if (checkWon()) {
            endGame(whoseTurn);
            return;
        } else if (checkDraw()) {
            endGame(-1);
            return;
        }
        whoseTurn = whoseTurn == 0 ? 1 : 0;
        if (whoseTurn == 0) {
            p0Img.setImageResource(R.mipmap.bg_you_in_army_foreground);
            p1Img.setImageResource(R.mipmap.bg_no_no_foreground);
        } else {
            p0Img.setImageResource(R.mipmap.bg_no_no_foreground);
            p1Img.setImageResource(R.mipmap.bg_you_in_army_foreground);
        }
        if (bot && whoseTurn != playerIs) {
            botTurn();
        }
    }

    protected void botTurn() {
        botTurn = true;
        int chosenSquare = -1;
        chosenSquare = Bot.getBestMove(gameBoard.clone(), playerIs == 0 ? 1 : 0, playerIs);
        onSquareClick(squares[chosenSquare], chosenSquare, true);
        botTurn = false;
    }

    protected boolean checkWon() {
        return chkTriEqOnGB(0, 1, 2) || chkTriEqOnGB(3, 4, 5) || chkTriEqOnGB(6, 7, 8) ||
                chkTriEqOnGB(0, 3, 6) || chkTriEqOnGB(1, 4, 7) || chkTriEqOnGB(2, 5, 8) ||
                chkTriEqOnGB(0, 4, 8) || chkTriEqOnGB(2, 4, 6);
    }

    protected boolean checkDraw() {
        return gameBoard[0] != -1 &&
                gameBoard[1] != -1 &&
                gameBoard[2] != -1 &&
                gameBoard[3] != -1 &&
                gameBoard[4] != -1 &&
                gameBoard[5] != -1 &&
                gameBoard[6] != -1 &&
                gameBoard[7] != -1 &&
                gameBoard[8] != -1;
    }

    protected void endGame(int whoWon) {
        botTurn = true;
        int delay = bot ? 2000 : 0;
        if (bot && whoWon == playerIs) delay = 0;
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            String title = "";
            if (bot) {
                if (whoWon == -1) title = "Draw, but you couldn't do better";
                else title = whoWon == playerIs ? "You were cheating" : "Bot won, no surprise here";
            } else title = whoWon == -1 ? "DRAW" : "Player " + (whoWon + 1) + " WON";

            String msg = "";
            if (bot) {
                if (whoWon == -1) msg = "Maybe next time you'll loose";
                else msg = whoWon == playerIs ? "Cheeeeeeeeater" : "You didn't have any chance";
            } else
                msg = whoWon == -1 ? "Maybe next time" : "Congratulations to Player " + (whoWon + 1);

            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(msg)
                    .setCancelable(false)
                    .setPositiveButton("Play again", (dialog, which) -> {
                        botTurn = false;
                        gameBoard = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1};
                        square0.setText("");
                        square1.setText("");
                        square2.setText("");
                        square3.setText("");
                        square4.setText("");
                        square5.setText("");
                        square6.setText("");
                        square7.setText("");
                        square8.setText("");
                        if (!bot)
                            whoseTurn = ThreadLocalRandom.current().nextInt(0, 1 + 1);
                        else {
                            playerIs = ThreadLocalRandom.current().nextInt(0, 1 + 1);
                            if (playerIs == 0) {
                                tvP0.setText("Player (O)");
                                tvP1.setText("Bot (X)");
                            } else {
                                tvP0.setText("Bot (O)");
                                tvP1.setText("Player (X)");
                            }
                            if (whoseTurn != playerIs) {
                                botTurn = true;
                                botTurn();
                            }
                        }
                    })
                    .setNegativeButton("Go to menu", (dialog, which) -> {
                        Intent intent = new Intent(OfflineGameActivity.this, MainActivity.class);
                        startActivity(intent);
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }, delay);

    }

    // CheckTripleEqualityOnGameBoard
    protected boolean chkTriEqOnGB(int a, int b, int c) {
        return gameBoard[a] == gameBoard[b] && gameBoard[b] == gameBoard[c] && gameBoard[a] != -1;
    }
}