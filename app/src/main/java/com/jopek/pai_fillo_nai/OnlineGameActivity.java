package com.jopek.pai_fillo_nai;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class OnlineGameActivity extends AppCompatActivity {
    protected boolean gameWasEnd = false;
    // 0player - O, 1player - X
    long whoseTurn = ThreadLocalRandom.current().nextInt(0, 1 + 1);
    List<Long> gameBoard = new ArrayList<>(
            Arrays.asList(-1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L)
    );
    List<ImageView> squares = new ArrayList<>();
    DatabaseReference rooms;
    DatabaseReference room;
    // FinishGameAlert
    AlertDialog fgAlert = null;
    long whoami = 0;
    boolean myTurn = false;
    boolean revengePossible = true;
    boolean revengeRequested = false;
    String TAG = "maks";
    ImageView p0Img;
    ImageView p1Img;
    ImageView square0, square1, square2, square3, square4, square5, square6, square7, square8;
    TextView tvName;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_online_game);

        p0Img = findViewById(R.id.p0Img);
        p1Img = findViewById(R.id.p1Img);
        tvName = findViewById(R.id.onPlayerName);

        square0 = findViewById(R.id.square0);
        square1 = findViewById(R.id.square1);
        square2 = findViewById(R.id.square2);
        square3 = findViewById(R.id.square3);
        square4 = findViewById(R.id.square4);
        square5 = findViewById(R.id.square5);
        square6 = findViewById(R.id.square6);
        square7 = findViewById(R.id.square7);
        square8 = findViewById(R.id.square8);
        squares.addAll(Arrays.asList(square0, square1, square2, square3, square4, square5, square6, square7, square8));
        int i = 0;
        for (ImageView square : squares) {
            int finalI = i;
            square.setOnClickListener(view -> onSquareClick((ImageView) view, finalI));
            i++;
        }
        rooms = FirebaseDatabase.getInstance("https://pai-fillo-nai-default-rtdb.europe-west1.firebasedatabase.app/").getReference("rooms");

        askForRoomId(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void askForRoomId(boolean errorOccured) {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(this)
                .setTitle(errorOccured ? "Error occured, choose room again" : "Choose room")
                .setMessage("(blank to create new)")
                .setCancelable(false)
                .setView(input)
                .setPositiveButton("Join room", (dialog, which) -> {
                    getRoom(String.valueOf(input.getText()));
                })
                .setIcon(R.drawable.ic_baseline_gamepad_24)
                .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("DefaultLocale")
    protected void getRoom(String roomId) {
        if (roomId.equals("")) {
            Random rand = new Random();

            int n = new Random().nextInt(900000) + 100000;

            String rid = String.valueOf(n);
            whoami = rand.nextInt(2);
            tvName.setText("You're player 1 (playing as " + (whoami == 0 ? "O" : "X") + ")");

            room = rooms.child(rid);
            Map<String, Object> data = new HashMap<>();
            data.put("started", 0);
            data.put("player1", whoami);
            data.put("player2", whoami == 0 ? 1 : 0);
            data.put("whoseTurn", rand.nextInt(2));
            data.put("board", gameBoard);
            data.put("winner", -1);
            data.put("revenge0", -1);
            data.put("revenge1", -1);
            room.setValue(data);

            waitFor2Player(rid);
        } else {
            rooms.child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!(dataSnapshot.getValue() instanceof HashMap)) {
                        askForRoomId(true);
                    }
                    HashMap data = (HashMap) dataSnapshot.getValue();
                    if ((long) data.get("started") == 1) {
                        askForRoomId(true);
                    }
                    whoami = (long) data.get("player2");
                    whoseTurn = (long) data.get("whoseTurn");
                    tvName.setText("You're player 1 (playing as " + (whoami == 0 ? "O" : "X") + ")");
                    room = rooms.child(roomId);
                    startGame(true);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    askForRoomId(true);
                }
            });
        }
    }

    protected void waitFor2Player(String roomId) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this)
                .setTitle("Waiting for 2nd player")
                .setMessage("Your room id = " + roomId)
                .setCancelable(false)
                .setIcon(R.drawable.ic_baseline_gamepad_24);
        AlertDialog alert = alertBuilder.create();
        alert.show();
        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        room.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap data = (HashMap) dataSnapshot.getValue();
                if ((long) data.get("started") == 1) {
                    room.removeEventListener(this);
                    alert.dismiss();
                    startGame(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Waiter Value Event Listener");
            }
        });
    }

    protected void startGame(boolean markStarted) {
        if (markStarted) {
            room.child("started").setValue(1);
        }
        room.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap data = (HashMap) dataSnapshot.getValue();
                if (data == null) {
                    room.removeEventListener(this);
                    return;
                }

                Log.d(TAG, "onDataChange: " + Arrays.toString(data.entrySet().toArray()));
                gameBoard = (List) data.get("board");
                Log.d(TAG, "onDataChange: " + Arrays.toString(gameBoard.toArray()));
                whoseTurn = (long) data.get("whoseTurn");
                myTurn = whoseTurn == whoami;
                playTurn();
                long winner = (long) data.get("winner");
                long opRevenge = (long) data.get("revenge" + (whoami == 0 ? 1 : 0));
                revengePossible = opRevenge != 0;
                if (revengeRequested && winner == -1) {
                    myTurn = whoami == (long) data.get("whoseTurn");
                    revengePossible = true;
                    revengeRequested = false;
                    gameWasEnd = false;
                    playTurn();
                } else if (!revengePossible && revengeRequested) {
                    fgAlert.dismiss();
                    new AlertDialog.Builder(OnlineGameActivity.this)
                            .setTitle("Sad info")
                            .setMessage("Second player don't want to play with ya :(")
                            .setCancelable(false)
                            .setNegativeButton("Go to menu without friend", (dialog, which) -> {
                                room.removeValue();
                                Intent intent = new Intent(OnlineGameActivity.this, MainActivity.class);
                                startActivity(intent);
                            })
                            .setIcon(R.drawable.ic_baseline_gamepad_24)
                            .show();
                } else if (opRevenge == 1 && revengeRequested) {
                    Map<String, Object> data2 = new HashMap<>();
                    gameBoard = new ArrayList<>(Arrays.asList(-1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L));
                    data2.put("started", 1);
                    data2.put("player1", whoami);
                    data2.put("player2", whoami == 0 ? 1L : 0L);
                    data2.put("whoseTurn", new Long(ThreadLocalRandom.current().nextInt(0, 1 + 1)));
                    data2.put("board", gameBoard);
                    data2.put("winner", -1L);
                    data2.put("revenge0", -1L);
                    data2.put("revenge1", -1L);
                    myTurn = whoami == (long) data2.get("whoseTurn");
                    room.setValue(data2);
                    playTurn();
                }

                if (winner != -1) {
                    endGame(winner);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Main Value Event Listener");
            }
        });
    }

    protected void playTurn() {
        int i = 0;
        for (ImageView square : squares) {
            if (gameBoard.get(i) == -1) {
                square.setImageResource(R.mipmap.emptiness_foreground);
            } else if (gameBoard.get(i) == 0) {
                square.setImageResource(R.mipmap.bg_o_foreground);
            } else if (gameBoard.get(i) == 1) {
                square.setImageResource(R.mipmap.bg_x_foreground);
            }
            i++;
        }
        if (myTurn) {
            p0Img.setImageResource(R.mipmap.bg_you_in_army_foreground);
        } else {
            p0Img.setImageResource(R.mipmap.bg_no_no_foreground);
        }
    }

    protected void finishTurn() {
        room.child("whoseTurn").setValue(whoseTurn);
        room.child("board").setValue(gameBoard);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void onSquareClick(ImageView square, int id) {
        if (gameBoard.get(id) != -1 || !myTurn) return;
        gameBoard.set(id, whoseTurn);
        square.setImageResource(whoseTurn == 0 ? R.mipmap.bg_o_foreground : R.mipmap.bg_x_foreground);
        if (checkWon()) {
            endGame(whoseTurn);
            return;
        } else if (checkDraw()) {
            endGame(2);
            return;
        }
        whoseTurn = whoseTurn == 0 ? 1 : 0;
        if (whoseTurn == whoami) {
            p0Img.setImageResource(R.mipmap.bg_you_in_army_foreground);
        } else {
            p0Img.setImageResource(R.mipmap.bg_no_no_foreground);
        }
        finishTurn();
    }

    protected boolean checkWon() {
        return chkTriEqOnGB(0, 1, 2) || chkTriEqOnGB(3, 4, 5) || chkTriEqOnGB(6, 7, 8) ||
                chkTriEqOnGB(0, 3, 6) || chkTriEqOnGB(1, 4, 7) || chkTriEqOnGB(2, 5, 8) ||
                chkTriEqOnGB(0, 4, 8) || chkTriEqOnGB(2, 4, 6);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected boolean checkDraw() {
        return gameBoard.get(0) != -1 &&
                gameBoard.get(1) != -1 &&
                gameBoard.get(2) != -1 &&
                gameBoard.get(3) != -1 &&
                gameBoard.get(4) != -1 &&
                gameBoard.get(5) != -1 &&
                gameBoard.get(6) != -1 &&
                gameBoard.get(7) != -1 &&
                gameBoard.get(8) != -1;
    }

    protected void endGame(long whoWon) {
        if (gameWasEnd) return;
        gameWasEnd = true;

        finishTurn();
        room.child("winner").setValue(whoWon);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this)
                .setTitle(whoWon == 2 ? "DRAW" : "Player " + (whoWon + 1) + " WON")
                .setMessage(whoWon == 2 ? "Maybe next time" : "Congratulations to Player " + (whoWon + 1))
                .setCancelable(false)
                .setPositiveButton("Request revenge", (dialog, which) -> {
                    revengeRequested = true;
                    room.child("revenge" + whoami).setValue(1);
                })
                .setNegativeButton("Go to menu", (dialog, which) -> {
                    room.child("revenge" + whoami).setValue(0);
                    if (!revengePossible) {
                        room.removeValue();
                    }
                    Intent intent = new Intent(OnlineGameActivity.this, MainActivity.class);
                    startActivity(intent);
                })
                .setIcon(R.drawable.ic_baseline_gamepad_24);
        fgAlert = alertBuilder.create();
        fgAlert.show();
    }

    // CheckTripleEqualityOnGameBoard
    protected boolean chkTriEqOnGB(int a, int b, int c) {
        return Objects.equals(gameBoard.get(a), gameBoard.get(b)) && gameBoard.get(b).equals(gameBoard.get(c)) && gameBoard.get(a) != -1;
    }
}