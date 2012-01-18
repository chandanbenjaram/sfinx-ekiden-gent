package be.sfinxekidengent.ui;

import android.os.Bundle;
import be.sfinxekidengent.R;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class MyMapActivity extends MapActivity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_2);
        MapView mapView = (MapView)findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
    }
    
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
