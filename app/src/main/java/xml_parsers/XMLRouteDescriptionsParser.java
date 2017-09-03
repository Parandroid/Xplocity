package xml_parsers;

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

public class XMLRouteDescriptionsParser extends XMLAbstractParser {
    // We don't use namespaces
    private static final String ns = null;

    public ArrayList<RouteDescription> parse(InputStream in) throws XmlPullParserException, IOException, ParseException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readChains(parser);
        } finally {
            in.close();
        }
    }



    private ArrayList<RouteDescription> readChains(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
        ArrayList<RouteDescription> routes = new ArrayList<RouteDescription>();

        parser.require(XmlPullParser.START_TAG, ns, "User");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("Chain")) {
                routes.add(readChain(parser));
            } else {
                skip(parser);
            }
        }
        return routes;
    }

    private RouteDescription readChain(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
        parser.require(XmlPullParser.START_TAG, ns, "Chain");

        RouteDescription route = new RouteDescription();

        int id = Integer.parseInt(parser.getAttributeValue(null, "id"));
        route.id = id;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("date")) {
                String str_date = readText(parser);
                route.date = parseDate(str_date);
            } else if (name.equals("name")) {
                route.name = readText(parser);
            } else if (name.equals("distance")) {
                route.distance = parseInt(readText(parser));
            } else if (name.equals("duration")) {
                route.duration = parseInt(readText(parser));
            } else if (name.equals("Locations")) {
                int[] locs = readLocationsCount(parser);
                route.locCntExplored = locs[0];
                route.locCntTotal = locs[1];
            } else {
                skip(parser);
            }
        }
        return route;
    }


    private int[] readLocationsCount(XmlPullParser parser) throws XmlPullParserException, IOException {
        int loc_cnt_explored = 0;
        int loc_cnt_total = 0;

        parser.require(XmlPullParser.START_TAG, ns, "Locations");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("explored")) {
                loc_cnt_explored = parseInt(readText(parser));
            } else if (name.equals("total")) {
                loc_cnt_total = parseInt(readText(parser));
            } else {
                skip(parser);
            }
        }

        return new int[] {loc_cnt_explored, loc_cnt_total};
    }


}