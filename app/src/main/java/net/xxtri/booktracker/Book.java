package net.xxtri.booktracker;


/**
 * Created by trinity on 9/22/15.
 */
public class Book {
    private String title;
    private String picURL;
    private String isbn;
    private String id;
    private int pageCount;
    private int progress;




    public Book(String title, String picURL, String isbn, int progress, int pageCount, String id){
        this.title = title;
        this.picURL = picURL;
        this.isbn = isbn;
        this.progress = progress;
        this.pageCount = pageCount;
        this.id = id;

    }


    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}


    public String getPicURL() {return picURL;}
    public void setPicURL(String picURL) {this.picURL = picURL;}

    public String getIsbn() { return isbn;}
    public void setIsbn(String isbn) {this.isbn = isbn;}

    public int getProgress() {return progress;}
    public void setProgress(int progress) {this.progress = progress;}

    public int getPageCount() {return pageCount;}
    public void setPageCount(int pageCount) {this.pageCount = pageCount;}


    public String getId() { return id;}
    public void setId(String id) {this.id = id; }



}
