package cn.staitech.fr.utils.geo;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

/**
 * Created by mihaildoronin on 11/11/15.
 */
public class PolygonParser extends BaseParser{


    public PolygonParser(GeometryFactory geometryFactory) {
        super(geometryFactory);
    }

    public static Polygon polygonFromJson(JsonNode node) {
        JsonNode arrayOfRings = node.get(GeoJson.COORDINATES);
        return polygonFromJsonArrayOfRings(arrayOfRings);
    }

    public static Polygon polygonFromJsonArrayOfRings(JsonNode arrayOfRings) {
        LinearRing shell = linearRingsFromJson(arrayOfRings.get(0));
        int size = arrayOfRings.size();
        LinearRing[] holes = new LinearRing[size - 1];
        for (int i = 1; i < size; i++) {
            holes[i - 1] = linearRingsFromJson(arrayOfRings.get(i));
        }
        return geometryFactory.createPolygon(shell, holes);
    }

    private static LinearRing linearRingsFromJson(JsonNode coordinates) {
        assert coordinates.isArray() : "expected coordinates array";
        return geometryFactory.createLinearRing(PointParser.coordinatesFromJson(coordinates));
    }

    public static Polygon geometryFromJson(JsonNode node) throws JsonMappingException {
        return polygonFromJson(node);
    }
}
