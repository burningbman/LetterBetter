package com.cor31.letterbetter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class LetterBank
{
    private static LetterBank instance = null;
    private static int intLetters;
    private SharedPreferences pref;

    protected LetterBank(Context context)
    {
        pref = context.getSharedPreferences("wordlePrefs", Context.MODE_PRIVATE);
        intLetters = pref.getInt(Constants.LETTER_BANK, 0);
    }

    public static LetterBank getInstance(Context context)
    {
        if (instance == null)
            instance = new LetterBank(context);

        return instance;
    }

    public int getCount()
    {
        return intLetters;
    }

    public void updateCount(int intChange)
    {
        intLetters += intChange;
        Editor editor = pref.edit();
        editor.putInt(Constants.LETTER_BANK, intLetters);
        editor.commit();
    }
}
