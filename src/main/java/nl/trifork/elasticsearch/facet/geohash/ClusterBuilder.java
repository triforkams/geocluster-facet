package nl.trifork.elasticsearch.facet.geohash;

import java.util.Map;

import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.common.geo.GeoPoint;

/**
 * Modified from the original on https://github.com/zenobase/geocluster-facet/blob/master/src/main/java/com/zenobase/search/facet/geocluster/GeoClusterBuilder.java
 */
public class ClusterBuilder {
    private static final int GEOHASH_MAX_LENGTH = 12;

	private final double factor;
	private final int prefixLength;
	private final Map<String, Cluster> clusters = Maps.newHashMap();

	public ClusterBuilder(double factor) {
		this.factor = factor;
        this.prefixLength = GEOHASH_MAX_LENGTH - (int) Math.round(factor * GEOHASH_MAX_LENGTH);
	}

	public ClusterBuilder add(GeoPoint point) {
        String prefix = point.geohash().substring(0, prefixLength);
        if (clusters.containsKey(prefix)) {
            clusters.get(prefix).add(point);
        }
        else {
            clusters.put(prefix, new Cluster(point, prefix));
        }
		return this;
	}

	public ImmutableList<Cluster> build() {
		return ImmutableList.copyOf(clusters.values());
	}

}
