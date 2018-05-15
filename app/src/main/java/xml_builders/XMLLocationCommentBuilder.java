package xml_builders;

import android.util.Xml;

import org.osmdroid.util.GeoPoint;
import org.xmlpull.v1.XmlSerializer;

import java.io.StringWriter;
import java.util.ArrayList;

import models.Location;
import models.LocationComment;
import models.Route;

/**
 * Created by dmitry on 05.09.17.
 */

public class XMLLocationCommentBuilder {

    public XMLLocationCommentBuilder() {}

    public String toXml(LocationComment locationComment) {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);

            serializer.startTag("", "LocationComment");
            serializer.startTag("", "message_type");
            serializer.text(Integer.toString(locationComment.messageType));
            serializer.endTag("", "message_type");

            serializer.startTag("", "message");
            serializer.text(locationComment.message);
            serializer.endTag("", "message");

            serializer.endTag("", "LocationComment");

            serializer.endDocument();
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
