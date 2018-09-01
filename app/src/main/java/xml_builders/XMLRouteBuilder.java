package xml_builders;

import android.util.Xml;

import com.google.android.gms.maps.model.LatLng;

import org.osmdroid.util.GeoPoint;
import org.xmlpull.v1.XmlSerializer;

import java.io.StringWriter;
import java.util.ArrayList;

import models.Location;
import models.Route;

/**
 * Created by dmitry on 05.09.17.
 */

public class XMLRouteBuilder {

    public XMLRouteBuilder() {}

    public String toXml(Route route) {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", "Chain");


            serializer.startTag("", "Info");

            serializer.startTag("", "distance");
            serializer.text(Integer.toString((int) route.distance));
            serializer.endTag("", "distance");
            serializer.startTag("", "duration");
            serializer.text(Integer.toString(route.duration));
            serializer.endTag("", "duration");

            serializer.endTag("", "Info");


            serializer.startTag("", "Locations");

            for (Location loc: route.locations){
                serializer.startTag("", "Location");
                serializer.attribute("", "id", Integer.toString(loc.id));
                serializer.startTag("", "explored");
                serializer.text(loc.explored() ? "1" : "0");
                serializer.endTag("", "explored");
                serializer.endTag("", "Location");
            }
            serializer.endTag("", "Locations");

            serializer.startTag("", "Route");
            serializer.text(pathToString(route.path));
            serializer.endTag("", "Route");

            serializer.endTag("", "Chain");
            serializer.endDocument();
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private String pathToString(ArrayList<GeoPoint> path) {
        String result = "";

        for (GeoPoint pos : path) {
            if (result.length() > 0) {
                result = result + ";";
            }
            result = result + Double.toString(pos.getLatitude()) + " " + Double.toString(pos.getLongitude());
        }

        return result;
    }


}
