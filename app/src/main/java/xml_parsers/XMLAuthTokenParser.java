package xml_parsers;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import models.AuthToken;
import models.LocationCategory;


/**
 * Created by dmitry on 04.08.17.
 */


public class XMLAuthTokenParser extends XMLAbstractParser {
    // We don't use namespaces
    private static final String ns = null;

    public AuthToken parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readAuthToken(parser);
        } finally {
            in.close();
        }
    }


    private AuthToken readAuthToken(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "hash");
        AuthToken auth_token = new AuthToken();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();



            if (name.equals("authentication_token")) {
                auth_token.auth_token = readText(parser);
            } else if (name.equals("user_id")) {
                auth_token.user_id = Integer.parseInt(readText(parser));
            } else {
                skip(parser);
            }
        }
        return auth_token;
    }

}
