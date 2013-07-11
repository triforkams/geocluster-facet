package nl.trifork.elasticsearch.facet.geohash;

import org.elasticsearch.common.inject.Module;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.search.facet.FacetModule;
import org.elasticsearch.search.facet.TransportFacetModule;

public class GeohashFacetPlugin extends AbstractPlugin {

	@Override
	public String name() {
		return "geohash-facet";
	}

	@Override
	public String description() {
		return "Facet for clustering geo points based on their geohash";
	}

    @Override
    public void processModule(Module module) {
        if (module instanceof FacetModule) {
            ((FacetModule) module).addFacetProcessor(GeohashFacetParser.class);
            InternalGeohashFacet.registerStreams();
        }
        if (module instanceof TransportFacetModule) {
            InternalGeohashFacet.registerStreams();
        }
    }
}
