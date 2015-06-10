package nl.trifork.elasticsearch.facet.geohash;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.index.fielddata.*;
import org.elasticsearch.search.facet.FacetExecutor;
import org.elasticsearch.search.facet.InternalFacet;

import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.mapper.Uid;

public class GeohashFacetExecutor extends FacetExecutor {

	private final IndexGeoPointFieldData indexFieldData;
	private final IndexFieldData<?> idIndexFieldData;
	private final double factor;
    private final boolean showGeohashCell;
    private final boolean showDocumentId;
	private final ClusterBuilder builder;
	
	public GeohashFacetExecutor(IndexGeoPointFieldData indexFieldData, IndexFieldData<?> idIndexFieldData,
			                    double factor, boolean showGeohashCell, boolean showDocumentId) {
		this.indexFieldData = indexFieldData;
		this.idIndexFieldData = idIndexFieldData;
		this.factor = factor;
        this.showGeohashCell = showGeohashCell;
        this.showDocumentId = showDocumentId;
		this.builder = new ClusterBuilder(factor);
	}

	@Override
	public FacetExecutor.Collector collector() {
		return new Collector();
	}

	@Override
	public InternalFacet buildFacet(String facetName) {
		return new InternalGeohashFacet(facetName, factor, showGeohashCell, showDocumentId, builder.build());
	}

	private class Collector extends FacetExecutor.Collector {

		private SortedBinaryDocValues ids;
		private MultiGeoPointValues values;

		@Override
		public void setNextReader(AtomicReaderContext context) throws IOException {
			ids = idIndexFieldData.load(context).getBytesValues();
			//ordinals = ids.ordinals();
			values = indexFieldData.load(context).getGeoPointValues();
		}

		@Override
		public void collect(int docId) throws IOException {
            ids.setDocument(docId);
            values.setDocument(docId);
            
            TypeAndId typeAndId = null;
            if (ids.count() > 0) {
                BytesRef[] bytesRefs =  Uid.splitUidIntoTypeAndId(ids.valueAt(0));
                typeAndId = new TypeAndId(bytesRefs[0].utf8ToString(), bytesRefs[1].utf8ToString());
            }
            
            for (int i = 0; i < values.count(); i++) {
            	GeoPoint gp = GeoPoints.copy(values.valueAt(i));
            	
                if(showDocumentId) {
                    builder.add(typeAndId, gp);
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
