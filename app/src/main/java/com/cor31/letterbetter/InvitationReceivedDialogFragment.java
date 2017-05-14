package com.cor31.letterbetter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class InvitationReceivedDialogFragment extends DialogFragment
{

    static InvitationReceivedDialogFragment newInstance(String strPlayerName)
    {
        InvitationReceivedDialogFragment f = new InvitationReceivedDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("name", strPlayerName);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        String strPlayerName = getArguments().getString("name");
        String strMessage = strPlayerName + R.string.invitationMessage;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(strMessage).setPositiveButton(R.string.accept, new DialogInterface
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
