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
    private static final int GEOHASH_MAX_LENGTH = 12;

	private final double factor;
    private final boolean showDocuments;
	private final int prefixLength;
	private final Map<String, Cluster> clusters = Maps.newHashMap();

	public ClusterBuilder(double factor, boolean showDocuments) {
		this.factor = factor;
        this.showDocuments = showDocuments;
        this.prefixLength = GEOHASH_MAX_LENGTH - (int) Math.round(factor * GEOHASH_MAX_LENGTH);
	}

    public ClusterBuilder add(String docId, GeoPoint point) {
        String prefix = point.geohash().substring(0, prefixLength);
        if (clusters.containsKey(prefix)) {
            clusters.get(prefix).add(point);
        }
        else {
            clusters.put(prefix, new Cluster(point, prefix, docId));
        }
		return this;
    }

	public ClusterBuilder add(GeoPoint point) {
        return add(new String(), point);
	}

	public ImmutableList<Cluster> build() {
		return ImmutableList.copyOf(clusters.values());
	}

}
