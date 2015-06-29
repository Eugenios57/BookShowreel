package app.android.eug.bookshowreel;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;


public class BookSearchMain extends ActionBarActivity {

    // components
    GridView imageGridView;
    SearchView bookSearchView;
    BookSearchMain bookSearchInstance = this;

    // basic query string
    // -- basically we append the input from the search bar here.
    String basicQueryString = "https://www.googleapis.com/books/v1/volumes?q=";
    String myKey = "";
    String actualKey = "&key=" + myKey;

    // Query bitmaps array
    Bitmap[] thumbnails = new Bitmap[10];

    String[] bookText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TAG", "super nice");
        setContentView(R.layout.activity_book_search_main);


        // bind the search view with the corresponding UI element
        bookSearchView = (SearchView)findViewById(R.id.bookSearch);
        bookSearchView.setQueryHint("Search Books");


        // set the async search listener
        bookSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // we only require to perform an action when we actually submit.
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(getBaseContext(), query, Toast.LENGTH_SHORT).show();

                return false;
            }

            // normally a catching scheme would be employed here to provide "hints".
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }


    private class FetchBookThumb extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                URL thumbnailURI = new URL(params[0]);
                URLConnection conn = thumbnailURI.openConnection();
                conn.connect();
                InputStream inStream = conn.getInputStream();
                BufferedInputStream bufInStream = new BufferedInputStream(inStream);

                thumbnails[Integer.parseInt(params[1])] = BitmapFactory.decodeStream(bufInStream);

                bufInStream.close();
                inStream.close();
            } catch(Exception e) {
                Log.d("TAG_EXCEPTION", e.toString());
            }
            return "";
        }

        protected void onPostExecute(String result) {
            //TODO show the result
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_book_search_main, menu);
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
