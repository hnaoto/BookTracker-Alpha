package net.xxtri.booktracker;





import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import com.google.android.gms.common.api.GoogleApiClient;

import net.xxtri.booktracker.list.BookListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;





public class BooksActivity extends AppCompatActivity implements View.OnClickListener,
                                            GoogleApiClient.ConnectionCallbacks, AsyncResponse{


    private String token;
    private RecyclerView rv;
    private TextView loading;
    private String status;
    AccountManager accountManager;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_books);
        Bundle bundle = getIntent().getExtras();
       //findViewById(R.id.menu_button).setOnClickListener(this);
        loading = (TextView) findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);

        token = bundle.getString("token");
        if (token != null) {
            Log.v("token", token);
            CatalogClient client = new CatalogClient();
           // List<Book> books = new ArrayList<>();
            client.delegate = this;
            client.execute("https://www.googleapis.com/books/v1/mylibrary/bookshelves/3/volumes?country=US");
            //client.delegate(this);

        }else{
            Intent intent = new Intent(this, MainActivity.class);
            startService(intent);
        }
    }


    public void processFinish(List<Book> books){
        //this you will received result fired from async class of onPostExecute(result) method.


        if (books!=null){

            rv=(RecyclerView)findViewById(R.id.rv);
            LinearLayoutManager llm = new LinearLayoutManager(this);
            rv.setLayoutManager(llm);
            rv.setHasFixedSize(true);
            loading.setVisibility(View.GONE);
            BookListAdapter adapter = new BookListAdapter(books, token);
            rv.setAdapter(adapter);


            /**
            LinearLayout linearLayout = new LinearLayout(this);
            setContentView(linearLayout);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            for( int i = 0; i < books.size(); i++ )
            {
                TextView title = new TextView(this);
                TextView isbn = new TextView(this);
                TextView pageCount = new TextView(this);
                ImageView img = new ImageView(this);
                title.setText(books.get(i).getTitle());
                isbn.setText(books.get(i).getIsbn());
                pageCount.setText(Double.toString(books.get(i).getPageCount()));
                linearLayout.addView(title);
                linearLayout.addView(isbn);
                linearLayout.addView(pageCount);
            }
             **/
        } else{
            //((TextView) findViewById(R.id.no_book)).setText("You have no books in this shelf");
            loading.setVisibility(View.VISIBLE);
            loading.setText("You have no books.");
        }

    }




    @Override
    public void onClick(View view) {
       // if (view.getId() == R.id.button_token) {
        //    ((TextView)findViewById(R.id.token_value)).setText(token);
       // }





    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {



    }



    public void onConnected(Bundle bundle) {
        // onConnected indicates that an account was selected on the device, that the selected
        // account has granted any requested permissions to our app and that we were able to
        // establish a service connection to Google Play services.




    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost. The GoogleApiClient will automatically
        // attempt to re-connect. Any UI elements that depend on connection to Google APIs should
        // be hidden or disabled until onConnected is called again.
        loading.setVisibility(View.VISIBLE);
        loading.setText("Google+ connection lost...");


    }






    public class CatalogClient extends AsyncTask<String, String, List<Book>> {

        public AsyncResponse delegate=null;



        @Override
        protected List<Book> doInBackground(String... params) {
            URL url;

            HttpURLConnection urlConnection = null;

            //JSONArray response = new JSONArray();
            List<Book> books= new ArrayList<>();


            try {

                url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Authorization", "Bearer "+ token);
                int responseCode = urlConnection.getResponseCode();
                String responseMessage = urlConnection.getResponseMessage();

                if(responseCode == HttpURLConnection.HTTP_OK){
                    String responseString = readStream(urlConnection.getInputStream());
                    Log.v("CatalogClient-rs", responseString);
                    //response = new JSONArray(responseString);
                    books = bookData(responseString);

                }else{
                    Log.v("CatalogClient", "Response code:"+ responseCode);
                    Log.v("CatalogClient", "Response:"+ responseMessage);

                    accountManager.invalidateAuthToken("com.google", token);

                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(urlConnection != null)
                    urlConnection.disconnect();
            }

            //Log.v("CatalogClient-rs", response.toString());
            return books;
        }

        private String readStream(InputStream in) {
            BufferedReader reader = null;
            StringBuffer response = new StringBuffer();
            try {
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return response.toString();
        }



        public int getReadingPosition(String urlString, String token){
            URL url;
            HttpURLConnection urlConnection = null;
            int progress = 0;

            try {
                url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Authorization", "Bearer "+ token);
                int responseCode = urlConnection.getResponseCode();
                String responseMessage = urlConnection.getResponseMessage();
                if(responseCode == HttpURLConnection.HTTP_OK){
                    String responseString = readStream(urlConnection.getInputStream());
                    Log.v("CatalogClient-rs", responseString);
                    progress = Integer.parseInt(positionData(responseString));
                }else if(responseCode == HttpURLConnection.HTTP_NOT_FOUND){
                    Log.v("CatalogClient", "Response code:"+ responseCode);
                    Log.v("CatalogClient", "Response:"+ responseMessage);
                    loading.setVisibility(View.VISIBLE);
                    loading.setText("Auth is expired. Please log in again.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(urlConnection != null)
                    urlConnection.disconnect();
            }
            return progress;

        }


        private String positionData(String jString) {
            String position = "0";
            try {
                JSONObject jObj = new JSONObject(jString);
                position = jObj.getString("gbImagePosition");
            } catch (JSONException e) {
                Log.e("CatalogClient", "unexpected JSON exception", e);
            }

            return position;
        }


        private List<Book> bookData(String jString){

            List<Book> bookList = new ArrayList<Book>();
            try {
                JSONObject jObj = new JSONObject(jString);
                String totalItems= jObj.getString("totalItems");
                Log.v("totalItems",totalItems);
                if (Integer.parseInt(totalItems) == 0) {
                   // ((TextView) findViewById(R.id.JSON_value)).setText("You have no books in this shelf");
                    return null;
                } else {
                    JSONArray items = jObj.getJSONArray("items");
                    if(items != null) {
                        for (int i = 0; i < items.length(); i++) {
                            String id = items.getJSONObject(i).getString("id");
                            String title = items.getJSONObject(i).getJSONObject("volumeInfo").getString("title");
                            String picURL = items.getJSONObject(i).getJSONObject("volumeInfo").getJSONObject("imageLinks").getString("thumbnail");
                            String isbn = items.getJSONObject(i).getJSONObject("volumeInfo").getJSONArray("industryIdentifiers").getJSONObject(1).getString("identifier");
                            int pageCount = 1000;
                            if (!items.getJSONObject(i).getJSONObject("volumeInfo").isNull("pageCount")) {
                                pageCount = items.getJSONObject(i).getJSONObject("volumeInfo").getInt("pageCount");
                            }

                            String getPositionUrl = "https://www.googleapis.com/books/v1/mylibrary/readingpositions/" + id + "?country=US";
                            int progress = getReadingPosition(getPositionUrl,token);

                            Book book = new Book(title, picURL, isbn, progress, pageCount, id);
                            bookList.add(book);
                            Log.v("bookList", "Title "+ title + "thumbnail "+ picURL + "isbn " + isbn + "pageCount " + pageCount);
                        }

                    }

                }

            } catch (JSONException e) {
                Log.e("CatalogClient", "unexpected JSON exception", e);
            }

            return bookList;
        }












        protected void onPostExecute(List<Book> books) {
            super.onPostExecute(books);
            if(books != null){
                delegate.processFinish(books);
            }
           // Log.v("", result.toString());


        }
    }





}