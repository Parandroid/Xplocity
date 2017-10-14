package xml_parsers;

import android.location.Location;
import android.util.Xml;

import com.google.android.gms.maps.model.LatLng;

import org.osmdroid.util.GeoPoint;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


/**
 * Created by dmitry on 02.08.17.
 */

public class XMLLocationsParser extends XMLAbstractParser {
    // We don't use namespaces
    private static final String ns = null;

    public ArrayList<models.Location> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readLocations(parser);
        } finally {
            in.close();
        }
    }



    private ArrayList<models.Location> readLocations(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<models.Location> locations = new ArrayList<models.Location>();

        parser.require(XmlPullParser.START_TAG, ns, "locations");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("location")) {
                locations.add(readLocation(parser));
            } else {
                skip(parser);
            }
        }
        return locations;
    }

    private models.Location readLocation(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "location");
        models.Location loc = new models.Location();

        double lat = 0d;
        double lon = 0d;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("id")) {
                loc.id = Integer.parseInt(readText(parser));
            } else if (name.equals("name")) {
                loc.name = readText(parser);
            } else if (name.equals("description")) {
                loc.description = readText(parser);
            } else if (name.equals("address")) {
                loc.address = readText(parser);
            } else if (name.equals("latitude")) {
                lat = Double.parseDouble(readText(parser));
            } else if (name.equals("longitude")) {
                lon = Double.parseDouble(readText(parser));
            } else {
                skip(parser);
            }
        }
        GeoPoint pos = new GeoPoint(lat, lon);
        loc.position = pos;


        return loc;
    }


}