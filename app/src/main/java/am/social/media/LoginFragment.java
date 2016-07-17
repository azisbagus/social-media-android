package am.social.media;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

@SuppressLint({ "NewApi", "SetJavaScriptEnabled" })
public class LoginFragment extends Fragment {

	ImageView login;
	Twitter twitter;
	RequestToken requestToken = null;
	AccessToken accessToken;
	String oauth_url, oauth_verifier, profile_url;
	Dialog auth_dialog;
	WebView web;
	SharedPreferences pref;
	ProgressDialog progress;
	Bitmap bitmap;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.login_fragment, container, false);
		login = (ImageView) view.findViewById(R.id.login);
		pref = getActivity().getPreferences(0);
		twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(pref.getString("CONSUMER_KEY", ""),pref.getString("CONSUMER_SECRET", ""));
		login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				if (pref.getString("ACCESS_TOKEN", "").isEmpty()) {
					new TokenGet().execute();

				} else

				{

					Fragment profile = new ProfileFragment();
					FragmentTransaction ft = getActivity().getFragmentManager()
							.beginTransaction();
					ft.replace(R.id.content_frame, profile);
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					ft.addToBackStack(null);
					ft.commit();
				}

			}
		});
		return view;

	}

	// async Task to get Request token then to get outh_url from Request token
	private class TokenGet extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			try {
				requestToken = twitter.getOAuthRequestToken();
				Log.d("twitter", "requesttoken" + requestToken);

				oauth_url = requestToken.getAuthorizationURL();

				Log.d("twitter", "oauth_url" + oauth_url);

			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
			return oauth_url;

		}

		@Override
		protected void onPostExecute(String oauth_url) {
			// TODO Auto-generated method stub
			if (oauth_url != null) {

				auth_dialog = new Dialog(getActivity());
				auth_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				auth_dialog.setContentView(R.layout.auth_dialog);
				web = (WebView) auth_dialog.findViewById(R.id.webv);
				web.getSettings().setJavaScriptEnabled(true);
				web.loadUrl(oauth_url);
				web.setWebViewClient(new WebViewClient() {
					boolean authComplete = false;

					@Override
					public void onPageStarted(WebView view, String url,
							Bitmap favicon) {
						super.onPageStarted(view, url, favicon);
					}

					@Override
					public void onPageFinished(WebView view, String url) {
						super.onPageFinished(view, url);
						if (url.contains("oauth_verifier")
								&& authComplete == false) {
							authComplete = true;
							Log.d("twitter", "url" + url);
							Uri uri = Uri.parse(url);
							oauth_verifier = uri
									.getQueryParameter("oauth_verifier");
							auth_dialog.dismiss();
							new AccessTokenGet().execute();
						} else if (url.contains("denied")) {
							auth_dialog.dismiss();
							Toast.makeText(getActivity(), " Permission Denied",
									Toast.LENGTH_SHORT).show();
						}
					}
				});
				auth_dialog.show();
				auth_dialog.setCancelable(true);
			} else {
				Toast.makeText(getActivity(),
						"Network Error or Invalid Credentials",
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	private class AccessTokenGet extends AsyncTask<String, String, Boolean> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			progress = new ProgressDialog(getActivity());
			progress.setMessage("Fetching Data ...");
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progress.setIndeterminate(true);
			progress.show();

		}

		@Override
		protected Boolean doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			try {
				accessToken = twitter.getOAuthAccessToken(requestToken,
						oauth_verifier);
				SharedPreferences.Editor edit = pref.edit();
				edit.putString("ACCESS_TOKEN", accessToken.getToken());
				edit.putString("ACCESS_TOKEN_SECRET",
						accessToken.getTokenSecret());
				User user = twitter.showUser(accessToken.getUserId());
				profile_url = user.getOriginalProfileImageURL();
				edit.putString("NAME", user.getName());
				edit.putString("IMAGE_URL", user.getOriginalProfileImageURL());
				edit.commit();
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			if (result) {
				progress.hide();
				Fragment profile = new ProfileFragment();
				FragmentTransaction ft = getActivity().getFragmentManager()
						.beginTransaction();
				ft.replace(R.id.content_frame, profile);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				ft.addToBackStack(null);
				ft.commit();
			}

		}

	}

}
