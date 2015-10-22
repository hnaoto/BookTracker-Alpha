package net.xxtri.booktracker.list;

import net.xxtri.booktracker.Book;
import net.xxtri.booktracker.BooksActivity;
import net.xxtri.booktracker.R;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import android.app.AlertDialog;



public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookViewHolder>  {



    public static class BookViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        String m_Text = "";
        String token = "";
        String id = "";
        CardView cv;
        TextView title;
        TextView progress;
        TextView pageCount;
        ImageView bookThumbnail;
        ImageButton menuButton;
        int pageNumber;



        BookViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            title = (TextView)itemView.findViewById(R.id.title);
            progress = (TextView)itemView.findViewById(R.id.progress);
            bookThumbnail = (ImageView)itemView.findViewById(R.id.book_thumbnail);
            pageCount = (TextView)itemView.findViewById(R.id.page_count);
            menuButton = (ImageButton)itemView.findViewById(R.id.menu_button);
            menuButton.setOnClickListener(this);
        }

        @Override
        public void onClick(final View v) {

            Log.d("click", getAdapterPosition() + "onClick ");
            PopupMenu popup = new PopupMenu(v.getContext(), menuButton);
            //Inflating the Popup using xml file
            popup.getMenuInflater().inflate(R.menu.pop_menu, popup.getMenu());
            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getTitle().toString()) {
                        case "Record Progress":
                            recordProgress(v.getContext());
                            //Log.v("id", id);
                            break;
                        case "Delete":
                            break;
                    }
                    /**
                     Toast.makeText(v.getContext(),
                     "You Clicked : " + item.getTitle(),
                     Toast.LENGTH_SHORT
                     ).show();
                     **/
                    return true;
                }
            });
            //show the popup menu
            popup.show();

        }


        public void recordProgress(Context mConext) {


            AlertDialog.Builder builder = new AlertDialog.Builder(mConext);
            builder.setTitle("Input the page number please.");
            // Set up the input
            final EditText input = new EditText(mConext);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);




            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    m_Text = input.getText().toString();
                    if (m_Text != "" && Integer.parseInt(m_Text) <=  Integer.parseInt(pageCount.getText().toString())) {
                        String url = "https://www.googleapis.com/books/v1/mylibrary/readingpositions/" + id +
                                "/setPosition?position=" + m_Text +
                                "&timestamp=2015-12-01T09:30:16.768-04:00&country=US";
                        CatalogClient client = new CatalogClient();
                        client.execute(url);
                    }

                    if (Integer.parseInt(m_Text) > pageNumber) {
                        m_Text = pageNumber + "";
                    }

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }



        public class CatalogClient extends AsyncTask<String, String, String> {
            @Override
            protected String doInBackground(String... params) {
                URL url;
                HttpURLConnection urlConnection = null;
                String status = "";

                try {
                    url = new URL(params[0]);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Authorization", "Bearer "+ token);
                    urlConnection.setRequestProperty( "Content-type", "application/x-www-form-urlencoded");
                    urlConnection.setDoOutput(true);
                    int responseCode = urlConnection.getResponseCode();
                    String responseMessage = urlConnection.getResponseMessage();
                    if(responseCode == HttpURLConnection.HTTP_OK){
                        status = "OK";
                    }else {
                        Log.v("CatalogClient", "Response code:"+ responseCode);
                        Log.v("CatalogClient", "Response:"+ responseMessage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if(urlConnection != null)
                        urlConnection.disconnect();
                }
                return status;
            }



            private void updateUI(boolean isSignedIn) {


            }

            @Override
            protected void onPostExecute(String status) {
                super.onPostExecute(status);
                if(status != null){
                    progress.setText(String.valueOf(m_Text + "/"));
                }
                // Log.v("", result.toString());

            }





        }





    }






    List<Book> books;
    String token = "";
    public BookListAdapter(List<Book> books, String token){
        this.books = books;
        this.token = token;
       // this.mContext = mContext;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public BookViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.book, viewGroup, false);
        BookViewHolder pvh = new BookViewHolder(v);
        return pvh;
    }





    @Override
    public void onBindViewHolder(BookViewHolder BookViewHolder, int i) {
        BookViewHolder.title.setText(books.get(i).getTitle());
        BookViewHolder.progress.setText(String.valueOf(books.get(i).getProgress() + "/"));
        BookViewHolder.pageCount.setText(String.valueOf(books.get(i).getPageCount()));
        BookViewHolder.token = token;
        BookViewHolder.id = books.get(i).getId();
        BookViewHolder.pageNumber = books.get(i).getPageCount();


        //BookViewHolder.bookThumbnail.setImageResource(books.get(i).);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }




}
