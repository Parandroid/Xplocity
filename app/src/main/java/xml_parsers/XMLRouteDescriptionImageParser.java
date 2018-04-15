package xml_parsers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;

import models.RouteDescription;

/**
 * Created by dmitry on 02.08.17.
 */

public class XMLRouteDescriptionImageParser extends XMLAbstractParser {
    // We don't use namespaces
    private static final String ns = null;

    public Bitmap parse(InputStream in) throws XmlPullParserException, IOException, ParseException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readImage(parser);
        } finally {
            in.close();
        }
    }



    private Bitmap readImage(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
        parser.require(XmlPullParser.START_TAG, ns, "hash");
        Bitmap image = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("body")) {
                String encodedImage = readText(parser);
                byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                image = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            }  else {
                skip(parser);
            }
        }
        return image;
    }



}