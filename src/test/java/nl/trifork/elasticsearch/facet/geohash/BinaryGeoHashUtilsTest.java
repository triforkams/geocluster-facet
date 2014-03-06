package nl.trifork.elasticsearch.facet.geohash;

import nl.trifork.elasticsearch.clustering.grid.test.Places;
import org.elasticsearch.common.geo.GeoPoint;
import org.testng.annotations.Test;
import static nl.trifork.elasticsearch.facet.geohash.BinaryGeoHashUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class BinaryGeoHashUtilsTest {

    private static final double _ = 123.456;

    @Test
    public void encodeAsLong_different_locations() throws Exception {


        for (int bits = 0; bits <= 6; bits++) {

            long denverHash = encodeAsLong(Places.DENVER, bits);
            long sanDiegoHash = encodeAsLong(Places.SAN_DIEGO, bits);

            assertThat(String.format("%016X and %016X don't share the first %d bits", denverHash, sanDiegoHash, bits),
                    denverHash == sanDiegoHash);
        }

        for (int bits = 7; bits <= 60; bits++) {

            long denverHash = encodeAsLong(Places.DENVER, bits);
            long sanDiegoHash = encodeAsLong(Places.SAN_DIEGO, bits);

            assertThat(String.format("%016X and %016X shouldn't share the first %d bits", denverHash, sanDiegoHash, bits),
                    denverHash != sanDiegoHash);
        }
    }

    @Test
    public void decodeCell_point_in_right_half() throws Exception {

        double[] bbox = decodeCell(encodeAsLong(new GeoPoint(0, 90), 1), 1);

        assertThat(bbox[2], closeTo(0, 0.1));
        assertThat(bbox[3], closeTo(180, 0.1));
    }

    @Test
    public void decodeCell_point_in_left_half() throws Exception {

        double[] bbox = decodeCell(encodeAsLong(new GeoPoint(0, -90), 1), 1);

        assertThat(bbox[2], closeTo(-180, 0.1));
        assertThat(bbox[3], closeTo(0, 0.1));
    }

    @Test
    public void decodeCell_point_in_upper_half() throws Exception {

        double[] bbox = decodeCell(encodeAsLong(new GeoPoint(45, 0), 2), 2);

        assertThat(bbox[0], closeTo(0, 0.1));
        assertThat(bbox[1], closeTo(90, 0.1));
    }

    @Test
    public void decodeCell_point_in_lower_half() throws Exception {

        double[] bbox = decodeCell(encodeAsLong(new GeoPoint(-45, 0), 2), 2);

        assertThat(bbox[0], closeTo(-90, 0.1));
        assertThat(bbox[1], closeTo(0, 0.1));
    }

    @Test
    public void decodeCell_denver_is_inside_in_its_own_cell() throws Exception {

        double[] bbox = decodeCell(encodeAsLong(Places.DENVER, MAX_PREFIX_LENGTH), MAX_PREFIX_LENGTH);

        assertThat(bbox[0], lessThan(Places.DENVER.lat()));
        assertThat(bbox[1], greaterThan(Places.DENVER.lat()));
        assertThat(bbox[2], lessThan(Places.DENVER.lon()));
        assertThat(bbox[3], greaterThan(Places.DENVER.lon()));
    }
}
