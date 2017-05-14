package com.cor31.letterbetter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.cor31.letterbetter.logic.Board;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer.LoadMatchResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

public class TurnBasedMultiPlayerGameActivity extends GameActivity implements
        ResultCallback<TurnBasedMultiplayer.LoadMatchResult>
{
    private String nextPlayerId, nextPlayerName, currentPlayerName;
    private String matchId;
    private MatchData md;
    private Board board;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Bundle bundle = getIntent().getExtras();
        matchId = bundle.getString(Constants.MATCH_ID);
        nextPlayerId = bundle.getString(Constants.NEXT_PLAYER_ID);
        nextPlayerName = bundle.getString(Constants.NEXT_PLAYER_NAME);
        currentPlayerName = bundle.getString(Constants.CURRENT_PLAYER_NAME, "");
        int bet = bundle.getInt(Constants.BET, 0);

        // If the next participant id is not p_2, we assume that there is a board loaded
        if (!nextPlayerId.equals(Constants.P2))
        {
            try
            {
                byte[] byteArrBoard = bundle.getByteArray(Constants.MATCH_DATA_STRING);
                ByteArrayInputStream bis = new ByteArrayInputStream(byteArrBoard);
                ObjectInputStream in = new ObjectInputStream(bis);
                Object o = in.readObject();
                md = (MatchData) o;
                board = md.getBoard();
            }
            catch (Exception e)
            {
                Log.e("MultiPlayerGame", "Error loading board.", e);
            }
        }
        else
        {
            board = new Board(getResources().getInteger(R.integer.num_tiles));
            md = new MatchData(board);
            md.setBet(bet);
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResult(LoadMatchResult result)
    {
        int statusCode = result.getStatus().getStatusCode();

        if (statusCode != GamesStatusCodes.STATUS_OK)
        {
            Constants.handleGooglePlayError(getApplicationContext(), statusCode);
            return;
        }

        TurnBasedMatch match = result.getMatch();
        nextPlayerId = match.getPendingParticipantId();
        Constants.getPlayerNameById(match, nextPlayerId);

        if (match.getData() != null)
            loadExistingBoard(match.getData());
    }

    @Override
    public void onPowerupClicked(View v)
    {
        int viewId = v.getId();
        int cost = getPowerupCost(viewId) + 1;
        runSelectedPowerup(cost, viewId);
    }

    private void loadExistingBoard(byte[] data)
    {
        String strData = new String(data);
        board = new Board(strData.split(","));
    }

    @Override
    protected void gameOver()
    {
        viewTimer.setText("0");
        Intent gameOver = new Intent(this, GameOverActivity.class);
        gameOver.putExtra(Constants.NEW_SCORE, iScore);
        gameOver.putExtra(Constants.GAMETYPE, Constants.GAMETYPE_MULTIPLAYER_TURN);
        gameOver.putExtra(Constants.NEXT_PLAYER_ID, nextPlayerId);
        gameOver.putExtra(Constants.NEXT_PLAYER_NAME, nextPlayerName);
        gameOver.putExtra(Constants.MATCH_ID, matchId);
        gameOver.putExtra(Constants.CURRENT_PLAYER_NAME, currentPlayerName);

        md.addPlayer(currentPlayerName, iScore);

        try
        {
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.registerTypeAdapter(MatchData.class, new MatchDataAdapter()).create();
            gameOver.putExtra(Constants.MATCH_DATA_STRING, gson.toJson(md));
        }
        catch (Exception e)
        {
            Log.e("MultiPlayerGame", "Error writing board", e);
        }
        this.startActivity(gameOver);
        this.finish();
    }
}