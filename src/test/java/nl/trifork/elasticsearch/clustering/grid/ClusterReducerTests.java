package nl.trifork.elasticsearch.clustering.grid;

import nl.trifork.elasticsearch.facet.geohash.Cluster;
import nl.trifork.elasticsearch.facet.geohash.ClusterReducer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static nl.trifork.elasticsearch.clustering.grid.test.Places.DENVER;
import static nl.trifork.elasticsearch.clustering.grid.test.Places.LAS_VEGAS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ClusterReducerTests {

    private ClusterReducer clusterReducer;

    @BeforeMethod
    public void setUp() throws Exception {
        clusterReducer = new ClusterReducer();
    }

    @Test
    public void testOneCluster() throws Exception {

        Iterable<Cluster> clusters = Arrays.asList(new Cluster(DENVER, "9xj6js7bxch6"));

        List<Cluster> reduced = clusterReducer.reduce(clusters);

        assertThat(reduced.size(), is(1));
    }

    @Test
    public void testTwoClustersWithDifferentGeohashes() throws Exception {

        Iterable<Cluster> clusters = Arrays.asList(
                new Cluster(DENVER, "9xj6js7bxch6"),
                new Cluster(LAS_VEGAS, "9qqj70pv0rrx")
        );

        List<Cluster> reduced = clusterReducer.reduce(clusters);

        assertThat(reduced.size(), is(2));

    }

    @Test
    public void testTwoClustersWithSameGeohash() throws Exception {

        Iterable<Cluster> clusters = Arrays.asList(
                new Cluster(DENVER, "9"),
                new Cluster(LAS_VEGAS, "9")
        );

        List<Cluster> reduced = clusterReducer.reduce(clusters);

        assertThat(reduced.size(), is(1));
        assertThat(reduced.get(0).size(), is(2));
    }
}
