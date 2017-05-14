package com.cor31.letterbetter;

import com.cor31.letterbetter.logic.Board;

import java.util.HashMap;

public class MatchData
{
    private Board board = new Board(4);
    private HashMap<String, Integer> mapScores = new HashMap<String, Integer>();
    private int bet = 0;
    private boolean complete = false;
    private boolean lettersCollected = false;

    public boolean isComplete()
    {
        return complete;
    }

    public void setComplete(boolean complete)
    {
        this.complete = complete;
    }

    public HashMap<String, Integer> getScores()
    {
        return mapScores;
    }

    public boolean isLettersCollected()
    {
        return lettersCollected;
    }

    public void setLettersCollected(boolean lettersCollected)
    {
        this.lettersCollected = lettersCollected;
    }

    public Board getBoard()
    {
        return board;
    }


    public MatchData(Board board)
    {
        this.board = board;
    }

    public MatchData(Board board, String participantName, int score)
    {
        this.board = board;
        mapScores.put(participantName, score);
    }

    public void addPlayer(String name, int score)
    {
        mapScores.put(name, score);
    }

    public String getFirstPlayerName()
    {
        return (String) mapScores.keySet().toArray()[0];
    }

    public int getPlayerScore(String player)
    {
        return mapScores.get(player);
    }

    public int getBet()
    {
        return bet;
    }

    public void setBet(int bet)
    {
        this.bet = bet;
    }
}
