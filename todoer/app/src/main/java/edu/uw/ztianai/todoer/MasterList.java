package edu.uw.ztianai.todoer;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import edu.uw.todoer.provider.TodoItem;
import edu.uw.todoer.provider.TodoListProvider;

/**
 * Created by Tianai Zhao on 16/4/15.
 */
public class MasterList extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter adapter;
    private static final String TAG = "MasterList";

    private int id;

    private OnTaskSelectedListener callback;

    public MasterList() {

    }

    //Take in an id to determine what data to load later
    public static MasterList newInstance(int id){
        MasterList fragment = new MasterList();
        Bundle bundle = new Bundle();
        bundle.putInt("type", id);
        fragment.setArguments(bundle);
        return fragment;
    }

    //A listener for task selected
    public interface OnTaskSelectedListener {
        public void onTaskSelected(Cursor task);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(getArguments() != null){
            Bundle bundle = getArguments();
            id = bundle.getInt("type");
        }

        View rootView;
        if(id == 1){ //If id is 1 then grab data related to the to do list view
            rootView = inflater.inflate(R.layout.fragment_master_list, container, false);
        }else { //otherwise grab data related to the completed list view
            rootView = inflater.inflate(R.layout.fragment_completed, container, false);
        }

        //Show the specific menu buttons for this fragment or data
        setHasOptionsMenu(true);

        //Adapter for showing the data in list view
        AdapterView listView = (AdapterView)rootView.findViewById(R.id.listView);

        adapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.list_item,
                null,
                new String[] {TodoItem.TITLE, TodoItem.DEADLINE},
                new int[] {R.id.txtTaskTitle, R.id.txtTaskDeadline},
                0
        );

        //Show date in a readable format
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder(){
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if(columnIndex == cursor.getColumnIndex(TodoItem.DEADLINE)) {
                    String deadline = cursor.getString(cursor.getColumnIndex(TodoItem.DEADLINE));
                    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm");
                    long milliSeconds = Long.parseLong(deadline);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(milliSeconds);
                    ((TextView)view).setText(formatter.format(calendar.getTime()));
                    return true;
                }
                return false;
            }
        });

        listView.setAdapter(adapter);


        //put a click listener on the item in the list
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor)parent.getItemAtPosition(position);
                callback.onTaskSelected(cursor);
            }
        });

        //initiate the loader for loading data
        getLoaderManager().initLoader(0, null, this);

        return rootView;
    }

    //Create a loader for loading necessary information
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{TodoItem.ID, TodoItem.TITLE, TodoItem.DETAILS, TodoItem.DEADLINE, TodoItem.COMPLETED};
        String selection = TodoItem.COMPLETED + "=0"; //uncompleted tasks
        String selectionCompleted = TodoItem.COMPLETED + "=1"; //completed tasks
        CursorLoader loader;
        if(this.id != 1){  //data for completed list, sorted by time created in ascending order
            loader = new CursorLoader(
                    getActivity(),
                    TodoListProvider.CONTENT_URI,
                    projection,
                    selectionCompleted,
                    null,
                    TodoItem.TIME_CREATED
            );
        }else if(id == 0){  //data for to do list, sorted by time created in ascending order
            loader = new CursorLoader(
                    getActivity(),
                    TodoListProvider.CONTENT_URI,
                    projection,
                    selection,
                    null,
                    TodoItem.TIME_CREATED
            );
        }else{ //data for to do list, sorted by deadline in ascending order
            loader = new CursorLoader(
                    getActivity(),
                    TodoListProvider.CONTENT_URI,
                    projection,
                    selection,
                    null,
                    TodoItem.DEADLINE
            );
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    //Checks to make sure the activity implements OnTaskSelectedListener
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            callback = (OnTaskSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnTaskSelectedListener");
        }
    }

    //Restart loader and sort data based on deadline
    public void sortByDeadline(){
        getLoaderManager().restartLoader(1, null, this);
    }

    //Restart loader and sort data based on the created date
    public void sortByCreatedDate(){
        getLoaderManager().restartLoader(0, null, this);
    }

    //If the user is on the to do list view, then user can see the sorting options
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(id == 1){
            inflater.inflate(R.menu.masterlist_fragment_menu, menu);
        }
    }

    //Responds to user's choice on sorting options
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.sort_deadline_menu_item:
                sortByDeadline();
                return true;
            case R.id.sort_menu_item:
                sortByCreatedDate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}


