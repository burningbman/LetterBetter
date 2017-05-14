package com.cor31.letterbetter;

import android.os.AsyncTask;

import com.cor31.letterbetter.logic.Dictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class PossibleWordFinderTask extends AsyncTask<Void, Void, String[]>
{
    int numTilesHorizontal;
    int numTilesVertical;
    char[][] boardArray;
    ArrayList<String> arrayListPossibleWords;
    String[] arrayPossibleWords;
    HashMap<String, String> mapCharToBinary;

    public PossibleWordFinderTask(int x, int y, char[][] board, String[] possibleWords)
    {
        numTilesHorizontal = x;
        numTilesVertical = y;
        boardArray = board;
        arrayPossibleWords = possibleWords;
        arrayListPossibleWords = new ArrayList<String>();
    }

    private void findPossibleWords()
    {
        boolean[][] usedInTempWord = new boolean[numTilesHorizontal][numTilesVertical];
        for (int tempx = 0; tempx < numTilesHorizontal; tempx++)
        {
            for (int tempy = 0; tempy < numTilesVertical; tempy++)
            {
                usedInTempWord[tempx][tempy] = false;
            }
        }
        // Loop through the board and add possible words to possibleWords
        for (int yIndex = 0; yIndex < numTilesVertical; yIndex++)
        {
            for (int xIndex = 0; xIndex < numTilesHorizontal; xIndex++)
            {
                solve(xIndex, yIndex, "", usedInTempWord);
            }
        }
    }

    private void solve(int x, int y, String strCurrentWord, boolean[][] usedInTempWord)
    {
        if (x < 0 || y < 0 || x > numTilesHorizontal || y > numTilesVertical)
            return;

        if (strCurrentWord.length() > 12)
            return;

        if (usedInTempWord[x][y])
        {
            usedInTempWord[x][y] = false;
            return;
        }

        usedInTempWord[x][y] = true;

        String strNewWord = strCurrentWord + boardArray[x][y];

        if (strNewWord.length() >= 3)
        {
            if (Arrays.binarySearch(Dictionary.getInstance(null).getDictionary(), strNewWord) > 0)
            {
                arrayListPossibleWords.add(strNewWord);
            }
        }

        for (int xIndex = Math.max(0, x - 1); xIndex <= Math.min(x + 1, numTilesHorizontal - 1);
             xIndex++)
        {
            for (int yIndex = Math.max(0, y - 1); yIndex <= Math.min(y + 1, numTilesVertical - 1)
                    ; yIndex++)
            {
                if (x != xIndex || y != yIndex)
                    solve(xIndex, yIndex, strNewWord, usedInTempWord);
            }
        }
    }

    @Override
    protected String[] doInBackground(Void... params)
    {
        findPossibleWords();
        arrayPossibleWords = new String[arrayListPossibleWords.size()];
        for (int i = 0; i < arrayListPossibleWords.size(); i++)
        {
            arrayPossibleWords[i] = arrayListPossibleWords.get(i);
        }
        return arrayPossibleWords;
    }
}
