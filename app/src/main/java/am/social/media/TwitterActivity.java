package am.social.media;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by affandymurad on 08/09/2015.
 */
public class TwitterActivity extends ActionBarActivity {
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.TwitterTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        saveAPItoSharePref();
        Fragment login = new LoginFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, login);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();

    }

    private void saveAPItoSharePref() {
        pref = getPreferences(0);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("CONSUMER_KEY", Config.API_KEY);
        edit.putString("CONSUMER_SECRET", Config.API_SECRET);
        edit.commit();
    }
}
