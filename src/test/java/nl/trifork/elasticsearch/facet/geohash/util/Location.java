package nl.trifork.elasticsearch.facet.geohash.util;

public class Location {
    private String id;
    private LatLon latLon;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LatLon getLatLon() {
        return latLon;
    }

    public void setLatLon(LatLon latLon) {
        this.latLon = latLon;
    }
}
