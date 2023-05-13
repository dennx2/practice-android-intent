package com.codecrafters.assignment3_intent;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static int PHOTO_REQUEST = 1000;

    private ImageView imgView;
    private EditText msgNumber;
    private EditText msgContent;

    private EditText title;
    private TextView beginDate;
    private TextView beginTime;
    private TextView endDate;
    private TextView endTime;
    private CheckBox isAllDayCheckBox;
    private EditText description;
    private EditText emails;
    private SwitchCompat accessSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the UI controls
        findViewById(R.id.btnAddCalendarEvent).setOnClickListener(this);
        findViewById(R.id.btnCapturePhoto).setOnClickListener(this);
        findViewById(R.id.btnSendTextMessage).setOnClickListener(this);

        findViewById(R.id.startDate).setOnClickListener(this);
        findViewById(R.id.startTime).setOnClickListener(this);
        findViewById(R.id.endDate).setOnClickListener(this);
        findViewById(R.id.endTime).setOnClickListener(this);

        title = findViewById(R.id.eventTitle);
        beginDate = findViewById(R.id.startDate);
        beginTime = findViewById(R.id.startTime);
        endDate = findViewById(R.id.endDate);
        endTime = findViewById(R.id.endTime);
        isAllDayCheckBox = findViewById(R.id.checkbox_isAllDay);
        accessSwitch = findViewById(R.id.accessType);

        description = findViewById(R.id.eventDescription);
        emails = findViewById(R.id.emailOfInvitees);

        imgView = findViewById(R.id.imgCapturePic);

        msgNumber = findViewById(R.id.txtNumber);
        msgContent = findViewById(R.id.txtMsgContent);


    }

    @Override
    public void onClick(View view) {

        // Figure out which button was clicked
        int viewClicked = view.getId();

        // When add calendar button clicked
        if (viewClicked == R.id.btnAddCalendarEvent) {

            // Get access level from switch toggle
            int accessLevel = accessSwitch.isChecked() ? CalendarContract.Events.ACCESS_PUBLIC : CalendarContract.Events.ACCESS_PRIVATE;

            // Validate dates are selected
            // Validate time are also selected when all day event is not checked
            String beginDateString = beginDate.getText().toString();
            String beginTimeString = beginTime.getText().toString();
            String endDateString = endDate.getText().toString();
            String endTimeString = endTime.getText().toString();

            boolean flag = true;
            if (beginDateString.isEmpty()) {
                beginDate.setHintTextColor(Color.RED);
                flag = false;
            }

            if (endDateString.isEmpty()) {
                endDate.setHintTextColor(Color.RED);
                flag = false;
            }

            if (beginTimeString.isEmpty() && !isAllDayCheckBox.isChecked()) {
                beginTime.setHintTextColor(Color.RED);
                flag = false;
            }

            if (endTimeString.isEmpty() && !isAllDayCheckBox.isChecked()) {
                endTime.setHintTextColor(Color.RED);
                flag = false;
            }

            if (!flag) {
                Toast.makeText(this, "Please fill the missing value(s)", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create intent
            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.Events.TITLE, title.getText().toString())
                    .putExtra(CalendarContract.Events.ALL_DAY, isAllDayCheckBox.isChecked())
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, timeInMillisParser(beginDateString, beginTimeString))
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, timeInMillisParser(endDateString, endTimeString))
                    .putExtra(CalendarContract.Events.DESCRIPTION, description.getText().toString())
                    .putExtra(CalendarContract.Events.ACCESS_LEVEL, accessLevel)
                    .putExtra(Intent.EXTRA_EMAIL, emails.getText().toString());


            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }

        // When capture photo button clicked
        else if (viewClicked == R.id.btnCapturePhoto) {
            Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (photoIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(photoIntent, PHOTO_REQUEST);
            }
        }

        // When send text msg button clicked
        else if (viewClicked == R.id.btnSendTextMessage) {
            Intent i = new Intent(Intent.ACTION_SENDTO)
                    .setData(Uri.parse("sms:" + msgNumber.getText().toString()))
                    .putExtra("sms_body", msgContent.getText().toString());

            if (i.resolveActivity(getPackageManager()) != null) {
                startActivity(i);
            }
        } else if (viewClicked == R.id.startDate) {
            onClickedDateTextView(beginDate);
        } else if (viewClicked == R.id.startTime) {
            onClickedTimeTextView(beginTime);
        } else if (viewClicked == R.id.endDate) {
            onClickedDateTextView(endDate);
        } else if (viewClicked == R.id.endTime) {
            onClickedTimeTextView(endTime);
        }

    }


    protected void onClickedDateTextView(TextView tv) {
        // Create a Calendar instance with the current date
        Calendar calendar = Calendar.getInstance();

        // Create a DatePickerDialog with the current date and an OnDateSetListener
        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                (view, year, month, dayOfMonth) -> {
                    // Update the text of the EditText field with the selected date
                    String dateString = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    tv.setText(dateString);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        // Show the DatePickerDialog
        datePickerDialog.show();
    }

    protected void onClickedTimeTextView(TextView tv) {
        // Create a Calendar instance with the current time
        Calendar calendar = Calendar.getInstance();

        // Create a TimePickerDialog with the current time
        TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                (view, hourOfDay, minute) -> {
                    // Set the selected time to the TextView
                    String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    tv.setText(selectedTime);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(MainActivity.this));

        // Show the TimePickerDialog
        timePickerDialog.show();
    }

    protected long timeInMillisParser(String dateString, String timeString) {

        // Define the format of the date string and time string
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm", Locale.getDefault());

        // Parse the date string and time string into Date objects
        Date date = null, time = null;
        Calendar calendar = Calendar.getInstance();
        long timeInMillis;

        if (isAllDayCheckBox.isChecked()) {
            try {
                date = dateFormat.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Combine the date and time into the Calendar object
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
        } else {
            try {
                date = dateFormat.parse(dateString);
                time = timeFormat.parse(timeString);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Combine the date and time into the Calendar object
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, time.getHours());
            calendar.set(Calendar.MINUTE, time.getMinutes());
        }

        // Get the time in milliseconds
        timeInMillis = calendar.getTimeInMillis();

        return timeInMillis;
    }


    public void onClickedIsAllDay(View view) {
        if (isAllDayCheckBox.isChecked()) {
            beginTime.setVisibility(View.GONE);
            endTime.setVisibility(View.GONE);
        } else {
            beginTime.setVisibility(View.VISIBLE);
            endTime.setVisibility(View.VISIBLE);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK) {

            // Retrieve the data from the result intent and look for the bitmap
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imgView.setImageBitmap(imageBitmap);
        }
    }
}