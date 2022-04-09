package xyz.scootaloo.thinking.pack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author flutterdash@qq.com
 * @since 2022/3/19 20:41
 */
public class JP51 {

    public List<List<String>> solveNQueens(int n) {
        int[] board = new int[n];
        List<List<String>> output = new ArrayList<>();
        solve(board, output);
        return output;
    }

    private void solve(int[] board, List<List<String>> output) {
        solveCore(board, 0, output);
    }

    private void solveCore(int[] board, int row, List<List<String>> output) {
        if (row == board.length) {
            collectResult(board, output);
            return;
        }
        for (int col = 0; col<board.length; col++) {

        }
    }

    private boolean check(int[] board, int row, int col) {
        for (int r = row - 1; r>=0; r--) {
            if (board[r] == col)
                return false;
            int diff = (row - r);
            if (col - diff >= 0 && board[r] == (r - diff))
                return false;
        }
        return true;
    }

    private void collectResult(int[] board, List<List<String>> output) {

    }
}
