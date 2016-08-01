package edu.uw.ztianai.todoer;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import edu.uw.todoer.provider.TodoItem;

public class MainActivity extends AppCompatActivity implements MasterList.OnTaskSelectedListener{

    private static final String TAG = "MainActivity";
    private boolean land;  //whether it is currently in landscape or portrait view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();  //the application will first show 2 fragments
        View land = findViewById(R.id.container_right); //see if we can find the view for landscape
        this.land = (land != null); //if land is not null, that means we need to show two pane view
        if(this.land) {
            ft.add(R.id.container_left, new MasterList().newInstance(1));  //the to do list fragment
            ft.add(R.id.container_right, new Add());  //to add a new task fragment
            ft.commit();
        }else{
            ft.add(R.id.container, new MasterList().newInstance(1));  //the to do list fragment
            ft.commit();
        }

    }

    //Responds to the time picker button
    public void onButtonClicked(View v){
        DialogFragment newFragment = new Add.TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "TimePicker");
    }

    //Responds to the date picker button
    public void onButtonClickedDate(View v){
        DialogFragment newFragment = new Add.DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "DatePicker");
    }

    //Show the main menu on the action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    //When a item of the menu is selected, load different fragments
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.to_do_menu_item:      //when to do menu button is click, to do list will show up in the left part of the screen
                FragmentTransaction ftTodo = getSupportFragmentManager().beginTransaction();
                if(land){ //show two pane view if the device in landscape mode
                    ftTodo.remove(getSupportFragmentManager().findFragmentById(R.id.container_right)); //right side of the screen would be blank
                    ftTodo.replace(R.id.container_left, new MasterList().newInstance(1), null);
                }else{
                    ftTodo.replace(R.id.container,new MasterList().newInstance(1), null);

                }
                ftTodo.addToBackStack(null);
                ftTodo.commit();
                return true;
            case R.id.completed_menu_item:     //when completed menu button is click, the right part of the screen will show the list of completed tasks
                FragmentTransaction ftComplete = getSupportFragmentManager().beginTransaction();
                if(land){
                    ftComplete.replace(R.id.container_right, new MasterList(), null);
                }else{
                    ftComplete.replace(R.id.container, new MasterList(), null);
                }
                ftComplete.addToBackStack(null);
                ftComplete.commit();
                return true;
            case R.id.add_menu_item:    //when add menu button is click, the right part of the screen will show the adding fragment
                FragmentTransaction ftAdd = getSupportFragmentManager().beginTransaction();
                if(land){
                    ftAdd.replace(R.id.container_right, new Add(), null);
                }else {
                    ftAdd.replace(R.id.container, new Add(), null);
                }
                ftAdd.addToBackStack(null);
                ftAdd.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //When a user select a specific item from the list view, data would be stored and detail fragment would show up in the right part of the screen
    public void onTaskSelected(Cursor task) {
        Bundle bundle = new Bundle();  //Storing the data into a bundle
        bundle.putString("title", task.getString(task.getColumnIndex(TodoItem.TITLE)));
        bundle.putString("deadline", task.getString(task.getColumnIndex(TodoItem.DEADLINE)));
        bundle.putString("detail", task.getString(task.getColumnIndex(TodoItem.DETAILS)));
        bundle.putString("id", task.getString(task.getColumnIndex(TodoItem.ID)));
        bundle.putString("complete", task.getString(task.getColumnIndex(TodoItem.COMPLETED)));


        Detail fragment = new Detail();
        fragment.setArguments(bundle);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if(land){ //if two pane view, show both completed list and the details
            if(task.getString(task.getColumnIndex(TodoItem.COMPLETED)).equals("1")){ //if the user is on the completed list view
                ft.replace(R.id.container_left, new MasterList(), null); //completed list will show up on the left side of the screen
                ft.replace(R.id.container_right, fragment, null); //detail page will show up on the right side of the screen
            }else{
                ft.replace(R.id.container_right, fragment, null);
            }
        }else{
            ft.replace(R.id.container, fragment, null);
        }

        ft.addToBackStack(null);
        ft.commit();
    }

    //If user clicks on back button, it will lead the user to the previous section
    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() != 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
