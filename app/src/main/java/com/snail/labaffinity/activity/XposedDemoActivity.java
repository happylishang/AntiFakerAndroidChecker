package com.snail.labaffinity.activity;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.snail.labaffinity.R;

public class XposedDemoActivity extends Activity {
    private static final String TAG = "XposedDemoActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        Log.e(TAG,"onCreate");
        
    	TextView textView =(TextView) findViewById(R.id.text);
 
 
		
 

    }

 

}
