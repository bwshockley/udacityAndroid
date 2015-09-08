package com.benjaminshockley.myappportfolio;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This method switches between each App.
     *
     * @param v is the view selected.
     */

    public void launchApp(View v) {
        switch (v.getId()) {
            case R.id.movies:
                displayToast("This button will launch the Movies App!");
                break;
            case R.id.scores:
                displayToast("This button will launch the Scores App!");
                break;
            case R.id.library:
                displayToast("This button will launch the Library App!");
                break;
            case R.id.bigger:
                displayToast("This button will launch the Make it Bigger App!");
                break;
            case R.id.material:
                displayToast("This button will launch the Material App!");
                break;
            case R.id.capstone:
                displayToast("This button will launch the Capstone App!");
                break;
        }
    }

    /**
     * This method will display the Toast notification.
     *
     * @param info is the text to display.
     */

    public void displayToast(String info) {
        Toast.makeText(this,info,Toast.LENGTH_SHORT).show();
    }

}
