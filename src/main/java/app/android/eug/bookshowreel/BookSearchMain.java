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

                new FetchBookInfo().execute(basicQueryString + query + actualKey);

                return false;
            }

            // normally a catching scheme would be employed here to provide "hints".
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        if(savedInstanceState != null) {
            thumbnails = (Bitmap[])savedInstanceState.getParcelableArray("thumbnails");
            bookText = savedInstanceState.getStringArray("entryText");
            // redraw it
            CustomImageGrid adapter = new CustomImageGrid(BookSearchMain.this, bookText, thumbnails);
            imageGridView =(GridView)findViewById(R.id.grid);
            imageGridView.setAdapter(adapter);
            imageGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Toast.makeText(BookSearchMain.this, "You Clicked at " + bookText[+position], Toast.LENGTH_SHORT).show();


                }
            });
        }

    }

    private class FetchBookInfo extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            StringBuilder strBuilder = new StringBuilder();
            for(String bookItem : params) {
                HttpClient fetchClient = new DefaultHttpClient();
                try {
                    HttpGet getBook = new HttpGet(bookItem);
                    HttpResponse bookFetchState = fetchClient.execute(getBook);
                    StatusLine bookFetchStatus = bookFetchState.getStatusLine();

                    if(bookFetchStatus.getStatusCode() == 200 ) { /* HTTP OK */
                        HttpEntity bookEntity = bookFetchState.getEntity();
                        InputStream bookContent = bookEntity.getContent();
                        InputStreamReader bookInput = new InputStreamReader(bookContent);
                        BufferedReader bookReader = new BufferedReader(bookInput);
                        String lineIn;
                        while ((lineIn=bookReader.readLine())!=null) {
                            strBuilder.append(lineIn);
                        }
                    }

                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            String s = strBuilder.toString();
            return strBuilder.toString();
        }

        protected void onPostExecute(String result) {
            try {
                JSONObject resultObject = new JSONObject(result);
                JSONArray bookArray = resultObject.getJSONArray("items");
                JSONObject bookObject;// = bookArray.getJSONObject(0);
                JSONObject volumeObject;// = bookObject.getJSONObject("volumeInfo");


                //Log.d("SEARCH_RESULT", "items fetched: " + bookArray.length());
                Toast.makeText(BookSearchMain.this, "Items fetched: " + bookArray.length(), Toast.LENGTH_SHORT).show();

                //int len = bookArray.length() > 2 ? 2 : bookArray.length();
                int len = bookArray.length();
                // allocate resources
                bookText = new String[len];

                for(int i = 0; i < len; i++) {
                    bookObject = bookArray.getJSONObject(i);
                    volumeObject = bookObject.getJSONObject("volumeInfo");
                    StringBuilder entryText = new StringBuilder();
                    // now build the text to display

                    // title
                    entryText.append("TITLE: " + volumeObject.getString("title") + "\n");
                    // authors
                    try {
                        StringBuilder authorStringBuilder = new StringBuilder();
                        JSONArray bookAuthors = volumeObject.getJSONArray("authors");
                        for(int j = 0; j < bookAuthors.length(); j++) {
                            if(j > 0) {authorStringBuilder.append(", ");}
                            authorStringBuilder.append(bookAuthors.getString(j));
                        }

                        // finally set the text
                        entryText.append("\nBook Authors: " + authorStringBuilder.toString());
                    } catch (Exception e) {e.printStackTrace();}

                    // publication date
                    try{entryText.append("\nPUBLISHED: " + volumeObject.getString("publishedDate"));}
                    catch(JSONException jse) {jse.printStackTrace();}

                    // description
                    try{entryText.append("\nDESCRIPTION: " + volumeObject.getString("description"));}
                    catch(JSONException jse) {jse.printStackTrace();}

                    // get the thumbnails
                    try{
                        JSONObject jsonImageInfo = volumeObject.getJSONObject("imageLinks");
                        FetchBookThumb imgThumb = new FetchBookThumb();
                        imgThumb.execute(jsonImageInfo.getString("smallThumbnail"), Integer.toString(i));
                        imgThumb.get(500, TimeUnit.MILLISECONDS);
                    } catch(JSONException jse) {jse.printStackTrace();}

                    // finally add the string.
                    bookText[i] = entryText.toString();
                }


                CustomImageGrid adapter = new CustomImageGrid(BookSearchMain.this, bookText, thumbnails);
                imageGridView =(GridView)findViewById(R.id.grid);
                imageGridView.setAdapter(adapter);
                imageGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Toast.makeText(BookSearchMain.this, "You Clicked at " + bookText[+position], Toast.LENGTH_SHORT).show();


                    }
                });

            } catch(Exception e) {
                e.printStackTrace();
            }
        }
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

    protected void saveInstanceState(Bundle saveBundle) {
        saveBundle.putStringArray("entryText", bookText);
        saveBundle.putParcelableArray("thumbnails", thumbnails);
    }
}
