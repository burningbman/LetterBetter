package com.cor31.letterbetter.mainmenu;

import android.content.Context;
import android.content.SharedPreferences;

import com.cor31.letterbetter.LetterBank;
import com.cor31.letterbetter.data.MainMenuRepository;
import com.cor31.letterbetter.logic.Dictionary;
import com.cor31.letterbetter.logic.MusicManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;

import java.lang.ref.WeakReference;

/**
 * Created by bbadders on 8/9/2016.
 */
public class MainMenuPresenter implements MainMenuContract.UserActionListener
{
    private WeakReference<MainMenuContract.View> mainView;
    private SharedPreferences pref;
    private boolean bPlaySound, bTurnBased = true, bSignInSuccess = false;
    LetterBank bank;
    private GoogleApiClient mGoogleApiClient;
    private TurnBasedMatch match;
    private MusicManager musicManager;

    // tag for debug logging
    final boolean ENABLE_DEBUG = true;

    // achievements and scores we're pending to push to the cloud
    // (waiting for the user to sign in, for instance)
    //AccomplishmentsOutbox mOutbox = new AccomplishmentsOutbox();

    public MainMenuPresenter(MainMenuContract.View view) {
        mainView = new WeakReference<>(view);

        // Load the dictionary
        Dictionary.getInstance(getApplicationContext());

        // Start music if user didn't disable it
        //pref = this.getSharedPreferences("wordlePrefs", Context.MODE_PRIVATE);
        //bPlaySound = pref.getBoolean("playSound", true);
        bank = LetterBank.getInstance(getApplicationContext());
        //musicManager = new MusicManager(pref, getApplicationContext());

        // IMPORTANT: if this Activity supported rotation, we'd have to be
        // more careful about adding the fragment, since the fragment would
        // already be there after rotation and trying to add it again would
        // result in overlapping fragments. But since we don't support rotation,
        // we don't deal with that for code simplicity.

        // load outbox from file
        //mOutbox.loadLocal(this);

        // Sign in to Google Play if the user hasn't declined
        //if (pref.getBoolean("signInToGoogle", true))
        {
            //beginUserInitiatedSignIn();
        }
    }

    public MainMenuPresenter (MainMenuRepository repo, MainMenuContract.View view) {
        new MainMenuPresenter(view);
    }

    @Override
    public void onResume()
    {

    }

    @Override
    public void onItemClicked(int position)
    {

    }

    @Override
    public void onDestroy()
    {

    }

    private MainMenuContract.View getView() throws NullPointerException{
        if (mainView != null) {}
            //return mainView.get();
        else
            throw new NullPointerException("View is unavailable");
        return null;
    }

    private Context getApplicationContext() {
        return getView().getApplicationContext();
    }
}

