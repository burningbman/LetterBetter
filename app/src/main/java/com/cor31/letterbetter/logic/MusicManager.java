package com.cor31.letterbetter.logic;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;

import com.cor31.letterbetter.R;

/**
 * Manages background music
 */
public class MusicManager
{
    MediaPlayer music;
    boolean musicPreference;

    public MusicManager(SharedPreferences preferences, Context context) {
        musicPreference = preferences.getBoolean("playMusic", true);
//        music = MediaPlayer.create(context, R.raw.wordle_theme);
        music = MediaPlayer.create(context, R.raw.bing_1);
        music.setLooping(true);
    }

    public void onStart() {
        if (musicPreference) {
            music.start();
        }
    }

    public void onStop() {
        if (music.isPlaying())
            music.pause();

        music.release();
    }

    public void onPause() {
        if (music.isPlaying())
        {
            music.pause();
        }
    }

    public void onResume() {
        if (music.isPlaying())
        {
            music.start();
        }
    }

    public void toggle() {
        musicPreference = !musicPreference;

        if (musicPreference) {
            music.start();
        } else {
            music.pause();
        }
    }

    public boolean getMusicPreference()
    {
        return musicPreference;
    }
}
