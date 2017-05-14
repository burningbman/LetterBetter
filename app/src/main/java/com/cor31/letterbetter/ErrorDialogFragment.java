package com.cor31.letterbetter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ErrorDialogFragment extends DialogFragment
{
    static ErrorDialogFragment newInstance(String strError)
    {
        ErrorDialogFragment f = new ErrorDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("error", strError);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        String strError = getArguments().getString("error");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(strError).setPositiveButton(R.string.accept, new DialogInterface
                .OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                ((MainMenuActivity) getActivity()).onInvitationAccepted();
            }
        }).setNegativeButton(R.string.decline, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                ((MainMenuActivity) getActivity()).onInvitationDeclined();
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
