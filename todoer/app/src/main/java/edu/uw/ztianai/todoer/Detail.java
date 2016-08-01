package edu.uw.ztianai.todoer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import edu.uw.todoer.provider.TodoItem;
import edu.uw.todoer.provider.TodoListProvider;

/**
 * Created by Tianai Zhao on 16/4/15.
 */
public class Detail extends Fragment implements CompoundButton.OnCheckedChangeListener{


    private ToggleButton toggle;

    public Detail(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false); //inflate the detail view

        Bundle args = getArguments();

        //Grabs data from the bundle and set it to the corresponding position
        ((TextView)rootView.findViewById(R.id.titleName)).setText(args.getString("title"));
        ((TextView)rootView.findViewById(R.id.detail)).setText(args.getString("detail"));
        String deadline = args.getString("deadline");

        //Changes date to readable format
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm");
        long milliSeconds = Long.parseLong(deadline);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        ((TextView)rootView.findViewById(R.id.deadline)).setText(formatter.format(calendar.getTime()));

        //If the task has completed (in database is represented by 1) the toggle button will be checked
        //Otherwise the toggle button is unchecked(uncompleted task)
        toggle = (ToggleButton)rootView.findViewById(R.id.toggleButton);
        if(args.getString("complete").equals("1")){
            toggle.setChecked(true);
        }
        toggle.setOnCheckedChangeListener(this);

        return rootView;
    }

    //Once the user clicks on the toggle button, database will be updated with the new information
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            // The toggle is enabled
            Bundle bundle = getArguments();
            String id = bundle.getString("id");

            ContentResolver resolver = getActivity().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(TodoItem.COMPLETED, 1); //1 - completed
            String selection = "_ID=" + id; //specify the particular row of data that need to be updated

            resolver.update(TodoListProvider.CONTENT_URI, values, selection, null);

        } else {
            // The toggle is disabled
            Bundle bundle = getArguments();
            String id = bundle.getString("id");
            ContentResolver resolver = getActivity().getContentResolver();

            ContentValues values = new ContentValues();
            values.put(TodoItem.COMPLETED, 0); //0 - uncompleted
            String selection = "_ID=" + id; //specify the particular row of data that need to be updated

            resolver.update(TodoListProvider.CONTENT_URI, values, selection, null);

        }
    }
}
