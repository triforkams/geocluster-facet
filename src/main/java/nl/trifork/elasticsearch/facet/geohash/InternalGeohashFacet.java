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

	private double factor;
    private boolean showGeohashCell;
	private List<Cluster> entries;

	InternalGeohashFacet() {

	}

	public InternalGeohashFacet(String name, double factor, boolean showGeohashCell, List<Cluster> entries) {
		super(name);
		this.factor = factor;
        this.showGeohashCell =showGeohashCell;
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
		return new InternalGeohashFacet(getName(), factor, showGeohashCell, reduced);
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
		factor = in.readDouble();
        showGeohashCell = in .readBoolean();
		entries = Lists.newArrayList();
		for (int i = 0, max = in.readVInt(); i < max; ++i) {
			entries.add(Cluster.readFrom(in));
		}
	}

	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeDouble(factor);
        out.writeBoolean(showGeohashCell);
		out.writeVInt(entries.size());
		for (Cluster entry : entries) {
			entry.writeTo(out);
		}
	}


    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(getName());
        builder.field(Fields._TYPE, TYPE);
        builder.field(Fields.FACTOR, factor);
        builder.startArray(Fields.CLUSTERS);
        for (Cluster entry : entries) {
            toXContent(entry, builder);
        }
        builder.endArray();
        builder.endObject();
        return builder;
    }

    public double factor() {
        return factor;
    }

    public boolean showGeohashCell() {
        return showGeohashCell;
    }

	private interface Fields {

		final XContentBuilderString _TYPE = new XContentBuilderString("_type");
		final XContentBuilderString FACTOR = new XContentBuilderString("factor");
		final XContentBuilderString CLUSTERS = new XContentBuilderString("clusters");
		final XContentBuilderString TOTAL = new XContentBuilderString("total");
		final XContentBuilderString CENTER = new XContentBuilderString("center");
		final XContentBuilderString TOP_LEFT = new XContentBuilderString("top_left");
		final XContentBuilderString BOTTOM_RIGHT = new XContentBuilderString("bottom_right");
		final XContentBuilderString LAT = new XContentBuilderString("lat");
		final XContentBuilderString LON = new XContentBuilderString("lon");
		final XContentBuilderString GEOHASH_CELL = new XContentBuilderString("geohash_cell");
	}

	private void toXContent(Cluster cluster, XContentBuilder builder) throws IOException {
		builder.startObject();
		builder.field(Fields.TOTAL, cluster.size());
		toXContent(cluster.center(), Fields.CENTER, builder);
		if (cluster.size() > 1) {
			toXContent(cluster.bounds().topLeft(), Fields.TOP_LEFT, builder);
			toXContent(cluster.bounds().bottomRight(), Fields.BOTTOM_RIGHT, builder);
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
