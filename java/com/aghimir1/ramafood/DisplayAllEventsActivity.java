package com.aghimir1.ramafood;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mongodb.BasicDBObject;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

import android.os.StrictMode;


public class DisplayAllEventsActivity extends AppCompatActivity {
    private MongoCollection<Document> foodEventsTable;
    private Document currentDocument;

    public static final String EVENT_TITLE = "com.aghimir1.ramaFood.EventTitle";
    public static final String EVENT_DATE= "com.aghimir1.ramaFood.EventDate";
    public static final String EVENT_DETAILS = "com.aghimir1.ramaFood.EventDetails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_all_events);

        //Changing status bar color
        changeStatusBarColor();

        //Make scrolling in the scroll view smooth
        ScrollView pageScrollView = findViewById( R.id.scrollView2 );
        pageScrollView.setSmoothScrollingEnabled(true);

        //Use Async task as a better alternative for the two lines below
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if( isNetworkAvailable() ){    //The user is connected to the internet
            connectToDatabase();
            setUpPageUserIsConnectedToInternet();
        }
        else
            setUpPageNoInternetConnection();
    }

    //The function below checks if app user is connected to internet. Got this function from
    // Alecandre Jasmine's answer here
    // https://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android#4239019
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        //If there is an error in the line above, copy and paste this line in your android manifest
        // <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void changeStatusBarColor(){
        if(Build.VERSION.SDK_INT >= 21){
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    private void addUnderline(LinearLayout layoutToAddUnderlineTo){
        int underlineColor = Color.argb(255, 80, 77, 78);

        //Add an underline after each event
        View underline = new View(this);
        underline.setBackgroundColor(underlineColor);
        underline.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5));
        layoutToAddUnderlineTo.addView(underline);
    }

    private void addThePageHeading(LinearLayout entirePage){

        TextView allEventsString = new TextView(this);
        allEventsString.setText(getResources().getString(R.string.allEvents));
        allEventsString.setTypeface(null, Typeface.BOLD);
        allEventsString.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
        entirePage.addView(allEventsString);

        addUnderline(entirePage);
    }

    private void setUpPageNoInternetConnection() {
        LinearLayout entirePage = findViewById(R.id.entirePageLinearLayout);
        setUpTextFields(entirePage, getResources().getString(R.string.noInternetMessage));
        setUpTextFields(entirePage, getResources().getString(R.string.noInternetToDo1));
        setUpTextFields(entirePage, getResources().getString(R.string.noInternetToDo2));
    }

    private void setUpTextFields(LinearLayout layout, String text){
        TextView textView = new TextView(this);
        textView.setText(text);
        layout.addView(textView);
    }

    private void setUpTextBoxesIfConnectedToInternet(LinearLayout anEventBox){
        TextView eventTitle = new TextView(this);
        eventTitle.setText(currentDocument.getString("title"));
        eventTitle.setTypeface(null, Typeface.BOLD);
        anEventBox.addView(eventTitle);

        TextView eventDate = new TextView(this);
        String date = ""+ currentDocument.get("eventDate");
        eventDate.setText(getFormattedDate(date) );   //Look at the android studio warning and fix this line
        eventDate.setTextColor(getResources().getColor(R.color.colorPrimaryDark)); //Change this to maroon later
        anEventBox.addView(eventDate);

        TextView eventDetails = new TextView(this);
        eventDetails.setText(currentDocument.getString("eventDetails"));
        eventDetails.setLines(3);
        anEventBox.addView(eventDetails);

        TextView readMore = new TextView(this);
        readMore.setText(R.string.readMore);
        readMore.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        readMore.setGravity(Gravity.END);
        anEventBox.addView(readMore);
    }

    private void setUpPageUserIsConnectedToInternet() {
        LinearLayout entirePage = findViewById(R.id.entirePageLinearLayout);
        addThePageHeading(entirePage);
        MongoCursor<Document> cursor = foodEventsTable.find().sort(new BasicDBObject("eventDate", 1)).iterator();

        try {
            int count = 0;
            while (cursor.hasNext()) {
                try{
                    LinearLayout anEventBox = new LinearLayout(this);
                    anEventBox.setOrientation(LinearLayout.VERTICAL);

                    currentDocument = cursor.next();

                    //Set the text fields
                    setUpTextBoxesIfConnectedToInternet(anEventBox);
                    anEventBox.setId(count);

                    anEventBox.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            // Code here executes on main thread after user presses button
                            startDisplayDetailedEventInfoActivity(v);
                        }
                    });

                    entirePage.addView(anEventBox);
                    addUnderline(entirePage);
                    count++;
                }
                catch(Exception e){
                    continue;
                }
            }
        }
        finally {
            cursor.close();
        }
    }


    //Original date format is Thu Mar 01 17:09:05 EST 2018.
    //Final date format should be Thu, Mar 01
    private StringBuilder getFormattedDate(String date){
        StringBuilder newDate = new StringBuilder();
        boolean commaPlaced = false;
        int numSpaces = 0; //Stop when you encounter three spaces
        for(int i = 0; i < date.length(); i++){
            if(!commaPlaced && numSpaces == 1){
                newDate.append(',');
                commaPlaced = true;
            }

            if(date.charAt(i+1) == ' ')
                numSpaces++;

            newDate.append( date.charAt(i) );

            if(numSpaces == 3)
                break;
        }
        return newDate;
    }


    public void startDisplayDetailedEventInfoActivity( View view ){
        Intent intent = new Intent(this, DisplayDetailedEventInfo.class);

        for(int i =0; i < foodEventsTable.count(); i++){
            if(i == view.getId()){
                LinearLayout anEventBox = findViewById(i);
                final int childCount = anEventBox.getChildCount() - 1; //-1 because we don't want to include "Read More.." textbox
                String [] intentKeys = {EVENT_TITLE, EVENT_DATE, EVENT_DETAILS};

                for (int j = 0; j < childCount; j++) {
                    View v = anEventBox.getChildAt(j);
                    intent.putExtra(intentKeys[j], ( (TextView)v ).getText());
                }
                break;
            }
        }
        startActivity(intent);
    }


    private void connectToDatabase(){
        //Connect to database
        //Be very careful here. I to make this more secure
        //Don't wanna put hardcoded confidential info here. That's why, I have used fake DB names, username
        // and password
        try{
            MongoClientURI connectionString = new MongoClientURI("yourMongoDBuRL");
            MongoClient mongoClient = new MongoClient(connectionString);
            MongoDatabase database = mongoClient.getDatabase("yourMongoDBdatabaseName");
            foodEventsTable = database.getCollection("theNameOfYourMongoDBCollection");
        }
        catch (Exception e){
            setUpPageNoInternetConnection();
        }
    }
}