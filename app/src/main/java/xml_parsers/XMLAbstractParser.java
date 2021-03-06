package xml_parsers;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by dmitry on 03.08.17.
 */

public abstract class XMLAbstractParser {
    protected String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    protected void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }


    protected DateTime parseDate(String strDate) throws ParseException {
        String pattern = "yyyy-MM-dd HH:mm:ss z";
        DateTime date  = DateTime.parse(strDate, DateTimeFormat.forPattern(pattern));
        return date;
    }

    protected int parseInt(String str_int) {
        try {
            return Integer.parseInt(str_int);
        } catch (Exception e) {
            return 0;
        }
    }
}
