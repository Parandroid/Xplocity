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

public class XMLReportedLocationBuilder {

    public XMLReportedLocationBuilder() {}

    public String toXml(Location location) {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", "ReportedLocation");
            serializer.startTag("", "name");
            serializer.text(location.name);
            serializer.endTag("", "name");
            serializer.startTag("", "description");
            serializer.text(location.description);
            serializer.endTag("", "description");
            serializer.startTag("", "latitude");
            serializer.text(Double.toString(location.position.getLatitude()));
            serializer.endTag("", "latitude");
            serializer.startTag("", "longitude");
            serializer.text(Double.toString(location.position.getLongitude()));
            serializer.endTag("", "longitude");
            serializer.endTag("", "ReportedLocation");
            serializer.endDocument();
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
