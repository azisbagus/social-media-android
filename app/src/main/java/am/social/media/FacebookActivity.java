package am.social.media;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;


public class FacebookActivity extends ActionBarActivity {
    private TextView info;
    private TextView pengguna;
    private LoginButton loginButton;
    private Button TimeLine;
    private CallbackManager callbackManager;
    private ProfilePictureView profilePictureView;

    // share button
    private ShareButton shareButton;
    ShareDialog shareDialog;
    //image



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.FacebookTheme);
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        FacebookSdk.setApplicationId(getResources().getString(R.string.facebook_app_id));
        setContentView(R.layout.activity_facebook);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        info = (TextView)findViewById(R.id.info);
        pengguna = (TextView)findViewById(R.id.pengguna);
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        loginButton = (LoginButton)findViewById(R.id.login_button);
        profilePictureView = (ProfilePictureView) findViewById(R.id.userImage);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                getFaceBookProfileDetails(loginResult.getAccessToken());
                info.setText(
                        "User ID: "
                                + loginResult.getAccessToken().getUserId()
                                + "\n" +
                                "Auth Token: "
                                + loginResult.getAccessToken().getToken()

                );


            }

            @Override
            public void onCancel() {
                info.setText("Login attempt canceled.");
            }

            @Override
            public void onError(FacebookException e) {
                info.setText("Login attempt failed.");
            }
        });

        //share button
        shareButton = (ShareButton) findViewById(R.id.share_btn);
        shareButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                postPicture();
            }
        });

        TimeLine = (Button) findViewById(R.id.story);
        TimeLine.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(FacebookActivity.this, FacebookStoryActivity.class);
                FacebookActivity.this.startActivity(myIntent);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void postPicture() {
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("Post to Facebook")
                    .setContentDescription(
                            "This is my Facebook Post")
                    .setContentUrl(Uri.parse("http://affandymurad.blogspot.com"))
                    .build();

            shareDialog.show(linkContent);
        }
    }

    private void getFaceBookProfileDetails(final AccessToken accessToken) {

        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {

                    @Override
                    public void onCompleted(final JSONObject object,GraphResponse response) {
                        JSONObject json = response.getJSONObject();
                        try {
                            Toast.makeText(getApplicationContext(), "User Name is: "+object.getString("name").toString(), Toast.LENGTH_LONG).show();
                            profilePictureView.setProfileId(object.optString("id"));
                            pengguna.setText(object.getString("name").toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        request.executeAsync();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
