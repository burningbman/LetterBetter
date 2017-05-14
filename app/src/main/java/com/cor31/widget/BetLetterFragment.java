package com.cor31.widget;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cor31.letterbetter.R;

public class BetLetterFragment extends DialogFragment
{
    public interface OnBetValueSelectedListener
    {
        void onBetValueSelected(int bet);

        void onMultiPlayerCreateCancelled(BetLetterFragment fragment);
    }

    int letterCount;
    TextView textViewLetterCount;
    SeekBar seekBar;
    Button buttonOK;
    OnBetValueSelectedListener listener;
    BetLetterFragment betLetterFragment;

    public static BetLetterFragment newInstance(int letterCount)
    {
        BetLetterFragment f = new BetLetterFragment();

        Bundle args = new Bundle();
        args.putInt("letterCount", letterCount);
        f.setArguments(args);

        return f;
    }

    public void initializeVariables(View v)
    {
        seekBar = (SeekBar) v.findViewById(R.id.seekBar);
        textViewLetterCount = (TextView) v.findViewById(R.id.textCount);
        textViewLetterCount.setText("0");
        TextView textViewLetterBankCount = (TextView) v.findViewById(R.id.textLetterCount);
        textViewLetterBankCount.setText(String.valueOf(letterCount));
    }

    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            listener = (OnBetValueSelectedListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " must implement " +
                    "OnBetValueSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState)
    {
        betLetterFragment = this;

        letterCount = getArguments().getInt("letterCount", 0);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View v = inflater.inflate(R.layout.bet_letters, container, false);

        initializeVariables(v);

        seekBar.setMax(letterCount);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                textViewLetterCount.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });

        View linearLayout = v.findViewById(R.id.linearLayoutButtons);
        buttonOK = (Button) linearLayout.findViewById(R.id.buttonOK);
        buttonOK.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                listener.onBetValueSelected(seekBar.getProgress());
            }
        });

        Button buttonCancel = (Button) linearLayout.findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                listener.onMultiPlayerCreateCancelled(betLetterFragment);
            }
        });

        return v;
    }
}
