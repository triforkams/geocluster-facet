package nl.trifork.elasticsearch.facet.geohash;

import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class ClusterReducer {

	public List<Cluster> reduce(Iterable<Cluster> clusters) {
        Map<String, Cluster> map = Maps.newHashMap();
        for (Cluster cluster : clusters) {
            String clusterGeohash = cluster.clusterGeohash();
            if (map.containsKey(clusterGeohash)) {
                map.put(clusterGeohash, map.get(clusterGeohash).merge(cluster));
            }
            else {
                map.put(clusterGeohash, cluster);
            }
        }
        return Lists.newArrayList(map.values());
	}

}
