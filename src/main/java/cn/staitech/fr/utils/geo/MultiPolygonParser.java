package cn.staitech.fr.utils.geo;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;


/**
 * Created by mihaildoronin on 11/11/15.
 */
public class MultiPolygonParser extends BaseParser{

    public MultiPolygonParser(GeometryFactory geometryFactory) {
        super(geometryFactory);
    }

    public static MultiPolygon multiPolygonFromJson(JsonNode root) {
        JsonNode arrayOfPolygons = root.get(GeoJson.COORDINATES);
        return geometryFactory.createMultiPolygon(polygonsFromJson(arrayOfPolygons));
    }

    private static Polygon[] polygonsFromJson(JsonNode arrayOfPolygons) {
        Polygon[] polygons = new Polygon[arrayOfPolygons.size()];
        for (int i = 0; i != arrayOfPolygons.size(); ++i) {
            polygons[i] = PolygonParser.polygonFromJsonArrayOfRings(arrayOfPolygons.get(i));
        }
        return polygons;
    }

    public static MultiPolygon geometryFromJson(JsonNode node) throws JsonMappingException {
        return multiPolygonFromJson(node);
    }
}
