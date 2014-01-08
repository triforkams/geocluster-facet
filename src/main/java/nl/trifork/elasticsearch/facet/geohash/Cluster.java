package nl.trifork.elasticsearch.facet.geohash;

import java.io.IOException;
import java.util.Arrays;

import org.elasticsearch.common.Preconditions;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

/**
 * Modified from the original on https://github.com/zenobase/geocluster-facet/blob/master/src/main/java/com/zenobase/search/facet/geocluster/GeoCluster.java
 */
public class Cluster {

	private int size;
	private GeoPoint center;
    private String clusterGeohash;
    private String docId;
	private BoundingBox bounds;

    /**
     * @param clusterGeohash - geohash of the cluster. Must be a prefix of geoItem.geoPoint().geoHash()
     */
    
	public Cluster(GeoPoint point, String clusterGeohash) {
        this(1, point, clusterGeohash, new String(), new BoundingBox(point));

	}

	public Cluster(GeoPoint point, String clusterGeohash, String docId) {
        this(1, point, clusterGeohash, docId, new BoundingBox(point));

	}

	public Cluster(int size, GeoPoint center, String clusterGeohash, String docId, BoundingBox bounds) {
        Preconditions.checkArgument(center.getGeohash().startsWith(clusterGeohash));

		this.size = size;
		this.center = center;
        this.clusterGeohash = clusterGeohash;
        this.docId = docId;
		this.bounds = bounds;
	}

	public void add(GeoPoint point) {
        Preconditions.checkArgument(point.geohash().startsWith(clusterGeohash));

		++size;
		center = mean(center, size - 1, point, 1);
		bounds = bounds.extend(point);
	}

	public Cluster merge(Cluster that) {
        Preconditions.checkArgument(clusterGeohash.equals(that.clusterGeohash()));

		GeoPoint center = mean(this.center, this.size(), that.center(), that.size());
		return new Cluster(this.size + that.size(),
                center, this.clusterGeohash, new String(), this.bounds.extend(that.bounds()));
	}

	private static GeoPoint mean(GeoPoint left, int leftWeight, GeoPoint right, int rightWeight) {
		double lat = (left.getLat() * leftWeight + right.getLat() * rightWeight) / (leftWeight + rightWeight);
		double lon = (left.getLon() * leftWeight + right.getLon() * rightWeight) / (leftWeight + rightWeight);
		return new GeoPoint(lat, lon);
	}

	public int size() {
		return size;
	}

	public GeoPoint center() {
		return center;
	}

	public BoundingBox bounds() {
		return bounds;
	}

    public String clusterGeohash() {
        return clusterGeohash;
    }

    public String docId() {
        return docId;
    }

    public static Cluster readFrom(StreamInput in) throws IOException {
		int size = in.readVInt();
		GeoPoint center = GeoPoints.readFrom(in);
        String docId = in.readString();
        String clusterGeohash = in.readString();
		BoundingBox bounds = size > 1
			? BoundingBox.readFrom(in)
			: new BoundingBox(center, center);
		return new Cluster(size, center, clusterGeohash, docId, bounds);
	}

	public void writeTo(StreamOutput out) throws IOException {
		out.writeVInt(size);
		GeoPoints.writeTo(center, out);
        out.writeString(clusterGeohash);
		if (size > 1) {
            out.writeString(new String());
			bounds.writeTo(out);
		} else {
            out.writeString(docId);
        }
	}

	@Override
	public boolean equals(Object that) {
		return that instanceof Cluster &&
			equals((Cluster) that);
	}

	private boolean equals(Cluster that) {
		return size == that.size() &&
			GeoPoints.equals(center, that.center()) &&
			bounds.equals(that.bounds()) &&
            clusterGeohash.equals(that.clusterGeohash());
	}

	@Override
	public int hashCode() {
		return hashCode(size, center.toString(), clusterGeohash, bounds);
	}

	private static int hashCode(Object... objects) {
		return Arrays.hashCode(objects);
	}

	@Override
	public String toString() {
		return String.format("%s %s (%d)", GeoPoints.toString(center), clusterGeohash, size);
	}
}
