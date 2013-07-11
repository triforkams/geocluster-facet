package nl.trifork.elasticsearch.clustering.grid.test;

import nl.trifork.elasticsearch.facet.geohash.BoundingBox;
import org.elasticsearch.common.geo.GeoPoint;

public interface Places {

	BoundingBox COLORADO = new BoundingBox(new GeoPoint(41.00, -109.05), new GeoPoint(37.00, -102.04));

	GeoPoint DENVER = new GeoPoint(39.75, -104.87);
	GeoPoint LAS_VEGAS = new GeoPoint(36.08, -115.17);
	GeoPoint SAN_DIEGO = new GeoPoint(32.82, -117.13);
}
