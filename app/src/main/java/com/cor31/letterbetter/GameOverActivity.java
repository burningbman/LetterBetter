package com.cor31.letterbetter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.ParticipantResult;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer.UpdateMatchResult;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.google.gson.Gson;

public class GameOverActivity extends BaseGameActivity implements
        ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>
{
    private String sGameType, sNextPlayerId, sNextPlayerName, sMatchId;
    private boolean bNewHighScore = false;
    private String strMatchDataJson;
    private GoogleApiClient mGoogleApiClient;
    private int iScore, iBet;
    private LetterBank bank;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        bank = LetterBank.getInstance(getApplicationContext());

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
                .LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.game_over_screen);

        Bundle bundle = getIntent().getExtras();
        sGameType = bundle.getString(Constants.GAMETYPE);
        iScore = bundle.getInt(Constants.NEW_SCORE);
        sNextPlayerId = bundle.getString(Constants.NEXT_PLAYER_ID);
        sNextPlayerName = bundle.getString(Constants.NEXT_PLAYER_NAME);
        sMatchId = bundle.getString(Constants.MATCH_ID);
        strMatchDataJson = bundle.getString(Constants.MATCH_DATA_STRING);
        boolean bSignInSuccess = bundle.getBoolean(Constants.SIGN_IN_GOOGLE, false);

        mGoogleApiClient = mHelper.getApiClient();
        if (bSignInSuccess && !mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting())
            mGoogleApiClient.connect();

        TextView textViewScore = (TextView) findViewById(R.id.score_game_over);
        textViewScore.setText(String.valueOf(iScore));

        checkHighScore(iScore);

        if (mGoogleApiClient.isConnected())
        {
            updateAchievements();

            if (!sGameType.equals(Constants.GAMETYPE_SINGLEPLAYER))
                handleMultiplayer();
        }
    }

    private void handleMultiplayer()
    {
        if (sNextPlayerId.equals(Constants.P2))
        {
            try
            {
                Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, sMatchId, strMatchDataJson.getBytes(),
                        //sNextPlayerId);
                        Constants.P1);
            }
            catch (Exception e)
            {
                Log.e("GameOverActivity", "Error taking turn.", e);
            }

            Toast.makeText(getApplicationContext(), getString(R.string.turn_based_sent) + " " +
                    sNextPlayerName, Toast.LENGTH_LONG).show();
        }
        else if (sNextPlayerId.equals(Constants.P1))
        {
            Gson gson = new Gson();
            MatchData matchData = gson.fromJson(strMatchDataJson, MatchData.class);
            iBet = matchData.getBet();

            ParticipantResult opponentResult;
            ParticipantResult myResult;
            String opponent = matchData.getFirstPlayerName();
            int opponentScore = matchData.getPlayerScore(opponent);
            boolean win = false;
            matchData.setComplete(true);

            String strMsg;
            if (opponentScore > iScore)
            {
                strMsg = getString(R.string.you_lose) + " " + opponent + ": " + opponentScore + "" +
                        ". " + iScore;
                opponentResult = new ParticipantResult("p_1", ParticipantResult.MATCH_RESULT_WIN,
                        1);
                myResult = new ParticipantResult("p_2", ParticipantResult.MATCH_RESULT_LOSS, 2);
            }
            else if (opponentScore < iScore)
            {
                strMsg = getString(R.string.you_win) + " " + opponent + ": " + opponentScore + "." +
                        " " + iScore;
                opponentResult = new ParticipantResult("p_1", ParticipantResult
                        .MATCH_RESULT_LOSS, 2);
                myResult = new ParticipantResult("p_2", ParticipantResult.MATCH_RESULT_WIN, 1);
                Toast.makeText(getApplicationContext(), 2 * matchData.getBet() + " " + getString
                        (R.string.letters_won_this_game), Toast.LENGTH_LONG).show();
                bank.updateCount(2 * matchData.getBet());
                matchData.setLettersCollected(true);
                win = true;
                Games.Achievements.increment(mGoogleApiClient, getResources().getString(R.string
                        .achievement_winner_winner), 1);
                Games.Achievements.increment(mGoogleApiClient, getResources().getString(R.string
                        .achievement_chicken_dinner), 1);
            }
            else
            {
                strMsg = getString(R.string.game_is_draw) + " " + opponent + ": " + opponentScore
                        + ". " + iScore;
                opponentResult = new ParticipantResult("p_1", ParticipantResult.MATCH_RESULT_TIE,
                        1);
                myResult = new ParticipantResult("p_2", ParticipantResult.MATCH_RESULT_TIE, 1);
                matchData.setBet(matchData.getBet() / 2);
            }

            TextView multiMsg = new TextView(getApplicationContext());
            multiMsg.setText(strMsg);
            LinearLayout ll = (LinearLayout) findViewById(R.id.game_over_ll);
            ll.addView(multiMsg, 0);


            try
            {
                // If player 2 won, they can end the game since they will instantly get the
                // letters in their bank.
                if (win)
                {
                    Games.TurnBasedMultiplayer.finishMatch(mGoogleApiClient, sMatchId, gson
                            .toJson(matchData).getBytes(), opponentResult, myResult)
                            .setResultCallback(new ResultCallback<TurnBasedMultiplayer
                                    .UpdateMatchResult>()

                            {
                                @Override
                                public void onResult(TurnBasedMultiplayer.UpdateMatchResult result)
                                {
                                    Log.d("UPDATE_MATCH_RESULT", result.getStatus().toString());
                                }
                            });
                }

                // If player 1 wins or there is a tie, a turn must be taken (instead of finishing
                // the match) so that player 1 can retrieve their points
                else
                {
                    Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, sMatchId, gson.toJson
                            (matchData).getBytes(), "p_1", opponentResult, myResult);
                }
            }
            catch (Exception e)
            {
                Log.e("GameOverActivity", "Error finishing match.", e);
            }
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        //EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        //EasyTracker.getInstance(this).activityStop(this);
    }

    public void startNewGame(View v)
    {
        Intent intentGame = new Intent(GameOverActivity.this, GameActivity.class);
        GameOverActivity.this.startActivity(intentGame);
        Log.i("Content ", " Game layout ");
        this.finish();
    }

    public void openMainMenu(View v)
    {
        Intent intentMainMenu = new Intent(GameOverActivity.this, MainMenuActivity.class);
        GameOverActivity.this.startActivity(intentMainMenu);
        Log.i("Content ", " Main menu layout ");
        this.finish();
    }

    private void checkHighScore(int newScore)
    {
        SharedPreferences pref = this.getSharedPreferences("wordlePrefs", Context.MODE_PRIVATE);
        int oldScore = pref.getInt("highScore", 0);

        TextView textViewHighScore = (TextView) findViewById(R.id.high_score);
        textViewHighScore.setText(String.valueOf(oldScore));

        // User recorded a new high score, update preferences and screen
        if (oldScore < newScore)
        {
            TextView textViewTop = (TextView) findViewById(R.id.game_over_top_text);
            textViewTop.setText(R.string.new_high_score);
            Editor edit = pref.edit();
            edit.putInt("highScore", newScore);
            edit.apply();

            // Used during achievement updating
            bNewHighScore = true;
        }
    }

    public void openLeaderboard(View v)
    {
        if (mGoogleApiClient.isConnected())
            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                    getResources().getString(R.string.leaderboard_singleplayer_standard)),
                    Constants.REQUEST_LEADERBOARD);
    }

    @Override
    public void onSignInFailed()
    {
    }

    @Override
    public void onSignInSucceeded()
    {
        if (!sGameType.equals(Constants.GAMETYPE_SINGLEPLAYER))
            handleMultiplayer();

        updateAchievements();
    }

    private void updateAchievements()
    {
        // Update singleplayer points achievements
        if (bNewHighScore && iScore >= getResources().getInteger(R.integer
                .achievement_wordsmith_points))
        {
            Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string
                    .achievement_wordsmith));
        }
        // Update leaderboard
        Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string
                .leaderboard_singleplayer_standard), iScore);

        // Update games played achievement
        Games.Achievements.increment(mGoogleApiClient, getResources().getString(R.string
                .achievement_beginner), 1);

        if (iScore > 0)
            Games.Achievements.increment(mGoogleApiClient, getResources().getString(R.string
                    .achievement_dedicated), iScore);
    }

    @Override
    public void onResult(UpdateMatchResult result)
    {
        int statusCode = result.getStatus().getStatusCode();
        if (statusCode != GamesStatusCodes.STATUS_OK)
        {
            Constants.handleGooglePlayError(getApplicationContext(), statusCode);
            return;
        }

        TextView multiMsg = new TextView(getApplicationContext());
        String strMsg = getResources().getString(R.string.turn_based_sent) + " " + sNextPlayerName;
        multiMsg.setText(strMsg);
        LinearLayout ll = (LinearLayout) findViewById(R.id.game_over_ll);
        ll.addView(multiMsg, 0);
    }

    // This is the main function that gets called when players choose a match
    // from the inbox, or else create a match and want to start it.
    public void updateMatch(TurnBasedMatch match)
    {
        //mMatch = match;

        int status = match.getStatus();
        int turnStatus = match.getTurnStatus();

        switch (status)
        {
            case TurnBasedMatch.MATCH_STATUS_CANCELED:
                showWarning("Canceled!", "This game was canceled!");
                return;
            case TurnBasedMatch.MATCH_STATUS_EXPIRED:
                showWarning("Expired!", "This game is expired.  So sad!");
                return;
            case TurnBasedMatch.MATCH_STATUS_AUTO_MATCHING:
                showWarning("Waiting for auto-match...", "We're still waiting for an automatch "
                        + "partner.");
                return;
            case TurnBasedMatch.MATCH_STATUS_COMPLETE:
                if (turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE)
                {
                    showWarning("Complete!", "This game is over; someone finished it, and so did " +
                            "" + "you!  There is nothing to be done.");
                    break;
                }

                // Note that in this state, you must still call "Finish" yourself,
                // so we allow this to continue.
                showWarning("Complete!", "This game is over; someone finished it!  You can only "
                        + "finish it now.");
        }

        // OK, it's active. Check on turn status.
        switch (turnStatus)
        {
            case TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN:
                //mTurnData = SkeletonTurn.unpersist(mMatch.getData());
                printOpponentName();
                return;
            case TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN:
                // Should return results.
                showWarning("Alas...", "It's not your turn.");
                break;
            case TurnBasedMatch.MATCH_TURN_STATUS_INVITED:
                showWarning("Good inititative!", "Still waiting for invitations.\n\nBe patient!");
        }

        //mTurnData = null;

        //setViewVisibility();
    }

    // Generic warning/info dialog
    public void showWarning(String title, String message)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(title).setMessage(message);

        // set dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface
                .OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                // if this button is clicked, close
                // current activity
            }
        });

        // create alert dialog
        AlertDialog mAlertDialog = alertDialogBuilder.create();

        // show it
        mAlertDialog.show();
    }

    // Returns false if something went wrong, probably. This should handle
    // more cases, and probably report more accurate results.
    private boolean checkStatusCode(TurnBasedMatch match, int statusCode)
    {
        switch (statusCode)
        {
            case GamesStatusCodes.STATUS_OK:
                return true;
            case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_DEFERRED:
                // This is OK; the action is stored by Google Play Services and will
                // be dealt with later.
                Toast.makeText(this, "Stored action for later.  (Please remove this toast before " +
                        "" + "release.)", Constants.TOAST_DELAY).show();
                // NOTE: This toast is for informative reasons only; please remove
                // it from your final application.
                return true;
            case GamesStatusCodes.STATUS_MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
                showErrorMessage(match, statusCode, R.string
                        .status_multiplayer_error_not_trusted_tester);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_ALREADY_REMATCHED:
                showErrorMessage(match, statusCode, R.string.match_error_already_rematched);
                break;
            case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_FAILED:
                showErrorMessage(match, statusCode, R.string.network_error_operation_failed);
                break;
            case GamesStatusCodes.STATUS_CLIENT_RECONNECT_REQUIRED:
                showErrorMessage(match, statusCode, R.string.client_reconnect_required);
                break;
            case GamesStatusCodes.STATUS_INTERNAL_ERROR:
                showErrorMessage(match, statusCode, R.string.internal_error);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_INACTIVE_MATCH:
                showErrorMessage(match, statusCode, R.string.match_error_inactive_match);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_LOCALLY_MODIFIED:
                showErrorMessage(match, statusCode, R.string.match_error_locally_modified);
                break;
            default:
                showErrorMessage(match, statusCode, R.string.unexpected_status);
                Log.d("GameOverActivity", "Did not have warning or string to deal with: " +
                        statusCode);
        }

        return false;
    }

    public void showErrorMessage(TurnBasedMatch match, int statusCode, int stringId)
    {

        showWarning("Warning", getResources().getString(stringId));
    }

    private void printOpponentName()
    {
        TextView text = (TextView) findViewById(R.id.game_over_top_text);
        text.setText(getString(R.string.turn_based_sent) + sNextPlayerName + ". " + getString(R
                .string.letters_bet) + " " + iBet + ".");
    }
}
