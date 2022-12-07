package com.jopek.pai_fillo_nai;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ThreadLocalRandom;

public class OfflineGameActivity extends AppCompatActivity {
    // 0player - O, 1player - X
    int whoseTurn = ThreadLocalRandom.current().nextInt(0, 1 + 1);
    int[] gameBoard = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1};

    ImageView p0Img;
    ImageView p1Img;

    TextView square0, square1, square2, square3, square4, square5, square6, square7, square8;

    final String TAG = "maks";

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_offline_game);

        square0 = findViewById(R.id.square0);
        square1 = findViewById(R.id.square1);
        square2 = findViewById(R.id.square2);
        square3 = findViewById(R.id.square3);
        square4 = findViewById(R.id.square4);
        square5 = findViewById(R.id.square5);
        square6 = findViewById(R.id.square6);
        square7 = findViewById(R.id.square7);
        square8 = findViewById(R.id.square8);

        Log.d(TAG, "onCreate: "+ savedInstanceState);
        if(savedInstanceState != null) {
            int[] maybeGameBoard = savedInstanceState.getIntArray("gameBoard");
            int maybeWhoseTurn = savedInstanceState.getInt("whoseTurn");
            if(maybeGameBoard != null) {
                gameBoard = maybeGameBoard;
                whoseTurn = maybeWhoseTurn;
            }
            Log.d(TAG, "onCreate: "+ gameBoard);
            Log.d(TAG, "onCreate: "+ whoseTurn);
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
            square.setOnClickListener(view -> onSquareClick((TextView) view, finalI));
            if(gameBoard[i] == -1)
                square.setText("");
            else
                square.setText(gameBoard[i] == 0 ? "O" : "X");
            i++;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray("gameBoard", gameBoard);
        outState.putInt("whoseTurn", whoseTurn);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void onSquareClick(TextView square, int id) {
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
    }

    protected boolean checkWon() {
        return chkTriEqOnGB(0, 1, 2) || chkTriEqOnGB(3, 4, 5) || chkTriEqOnGB(6, 7, 8) ||
                chkTriEqOnGB(0, 3, 6) || chkTriEqOnGB(1, 4, 7) || chkTriEqOnGB(2, 5, 8) ||
                chkTriEqOnGB(0, 4, 8) || chkTriEqOnGB(2, 4, 6);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
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
        new AlertDialog.Builder(this)
                .setTitle(whoWon == -1 ? "DRAW" : "Player " + (whoWon + 1) + " WON")
                .setMessage(whoWon == -1 ? "Maybe next time" : "Congratulations to Player " + (whoWon + 1))
                .setCancelable(false)
                .setPositiveButton("Play again", (dialog, which) -> {
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
                    whoseTurn = ThreadLocalRandom.current().nextInt(0, 1 + 1);
                })
                .setNegativeButton("Go to menu", (dialog, which) -> {
                    Intent intent = new Intent(OfflineGameActivity.this, MainActivity.class);
                    startActivity(intent);
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // CheckTripleEqualityOnGameBoard
    protected boolean chkTriEqOnGB(int a, int b, int c) {
        return gameBoard[a] == gameBoard[b] && gameBoard[b] == gameBoard[c] && gameBoard[a] != -1;
    }
}