package nl.trifork.elasticsearch.facet.geohash;

import java.util.Map;

import org.apache.lucene.util.BytesRef;
import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.common.geo.GeoPoint;

/**
 * Modified from the original on https://github.com/zenobase/geocluster-facet/blob/master/src/main/java/com/zenobase/search/facet/geocluster/GeoClusterBuilder.java
 */
public class ClusterBuilder {

	private final int geohashBits;
	private final Map<Long, Cluster> clusters = Maps.newHashMap();

	public ClusterBuilder(int precisionBits) {
        this.geohashBits = precisionBits;
	}

	public ClusterBuilder add(TypeAndId typeAndId, GeoPoint point) {
        long geohash = BinaryGeoHashUtils.encodeAsLong(point, geohashBits);
        if (clusters.containsKey(geohash)) {
            clusters.get(geohash).add(point);
        }
        else {
            if (typeAndId == null) {

                clusters.put(geohash, new Cluster(point, geohash, geohashBits));
            } else {

                clusters.put(geohash, new Cluster(point, geohash, geohashBits, typeAndId));
            }
        }
		return this;
    }

	public ClusterBuilder add(GeoPoint point) {
        return add(null, point);
	}

	public ImmutableList<Cluster> build() {
		return ImmutableList.copyOf(clusters.values());
	}

}
