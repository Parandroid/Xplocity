package xml_parsers;


import android.util.Xml;

import org.osmdroid.util.GeoPoint;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import models.Location;
import models.Route;
import models.enums.LocationExploreState;
import models.enums.TravelTypes;


/**
 * Created by dmitry on 02.08.17.
 */

public class XMLRouteParser extends XMLAbstractParser {
    // We don't use namespaces
    private static final String ns = null;
    private Route route;

    public Route parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readRoute(parser);
        } finally {
            in.close();
        }
    }


    private Route readRoute(XmlPullParser parser) throws XmlPullParserException, IOException {
        route = new Route();

        parser.require(XmlPullParser.START_TAG, ns, "Chain");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("date")) {
                String str_date = readText(parser);
                try {
                    if ((str_date != "") && (str_date != ""))
                        route.date = parseDate(str_date);
                } catch (ParseException e) {

                }

            } else if (name.equals("distance")) {
                route.distance = parseInt(readText(parser));
            } else if (name.equals("duration")) {
                route.duration = parseInt(readText(parser));
            } else if (name.equals("travel_type")) {
                String tt = readText(parser);

                switch (tt) {
                    case "CYCLING":
                    case "cycling":
                        route.travelType = TravelTypes.CYCLING;
                        break;
                    case "WALKING":
                    case "walking":
                        route.travelType = TravelTypes.WALKING;
                        break;
                    default:
                        route.travelType = TravelTypes.WALKING;
                }


            } else if (name.equals("Locations")) {
                readLocations(parser);
            } else if (name.equals("Route")) {
                route.path = Route.string_to_route(readText(parser));
            } else {
                skip(parser);
            }
        }
        return route;
    }


    private void readLocations(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, ns, "Locations");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Location")) {
                Location loc = readLocation(parser);

                route.loc_cnt_total++;
                if (loc.explored()) {
                    route.loc_cnt_explored++;
                }
                route.locations.add(loc);
            } else {
                skip(parser);
            }
        }
    }


    private Location readLocation(XmlPullParser parser) throws XmlPullParserException, IOException {
        Location loc = new Location();
        parser.require(XmlPullParser.START_TAG, ns, "Location");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("explored")) {
                loc.exploreState = parseLocationExploreState(readText(parser));
            } else if (name.equals("explored_date")) {
                String str_date = readText(parser);
                try {
                    if ((str_date != "") && (str_date != ""))
                        loc.dateReached = parseDate(str_date);
                } catch (ParseException e) {

                }
            } else if (name.equals("name")) {
                loc.name = readText(parser);
            } else if (name.equals("description")) {
                loc.description = readText(parser);
            } else if (name.equals("address")) {
                loc.address = readText(parser);
            } else if (name.equals("Position")) {
                loc.position = readPosition(parser);
            } else {
                skip(parser);
            }
        }

        if (loc.exploreState == LocationExploreState.CIRCLE) {
            loc.setStateCircle();
            loc.hasCircle = true;
        }


        return loc;
    }


    private LocationExploreState parseLocationExploreState(String explored) {
        switch (explored) {
            case "0":
                return LocationExploreState.POINT_NOT_EXPLORED;
            case "1":
                return LocationExploreState.POINT_EXPLORED;
            case "2":
            default:
                return LocationExploreState.CIRCLE;
        }
    }


    private GeoPoint readPosition(XmlPullParser parser) throws XmlPullParserException, IOException {
        double lat = 0d;
        double lon = 0d;
        parser.require(XmlPullParser.START_TAG, ns, "Position");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("latitude")) {
                lat = Double.parseDouble(readText(parser));
            } else if (name.equals("longitude")) {
                lon = Double.parseDouble(readText(parser));
            } else {
                skip(parser);
            }


        }
        GeoPoint pos = new GeoPoint(lat, lon);
        return pos;
    }


}