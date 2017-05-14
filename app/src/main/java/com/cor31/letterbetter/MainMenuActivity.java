package com.cor31.letterbetter;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.cor31.letterbetter.logic.Dictionary;
import com.cor31.letterbetter.logic.MusicManager;
import com.cor31.widget.BetLetterFragment;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.ParticipantResult;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer.InitiateMatchResult;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.util.ArrayList;

import static com.cor31.letterbetter.Constants.RC_INVITATION_INBOX;
import static com.cor31.letterbetter.Constants.RC_SELECT_PLAYERS;
import static com.cor31.letterbetter.Constants.REQUEST_ACHIEVEMENTS;
import static com.cor31.letterbetter.Constants.REQUEST_LEADERBOARD;

public class MainMenuActivity extends BaseGameActivity implements OnClickListener,
        ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>, BetLetterFragment
                .OnBetValueSelectedListener
{
    private SharedPreferences pref;
    private boolean bPlaySound, bTurnBased = true, bSignInSuccess = false;
    private LetterBank bank;
    private GoogleApiClient mGoogleApiClient;
    private TurnBasedMatch match;
    private MusicManager musicManager;

    // tag for debug logging
    final boolean ENABLE_DEBUG = true;

    // achievements and scores we're pending to push to the cloud
    // (waiting for the user to sign in, for instance)
    AccomplishmentsOutbox mOutbox = new AccomplishmentsOutbox();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Load the dictionary
        Dictionary.getInstance(getApplicationContext());

        // Start music if user didn't disable it
        pref = this.getSharedPreferences("wordlePrefs", Context.MODE_PRIVATE);
        bPlaySound = pref.getBoolean("playSound", true);
        bank = LetterBank.getInstance(getApplicationContext());
        musicManager = new MusicManager(pref, getApplicationContext());

        enableDebugLog(ENABLE_DEBUG);

        // IMPORTANT: if this Activity supported rotation, we'd have to be
        // more careful about adding the fragment, since the fragment would
        // already be there after rotation and trying to add it again would
        // result in overlapping fragments. But since we don't support rotation,
        // we don't deal with that for code simplicity.

        // load outbox from file
        mOutbox.loadLocal(this);

        // Sign in to Google Play if the user hasn't declined
        if (pref.getBoolean("signInToGoogle", true))
            beginUserInitiatedSignIn();

        // Set up handlers for Google sign in and sign out buttons
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

        mGoogleApiClient = mHelper.getApiClient();

        // Remove title bar and notification bar
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        TextView viewBank = (TextView) findViewById(R.id.textViewLetterBankCount);
        viewBank.setText(String.valueOf(bank.getCount()));

        TextView textViewTemp = (TextView) findViewById(R.id.newGame);
        Typeface font = Typeface.createFromAsset(getAssets(), "CinzelDecorative-Bold.otf");
        textViewTemp.setTypeface(font);

        textViewTemp = (TextView) findViewById(R.id.buttonMultiplayerInvite);
        textViewTemp.setTypeface(font);

        textViewTemp = (TextView) findViewById(R.id.buttonInbox);
        textViewTemp.setTypeface(font);

        textViewTemp = (TextView) findViewById(R.id.buttonAchievements);
        textViewTemp.setTypeface(font);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
                .LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onBetValueSelected(int bet)
    {
        bank.updateCount(-bet);
        Intent intentGame = new Intent(MainMenuActivity.this, TurnBasedMultiPlayerGameActivity
                .class);
        intentGame.putExtra(Constants.GAMETYPE, Constants.GAMETYPE_MULTIPLAYER_TURN);
        intentGame.putExtra(Constants.PLAY_MUSIC, musicManager.getMusicPreference());
        intentGame.putExtra(Constants.PLAY_SOUND, bPlaySound);
        intentGame.putExtra(Constants.MATCH_ID, match.getMatchId());
        intentGame.putExtra(Constants.NEXT_PLAYER_ID, "p_2");
        intentGame.putExtra(Constants.NEXT_PLAYER_NAME, Constants.getPlayerNameById(match, "p_2"));
        intentGame.putExtra(Constants.CURRENT_PLAYER_NAME, Constants.getPlayerNameById(match,
                "p_1"));
        intentGame.putExtra(Constants.SIGN_IN_GOOGLE, bSignInSuccess);
        intentGame.putExtra(Constants.MATCH_DATA_STRING, match.getData());
        intentGame.putExtra(Constants.BET, bet);
        Log.d("MainMenuActivity", "Starting multiplayer game.");
        MainMenuActivity.this.startActivity(intentGame);
        this.finish();
    }

    public void onMultiPlayerCreateCancelled(BetLetterFragment fragment)
    {
        fragment.dismiss();
        Games.TurnBasedMultiplayer.cancelMatch(mGoogleApiClient, match.getMatchId());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void openAchievements(View view)
    {
        startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()),
                REQUEST_ACHIEVEMENTS);
    }

    public void startSingleplayerGame(View view)
    {
        Intent intentGame = new Intent(MainMenuActivity.this, GameActivity.class);
        intentGame.putExtra(Constants.GAMETYPE, Constants.GAMETYPE_SINGLEPLAYER);
        intentGame.putExtra(Constants.PLAY_MUSIC, musicManager.getMusicPreference());
        intentGame.putExtra(Constants.PLAY_SOUND, bPlaySound);
        MainMenuActivity.this.startActivity(intentGame);
        this.finish();
    }

    public void startInviteMultiplayerGame(View view)
    {
        // launch the player selection screen
        // minimum: 1 other player; maximum: 3 other players
        Intent intent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, 1,
                1, true);
        startActivityForResult(intent, RC_SELECT_PLAYERS);
    }

    public void startRandomMultiplayerGame(View view)
    {
        /*
        // auto-match criteria to invite one random automatch opponent.
	    // You can also specify more opponents (up to 3).
	    Bundle am = RoomConfig.createAutoMatchCriteria(1, 1, 0);

	    // build the room config:
	    RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
	    roomConfigBuilder.setAutoMatchCriteria(am);
	    RoomConfig roomConfig = roomConfigBuilder.build();

		TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
				.addInvitedPlayers(invitees)
						//.setAutoMatchCriteria(autoMatchCriteria)
				.build();

	    // create room:
	    Games.TurnBasedMultiplayer.createMatch(mGoogleApiClient, roomConfig);

	    // prevent screen from sleeping during handshake
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);*/
        Toast.makeText(this, "I don't do anything right now.", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        musicManager.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        musicManager.onResume();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        musicManager.onStart();
        //EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        musicManager.onStop();
        //EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onSignInFailed()
    {
        View viewSignIn = findViewById(R.id.sign_in_button);
        View viewSignOut = findViewById(R.id.sign_out_button);

        viewSignIn.setVisibility(View.VISIBLE);
        viewSignOut.setVisibility(View.GONE);

        viewSignIn.invalidate();
        viewSignOut.invalidate();

        bSignInSuccess = false;
    }


    @Override
    public void onSignInSucceeded()
    {
        View viewSignIn = findViewById(R.id.sign_in_button);
        View viewSignOut = findViewById(R.id.sign_out_button);

        viewSignIn.setVisibility(View.GONE);
        viewSignOut.setVisibility(View.VISIBLE);

        viewSignIn.invalidate();
        viewSignOut.invalidate();

        bSignInSuccess = true;

        /*
        if (getInvitationId() != null)
        {

            RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
            roomConfigBuilder.setInvitationIdToAccept(getInvitationId());
            Games.RealTimeMultiplayer.join(getApiClient(), roomConfigBuilder.build());

            // prevent screen from sleeping during handshake
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // go to game screen
            Intent intent = new Intent(MainMenuActivity.this, GameActivity.class);
            startActivityForResult(intent, RC_MULTIPLAYER_RT);
        }*/
    }

    class AccomplishmentsOutbox
    {
        boolean mPrimeAchievement = false;
        boolean mHumbleAchievement = false;
        boolean mLeetAchievement = false;
        boolean mArrogantAchievement = false;
        int mBoredSteps = 0;
        int mEasyModeScore = -1;
        int mHardModeScore = -1;

        boolean isEmpty()
        {
            return !mPrimeAchievement && !mHumbleAchievement && !mLeetAchievement &&
                    !mArrogantAchievement && mBoredSteps == 0 && mEasyModeScore < 0 &&
                    mHardModeScore < 0;
        }

        public void saveLocal(Context ctx)
        {
            /* TODO: This is left as an exercise. To make it more difficult to cheat,
             * this data should be stored in an encrypted file! And remember not to
             * expose your encryption key (obfuscate it by building it from bits and
             * pieces and/or XORing with another string, for instance). */
        }

        public void loadLocal(Context ctx)
        {
            /* TODO: This is left as an exercise. Write code here that loads data
             * from the file you wrote in saveLocal(). */
        }
    }

    private void onInviteMultiplayerReturn(int response, Intent data)
    {
        if (response != Activity.RESULT_OK)
        {
            // user canceled
            return;
        }

        // get the invitee list
        Bundle extras = data.getExtras();
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

        // get auto-match criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

        if (minAutoMatchPlayers > 0)
        {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(minAutoMatchPlayers,
                    maxAutoMatchPlayers, 0);
        }
        else
        {
            autoMatchCriteria = null;
        }

        if (!bTurnBased)
        {
            // create the room and specify a variant if appropriate
            /*
            RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
            roomConfigBuilder.addPlayersToInvite(invitees);
            if (autoMatchCriteria != null)
                roomConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);

            RoomConfig roomConfig = roomConfigBuilder.build();
            Games.RealTimeMultiplayer.create(mGoogleApiClient, roomConfig);*/
        }
        else
        {
            TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder().addInvitedPlayers(invitees)
                    //.setAutoMatchCriteria(autoMatchCriteria)
                    .build();
            Games.TurnBasedMultiplayer.createMatch(mGoogleApiClient, tbmc).setResultCallback(this);
        }

        // prevent screen from sleeping during handshake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
/*
	@Override
	public void onInvitationReceived(Invitation invitation) {
	    // show in-game popup to let user know of pending invitation
	    // DialogFragment.show() will take care of adding the fragment
	    // in a transaction.  We also want to remove any currently showing
	    // dialog, so make our own transaction and take care of that here.
		String strInviterName = invitation.getInviter().getDisplayName();
		InvitationReceivedDialogFragment popup = InvitationReceivedDialogFragment.newInstance
		(strInviterName);
		popup.show(getSupportFragmentManager(), getResources().getString(R.string
		.invitation_received));

	    // store invitation for use when player accepts this invitation
	    mIncomingInvitationId = invitation.getInvitationId();
	}

	@Override
	public void onInvitationRemoved(String invitationId) {
		// TODO Auto-generated method stub

	}*/

    // Invitation to play has been accepted, join the room and open the game
    public void onInvitationAccepted()
    {
        /*
        RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
        roomConfigBuilder.setInvitationIdToAccept(mIncomingInvitationId);
        Games.RealTimeMultiplayer.join(getApiClient(), roomConfigBuilder.build());

        // prevent screen from sleeping during handshake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // now, go to game screen
        Intent intent = new Intent(MainMenuActivity.this, GameActivity.class);
        startActivityForResult(intent, RC_MULTIPLAYER_RT);*/
    }

    public void onInvitationInboxOpened(View view)
    {
        Intent intent = Games.TurnBasedMultiplayer.getInboxIntent(mGoogleApiClient);
        this.startActivityForResult(intent, RC_INVITATION_INBOX);
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.sign_in_button)
        {
            // start the asynchronous sign in flow
            beginUserInitiatedSignIn();
        }
        else if (v.getId() == R.id.sign_out_button)
        {
            // sign out.
            signOut();

            // show sign-in button, hide the sign-out button
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
        }
    }

    public void openLeaderboard(View v)
    {
        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                getResources().getString(R.string.leaderboard_singleplayer_standard)),
                REQUEST_LEADERBOARD);
    }

    @Override
    public void onResult(InitiateMatchResult result)
    {
        int statusCode = result.getStatus().getStatusCode();

        if (statusCode != GamesStatusCodes.STATUS_OK)
        {
            Constants.handleGooglePlayError(getApplicationContext(), statusCode);
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        BetLetterFragment fragment = BetLetterFragment.newInstance(bank.getCount());
        fragment.show(fragmentManager, "dialog");
        match = result.getMatch();
    }

    public void onInvitationDeclined()
    {
        return;
    }

    @Override
    protected void onActivityResult(int request, int response, Intent data)
    {
        if (request == RC_SELECT_PLAYERS)
        {
            onInviteMultiplayerReturn(response, data);
        }
        else if (request == RC_INVITATION_INBOX)
        {
            if (response == RESULT_OK)
            {
                match = data.getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH);

                int status = match.getStatus();

                switch (status)
                {
                    case TurnBasedMatch.MATCH_STATUS_ACTIVE:
                        int turnStatus = match.getTurnStatus();
                        MatchData matchData;
                        try
                        {
                            GsonBuilder gsonBuilder = new GsonBuilder();
                            Gson gson = gsonBuilder.registerTypeAdapter(MatchData.class, new MatchDataAdapter()).create();
                            matchData = gson.fromJson(new StringReader(new String(match.getData())), MatchData.class);
                        }
                        catch (Exception e)
                        {
                            Log.e("MainMenu", "Error loading match data.", e);
                            Toast.makeText(getApplicationContext(), R.string
                                    .error_loading_game_data, Toast.LENGTH_LONG).show();
                            return;
                        }

                        switch (turnStatus)
                        {
                            case TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN:
                                if (!matchData.isComplete())
                                {
                                    FragmentManager fragmentManager = getFragmentManager();
                                    BetLetterFragment fragment = BetLetterFragment.newInstance
                                            (bank.getCount());
                                    fragment.show(fragmentManager, "dialog");
                                }
                                else
                                {
                                    Toast.makeText(getApplicationContext(), getString(R.string
                                            .won_match_uncollected) + matchData.getBet(), Toast
                                            .LENGTH_LONG).show();
                                    bank.updateCount(matchData.getBet());

                                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                    ObjectOutput out = null;

                                    try
                                    {
                                        out = new ObjectOutputStream(bos);
                                        out.writeObject(matchData);
                                        ArrayList<Participant> alParticipants = match
                                                .getParticipants();
                                        ParticipantResult result1 = null;
                                        ParticipantResult result2 = null;
                                        for (Participant p : alParticipants)
                                        {
                                            if (result1 == null)
                                                result1 = p.getResult();
                                            else
                                                result2 = p.getResult();
                                        }
                                        Games.TurnBasedMultiplayer.finishMatch(mGoogleApiClient,
                                                match.getMatchId(), bos.toByteArray(), result1,
                                                result2);
                                    }
                                    catch (IOException e)
                                    {
                                        Log.e("MainMenuActivity", "Error finishing match.", e);
                                    }
                                    finally
                                    {
                                        try
                                        {
                                            if (out != null)
                                            {
                                                out.close();
                                            }
                                        }
                                        catch (IOException ex)
                                        {
                                            Log.e("GameOver", "Error closing ObjectOutput", ex);
                                        }
                                        try
                                        {
                                            bos.close();
                                        }
                                        catch (IOException ex)
                                        {
                                            Log.e("GameOver", "Error closing " +
                                                    "ByteArrayOutputStream", ex);
                                        }
                                    }
                                }
                                break;
                            //TODO: Show toasts regarding other status.
                        }

                        break;
                    case TurnBasedMatch.MATCH_STATUS_EXPIRED:
                    case TurnBasedMatch.MATCH_STATUS_CANCELED:
                    case TurnBasedMatch.MATCH_STATUS_COMPLETE:
                        String id = Constants.getOppositeId(match.getDescriptionParticipantId());
                        Participant participant = match.getParticipant(id);
                        ParticipantResult result = participant.getResult();

                        if (result == null)
                            return;

                        int resultStatus = result.getResult();
                        switch (resultStatus)
                        {
                            case ParticipantResult.MATCH_RESULT_LOSS:
                                Toast.makeText(getApplicationContext(), R.string.lost_match,
                                        Toast.LENGTH_LONG).show();
                                return;
                            case ParticipantResult.MATCH_RESULT_WIN:
                                Toast.makeText(getApplicationContext(), R.string
                                        .won_match_collected, Toast.LENGTH_LONG).show();
                                return;
                        }
                        break;
                    //TODO: Get winnings. Update game state.
                }
            }
        }
    }
}
