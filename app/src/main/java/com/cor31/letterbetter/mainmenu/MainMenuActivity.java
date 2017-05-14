package com.cor31.letterbetter.mainmenu;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.cor31.letterbetter.R;

/**
 * Created by bbadders on 8/9/2016.
 */
public class MainMenuActivity extends Activity implements MainMenuContract.View
{
    private MainMenuPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        //presenter = new MainMenuPresenterImpl(Injection.provideMainMenuPresenter(), this);
    }

    @Override
    public Context getApplicationContext() {
        return this.getApplicationContext();
    }
}
