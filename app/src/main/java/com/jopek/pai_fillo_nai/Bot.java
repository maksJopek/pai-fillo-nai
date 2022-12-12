package com.jopek.pai_fillo_nai;

import android.os.Build;

import java.util.Arrays;

public class Bot {
    static int meTheBot, opponent;
    static int[] board;

    static boolean isntBoardFull() {
        for (int j : board)
            if (j == -1)
                return true;
        return false;
    }

    static int goodOrBad() {
        int[][] possibilities = new int[][]{
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                {0, 4, 8}, {2, 4, 6}
        };
        for (int[] poss : possibilities) {
            if (chkTriEqOnGB(poss[0], poss[1], poss[2])) {
                if (board[poss[0]] == meTheBot) return +10;
                else return -10;
            }
        }

        return 0;
    }

    static int minimax(int depth, Boolean isMax) {
        int score = goodOrBad();

        if (score == 10)
            return score;

        if (score == -10)
            return score;

        if (!isntBoardFull())
            return 0;

        int best;
        if (isMax) {
            best = -1000;

            for (int i = 0; i < board.length; i++) {
                if (board[i] == -1) {
                    board[i] = meTheBot;
                    best = Math.max(best, minimax(depth + 1, !isMax));
                    board[i] = -1;
                }
            }
        } else {
            best = 1000;

            for (int i = 0; i < board.length; i++) {
                if (board[i] == -1) {
                    board[i] = opponent;
                    best = Math.min(best, minimax(depth + 1, !isMax));
                    board[i] = -1;
                }
            }
        }
        return best;
    }

    static int getBestMove(int[] board, int bot, int opp) {
        Bot.board = board;
        Bot.meTheBot = bot;
        Bot.opponent = opp;
        int bestVal = -1000;
        int bestMove = -1;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if(Arrays.stream(board).filter(c -> c == -1).count() == 9) return 4;
        }

        for (int i = 0; i < board.length; i++) {
            if (board[i] == -1) {
                board[i] = meTheBot;
                int moveVal = minimax(0, false);
                board[i] = -1;

                if (moveVal > bestVal) {
                    bestMove = i;
                    bestVal = moveVal;
                }
            }
        }

        return bestMove;
    }

    // CheckTripleEqualityOnGameBoard
    public static boolean chkTriEqOnGB(int a, int b, int c) {
        return board[a] == board[b] && board[b] == board[c] && board[a] != -1;
    }
}
