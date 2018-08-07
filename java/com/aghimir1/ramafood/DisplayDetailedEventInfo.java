package com.aghimir1.ramafood;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class DisplayDetailedEventInfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_detailed_event_info);
        changeStatusBarColor();

        Intent intent = getIntent();
        String eventTitle = intent.getStringExtra(DisplayAllEventsActivity.EVENT_TITLE);
        String eventDate = intent.getStringExtra(DisplayAllEventsActivity.EVENT_DATE);
        String eventDetails = intent.getStringExtra(DisplayAllEventsActivity.EVENT_DETAILS);

        setUpTextFields(eventTitle, eventDate, eventDetails);
    }

    private void setUpTextFields(String eventTitle, String eventDate, String eventDetails){
        TextView eventTitleTextBox = findViewById(R.id.textView3);
        eventTitleTextBox.setText(eventTitle);

        TextView eventDateTextBox = findViewById(R.id.textView4);
        eventDateTextBox.setText(eventDate);

        TextView eventDetailsTextBox = findViewById(R.id.textView5);
        eventDetailsTextBox.setText(eventDetails);
    }

    public void changeStatusBarColor(){
        if(Build.VERSION.SDK_INT >= 21){
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));
        }
    }
}
