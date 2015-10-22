package net.xxtri.booktracker;

import java.util.List;

/**
 * Created by trinity on 9/23/15.
 */
public interface AsyncResponse {
    void processFinish(List<Book> books);
}
