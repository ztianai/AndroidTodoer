package edu.uw.ztianai.todoer;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import edu.uw.todoer.provider.TodoItem;
import edu.uw.todoer.provider.TodoListProvider;

/**
 * Created by Tianai Zhao on 16/4/15.
 */
public class Add extends Fragment implements View.OnClickListener{

    private static final String TAG = "add";

    private View rootView;
    private Button addButton;

    public Add() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflate add fragment view
        rootView  = inflater.inflate(R.layout.fragment_add, container, false);
        addButton = (Button)rootView.findViewById(R.id.btnAdd);
        addButton.setOnClickListener(this); //put a click listener on the add task button

        return rootView;
    }

    //Allow user to pick a specific time
    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute =c.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute){
            TextView time = (TextView) getActivity().findViewById(R.id.time);

            time.setText(String.valueOf(hourOfDay) +":" + String.valueOf(minute)); //show the picked time
        }
    }

    //Allow user to pick a specific date
    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            TextView date = (TextView) getActivity().findViewById(R.id.date);

            String stringOfDate = (monthOfYear + 1) + "-" + dayOfMonth + "-" + year;
            date.setText(stringOfDate);  //show the picked date
        }
    }

    //When user clicks on the add task button, data will be store in database
    @Override
    public void onClick(View v) {
        ContentResolver resolver = getActivity().getContentResolver();
        String title = ((EditText)rootView.findViewById(R.id.addTitle)).getText().toString(); //gets the input task title
        String detail = ((EditText)rootView.findViewById(R.id.addDescr)).getText().toString(); //gets the input task detail
        String time = ((TextView)rootView.findViewById(R.id.time)).getText().toString(); //gets the input deadline time
        String date = ((TextView)rootView.findViewById(R.id.date)).getText().toString(); //gets the input deadline date

        String toParse = date + " " + time;

        long millis = 0;

        try{
            SimpleDateFormat formatter = new SimpleDateFormat("M-d-yyyy hh:mm"); //date format

            Date dateFinal = formatter.parse(toParse);
            millis = dateFinal.getTime(); //turns string format date to milliseconds format

        }catch (ParseException e){
            e.printStackTrace();
        }

        ContentValues values = new ContentValues();
        values.put(TodoItem.TITLE, title);
        values.put(TodoItem.DETAILS, detail);
        values.put(TodoItem.DEADLINE, millis);
        resolver.insert(TodoListProvider.CONTENT_URI, values); //put data in database
        Toast.makeText(getActivity(), "New Task Added!", Toast.LENGTH_LONG).show(); //show a toast message to notify user

        if(getFragmentManager().getBackStackEntryCount() != 0) {  //Once finished adding, return to the previous page that user was on
            getFragmentManager().popBackStack();
        }
    }
}
