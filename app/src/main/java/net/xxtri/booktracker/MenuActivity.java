package net.xxtri.booktracker;

/**
 * Created by trinity on 9/29/15.
 *
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

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


public class MenuActivity extends AppCompatActivity implements View.OnClickListener, AsyncResponse {
    private String token;
    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";
    private String isbn;
    private String booktitle;
    private String addBookStatus;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        findViewById(R.id.readButton).setOnClickListener(this);
        findViewById(R.id.addButton).setOnClickListener(this);
        findViewById(R.id.gpsButton).setOnClickListener(this);
        findViewById(R.id.shareButton).setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        token = bundle.getString("token");


    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.readButton:
                Intent intent = new Intent(MenuActivity.this, BooksActivity.class);
                intent.putExtra("token", token);
                startActivity(intent);
                break;
            case R.id.gpsButton:
                Intent intent2 = new Intent(MenuActivity.this, MapsActivity.class);
                startActivity(intent2);
                break;
            case R.id.addButton:
                //Intent intent3 = new Intent(MenuActivity.this, AddBookActivity.class);
                //intent3.putExtra("token", token);
                //startActivity(intent3);
                //break;
                Intent intent3 = new Intent(this, BarcodeCaptureActivity.class);
                intent3.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                startActivityForResult(intent3, RC_BARCODE_CAPTURE);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    //statusMessage.setText(R.string.barcode_success);
                   // barcodeValue.setText(barcode.displayValue);
                    //findViewById(R.id.add_book).setVisibility(View.VISIBLE);
                    isbn = barcode.displayValue;
                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                    addBook();
                } else {
                    //statusMessage.setText(R.string.barcode_failure);
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                //statusMessage.setText(String.format(getString(R.string.barcode_error),
                     //   CommonStatusCodes.getStatusCodeString(resultCode)));
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

    @Override
    public void processFinish(List<Book> books) {

        AlertDialog alertDialog = new AlertDialog.Builder(MenuActivity.this).create();
        alertDialog.setTitle("Add Book Status");
        if(addBookStatus == "OK") {
            alertDialog.setMessage(booktitle + " has been added.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
        if (addBookStatus == "NO") {

            AlertDialog alertDialog2= new AlertDialog.Builder(MenuActivity.this).create();
            alertDialog2.setTitle("Add Book Status");
            alertDialog2.setMessage("No book could be found.Please scan a valid barcode and try again.");
            alertDialog2.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog2.show();
        }

    }


    public class CatalogClient extends AsyncTask<String, String, String> {

        public AsyncResponse delegate=null;
        private List<Book> books= new ArrayList<>();

        @Override
        protected String doInBackground(String... params) {
            URL url;
            HttpURLConnection urlConnection = null;

            addBookStatus = "NO";
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
                        conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                        conn.setRequestMethod("POST");
                        conn.setDoOutput(true);
                        conn.setRequestProperty("Authorization", "Bearer " + token);
                        Log.v("Post response", conn.getResponseCode() + "");
                        if(conn.getResponseCode() == 204){
                            addBookStatus = "OK";
                        }

                    }
                }else{
                    Log.v("CatalogClient", "Response code:"+ responseCode);
                    Log.v("CatalogClient", "Response message:"+ responseMessage);
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(urlConnection != null)
                    urlConnection.disconnect();
            }

            return addBookStatus;
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

                            booktitle = title;

                            Book book = new Book(title, picURL, isbn, 0, pageCount, id);
                            bookList.add(book);
                            //Log.v("bookList", "Title "+ title + "thumbnail "+ picURL + "isbn " + isbn + "pageCount " + pageCount);
                        }

                    }

                }

            } catch (JSONException e) {
                Log.e("CatalogClient", "unexpected JSON exception", e);
            }

            return bookList;
        }






        protected void onPostExecute(String status) {
            super.onPostExecute(status);
            if(books != null){
                delegate.processFinish(books);
            }
            Log.v("Add book status", status);

            // Log.v("", result.toString());
        }
    }


}







