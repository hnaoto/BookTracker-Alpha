package net.xxtri.booktracker.list;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import net.xxtri.booktracker.R;

public class CardViewActivity extends Activity {

    TextView title;
    TextView progress;
    ImageView bookThumbnail;
    TextView pageCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.cardview_activity);
        title = (TextView)findViewById(R.id.title);
        progress = (TextView)findViewById(R.id.progress);
        pageCount = (TextView)findViewById(R.id.page_count);
        bookThumbnail = (ImageView)findViewById(R.id.book_thumbnail);


        title.setText("Node JS in action");
        progress.setText("0");
        pageCount.setText("0");
        bookThumbnail.setImageResource(R.drawable.reddit_placeholder);




    }
}
