package nl.trifork.elasticsearch.facet.geohash;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilderException;
import org.elasticsearch.search.facet.FacetBuilder;

public class GeoFacetBuilder extends FacetBuilder {
    private String fieldName;
    private int precisionBits;
    private boolean showGeohashCell;
    private boolean showDocId;

    /**
     * Construct a new term facet with the provided facet name.
     *
     * @param name The facet name.
     */
    public GeoFacetBuilder(String name) {
        super(name);
    }

    /**
     * The field the terms will be collected from.
     */
    public GeoFacetBuilder field(String field) {
        this.fieldName = field;
        return this;
    }

    public GeoFacetBuilder showGeohashCell(boolean showGeohashCell) {
        this.showGeohashCell = showGeohashCell;
        return this;
    }

    public GeoFacetBuilder showDocId(boolean showDocId) {
        this.showDocId = showDocId;
        return this;
    }

    public GeoFacetBuilder precisionBits(int precisionBits) {
        this.precisionBits = precisionBits;
        return this;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        if (fieldName == null) {
            throw new SearchSourceBuilderException("field must be set facet for facet [" + name + "]");
        }
        builder.startObject(name);

        builder.startObject("geohash");
        builder.field("field", fieldName);
        builder.field("precision_bits", precisionBits);
        builder.field("show_geohash_cell", showGeohashCell);
        builder.field("show_doc_id", showDocId);

        builder.endObject();
        addFilterFacetAndGlobal(builder, params);
        builder.endObject();
        return builder;
    }
}
