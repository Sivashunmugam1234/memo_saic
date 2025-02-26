package com.example.memo_saic;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class calculator extends AppCompatActivity {
    private TextView dateInput1, dateInput2, resultText;
    private Button calculateButton;
    private CheckBox checkCurrentDate;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        // Initialize views
        dateInput1 = findViewById(R.id.dateInput1);
        dateInput2 = findViewById(R.id.dateInput2);
        resultText = findViewById(R.id.resultText);
        calculateButton = findViewById(R.id.calculateButton);
        checkCurrentDate = findViewById(R.id.checkCurrentDate);

        // Initialize date format
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Set click listeners for date inputs
        dateInput1.setOnClickListener(v -> showDatePickerDialog(dateInput1));
        dateInput2.setOnClickListener(v -> showDatePickerDialog(dateInput2));

        // Set listener for checkbox
        checkCurrentDate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Set the second date to current date
                Calendar calendar = Calendar.getInstance();
                String currentDate = dateFormat.format(calendar.getTime());
                dateInput2.setText(currentDate);
                dateInput2.setEnabled(false);
            } else {
                dateInput2.setText("Select second date");
                dateInput2.setEnabled(true);
            }
        });

        // Set click listener for calculate button
        calculateButton.setOnClickListener(v -> calculateDateDifference());
    }

    private void showDatePickerDialog(TextView textView) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    textView.setText(selectedDate);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void calculateDateDifference() {
        String date1Str = dateInput1.getText().toString();
        String date2Str = dateInput2.getText().toString();

        // Check if dates are selected
        if (date1Str.equals("Select first date") || date2Str.equals("Select second date")) {
            Toast.makeText(this, "Please select both dates", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Parse the date strings to Date objects
            Date date1 = dateFormat.parse(date1Str);
            Date date2 = dateFormat.parse(date2Str);

            if (date1 == null || date2 == null) {
                Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Calculate the difference in milliseconds
            long diffInMillis = Math.abs(date2.getTime() - date1.getTime());

            // Convert to days
            long diffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

            // Calculate years, months, and remaining days
            int years = (int) (diffInDays / 365);
            int months = (int) ((diffInDays % 365) / 30);
            int days = (int) ((diffInDays % 365) % 30);

            // Format the result string
            StringBuilder resultBuilder = new StringBuilder();

            if (years > 0) {
                resultBuilder.append(years).append(years == 1 ? " year" : " years");
                if (months > 0 || days > 0) resultBuilder.append(", ");
            }

            if (months > 0) {
                resultBuilder.append(months).append(months == 1 ? " month" : " months");
                if (days > 0) resultBuilder.append(", ");
            }

            if (days > 0 || (years == 0 && months == 0)) {
                resultBuilder.append(days).append(days == 1 ? " day" : " days");
            }

            // Display the result
            resultText.setText("Difference: " + resultBuilder.toString());

        } catch (ParseException e) {
            Toast.makeText(this, "Error parsing dates", Toast.LENGTH_SHORT).show();
        }
    }
}