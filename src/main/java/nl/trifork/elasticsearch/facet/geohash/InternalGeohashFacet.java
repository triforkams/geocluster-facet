package nl.trifork.elasticsearch.facet.geohash;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.bytes.HashedBytesArray;
import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.geo.GeoHashUtils;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentBuilderString;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.InternalFacet;

public class InternalGeohashFacet extends InternalFacet implements GeohashFacet {

	private static final BytesReference STREAM_TYPE = new HashedBytesArray("geohashGroup".getBytes());

	private static InternalFacet.Stream STREAM = new Stream() {

		@Override
		public Facet readFacet(StreamInput in) throws IOException {
			return readGeoClusterFacet(in);
		}
	};

    public static void registerStreams() {
		Streams.registerStream(STREAM, STREAM_TYPE);
	}

	private int precisionBits;
    private boolean showGeohashCell;
    private boolean showDocuments;
	private List<Cluster> entries;

	InternalGeohashFacet() {

	}

	public InternalGeohashFacet(String name, int precisionBits, boolean showGeohashCell, boolean showDocuments, List<Cluster> entries) {
		super(name);
		this.precisionBits = precisionBits;
        this.showGeohashCell = showGeohashCell;
        this.showDocuments = showDocuments;
		this.entries = entries;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public BytesReference streamType() {
		return STREAM_TYPE;
	}

	@Override
	public List<Cluster> getEntries() {
		return ImmutableList.copyOf(entries);
	}

	@Override
	public Iterator<Cluster> iterator() {
		return getEntries().iterator();
	}

	@Override
	public Facet reduce(ReduceContext context) {
		ClusterReducer reducer = new ClusterReducer();
		List<Cluster> reduced = reducer.reduce(flatMap(context.facets()));
		return new InternalGeohashFacet(getName(), precisionBits, showGeohashCell, showDocuments, reduced);
	}

	private List<Cluster> flatMap(Iterable<Facet> facets) {
		List<Cluster> entries = Lists.newArrayList();
		for (Facet facet : facets) {
			entries.addAll(((GeohashFacet) facet).getEntries());
		}
		return entries;
	}

	public static InternalGeohashFacet readGeoClusterFacet(StreamInput in) throws IOException {
		InternalGeohashFacet facet = new InternalGeohashFacet();
		facet.readFrom(in);
		return facet;
	}

	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		precisionBits = in.readInt();
        showGeohashCell = in .readBoolean();
        showDocuments = in.readBoolean();
        int entriesCount = in.readVInt();
		entries = Lists.newArrayList();
		for (int i = 0, max = entriesCount; i < max; ++i) {
			entries.add(Cluster.readFrom(in));
		}
	}

	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeInt(precisionBits);
        out.writeBoolean(showGeohashCell);
        out.writeBoolean(showDocuments);
		out.writeVInt(entries.size());
		for (Cluster entry : entries) {
			entry.writeTo(out);
		}
	}


    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(getName());
        builder.field(Fields._TYPE, TYPE);
        builder.field(Fields.PRECISION_BITS, precisionBits);
        double factor = (1.0 * BinaryGeoHashUtils.MAX_PREFIX_LENGTH - precisionBits) / BinaryGeoHashUtils.MAX_PREFIX_LENGTH;
        builder.field(Fields.FACTOR, factor);
        builder.field(Fields.CLUSTERS_TOTAL, entries.size());
        builder.startArray(Fields.CLUSTERS);
        for (Cluster entry : entries) {
            toXContent(entry, builder);
        }
        builder.endArray();
        builder.endObject();
        return builder;
    }

    public double precisionBits() {
        return precisionBits;
    }

    public boolean showGeohashCell() {
        return showGeohashCell;
    }

    public boolean showDocuments() {
        return showDocuments;
    }

	private interface Fields {

		final XContentBuilderString _TYPE = new XContentBuilderString("_type");
		final XContentBuilderString PRECISION_BITS = new XContentBuilderString("precision_bits");
		final XContentBuilderString FACTOR = new XContentBuilderString("factor");
		final XContentBuilderString CLUSTERS_TOTAL = new XContentBuilderString("clusters_total");
		final XContentBuilderString CLUSTERS = new XContentBuilderString("clusters");
		final XContentBuilderString TOTAL = new XContentBuilderString("total");
		final XContentBuilderString CENTER = new XContentBuilderString("center");
		final XContentBuilderString TOP_LEFT = new XContentBuilderString("top_left");
		final XContentBuilderString BOTTOM_RIGHT = new XContentBuilderString("bottom_right");
		final XContentBuilderString LAT = new XContentBuilderString("lat");
		final XContentBuilderString LON = new XContentBuilderString("lon");
		final XContentBuilderString GEOHASH_CELL = new XContentBuilderString("geohash_cell");
        final XContentBuilderString DOC_ID = new XContentBuilderString("doc_id");
        final XContentBuilderString DOC_TYPE = new XContentBuilderString("doc_type");
    }

	private void toXContent(Cluster cluster, XContentBuilder builder) throws IOException {
		builder.startObject();
		builder.field(Fields.TOTAL, cluster.size());
		toXContent(cluster.center(), Fields.CENTER, builder);
		if (cluster.size() > 1) {
			toXContent(cluster.bounds().topLeft(), Fields.TOP_LEFT, builder);
			toXContent(cluster.bounds().bottomRight(), Fields.BOTTOM_RIGHT, builder);
		} else if (showDocuments) {
			builder.field(Fields.DOC_TYPE, cluster.typeAndId().type());
			builder.field(Fields.DOC_ID, cluster.typeAndId().id());
        }
        if (showGeohashCell) {
            addGeohashCell(cluster, builder);
        }
		builder.endObject();
	}

    private void addGeohashCell(Cluster cluster, XContentBuilder builder) throws IOException {
        builder.startObject(Fields.GEOHASH_CELL);
        GeoPoint geohashCellTopLeft = new GeoPoint();
        GeoPoint geohashCellBottomRight = new GeoPoint();
        BinaryGeoHashUtils.decodeCell(cluster.clusterGeohash(), cluster.clusterGeohashBits(), geohashCellTopLeft, geohashCellBottomRight);
        toXContent(geohashCellTopLeft, Fields.TOP_LEFT, builder);
        toXContent(geohashCellBottomRight, Fields.BOTTOM_RIGHT, builder);
        builder.endObject();
    }

	private static void toXContent(GeoPoint point, XContentBuilderString field, XContentBuilder builder) throws IOException {
		builder.startObject(field);
		builder.field(Fields.LAT, point.getLat());
		builder.field(Fields.LON, point.getLon());
		builder.endObject();
	}
}
