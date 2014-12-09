package nl.trifork.elasticsearch.facet.geohash;

import java.io.IOException;

import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.fielddata.IndexGeoPointFieldData;
import org.elasticsearch.index.fielddata.IndexFieldData;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.search.facet.FacetExecutor;
import org.elasticsearch.search.facet.FacetExecutor.Mode;
import org.elasticsearch.search.facet.FacetParser;
import org.elasticsearch.search.facet.FacetPhaseExecutionException;
import org.elasticsearch.search.internal.SearchContext;

/**
 * Modified from the original on https://github.com/zenobase/geocluster-facet/blob/master/src/main/java/com/zenobase/search/facet/geocluster/GeoClusterFacetParser.java
 */
public class GeohashFacetParser extends AbstractComponent implements FacetParser {

	@Inject
	public GeohashFacetParser(Settings settings) {
		super(settings);
		InternalGeohashFacet.registerStreams();
	}

	@Override
	public String[] types() {
		return new String[] {
			GeohashFacet.TYPE
		};
	}

	@Override
	public Mode defaultMainMode() {
		return FacetExecutor.Mode.COLLECTOR;
	}

	@Override
	public Mode defaultGlobalMode() {
		return FacetExecutor.Mode.COLLECTOR;
	}

	@Override
	public FacetExecutor parse(String facetName, XContentParser parser, SearchContext context) throws IOException {

		String fieldName = null;
		double factor = 0.1;
		int precisionBits = -1;
        boolean showGeohashCell = false;
        boolean showDocumentId = false;

		String currentName = parser.currentName();
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentName = parser.currentName();
			} else if (token.isValue()) {
				if ("field".equals(currentName)) {
					fieldName = parser.text();
				} else if ("factor".equals(currentName)) {
					factor = parser.doubleValue();
				} else if ("precision_bits".equals(currentName)) {
					precisionBits = parser.intValue();
				} else if ("show_geohash_cell".equals(currentName)) {
					showGeohashCell = parser.booleanValue();
				} else if ("show_doc_id".equals(currentName)) {
                    showDocumentId = parser.booleanValue();
                }
			}
		}

		if (precisionBits != -1) {
			if (precisionBits < 0.0 || precisionBits > BinaryGeoHashUtils.MAX_PREFIX_LENGTH) {
				throw new FacetPhaseExecutionException(facetName, "precision_bits value [" + precisionBits + "] is not in range [0, " + BinaryGeoHashUtils.MAX_PREFIX_LENGTH + "]");
			}
		} else {
			if (factor < 0.0 || factor > 1.0) {
				throw new FacetPhaseExecutionException(facetName, "factor value [" + factor + "] is not in range [0.0, 1.0]");
			}
			precisionBits = BinaryGeoHashUtils.MAX_PREFIX_LENGTH - (int) Math.round(factor * BinaryGeoHashUtils.MAX_PREFIX_LENGTH);
		}

		FieldMapper<?> fieldMapper = context.smartNameFieldMapper(fieldName);
        if (fieldMapper == null) {
            throw new FacetPhaseExecutionException(facetName, "failed to find mapping for [" + fieldName + "]");
        }
        IndexGeoPointFieldData<?> indexFieldData = context.fieldData().getForField(fieldMapper);
        
        FieldMapper<?> idFieldMapper = context.smartNameFieldMapper("_uid");
        if (idFieldMapper == null) {
            throw new FacetPhaseExecutionException(facetName, "failed to find mapping for [_id]");
        }
        IndexFieldData<?> idIndexFieldData = context.fieldData().getForField(idFieldMapper);
		return new GeohashFacetExecutor(indexFieldData, idIndexFieldData, precisionBits, showGeohashCell, showDocumentId);
	}
}
