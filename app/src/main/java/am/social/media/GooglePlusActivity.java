package am.social.media;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.plus.Plus.PlusOptions;
import com.google.android.gms.plus.model.people.Person;

public class GooglePlusActivity extends ActionBarActivity implements OnClickListener,
        ConnectionCallbacks, OnConnectionFailedListener {

    private static final int PICK_MEDIA_REQUEST_CODE = 8;
    private static final int SHARE_MEDIA_REQUEST_CODE = 9;
    private static final int SIGN_IN_REQUEST_CODE = 10;
    private static final int ERROR_DIALOG_REQUEST_CODE = 11;

    // layout for showing user control buttons
    private LinearLayout userOptionsLayout;
    // layout for showing signed in user info
    private LinearLayout userInfoLayout;

    private ImageView userProfilePic;
    private TextView userName;
    private TextView userEmail;
    private TextView userLocation;
    private TextView userTagLine;
    private TextView userAboutMe;
    private TextView userBirthday;

    Button signOutButton;
    Button userInfoButton;
    Button sharePostButton;
    Button shareMediaButton;
    Button revokeAccessButton;

    // For communicating with Google APIs
    private GoogleApiClient mGoogleApiClient;
    private boolean mSignInClicked;
    private boolean mIntentInProgress;
    // contains all possible error codes for when a client fails to connect to
    // Google Play services
    private ConnectionResult mConnectionResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.GPlusTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gplus);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        signOutButton = (Button) findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(this);
        userInfoButton = (Button) findViewById(R.id.show_userinfo_button);
        userInfoButton.setOnClickListener(this);
        sharePostButton = (Button) findViewById(R.id.share_post_button);
        sharePostButton.setOnClickListener(this);
        shareMediaButton = (Button) findViewById(R.id.share_media_button);
        shareMediaButton.setOnClickListener(this);
        revokeAccessButton = (Button) findViewById(R.id.revoke_access_button);
        revokeAccessButton.setOnClickListener(this);
        findViewById(R.id.user_options_button).setOnClickListener(this);

        userOptionsLayout = (LinearLayout) findViewById(R.id.user_options_layout);
        userInfoLayout = (LinearLayout) findViewById(R.id.user_info_layout);

        userProfilePic = (ImageView) findViewById(R.id.user_profile_pic);
        userName = (TextView) findViewById(R.id.user_name);
        userEmail = (TextView) findViewById(R.id.user_email);
        userLocation = (TextView) findViewById(R.id.user_location);
        userTagLine = (TextView) findViewById(R.id.user_tagLine);
        userAboutMe = (TextView) findViewById(R.id.user_aboutme);
        userBirthday = (TextView) findViewById(R.id.user_birthday);

        // Initializing google plus api client
        mGoogleApiClient = buildGoogleAPIClient();
    }

    /**
     * API to return GoogleApiClient Make sure to create new after revoking
     * access or for first time sign in
     *
     * @return
     */
    private GoogleApiClient buildGoogleAPIClient() {
        return new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // make sure to initiate connection
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // disconnect api if it is connected
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    /**
     * Handle Button onCLick Events based upon their view ID
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.sign_in_button:
                processSignIn();
                break;
            case R.id.sign_out_button:
                processSignOut();
                break;
            case R.id.show_userinfo_button:
                userOptionsLayout.setVisibility(View.INVISIBLE);
                userInfoLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.share_post_button:
                processSharePost();
                break;
            case R.id.share_media_button:
                processShareMedia();
                break;
            case R.id.revoke_access_button:
                processRevokeRequest();
                break;
            case R.id.user_options_button:
                userInfoLayout.setVisibility(View.INVISIBLE);
                userOptionsLayout.setVisibility(View.VISIBLE);
                break;
        }

    }

    /**
     * API to update layout views based upon user signed in status
     *
     * @param isUserSignedIn
     */
    private void processUIUpdate(boolean isUserSignedIn) {
        if (isUserSignedIn) {
            signOutButton.setEnabled(true);
            userInfoButton.setEnabled(true);
            sharePostButton.setEnabled(true);
            shareMediaButton.setEnabled(true);
            revokeAccessButton.setEnabled(true);
        } else {
            signOutButton.setEnabled(false);
            userInfoButton.setEnabled(false);
            sharePostButton.setEnabled(false);
            shareMediaButton.setEnabled(false);
            revokeAccessButton.setEnabled(false);
        }
    }

    /**
     * Handle results for your startActivityForResult() calls. Use requestCode
     * to differentiate.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        } else if (requestCode == PICK_MEDIA_REQUEST_CODE) {
            // If picking media is success, create share post using
            // PlusShare.Builder
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                ContentResolver cr = this.getContentResolver();
                String mime = cr.getType(selectedImage);

                PlusShare.Builder share = new PlusShare.Builder(this);
                share.setText("Hello from AndroidSRC.net");
                share.addStream(selectedImage);
                share.setType(mime);
                startActivityForResult(share.getIntent(),
                        SHARE_MEDIA_REQUEST_CODE);
            }
        }
    }

    /**
     * API to revoke granted access After revoke user will be asked to grant
     * permission on next sign in
     */
    private void processRevokeRequest() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
                    .setResultCallback(new ResultCallback() {
                        @Override
                        public void onResult(Result result) {
                            Toast.makeText(getApplicationContext(),
                                    "User permissions revoked",
                                    Toast.LENGTH_LONG).show();
                            mGoogleApiClient = buildGoogleAPIClient();
                            mGoogleApiClient.connect();
                            processUIUpdate(false);
                        }


                    });

        }

    }

    /**
     * API to process media post request start activity with MIME type as video
     * and image
     */
    private void processShareMedia() {
        Intent photoPicker = new Intent(Intent.ACTION_PICK);
        photoPicker.setType("video/*, image/*");
        startActivityForResult(photoPicker, PICK_MEDIA_REQUEST_CODE);

    }

    /**
     * API to process post share request Use PlusShare.Builder to create share
     * post.
     */
    private void processSharePost() {
        // Launch the Google+ share dialog with attribution to your app.
        Intent shareIntent = new PlusShare.Builder(this).setType("text/plain")
                .setText("Google+ Demo http://androidsrc.net")
                .setContentUrl(Uri.parse("http://androidsrc.net")).getIntent();

        startActivityForResult(shareIntent, 0);

    }

    /**
     * API to handle sign out of user
     */
    private void processSignOut() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
            processUIUpdate(false);
        }

    }

    /**
     * API to handler sign in of user If error occurs while connecting process
     * it in processSignInError() api
     */
    private void processSignIn() {

        if (!mGoogleApiClient.isConnecting()) {
            processSignInError();
            mSignInClicked = true;
        }

    }

    /**
     * API to process sign in error Handle error based on ConnectionResult
     */
    private void processSignInError() {
        if (mConnectionResult != null && mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this,
                        SIGN_IN_REQUEST_CODE);
            } catch (SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    /**
     * Callback for GoogleApiClient connection failure
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
                    ERROR_DIALOG_REQUEST_CODE).show();
            return;
        }
        if (!mIntentInProgress) {
            mConnectionResult = result;

            if (mSignInClicked) {
                processSignInError();
            }
        }

    }

    /**
     * Callback for GoogleApiClient connection success
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        mSignInClicked = false;
        Toast.makeText(getApplicationContext(), "Signed In Successfully",
                Toast.LENGTH_LONG).show();

        processUserInfoAndUpdateUI();

        processUIUpdate(true);

    }

    /**
     * Callback for suspension of current connection
     */
    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();

    }

    /**
     * API to update signed in user information
     */
    private void processUserInfoAndUpdateUI() {
        Person signedInUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        if (signedInUser != null) {

            if (signedInUser.hasDisplayName()) {
                String userName = signedInUser.getDisplayName();
                this.userName.setText("Name: " + userName);
            }

            if (signedInUser.hasTagline()) {
                String tagLine = signedInUser.getTagline();
                this.userTagLine.setText("TagLine: " + tagLine);
                this.userTagLine.setVisibility(View.VISIBLE);
            }

            if (signedInUser.hasAboutMe()) {
                String aboutMe = signedInUser.getAboutMe();
                this.userAboutMe.setText("About Me: " + aboutMe);
                this.userAboutMe.setVisibility(View.VISIBLE);
            }

            if (signedInUser.hasBirthday()) {
                String birthday = signedInUser.getBirthday();
                this.userBirthday.setText("DOB " + birthday);
                this.userBirthday.setVisibility(View.VISIBLE);
            }

            if (signedInUser.hasCurrentLocation()) {
                String userLocation = signedInUser.getCurrentLocation();
                this.userLocation.setText("Location: " + userLocation);
                this.userLocation.setVisibility(View.VISIBLE);
            }

            String userEmail = Plus.AccountApi.getAccountName(mGoogleApiClient);
            this.userEmail.setText("Email: " + userEmail);

            if (signedInUser.hasImage()) {
                String userProfilePicUrl = signedInUser.getImage().getUrl();
                // default size is 50x50 in pixels.changes it to desired size
                int profilePicRequestSize = 250;

                userProfilePicUrl = userProfilePicUrl.substring(0,
                        userProfilePicUrl.length() - 2) + profilePicRequestSize;
                new UpdateProfilePicTask(userProfilePic)
                        .execute(userProfilePicUrl);
            }

        }
    }

    /**
     * Background task to download user profile picture
     *
     * @author androidsrc.net
     *
     */
    private class UpdateProfilePicTask extends AsyncTask<String, Void, Bitmap>{

        WeakReference profileView;

        public UpdateProfilePicTask(ImageView img) {
            profileView = new WeakReference(img);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap profilePic = null;
            try {
                URL downloadURL = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) downloadURL
                        .openConnection();
                int responseCode = conn.getResponseCode();
                if (responseCode != 200)
                    throw new Exception("Error in connection");
                InputStream is = conn.getInputStream();
                profilePic = BitmapFactory.decodeStream(is);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return profilePic;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // TODO Auto-generated method stub
            if (result != null && profileView != null) {
                ImageView view = (ImageView) profileView.get();
                if (view != null)
                    view.setImageBitmap(result);
            }
        }

    }
}