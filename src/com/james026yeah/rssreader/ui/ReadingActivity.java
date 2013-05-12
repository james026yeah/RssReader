package com.james026yeah.rssreader.ui;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.jsoup.nodes.Document;

import com.james026yeah.rssreader.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class ReadingActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reading_layout);
		Intent intent = getIntent();
	    Toast.makeText(getApplicationContext(), "url:" + intent.getStringExtra("url"), 3000).show();
	    Log.d("james","url:" + intent.getStringExtra("url"));
	    String url = intent.getStringExtra("url");
	    
	    String plainText = "";
	    
	    Document doc;
		try {
			doc = Jsoup.connect(url).get();
			HtmlToPlainText formatter = new HtmlToPlainText();
			plainText = formatter.getPlainText(doc);
			Log.d("james", plainText);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        
	    TextView tv = (TextView) findViewById(R.id.article);
	    tv.setText(plainText);
	}
}
