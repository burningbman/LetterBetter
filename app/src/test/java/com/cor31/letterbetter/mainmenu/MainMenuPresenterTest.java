package com.cor31.letterbetter.mainmenu;

import com.cor31.letterbetter.data.MainMenuRepository;
import com.cor31.letterbetter.mainmenu.MainMenuContract;
import com.cor31.letterbetter.mainmenu.MainMenuPresenter;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

/**
 * Created by bbadders on 8/26/2016.
 */
public class MainMenuPresenterTest
{
    private MainMenuContract.View mMainMenuView;
    private MainMenuRepository mMainMenuRepository;
    private MainMenuPresenter mMainMenuPresenter;

    @Before
    public void setupNotesPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mMainMenuPresenter = new MainMenuPresenter(mMainMenuRepository, mMainMenuView);
    }

    @Test
    public void testInstantiation() {
        assertNotNull(mMainMenuPresenter.bank);
        fail("Not implemented.");
    }
}
