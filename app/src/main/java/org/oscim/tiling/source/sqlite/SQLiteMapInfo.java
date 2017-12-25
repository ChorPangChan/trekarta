package org.oscim.tiling.source.sqlite;

import org.oscim.core.BoundingBox;

/**
 * Contains the immutable metadata of a map path.
 */
public class SQLiteMapInfo {
    /**
     * The bounding box of the map path.
     */
    public final BoundingBox boundingBox;

    /**
     * The name of the map.
     */
    public final String name;

    /**
     * The attribution of the map;
     */
    public final String attribution;

    SQLiteMapInfo(String name, String attribution, BoundingBox boundingBox) {
        this.name = name;
        this.attribution = attribution;
        this.boundingBox = boundingBox;
    }
}
