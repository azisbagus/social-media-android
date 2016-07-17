package am.social.media;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

@TargetApi(11)
public class ProfileFragment extends Fragment {
	TextView prof_name;
    SharedPreferences pref;
    Bitmap bitmap;
    ImageView prof_img,tweet,signout,post_tweet;
    EditText tweet_text,txtsearch;
    ProgressDialog progress;
    Dialog tDialog;
    String tweetText,searchText;
    ArrayList<String> tweetTexts = new ArrayList();
    Button search;
    ListView list;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.profile_details, container, false);
        prof_name = (TextView)view.findViewById(R.id.prof_name);
        pref = getActivity().getPreferences(0);
        prof_img = (ImageView)view.findViewById(R.id.prof_image);
        tweet = (ImageView)view.findViewById(R.id.tweet);
        signout = (ImageView)view.findViewById(R.id.signout);
        signout.setOnClickListener(new SignOut());
        tweet.setOnClickListener(new Tweet());
        search =(Button)view.findViewById(R.id.search);
        txtsearch = (EditText)view.findViewById(R.id.txtsearch);
        list = (ListView)view.findViewById(R.id.list);
        search.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				new getTweets().execute();
			}
		});

		new LoadProfile().execute();
		return view;

    }
    
    
    private class SignOut implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			//  // TODO Auto-generated method stub
		      SharedPreferences.Editor edit = pref.edit();
	            edit.putString("ACCESS_TOKEN", "");
	            edit.putString("ACCESS_TOKEN_SECRET", "");
	            edit.commit();
	            Fragment login = new LoginFragment();
	            FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
	            ft.replace(R.id.content_frame, login);
	            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
	            ft.addToBackStack(null);
	            ft.commit();
	}
    }
      
	private class Tweet implements OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			tDialog = new Dialog(getActivity());
			tDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			tDialog.setContentView(R.layout.tweet_dialog);
			tweet_text = (EditText) tDialog.findViewById(R.id.tweet_text);
			post_tweet = (ImageView) tDialog.findViewById(R.id.post_tweet);
			post_tweet.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					new PostTweet().execute();
				}
			});
			tDialog.show();

		}

	}
		
		private class PostTweet extends AsyncTask<String, String, String>{
				@Override
			protected void onPostExecute(String res) {
				// TODO Auto-generated method stub
				if(res != null){
		               progress.dismiss();
		               Toast.makeText(getActivity(), "Tweet Sucessfully Posted", Toast.LENGTH_SHORT).show();
		               tDialog.dismiss();
		             }else{
		               progress.dismiss();
		                   Toast.makeText(getActivity(), "Error while tweeting !", Toast.LENGTH_SHORT).show();
		                   tDialog.dismiss();
		             }

			}

			@Override
			protected void onPreExecute() {
				// TODO Auto-generated method stub
				super.onPreExecute();
                progress = new ProgressDialog(getActivity());
                progress.setMessage("Posting tweet ...");
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.setIndeterminate(true);
                tweetText = tweet_text.getText().toString();
                progress.show();

			}

			@Override
			protected String doInBackground(String... arg0) {
				// TODO Auto-generated method stub
				 Twitter mTwitter = getTwitter();
	              try {
	          twitter4j.Status response = mTwitter.updateStatus(tweetText +" @techpapers ");
	          
	          return response.toString();
	        } catch (TwitterException e) {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
	        

			}
	              return null;
		}
		
			
			
		
		}
		
		
		
		
		private class getTweets extends AsyncTask<String, String, ArrayList<String>> {
			@Override
			protected void onPostExecute(ArrayList<String> result) {
				// TODO Auto-generated method stub
				super.onPostExecute(result);
				 progress.hide();
				 ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, result);
				 list.setAdapter(itemsAdapter);
				 Toast.makeText(getActivity(), "Tweet searched ", Toast.LENGTH_SHORT).show();
			}

			@Override
			protected void onPreExecute() {
				// TODO Auto-generated method stub
				super.onPreExecute();
				 progress = new ProgressDialog(getActivity());
	                progress.setMessage("Searching tweet ...");
	                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	                progress.setIndeterminate(true);
	                searchText = txtsearch.getText().toString();
	                progress.show();

			}

			@Override
			protected ArrayList<String> doInBackground(String... arg0) {
				// TODO Auto-generated method stub
				List<twitter4j.Status> tweets = new ArrayList();
				tweetTexts.clear();

				String userName ="7Langit";



				 Twitter mTwitter = getTwitter();
				 try {

					 Paging page = new Paging (1, 50);//page number, number per page
				 tweets = mTwitter.getUserTimeline(userName, page);
				 for (twitter4j.Status t : tweets) {

					 tweetTexts.add("@" + t.getUser().getScreenName() +"   ("+t.getUser().getName()+")"+"\n"+ t.getText() + "\n\n"+"Created at:"+t.getUser().getCreatedAt());
					 }
				 
				 
				 } catch (Exception e) {
				 tweetTexts.add("Twitter query failed: " + e.toString());
				 }
				 
				 return tweetTexts;
			}

	       
	    }

		private class LoadProfile extends AsyncTask<String, String, Bitmap>{
			@Override
			protected void onPostExecute(Bitmap image) {
				// TODO Auto-generated method stub
				Bitmap image_circle = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
				//prepare a paint with shader
	             BitmapShader shader = new BitmapShader (bitmap,  TileMode.CLAMP, TileMode.CLAMP);
	             Paint paint = new Paint();
	             paint.setShader(shader);
	             Canvas c = new Canvas(image_circle);
	             c.drawCircle(image.getWidth()/2, image.getHeight()/2, image.getWidth()/2, paint);
	               prof_img.setImageBitmap(image_circle);
	               prof_name.setText("Welcome " +pref.getString("NAME", ""));
	               progress.hide();
			}
			@Override
			protected void onPreExecute() {
				// TODO Auto-generated method stub
				   super.onPreExecute();
	                progress = new ProgressDialog(getActivity());
	                progress.setMessage("Loading Profile ...");
	                progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	                progress.setIndeterminate(true);
	                progress.show();

			}

			@Override
			protected Bitmap doInBackground(String... arg0) {
				// TODO Auto-generated method stub
				try {
					
					 bitmap = BitmapFactory.decodeStream((InputStream)new URL(pref.getString("IMAGE_URL", "")).getContent());
					
	            } catch (Exception e) {
	                  e.printStackTrace();
	            }
	          return bitmap;

			}
		}

		private Twitter getTwitter() {
			ConfigurationBuilder builder = new ConfigurationBuilder();
             builder.setOAuthConsumerKey(pref.getString("CONSUMER_KEY", ""));
             builder.setOAuthConsumerSecret(pref.getString("CONSUMER_SECRET", ""));
             AccessToken accessToken = new AccessToken(pref.getString("ACCESS_TOKEN", ""), pref.getString("ACCESS_TOKEN_SECRET", ""));
             Twitter mTwitter = new TwitterFactory(builder.build()).getInstance(accessToken);
			return mTwitter;
		}
		
		}
		
		
		
    
    


