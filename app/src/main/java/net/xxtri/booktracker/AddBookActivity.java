package net.xxtri.booktracker;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

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

/**
 * Main activity demonstrating how to pass extra parameters to an activity that
 * reads barcodes.
 */
public class AddBookActivity extends Activity implements View.OnClickListener, AsyncResponse{

    // use a compound button so either checkbox or switch widgets work.
    private CompoundButton autoFocus;
    private CompoundButton useFlash;
    private TextView statusMessage;
    private TextView barcodeValue;
    private String isbn;
    private String token;

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addbook);

        statusMessage = (TextView)findViewById(R.id.status_message);
        barcodeValue = (TextView)findViewById(R.id.barcode_value);

        autoFocus = (CompoundButton) findViewById(R.id.auto_focus);
        useFlash = (CompoundButton) findViewById(R.id.use_flash);

        findViewById(R.id.read_barcode).setOnClickListener(this);
        findViewById(R.id.add_book).setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        token = bundle.getString("token");
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.read_barcode:
                // launch barcode activity.
                Intent intent = new Intent(this, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus.isChecked());
                intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());
                startActivityForResult(intent, RC_BARCODE_CAPTURE);
                break;
            case R.id.add_book:
                Log.d("isbn", isbn);
                addBook();
                break;
        }

        /**
        if (v.getId() == R.id.read_barcode) {
            // launch barcode activity.
            Intent intent = new Intent(this, BarcodeCaptureActivity.class);
            intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus.isChecked());
            intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());

            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        }
        **/

    }

    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * <p/>
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.
     * <p/>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    statusMessage.setText(R.string.barcode_success);
                    barcodeValue.setText(barcode.displayValue);
                    findViewById(R.id.add_book).setVisibility(View.VISIBLE);
                    isbn = barcode.displayValue;
                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                } else {
                    statusMessage.setText(R.string.barcode_failure);
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    public void addBook(){
        Log.v("isbn",isbn);
        if(isbn!=null) {
            CatalogClient client = new CatalogClient();
            client.delegate = this;
            client.execute("https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn);
        }
    }


    public void processFinish(List<Book> books) {


    }



    public class CatalogClient extends AsyncTask<String, String, List<Book>> {

        public AsyncResponse delegate=null;


        @Override
        protected List<Book> doInBackground(String... params) {
            URL url;
            HttpURLConnection urlConnection = null;
            List<Book> books= new ArrayList<>();
            try {
                url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                int responseCode = urlConnection.getResponseCode();
                String responseMessage = urlConnection.getResponseMessage();
                if(responseCode == HttpURLConnection.HTTP_OK){
                    String responseString = readStream(urlConnection.getInputStream());
                    Log.v("CatalogClient-rs", responseString);
                    //response = new JSONArray(responseString);
                    books = bookData(responseString);
                    Log.v("book-id", books.get(0).getId());
                    if (books != null) {
                        url = new URL("https://www.googleapis.com/books/v1/mylibrary/bookshelves/3/addVolume?volumeId=" + books.get(0).getId() + "&key=AIzaSyD_gWCV3d-lGINcjP1z0sAToydIiyVrUcI" + "?Country=US");
                        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                        Log.v("post", url.toString());
                        Log.v("token", token);
                        conn.setRequestProperty( "Content-type", "application/x-www-form-urlencoded");
                        conn.setRequestMethod("POST");
                        conn.setDoOutput(true);
                        conn.setRequestProperty("Authorization", "Bearer "+ token);
                        Log.v("Post response", conn.getResponseMessage().toString());

                    }
                }else{
                    Log.v("CatalogClient", "Response code:"+ responseCode);
                    Log.v("CatalogClient", "Response message:"+ responseMessage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(urlConnection != null)
                    urlConnection.disconnect();
            }

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




        private List<Book> bookData(String jString){

            List<Book> bookList = new ArrayList<Book>();
            try {
                JSONObject jObj = new JSONObject(jString);
                String totalItems= jObj.getString("totalItems");
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



                            Book book = new Book(title, picURL, isbn, 0, pageCount, id);
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
