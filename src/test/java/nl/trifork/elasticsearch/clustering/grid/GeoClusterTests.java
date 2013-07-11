package nl.trifork.elasticsearch.clustering.grid;

import static nl.trifork.elasticsearch.clustering.grid.test.GeoPointMatchers.closeTo;
import static nl.trifork.elasticsearch.clustering.grid.test.Places.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;

import nl.trifork.elasticsearch.facet.geohash.BoundingBox;
import nl.trifork.elasticsearch.facet.geohash.Cluster;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.io.stream.BytesStreamInput;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.junit.Ignore;
import org.testng.annotations.Test;

@Ignore
public class GeoClusterTests {

}
