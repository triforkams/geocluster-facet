package nl.trifork.elasticsearch.facet.geohash;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.facet.FacetBuilder;

// TODO: remove?
public class GeohashFacetBuilder extends FacetBuilder {

	private final String fieldName;
	private final double factor;

	public GeohashFacetBuilder(String name, String fieldName, double factor) {
		super(name);
		this.fieldName = fieldName;
		this.factor = factor;
	}

	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(name);
		builder.startObject(GeohashFacet.TYPE);
		builder.field("field", fieldName);
		builder.field("factor", factor);
		builder.endObject();
		addFilterFacetAndGlobal(builder, params);
		builder.endObject();
		return builder;
	}
}
