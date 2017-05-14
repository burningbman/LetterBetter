package com.cor31.letterbetter.mainmenu;

import android.content.Context;

/**
 * Created by bbadders on 8/9/2016.
 */
public class MainMenuContract
{
    public interface View {
        Context getApplicationContext();
    }

    public interface UserActionListener {
        void onResume();

        void onItemClicked(int position);

        void onDestroy();
    }
}
