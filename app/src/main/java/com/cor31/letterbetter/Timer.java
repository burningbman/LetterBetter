package com.cor31.letterbetter;

import android.content.Context;

public class Timer
{
    private int timerLength;
    private long startTime;
    private long pauseTime;
    private boolean paused;

    public Timer(Context context)
    {
        startTime = System.currentTimeMillis();
        timerLength = context.getResources().getInteger(R.integer.timer_length);
    }

    public boolean timeUp()
    {
        long timeRemaining = timerLength - (int) (System.currentTimeMillis() - startTime) / 1000;
        return timeRemaining <= 0;
    }

    public int getSecondsRemaining()
    {
        long timePlayed = System.currentTimeMillis() - startTime;
        return timerLength - (int) timePlayed / 1000;
    }

    public void pause()
    {
        paused = true;
        pauseTime = System.currentTimeMillis();
    }

    public void resume()
    {
        if (paused)
        {
            startTime = System.currentTimeMillis() - (pauseTime - startTime);
            paused = false;
        }
    }

    public void addTime(int time)
    {
        startTime += time * 1000;
    }

    public boolean isPaused()
    {
        return paused;
    }
}
