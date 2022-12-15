package hsnb.de.locationtracking;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class ShowSavedLocationsList extends AppCompatActivity {

    ListView lv_savedLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_saved_locations_list);

        lv_savedLocations = findViewById(R.id.lv_waypoints);


        MyApplication myApplication = (MyApplication)getApplicationContext();
        List<Location> savedLocations = myApplication.getMyLocation();

        //android.R.layout. chooses the type of listing layout

        lv_savedLocations.setAdapter(new ArrayAdapter<Location>( this, android.R.layout.preference_category, savedLocations));



    }
}