package nl.trifork.elasticsearch.facet.geohash;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;
import org.elasticsearch.index.fielddata.GeoPointValues;
import org.elasticsearch.index.fielddata.GeoPointValues.Iter;
import org.elasticsearch.index.fielddata.IndexGeoPointFieldData;
import org.elasticsearch.search.facet.FacetExecutor;
import org.elasticsearch.search.facet.InternalFacet;

public class GeohashFacetExecutor extends FacetExecutor {

	private final IndexGeoPointFieldData<?> indexFieldData;
	private final double factor;
	private final ClusterBuilder builder;

	public GeohashFacetExecutor(IndexGeoPointFieldData<?> indexFieldData, double factor) {
		this.indexFieldData = indexFieldData;
		this.factor = factor;
		this.builder = new ClusterBuilder(factor);
	}

	@Override
	public FacetExecutor.Collector collector() {
		return new Collector();
	}

	@Override
	public InternalFacet buildFacet(String facetName) {
		return new InternalGeohashFacet(facetName, factor, builder.build());
	}

	private class Collector extends FacetExecutor.Collector {

		private GeoPointValues values;

		@Override
		public void setNextReader(AtomicReaderContext context) throws IOException {
			values = indexFieldData.load(context).getGeoPointValues();
		}

		@Override
		public void collect(int docId) throws IOException {
			for (Iter iter = values.getIter(docId); iter.hasNext();) {
				builder.add(GeoPoints.copy(iter.next())); // iter.next() recycles GeoPoint instances!
			}
		}

		@Override
		public void postCollection() {

		}
	}
}
