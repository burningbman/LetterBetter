package com.cor31.letterbetter.logic;

import android.content.Context;

import com.cor31.letterbetter.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class Game
{
    private ArrayList<String> submittedWords = new ArrayList<>();

    private String currentWord = "";

    private String[] dict;

    private Board board;

    public Game(Context context) {
        dict = Dictionary.getInstance(context).getDictionary();
        if (board == null)
            board = new Board(context.getResources().getInteger(R.integer.num_tiles));
    }

    public String getCurrentWord()
    {
        return currentWord;
    }

    public void letterClicked(CharSequence letter)
    {
        currentWord += letter;
    }

    public void resetCurrentWord()
    {
        currentWord = "";
    }

    public boolean isValidWord()
    {
        currentWord = currentWord.toUpperCase(Locale.US);
        if (currentWord.length() >= 3 && Arrays.binarySearch(dict, currentWord) > 0
                && !submittedWords.contains(currentWord))
        {
            submittedWords.add(currentWord);
            return true;
        }

        return false;
    }

    public int getNumTilesVertical()
    {
        return board.getNumTilesVertical();
    }

    public int getNumTilesHorizontal()
    {
        return board.getNumTilesHorizontal();
    }

    public String getLetterAt(int x, int y)
    {
        return board.getLetterAt(x, y);
    }

    public void shuffleBoard()
    {
        board.shuffle();
    }

    public void swapLetters(int[] dest, int[] src)
    {
        board.swap(dest, src);
    }
}
