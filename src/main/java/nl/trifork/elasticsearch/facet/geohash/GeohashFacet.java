package nl.trifork.elasticsearch.facet.geohash;

import java.util.List;

import org.elasticsearch.search.facet.Facet;

public interface GeohashFacet extends Facet, Iterable<Cluster> {

	/**
	 * The type of the filter facet.
	 */
	public String TYPE = "geohash";

	/**
	 * A list of geo clusters.
	 */
	List<Cluster> getEntries();
}
