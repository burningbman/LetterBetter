package com.cor31.widget;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import com.cor31.letterbetter.R;

public class PauseDialogFragment extends DialogFragment
{

    public interface PauseDialogListener
    {
        void onDialogResumeClick(DialogFragment dialog);

        void onDialogMainMenuClick(DialogFragment dialog);
    }

    PauseDialogListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, android.view.ViewGroup container, Bundle
            savedInstanceState)
    {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View v = inflater.inflate(R.layout.pause_screen, container, false);

        // Watch for button clicks.
        Button button = (Button) v.findViewById(R.id.button_resume);
        button.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                onResumeClick();
            }
        });

        button = (Button) v.findViewById(R.id.button_main_menu);
        button.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                onMainMenuClick();
            }
        });

        return v;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        try
        {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (PauseDialogListener) activity;
        }
        catch (ClassCastException e)
        {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString() + " must implement " +
                    "NoticeDialogListener");
        }

    }

    public void onResumeClick()
    {
        listener.onDialogResumeClick(this);
    }

    public void onMainMenuClick()
    {
        listener.onDialogMainMenuClick(this);
    }
}
