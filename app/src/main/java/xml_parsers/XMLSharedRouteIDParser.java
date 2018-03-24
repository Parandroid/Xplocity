package xml_parsers;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

import models.AuthToken;


/**
 * Created by dmitry on 04.08.17.
 */


public class XMLSharedRouteIDParser extends XMLAbstractParser {
    // We don't use namespaces
    private static final String ns = null;

    public String parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readSharedID(parser);
        } finally {
            in.close();
        }
    }


    private String readSharedID(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "hash");
        String sharedRouteID = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("id")) {
                sharedRouteID = readText(parser);
            }  else {
                skip(parser);
            }
        }
        return sharedRouteID;
    }

}
