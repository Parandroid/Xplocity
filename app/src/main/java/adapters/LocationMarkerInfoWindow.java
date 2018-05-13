package adapters;

import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.xplocity.xplocity.LocationInfoDialog;
import com.xplocity.xplocity.R;
import com.xplocity.xplocity.ReportNewLocation;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import models.Location;

/**
 * Created by dmitry on 13.05.18.
 */

public class LocationMarkerInfoWindow extends InfoWindow {

        public LocationMarkerInfoWindow(int layoutResId, MapView mapView) {
            super(layoutResId, mapView);
        }

        @Override
        public void onClose() {
        }

        @Override
        public void onOpen(Object arg0) {
            closeAllInfoWindowsOn(getMapView());

            TextView txt_name = ((TextView) mView.findViewById(R.id.name));
            TextView txt_address = ((TextView) mView.findViewById(R.id.address));
            TextView txt_description = ((TextView) mView.findViewById(R.id.description));
            Button btn_open_location_info_dialog = ((Button) mView.findViewById(R.id.btn_open_location_info_dialog));

            final Location loc = (Location) ((Marker) arg0).getRelatedObject();

            txt_name.setText(loc.name);
            txt_address.setText(loc.address);
            txt_description.setText(loc.description);
            btn_open_location_info_dialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fm = ((AppCompatActivity) getMapView().getContext()).getSupportFragmentManager();
                    LocationInfoDialog fragment=  LocationInfoDialog.newInstance(loc);
                    fragment.show(fm, "reportNewLocationFragment");
                }
            });
        }




}
