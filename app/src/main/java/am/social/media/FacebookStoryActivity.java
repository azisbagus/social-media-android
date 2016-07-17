package am.social.media;


import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.webkit.WebView;


public class FacebookStoryActivity extends ActionBarActivity {
    private WebView info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.FacebookTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timeline_facebook);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        info = (WebView) findViewById(R.id.web);
        info.loadUrl("https://m.facebook.com/7langit");

    }
}
