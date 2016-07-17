package am.social.media;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;


/**
 * Created by affandymurad on 08/09/2015.
 */
public class MainActivity extends ActionBarActivity {
    public Button FacebookButton;
    public Button TwitterButton;
    public Button GPLusButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FacebookButton = (Button)findViewById(R.id.button_facebook);
        TwitterButton = (Button)findViewById(R.id.button_twitter);
        GPLusButton = (Button)findViewById(R.id.button_gplus);

        FacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookan();
            }
        });

        TwitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                twitteran();
            }
        });

        GPLusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gplusan();
            }
        });
    }

    public void facebookan()
    {
        Intent fb = new Intent (MainActivity.this, FacebookActivity.class);
        startActivity(fb);
    }

    public void twitteran()
    {
        Intent twt = new Intent (MainActivity.this, TwitterActivity.class);
        startActivity(twt);
    }

    public void gplusan()
    {
        Intent gp = new Intent (MainActivity.this, GooglePlusActivity.class);
        startActivity(gp);
    }
}
