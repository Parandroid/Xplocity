package xml_parsers;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;


/**
 * Created by dmitry on 04.08.17.
 */


public class XMLDeleteRouteResultParser extends XMLAbstractParser {
    // We don't use namespaces
    private static final String ns = null;

    public int parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readRouteId(parser);
        } finally {
            in.close();
        }
    }


    private int readRouteId(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "hash");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("deleted_id")) {
                return Integer.valueOf(readText(parser));
            } else {
                skip(parser);
            }
        }
        return 0;
    }

}
