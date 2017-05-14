package com.cor31.letterbetter;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;

import java.util.ArrayList;

public final class Constants
{

    private Constants()
    {
    }

    public static final String STATE_RESOLVING_ERROR = "resolving_error";
    public static final String TAG = "MainFragment";
    public static final String GAMETYPE_SINGLEPLAYER = "single";
    public static final String GAMETYPE_MULTIPLAYER_TURN = "multi_turn";
    public static final String GAMETYPE_MULTIPLAYER_REAL = "multi_real";
    public static final String GAMETYPE = "gameType";
    public static final String NEW_SCORE = "newScore";
    public static final String MULTIPLAYER_RESULT = "multiResult";
    public static final String WIN = "win";
    public static final String LOSS = "loss";
    public static final String LETTER_BANK = "letterBank";
    public static final String PLAY_MUSIC = "playMusic";
    public static final String PLAY_SOUND = "playSound";
    public static final String FIRST_TURN = "firstTurn";
    public static final String NEXT_PLAYER_ID = "nextPlayerId";
    public static final String NEXT_PLAYER_NAME = "nextPlayerName";
    public static final String CURRENT_PLAYER_NAME = "currentPlayerName";
    public static final String MATCH_ID = "matchId";
    public static final String BOARD_BYTE_ARRAY = "boardByteArray";
    public static final String MATCH_DATA_STRING = "matchDataString";
    public static final String SIGN_IN_GOOGLE = "signInGoogle";
    public static final String P1 = "p_1";
    public static final String P2 = "p_2";
    public static final String BET = "bet";

    public static final int REQUEST_LEADERBOARD = 0;
    public static final int REQUEST_RESOLVE_ERROR = 1;
    public static final int RC_WAITING_ROOM = 2;
    public static final int RC_INVITATION_INBOX = 3;
    public static final int RC_RESOLVE = 4;
    public static final int RC_UNUSED = 5;
    public static final int RC_SELECT_PLAYERS = 6;
    public static final int RC_MULTIPLAYER_RT = 7;
    public static final int RC_MULTIPLAYER_TB = 8;
    public static final int REQUEST_ACHIEVEMENTS = 9;
    public static final int TOAST_DELAY = Toast.LENGTH_SHORT;

    public static void showToast(Context context, String strMessage)
    {
        Toast toast = Toast.makeText(context, strMessage, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 100, 0);
        toast.show();
    }

    public static void handleGooglePlayError(Context context, int statusCode)
    {
        String strError = "";

        switch (statusCode)
        {
            case GamesStatusCodes.STATUS_NETWORK_ERROR_NO_DATA:
                strError = context.getResources().getString(R.string
                        .status_network_error_no_data_msg);
                break;
            case GamesStatusCodes.STATUS_NETWORK_ERROR_STALE_DATA:
                strError = context.getResources().getString(R.string
                        .status_network_error_stale_data_msg);
                break;
            case GamesStatusCodes.STATUS_MATCH_NOT_FOUND:
                strError = context.getResources().getString(R.string.status_match_not_found_msg);
                break;
            case GamesStatusCodes.STATUS_CLIENT_RECONNECT_REQUIRED:
                strError = context.getResources().getString(R.string
                        .status_client_reconnect_required_msg);
                break;
            default:
                strError = context.getResources().getString(R.string.status_internal_error_msg);
                break;
        }

        showToast(context, strError);
    }

    public static String getPlayerNameById(TurnBasedMatch match, String playerId)
    {
        ArrayList<Participant> participants = match.getParticipants();
        for (Participant p : participants)
        {
            if (p.getParticipantId().equals(playerId))
            {
                return p.getDisplayName();
            }
        }

        return null;
    }

    public static String getOppositeId(String id)
    {
        switch (id)
        {
            case "p_1":
                return "p_2";
            case "p_2":
                return "p_1";
            default:
                return null;
        }
    }
}
