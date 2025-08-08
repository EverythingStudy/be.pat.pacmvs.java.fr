package cn.staitech.fr.utils.geo;

import org.locationtech.jts.geom.GeometryFactory;

/**
 * Created by mihaildoronin on 11/11/15.
 */
public class BaseParser {

    protected static GeometryFactory geometryFactory = new GeometryFactory();

    public BaseParser(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

}
