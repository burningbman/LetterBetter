package com.cor31.letterbetter;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.cor31.letterbetter.logic.Dictionary;
import com.cor31.letterbetter.logic.Game;
import com.cor31.letterbetter.logic.MusicManager;
import com.cor31.widget.LetterButton;
import com.cor31.widget.PauseDialogFragment;
import com.cor31.widget.VowelDialogFragment;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.example.games.basegameutils.BaseGameActivity;

import java.util.ArrayList;

public class GameActivity extends BaseGameActivity implements PauseDialogFragment
        .PauseDialogListener, OnTouchListener
{
    private int[] ARRAY_TILE_REFERENCES = {R.id.tile0, R.id.tile1, R.id.tile2, R.id.tile3, R.id
            .tile4, R.id.tile5, R.id.tile6, R.id.tile7, R.id.tile8, R.id.tile9, R.id.tile10, R.id
            .tile11, R.id.tile12, R.id.tile13, R.id.tile14, R.id.tile15};
    private int[] ARRAY_ROW_REFERENCES = {R.id.row0, R.id.row1, R.id.row2, R.id.row3};

    protected MusicManager musicManager;
    protected boolean bSound, bSwapping = false, bSignInSuccess, bSpellDragging = false;
    protected ArrayList<LetterButton> alSelButtons = new ArrayList<>();
    protected TextView viewTimer, viewScore, viewBank;
    protected String sGameType, sNextPlayerId, sNextPlayerName;
    protected CharSequence csVowel = null;
    protected int iScore = 0, submittedWordRow = 0, iWordColor = 0, iPowerupTimeCounter = 0, soundGood,
            soundBad;
    protected Timer timer;
    protected Handler handlerTime = new Handler();
    protected MediaPlayer mediaMusic;
    protected SoundPool sounds;
    protected SharedPreferences pref;
    protected VowelDialogFragment fragVowel;
    protected LetterBank bank;
    protected GoogleApiClient mGoogleApiClient;
    protected Game game;
    LetterButton lbDown = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        // Retrieve values from the passed in bundle
        getBundleValues();

        // Get the Letter Bank instance
        bank = LetterBank.getInstance(getApplicationContext());

        // Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
                .LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.game_screen);

        // Attempt to sign in to Play Games
        handleGoogleSignIn();

        // Initialize the timer
        timer = new Timer(this);
        handlerTime.postDelayed(timerThread, 0);

        // Get the dictionary instance
        Dictionary.getInstance(getApplicationContext());

        // Build the background music manager
        musicManager = new MusicManager(pref, getApplicationContext());

        // Build the sounds
        initializeMedia();

        // Set initial text displays
        initializeTextViews();

        // Ensure power-ups are properly enabled/disabled
        updatePowerupDisplays();

        game = new Game(getApplicationContext());
    }

    /**
     * Retrieve user-defined values from activity intent
     */
    private void getBundleValues() {
        Bundle bundle = getIntent().getExtras();
        bSound = bundle.getBoolean(Constants.PLAY_SOUND, true);
        bSignInSuccess = bundle.getBoolean(Constants.SIGN_IN_GOOGLE, false);
        sGameType = bundle.getString(Constants.GAMETYPE);
        sNextPlayerId = bundle.getString(Constants.NEXT_PLAYER_ID, "");
        sNextPlayerName = bundle.getString(Constants.NEXT_PLAYER_NAME, "");
    }

    /**
     * Set initial text view values
     */
    private void initializeTextViews()
    {
        LinearLayout viewScoreAndTime = (LinearLayout) findViewById(R.id.score_and_time_layout);
        viewTimer = (TextView) viewScoreAndTime.findViewById(R.id.time);
        viewTimer.setText("60");
        viewScore = (TextView) viewScoreAndTime.findViewById(R.id.score);
        viewScore.setText(String.valueOf(0));
        viewBank = (TextView) viewScoreAndTime.findViewById(R.id.textViewLetterBankCount);
        viewBank.setText(String.valueOf(bank.getCount()));
    }

    /**
     * Sign in to Play Games
     */
    protected void handleGoogleSignIn()
    {
        mGoogleApiClient = mHelper.getApiClient();
        if (bSignInSuccess)
            mGoogleApiClient.connect();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        musicManager.onStop();
        //EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        musicManager.onStart();
        //EasyTracker.getInstance(this).activityStart(this);
        setLetterText();
    }

    @Override
    public void onBackPressed()
    {
        FragmentManager fragmentManager = getFragmentManager();
        PauseDialogFragment fragPause = new PauseDialogFragment();
        fragPause.show(fragmentManager, "dialog");
        onPause();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        timer.pause();
        musicManager.onPause();
        cleanUpMedia();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        timer.resume();
        musicManager.onResume();
        initializeMedia();
    }

    /**
     * Determines if the specified letter can be clicked. If it can, the letter is
     * clicked. If is the same letter as the last letter clicked, the word is then
     * submitted.
     *
     * @param v View clicked
     * @return true if word is submitted.
     */
    public boolean letterClicked(View v)
    {
        boolean wordSubmitted = false;
        LetterButton clickedButton = (LetterButton) v;

        // Button is not a neighbor of the last selected button, do nothing
        if (!buttonCanBeSelected(clickedButton))
            return false;

        if (!alSelButtons.isEmpty())
        {
            LetterButton last = alSelButtons.get(alSelButtons.size() - 1);

            // Clicked same button twice, submit the word and reset selected buttons
            if (last == clickedButton)
            {
                submitWord();
                wordSubmitted = true;
            }
        }

        // Select this button
        clickedButton.setSelected(true);
        alSelButtons.add(clickedButton);
        game.letterClicked(clickedButton.getText());

        return wordSubmitted;
    }

    /**
     *
     */
    public void submitWord()
    {
        if (game.isValidWord())
        {
            if (bSound)
                sounds.play(soundGood, 1, 1, 0, 0, 1);

            int len = game.getCurrentWord().length();
            switch (len)
            {
                case 3:
                    iScore += 1;
                    iWordColor = getResources().getColor(R.color.three_letter_word, null);
                    break;
                case 4:
                    iScore += 1;
                    iWordColor = getResources().getColor(R.color.four_letter_word, null);
                    break;
                case 5:
                    iScore += 2;
                    iWordColor = getResources().getColor(R.color.five_letter_word, null);
                    break;
                case 6:
                    iScore += 3;
                    iWordColor = getResources().getColor(R.color.six_letter_word, null);
                    break;
                case 7:
                    iScore += 5;
                    iWordColor = getResources().getColor(R.color.seven_letter_word, null);
                    break;
                default:
                    iScore += 11;
                    iWordColor = getResources().getColor(R.color.eight_letter_word, null);
                    break;
            }

            displayWord();
            updateScoreDisplay();
            updateLetterBank();
        }
        else
        {
            if (bSound)
                sounds.play(soundBad, 1, 1, 0, 0, 1);
        }
        // Reset selected buttons
        for (LetterButton selectedButton : alSelButtons)
        {
            selectedButton.setSelected(false);
        }

        resetWordAndButtons();
    }

    public void toggleSound(View v)
    {
        bSound = !bSound;

        Editor edit = pref.edit();
        edit.putBoolean("playSound", bSound);
        edit.apply();
    }

    public void toggleMusic(View v)
    {
		musicManager.toggle();

        Editor edit = pref.edit();
        edit.putBoolean("playMusic", musicManager.getMusicPreference());
        edit.apply();
    }

    public void pauseClicked(View v)
    {
        FragmentManager fragmentManager = getFragmentManager();
        PauseDialogFragment fragPause = new PauseDialogFragment();
        fragPause.show(fragmentManager, "dialog");
        onPause();
    }

    /**
     * Draws letters from the code Board on to the displayed board.
     */
    protected void setLetterText()
    {
        LinearLayout boardLayout = (LinearLayout) findViewById(R.id.board_layout);

        for (int i = 0; i < game.getNumTilesVertical(); i++)
        {
            for (int j = 0; j < game.getNumTilesHorizontal(); j++)
            {
                LinearLayout row = (LinearLayout) boardLayout.findViewById(ARRAY_ROW_REFERENCES[i]);
                Button tile = (Button) row.findViewById(ARRAY_TILE_REFERENCES[game
                        .getNumTilesHorizontal() * i + j]);
                tile.setText(String.valueOf(game.getLetterAt(j, i)));
                tile.setOnTouchListener(this);
            }
        }
    }

    /**
     * Checks to see if the most recently clicked button is a neighbor to the most recently
     * selected button
     *
     * @param button Button that has just been clicked
     * @return true if this Button is a neighbor to the previously selected button, false otherwise
     */
    protected boolean buttonCanBeSelected(LetterButton button)
    {
        if (alSelButtons.isEmpty())
            return true;

        if (alSelButtons.contains(button) && alSelButtons.get(alSelButtons.size() - 1) != button)
            return false;

        LetterButton lastButtonClicked = alSelButtons.get(alSelButtons.size() - 1);

        int[] intArrLastButton = lastButtonClicked.getLocation();
        int[] intArrThisButton = button.getLocation();

        int intDistanceX = intArrLastButton[0] - intArrThisButton[0];
        int intDistanceY = intArrLastButton[1] - intArrThisButton[1];

        return (-1 <= intDistanceX && intDistanceX <= 1 && -1 <= intDistanceY && intDistanceY <= 1);
    }

    /**
     * Called when time is up. Starts GameOverActivity and passes needed variables.
     */
    protected void gameOver()
    {
        viewTimer.setText("0");
        Intent gameOver = new Intent(this, GameOverActivity.class);
        gameOver.putExtra(Constants.NEW_SCORE, iScore);
        gameOver.putExtra(Constants.GAMETYPE, sGameType);
        gameOver.putExtra(Constants.SIGN_IN_GOOGLE, bSignInSuccess);
        gameOver.putExtra(Constants.NEXT_PLAYER_ID, sNextPlayerId);
        gameOver.putExtra(Constants.NEXT_PLAYER_NAME, sNextPlayerName);
        game = null;
        fragVowel = null;
        mediaMusic = null;
        this.startActivity(gameOver);
        this.finish();
    }

    /**
     * Handles determining game over and pausing the time when the game is paused.
     */
    final Runnable timerThread = new Runnable()
    {
        public void run()
        {
            if (timer.timeUp() && !timer.isPaused())
            {
                gameOver();
            }
            else
            {
                handlerTime.postDelayed(timerThread, 0);

                if (!timer.isPaused())
                    viewTimer.setText(String.valueOf(timer.getSecondsRemaining()));
            }
        }
    };

    /**
     * Resumes game after Resume has been clicked from pause dialog.
     */
    @Override
    public void onDialogResumeClick(DialogFragment dialog)
    {
        dialog.dismiss();
        onResume();
    }

    /**
     * Opens Main Menu activity after Main Menu button has been clicked from pause dialog.
     */
    @Override
    public void onDialogMainMenuClick(DialogFragment dialog)
    {
        dialog.dismiss();
        goToMain();
    }

    /**
     * Adds submitted word to TableLayout
     */
    protected void displayWord()
    {
        TableLayout table = (TableLayout) findViewById(R.id.table_submitted_words);

        if (submittedWordRow > getResources().getInteger(R.integer.max_row_count) - 1)
            submittedWordRow = 0;

        TableRow row = (TableRow) table.getChildAt(submittedWordRow);

        // Row doesn't exist. Add it.
        if (row == null)
        {
            row = new TableRow(this);
            table.addView(row, submittedWordRow);
        }

        LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, getResources().getInteger(R.integer.row_margin), 0);
        TextView text = new TextView(this);
        text.setText(game.getCurrentWord());
        text.setTextColor(Color.BLACK);
        text.setLayoutParams(params);

        row.addView(text);

        table.invalidate();
        table.refreshDrawableState();

        submittedWordRow++;
    }

    protected void updateScoreDisplay()
    {
        viewScore.setText(String.valueOf(iScore));
    }

    /**
     * Updates letter bank for player based on submitted word.
     */
    protected void updateLetterBank()
    {
        int intLettersEarned = 0;
        // Check for corner-to-corner letters
        int numCorners = 0;
        for (int i = 0; i < alSelButtons.size(); i++)
        {
            LetterButton buttonTemp = alSelButtons.get(i);
            if (buttonTemp.isCorner())
                numCorners++;
        }
        if (numCorners >= 2)
            intLettersEarned++;

        // Check for word length
        int wordLength = game.getCurrentWord().length();
        if (wordLength == 6)
            intLettersEarned += 1;
        else if (wordLength == 7)
            intLettersEarned += 2;
        else if (wordLength >= 8)
            intLettersEarned += 3;

        // Check for difficult letter use
        char[] charArrWord = new char[wordLength];
        game.getCurrentWord().getChars(0, wordLength, charArrWord, 0);
        for (int i = 0; i < wordLength; i++)
        {
            char charTemp = charArrWord[i];
            if (charTemp == 'Q' || charTemp == 'X' || charTemp == 'Z')
                intLettersEarned++;
        }

        if (intLettersEarned > 0)
        {
            updateBank(intLettersEarned);
        }
    }

    protected void updateBank(int i)
    {
        // Update running letter total
        bank.updateCount(i);

        // If letters were earned, alert the user
        if (i > 0)
            showLetterToast(i);

        // Update bank display
        int iLetters = bank.getCount();
        viewBank.setText(String.valueOf(iLetters));

        // Enable/Disable powerups
        updatePowerupDisplays();
    }

    /**
     * Updates the enabled/disabled display for the poweurps based on the letter count
     * in the letter bank
     */
    private void updatePowerupDisplays()
    {
        int iLetters = bank.getCount();

        View viewRow = findViewById(R.id.powerup_layout);
        View viewTime = viewRow.findViewById(R.id.buttonPowerupTime);
        View viewSwap = viewRow.findViewById(R.id.buttonPowerupSwap);
        View viewVowel = viewRow.findViewById(R.id.buttonPowerupVowel);
        View viewShuffle = viewRow.findViewById(R.id.buttonPowerupShuffle);

        // Swap Powerup button
        if (getPowerupCost(R.id.buttonPowerupSwap) > iLetters)
            viewSwap.setBackgroundResource(R.drawable.button_powerup_swap_disabled);
        else
            viewSwap.setBackgroundResource(R.drawable.button_powerup_swap);

        // Time Powerup button
        if (getPowerupCost(R.id.buttonPowerupTime) > iLetters)
            viewTime.setBackgroundResource(R.drawable.button_powerup_time_disabled);
        else
            viewTime.setBackgroundResource(R.drawable.button_powerup_time);

        // Vowel Powerup button
        if (getPowerupCost(R.id.buttonPowerupVowel) > iLetters)
            viewVowel.setBackgroundResource(R.drawable.button_powerup_vowel_disabled);
        else
            viewVowel.setBackgroundResource(R.drawable.button_powerup_vowel);

        // Shuffle Powerup button
        if (getPowerupCost(R.id.buttonPowerupShuffle) > iLetters)
            viewShuffle.setBackgroundResource(R.drawable.button_powerup_shuffle_disabled);
        else
            viewShuffle.setBackgroundResource(R.drawable.button_powerup_shuffle);

    }

    protected int getPowerupCost(int viewId)
    {
        int intLetterCost = 1;

        switch (viewId)
        {
            case R.id.buttonPowerupShuffle:
                intLetterCost += 2;
                break;
            case R.id.buttonPowerupVowel:
                intLetterCost++;
                break;
        }

        return intLetterCost;
    }

    public void runSelectedPowerup(int cost, int viewId)
    {
        if (cost > bank.getCount())
        {
            showErrorToast(viewId);
            return;
        }

        switch (viewId)
        {
            case R.id.buttonPowerupSwap:
                doPowerupSwap();
                break;
            case R.id.buttonPowerupTime:
                doPowerupTime();
                break;
            case R.id.buttonPowerupShuffle:
                doPowerupShuffle();
                break;
            case R.id.buttonPowerupVowel:
                doPowerupVowel();
                break;
        }

        updateBank(-cost);
    }

    public void onPowerupClicked(View v)
    {
        int intViewId = v.getId();
        int intLetterCost = getPowerupCost(intViewId);
        runSelectedPowerup(intLetterCost, intViewId);
    }

    protected void doPowerupVowel()
    {
        FragmentManager fragmentManager = getFragmentManager();
        fragVowel = new VowelDialogFragment();
        fragVowel.show(fragmentManager, "dialog");
        timer.pause();
    }

    protected void doPowerupShuffle()
    {
        game.shuffleBoard();
        setLetterText();
        Button buttonPowerupShuffle = (Button) findViewById(R.id.buttonPowerupShuffle);
        buttonPowerupShuffle.setEnabled(false);
    }

    protected void doPowerupTime()
    {
        timer.addTime(getResources().getInteger(R.integer.powerup_time_increment));
        iPowerupTimeCounter += 1;
        if (iPowerupTimeCounter == 3)
        {
            Button buttonPowerupTime = (Button) findViewById(R.id.buttonPowerupTime);
            buttonPowerupTime.setEnabled(false);
        }
    }

    protected void doPowerupSwap()
    {
        timer.pause();
        Constants.showToast(getApplicationContext(), getResources().getString(R.string
                .powerup_swap_instructions));
        bSwapping = true;
    }

    public void endPowerupSwap(LetterButton dest, LetterButton src)
    {
        CharSequence charSeqTemp = dest.getText();
        dest.setText(src.getText());
        src.setText(charSeqTemp);
        bSwapping = false;
        timer.resume();
    }

    protected void showErrorToast(int intViewId)
    {
        String strErrorMessage = "";

        switch (intViewId)
        {
            case R.id.buttonPowerupTime:
                strErrorMessage = getResources().getString(R.string
                        .powerup_time_error_singleplayer);
                break;
            case R.id.buttonPowerupSwap:
                strErrorMessage = getResources().getString(R.string
                        .powerup_swap_error_singleplayer);
                break;
            case R.id.buttonPowerupShuffle:
                strErrorMessage = getResources().getString(R.string
                        .powerup_shuffle_error_singleplayer);
                break;
            case R.id.buttonPowerupVowel:
                strErrorMessage = getResources().getString(R.string
                        .powerup_vowel_error_singleplayer);
                break;
        }

        Constants.showToast(getApplicationContext(), strErrorMessage);
    }

    protected void showLetterToast(int intLettersEarned)
    {
        String strMessage = intLettersEarned + " " + getResources().getString(R.string
                .letters_earned);
        Constants.showToast(getApplicationContext(), strMessage);
    }

    public boolean isSwapping()
    {
        return bSwapping;
    }

    public boolean isPlacingVowel()
    {
        return csVowel != null;
    }

    public void swapTiles(LetterButton dest, LetterButton src)
    {
        int[] destLocation = dest.getLocation();
        int[] srcLocation = src.getLocation();

        if (destLocation == srcLocation)
            return;

        game.swapLetters(destLocation, srcLocation);
        setLetterText();
        timer.resume();
    }

    public void endPowerupVowel(LetterButton dest)
    {
        dest.setText(csVowel);
        csVowel = null;
        timer.resume();
    }

    public void onVowelCancel(View v)
    {
        fragVowel.dismiss();
    }

    public void onVowelClicked(View v)
    {
        fragVowel.dismiss();
        LetterButton button = (LetterButton) v;
        csVowel = button.getText();
        String strMessage = getResources().getString(R.string.choose_letter_to_swap_with_vowel) +
                " \'" + csVowel + "\'";
        Constants.showToast(getApplicationContext(), strMessage);
    }

    public VowelDialogFragment getVowelFragment()
    {
        return fragVowel;
    }

    public void goToMain()
    {
        Intent mainMenu = new Intent(this, MainMenuActivity.class);
        this.startActivity(mainMenu);
        this.finish();
    }

    @Override
    public void onSignInFailed()
    {
        bSignInSuccess = false;
    }

    @Override
    public void onSignInSucceeded()
    {
        bSignInSuccess = true;
    }

    private LetterButton getButtonAt(MotionEvent event)
    {
        float x = event.getRawX();
        float y = event.getRawY();
        int[] location = new int[2];

        for (int i = 0; i < game.getNumTilesVertical(); i++)
        {
            LinearLayout row = (LinearLayout) findViewById(ARRAY_ROW_REFERENCES[i]);

            for (int j = 0; j < game.getNumTilesHorizontal(); j++)
            {
                LetterButton tile = (LetterButton) row.findViewById(ARRAY_TILE_REFERENCES[game
                        .getNumTilesHorizontal() * i + j]);
                tile.getLocationOnScreen(location);

                if (location[0] <= x && x <= location[0] + tile.getWidth() && location[1] <= y &&
                        y <= location[1] + tile.getHeight())
                    return tile;
            }
        }

        return null;
    }

    private void resetWordAndButtons()
    {
        alSelButtons.clear();
        game.resetCurrentWord();
        bSpellDragging = false;
    }

    private void cleanUpMedia()
    {
        if (sounds != null)
        {
            sounds.release();
            sounds = null;
        }
    }

    private void initializeMedia()
    {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        sounds = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .build();
        soundGood = sounds.load(getApplicationContext(), R.raw.bing_1, 1);
        soundBad = sounds.load(getApplicationContext(), R.raw.bing_2, 1);
    }

    @Override
    public synchronized boolean onTouch(View view, MotionEvent event)
    {
        int action = event.getActionMasked();
        LetterButton temp = getButtonAt(event);

        try
        {
            switch (action)
            {
                case MotionEvent.ACTION_DOWN:
                    Log.d("GameActivity", "Down");
                    lbDown = temp;
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.d("GameActivity", "Move");

                    // Something weird happened
                    if (temp == null || lbDown == null)
                        break;

                    // Movement from LB lbDown to LB temp, click them both
                    if (lbDown != temp && alSelButtons.isEmpty())
                    {
                        bSpellDragging = true;
                        letterClicked(lbDown);
                        letterClicked(temp);
                    }

                    // Dragging has already begun. Click LB temp if it hasn't been clicked already.
                    else if (bSpellDragging && !alSelButtons.contains(temp) && temp
                            .draggedThroughMiddle(event))
                        letterClicked(temp);
                    break;

                case MotionEvent.ACTION_UP:
                    Log.d("GameActivity", "Up");
                    if (bSpellDragging)
                        submitWord();
                    else
                    {
                        // For some reason, word submission has to happen again when clicking
                        if (letterClicked(temp))
                            submitWord();
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    Log.d("GameActivity", "Cancel");
                    break;
            }
            if (temp != null)
            {
                Log.d("GameActivity", (String) temp.getText());
            }
        }
        catch (Exception e)
        {
            Log.d("GameActivity", "Error during Letter Button touch", e);
        }
        return true;
    }
}
