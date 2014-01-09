package nl.trifork.elasticsearch.facet.geohash;

import java.io.IOException;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.index.AtomicReaderContext;
import org.elasticsearch.index.fielddata.GeoPointValues;
import org.elasticsearch.index.fielddata.IndexGeoPointFieldData;
import org.elasticsearch.index.fielddata.BytesValues;
import org.elasticsearch.index.fielddata.IndexFieldData;
import org.elasticsearch.search.facet.FacetExecutor;
import org.elasticsearch.search.facet.InternalFacet;

import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.mapper.Uid;

public class GeohashFacetExecutor extends FacetExecutor {

	private final IndexGeoPointFieldData<?> indexFieldData;
	private final IndexFieldData<?> idIndexFieldData;
	private final double factor;
    private final boolean showGeohashCell;
    private final boolean showDocuments;
	private final ClusterBuilder builder;
	
	public GeohashFacetExecutor(IndexGeoPointFieldData<?> indexFieldData, IndexFieldData<?> idIndexFieldData, 
			                    double factor, boolean showGeohashCell, boolean showDocuments) {
		this.indexFieldData = indexFieldData;
		this.idIndexFieldData = idIndexFieldData;
		this.factor = factor;
        this.showGeohashCell = showGeohashCell;
        this.showDocuments = showDocuments;
		this.builder = new ClusterBuilder(factor, showDocuments);
	}

	@Override
	public FacetExecutor.Collector collector() {
		return new Collector();
	}

	@Override
	public InternalFacet buildFacet(String facetName) {
		return new InternalGeohashFacet(facetName, factor, showGeohashCell, showDocuments, builder.build());
	}

	private class Collector extends FacetExecutor.Collector {

		private BytesValues ids;
		private GeoPointValues values;

		@Override
		public void setNextReader(AtomicReaderContext context) throws IOException {
			ids = idIndexFieldData.load(context).getBytesValues(false);
			//ordinals = ids.ordinals();
			values = indexFieldData.load(context).getGeoPointValues();
		}

		@Override
		public void collect(int docId) throws IOException {
            final int length_ = ids.setDocument(docId);
            final int length = values.setDocument(docId);
            
            String _id;
            if (length_ > 0) {
            	_id = Uid.idFromUid(ids.nextValue().utf8ToString());
            } else {
            	_id = new String();
            }
            
            for (int i = 0; i < length; i++) {
            	GeoPoint gp = GeoPoints.copy(values.nextValue()); // nextValue() may recycle GeoPoint instances!
            	
                if(showDocuments) {
                    builder.add(_id, gp);
                } else {
                    builder.add(gp);
                }
            }
		}

		@Override
		public void postCollection() {

		}
	}
}
