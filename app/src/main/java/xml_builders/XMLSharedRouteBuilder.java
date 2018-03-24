package xml_builders;

import android.util.Xml;

import org.osmdroid.util.GeoPoint;
import org.xmlpull.v1.XmlSerializer;

import java.io.StringWriter;
import java.util.ArrayList;

import models.Location;
import models.Route;

/**
 * Created by dmitry on 05.09.17.
 */

public class XMLSharedRouteBuilder {

    public XMLSharedRouteBuilder() {}

    public String toXml(Route route) {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);

            serializer.startTag("", "Locations");

            for (Location loc: route.locations){
                serializer.startTag("", "Location");
                serializer.attribute("", "id", Integer.toString(loc.id));
                serializer.endTag("", "Location");
            }
            serializer.endTag("", "Locations");

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
