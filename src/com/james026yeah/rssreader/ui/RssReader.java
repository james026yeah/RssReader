package com.james026yeah.rssreader.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.james026yeah.rssreader.R;
import com.james026yeah.rssreader.RssItem;
import com.james026yeah.rssreader.R.id;
import com.james026yeah.rssreader.R.layout;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TwoLineListItem;

public class RssReader extends ListActivity {
    private RSSListAdapter mAdapter;
    private EditText mUrlText;
    private TextView mStatusText;
    private Handler mHandler;
    private RSSWorker mWorker;
    public static final int SNIPPET_LENGTH = 90;
    
    public static final String STRINGS_KEY = "strings";
    public static final String SELECTION_KEY = "selection";
    public static final String URL_KEY = "url";
    public static final String STATUS_KEY = "status";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.rss_layout);

        List<RssItem> items = new ArrayList<RssItem>();
        mAdapter = new RSSListAdapter(this, items);
        getListView().setAdapter(mAdapter);

        // Get pointers to the UI elements in the rss_layout
        mUrlText = (EditText)findViewById(R.id.urltext);
        mStatusText = (TextView)findViewById(R.id.statustext);
        
        Button download = (Button)findViewById(R.id.download);
        download.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doRSS(mUrlText.getText());
            }
        });

        // Need one of these to post things back to the UI thread.
        mHandler = new Handler();
        
        // NOTE: this could use the icicle as done in
        // onRestoreInstanceState().
    }

    /**
     * ArrayAdapter encapsulates a java.util.List of T, for presentation in a
     * ListView. This subclass specializes it to hold RssItems and display
     * their title/description data in a TwoLineListItem.
     */
    private class RSSListAdapter extends ArrayAdapter<RssItem> {
        private LayoutInflater mInflater;

        public RSSListAdapter(Context context, List<RssItem> objects) {
            super(context, 0, objects);

            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        /**
         * This is called to render a particular item for the on screen list.
         * Uses an off-the-shelf TwoLineListItem view, which contains text1 and
         * text2 TextViews. We pull data from the RssItem and set it into the
         * view. The convertView is the view from a previous getView(), so
         * we can re-use it.
         * 
         * @see ArrayAdapter#getView
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TwoLineListItem view;

            // Here view may be passed in for re-use, or we make a new one.
            if (convertView == null) {
                view = (TwoLineListItem) mInflater.inflate(android.R.layout.simple_list_item_2,
                        null);
            } else {
                view = (TwoLineListItem) convertView;
            }

            RssItem item = this.getItem(position);

            // Set the item title and description into the view.
            // This example does not render real HTML, so as a hack to make
            // the description look better, we strip out the
            // tags and take just the first SNIPPET_LENGTH chars.
            view.getText1().setText(item.getTitle());
            String descr = item.getDescription().toString();
            descr = removeTags(descr);
            view.getText2().setText(descr.substring(0, Math.min(descr.length(), SNIPPET_LENGTH)));
            return view;
        }

    }

    /**
     * Simple code to strip out <tag>s -- primitive way to sortof display HTML as
     * plain text.
     */
    public String removeTags(String str) {
        str = str.replaceAll("<.*?>", " ");
        str = str.replaceAll("\\s+", " ");
        return str;
    }

    /**
     * Called when user clicks an item in the list. Starts an activity to
     * open the url for that item.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        RssItem item = mAdapter.getItem(position);

        // Creates and starts an intent to open the item.link url.
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getLink().toString()));
        Intent intent = new Intent(getApplicationContext(), ReadingActivity.class);
        intent.putExtra("url", item.getLink());
        startActivity(intent);
    }

    /**
     * Resets the output UI -- list and status text empty.
     */
    public void resetUI() {
        // Reset the list to be empty.
        List<RssItem> items = new ArrayList<RssItem>();
        mAdapter = new RSSListAdapter(this, items);
        getListView().setAdapter(mAdapter);

        mStatusText.setText("");
        mUrlText.requestFocus();
    }

    /**
     * Sets the currently active running worker. Interrupts any earlier worker,
     * so we only have one at a time.
     * 
     * @param worker the new worker
     */
    public synchronized void setCurrentWorker(RSSWorker worker) {
        if (mWorker != null) mWorker.interrupt();
        mWorker = worker;
    }

    /**
     * Is the given worker the currently active one.
     * 
     * @param worker
     * @return
     */
    public synchronized boolean isCurrentWorker(RSSWorker worker) {
        return (mWorker == worker);
    }

    /**
     * Given an rss url string, starts the rss-download-thread going.
     * 
     * @param rssUrl
     */
    private void doRSS(CharSequence rssUrl) {
        RSSWorker worker = new RSSWorker(rssUrl);
        setCurrentWorker(worker);

        resetUI();
        mStatusText.setText("Downloading\u2026");

        worker.start();
    }

    /**
     * Runnable that the worker thread uses to post RssItems to the
     * UI via mHandler.post
     */
    private class ItemAdder implements Runnable {
        RssItem mItem;

        ItemAdder(RssItem item) {
            mItem = item;
        }

        public void run() {
            mAdapter.add(mItem);
        }

        // NOTE: Performance idea -- would be more efficient to have he option
        // to add multiple items at once, so you get less "update storm" in the UI
        // compared to adding things one at a time.
    }

    /**
     * Worker thread takes in an rss url string, downloads its data, parses
     * out the rss items, and communicates them back to the UI as they are read.
     */
    private class RSSWorker extends Thread {
        private CharSequence mUrl;

        public RSSWorker(CharSequence url) {
            mUrl = url;
        }

        @Override
        public void run() {
            String status = "";
            try {
                // Standard code to make an HTTP connection.
                URL url = new URL(mUrl.toString());
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(10000);

                connection.connect();
                InputStream in = connection.getInputStream();

                parseRSS(in, mAdapter);
                status = "done";
            } catch (Exception e) {
                status = "failed:" + e.getMessage();
            }

            // Send status to UI (unless a newer worker has started)
            // To communicate back to the UI from a worker thread,
            // pass a Runnable to handler.post().
            final String temp = status;
            if (isCurrentWorker(this)) {
                mHandler.post(new Runnable() {
                    public void run() {
                        mStatusText.setText(temp);
                    }
                });
            }
        }
    }

    /**
     * Populates the menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, 0, 0, "新闻要闻")
            .setOnMenuItemClickListener(new RSSMenu("http://rss.sina.com.cn/news/marquee/ddt.xml"));

        menu.add(0, 0, 0, "真情时刻")
            .setOnMenuItemClickListener(new RSSMenu("http://rss.sina.com.cn/news/society/feeling15.xml"));
        
        menu.add(0, 0, 0, "News.com")
            .setOnMenuItemClickListener(new RSSMenu("http://news.com.com/2547-1_3-0-20.xml"));

        menu.add(0, 0, 0, "Bad Url")
            .setOnMenuItemClickListener(new RSSMenu("http://nifty.stanford.edu:8080"));

        menu.add(0, 0, 0, "Reset")
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                resetUI();
                return true;
            }
        });

        return true;
    }
    
    private class RSSMenu implements MenuItem.OnMenuItemClickListener {
        private CharSequence mUrl;

        RSSMenu(CharSequence url) {
            mUrl = url;
        }

        public boolean onMenuItemClick(MenuItem item) {
            mUrlText.setText(mUrl);
            mUrlText.requestFocus();
            return true;
        }
    }


    /**
     * Called for us to save out our current state before we are paused,
     * such a for example if the user switches to another app and memory
     * gets scarce. The given outState is a Bundle to which we can save
     * objects, such as Strings, Integers or lists of Strings. In this case, we
     * save out the list of currently downloaded rss data, (so we don't have to
     * re-do all the networking just because the user goes back and forth
     * between aps) which item is currently selected, and the data for the text views.
     * In onRestoreInstanceState() we look at the map to reconstruct the run-state of the
     * application, so returning to the activity looks seamlessly correct.
     * TODO: the Activity javadoc should give more detail about what sort of
     * data can go in the outState map.
     * 
     * @see android.app.Activity#onSaveInstanceState
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        int count = mAdapter.getCount();

        ArrayList<CharSequence> strings = new ArrayList<CharSequence>();
        for (int i = 0; i < count; i++) {
            RssItem item = mAdapter.getItem(i);
            strings.add(item.getTitle());
            strings.add(item.getLink());
            strings.add(item.getDescription());
        }
        outState.putSerializable(STRINGS_KEY, strings);

        if (getListView().hasFocus()) {
            outState.putInt(SELECTION_KEY, Integer.valueOf(getListView().getSelectedItemPosition()));
        }

        outState.putString(URL_KEY, mUrlText.getText().toString());
        
        outState.putCharSequence(STATUS_KEY, mStatusText.getText());
    }

    /**
     * Called to "thaw" re-animate the app from a previous onSaveInstanceState().
     * 
     * @see android.app.Activity#onRestoreInstanceState
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

        // Note: null is a legal value for onRestoreInstanceState.
        if (state == null) return;

        // Restore items from the big list of CharSequence objects
        List<CharSequence> strings = (ArrayList<CharSequence>)state.getSerializable(STRINGS_KEY);
        List<RssItem> items = new ArrayList<RssItem>();
        for (int i = 0; i < strings.size(); i += 3) {
            items.add(new RssItem(strings.get(i), strings.get(i + 1), strings.get(i + 2)));
        }

        // Reset the list view to show this data.
        mAdapter = new RSSListAdapter(this, items);
        getListView().setAdapter(mAdapter);

        // Restore selection
        if (state.containsKey(SELECTION_KEY)) {
            getListView().requestFocus(View.FOCUS_FORWARD);
            // todo: is above right? needed it to work
            getListView().setSelection(state.getInt(SELECTION_KEY));
        }
        
        // Restore url
        mUrlText.setText(state.getCharSequence(URL_KEY));
        
        // Restore status
        mStatusText.setText(state.getCharSequence(STATUS_KEY));
    }

    
    
    void parseRSS(InputStream in, RSSListAdapter adapter) throws IOException,
            XmlPullParserException {
        // TODO: switch to sax

        XmlPullParser xpp = Xml.newPullParser();
        xpp.setInput(in, null);  // null = default to UTF-8

        int eventType;
        String title = "";
        String link = "";
        String description = "";
        eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String tag = xpp.getName();
                if (tag.equals("item")) {
                    title = link = description = "";
                } else if (tag.equals("title")) {
                    xpp.next(); // Skip to next element -- assume text is directly inside the tag
                    title = xpp.getText();
                } else if (tag.equals("link")) {
                    xpp.next();
                    link = xpp.getText();
                } else if (tag.equals("description")) {
                    xpp.next();
                    description = xpp.getText();
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                // We have a comlete item -- post it back to the UI
                // using the mHandler (necessary because we are not
                // running on the UI thread).
                String tag = xpp.getName();
                if (tag.equals("item")) {
                    RssItem item = new RssItem(title, link, description);
                    mHandler.post(new ItemAdder(item));
                }
            }
            eventType = xpp.next();
        }
    }
    
    // SAX version of the code to do the parsing.
    /*
    private class RSSHandler extends DefaultHandler {
        RSSListAdapter mAdapter;
        
        String mTitle;
        String mLink;
        String mDescription;
        
        StringBuilder mBuff;
        
        boolean mInItem;
        
        public RSSHandler(RSSListAdapter adapter) {
            mAdapter = adapter;
            mInItem = false;
            mBuff = new StringBuilder();
        }
        
        public void startElement(String uri,
                String localName,
                String qName,
                Attributes atts)
                throws SAXException {
            String tag = localName;
            if (tag.equals("")) tag = qName;
            
            // If inside <item>, clear out buff on each tag start
            if (mInItem) {
                mBuff.delete(0, mBuff.length());
            }
            
            if (tag.equals("item")) {
                mTitle = mLink = mDescription = "";
                mInItem = true;
            }
        }
        
        public void characters(char[] ch,
                      int start,
                      int length)
                      throws SAXException {
            // Buffer up all the chars when inside <item>
            if (mInItem) mBuff.append(ch, start, length);
        }
                      
        public void endElement(String uri,
                      String localName,
                      String qName)
                      throws SAXException {
            String tag = localName;
            if (tag.equals("")) tag = qName;
            
            // For each tag, copy buff chars to right variable
            if (tag.equals("title")) mTitle = mBuff.toString();
            else if (tag.equals("link")) mLink = mBuff.toString();
            if (tag.equals("description")) mDescription = mBuff.toString();
            
            // Have all the data at this point .... post it to the UI.
            if (tag.equals("item")) {
                RssItem item = new RssItem(mTitle, mLink, mDescription);
                mHandler.post(new ItemAdder(item));
                mInItem = false;
            }
        }
    }
    */
    
    /*
    public void parseRSS2(InputStream in, RSSListAdapter adapter) throws IOException {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            DefaultHandler handler = new RSSHandler(adapter);
            
            parser.parse(in, handler);
            // TODO: does the parser figure out the encoding right on its own?
    }
    */
}
