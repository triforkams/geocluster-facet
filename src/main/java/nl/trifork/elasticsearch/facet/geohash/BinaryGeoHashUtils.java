package nl.trifork.elasticsearch.facet.geohash;

import org.elasticsearch.common.geo.GeoHashUtils;
import org.elasticsearch.common.geo.GeoPoint;

public class BinaryGeoHashUtils {
    public static final int MAX_PREFIX_LENGTH = 60;

    private static final int[] BITS = {16, 8, 4, 2, 1};

    /**
     * Encodes a geohash as a bit string.
     *
     * @param bits - values: from 0 to {@link nl.trifork.elasticsearch.facet.geohash.BinaryGeoHashUtils#MAX_PREFIX_LENGTH}
     */
    public static long encodeAsLong(GeoPoint geoPoint, int bits) {

        if (bits == 0) {
            return 0x0000000000000000;
        }
        return GeoHashUtils.encodeAsLong(geoPoint.lat(), geoPoint.lon(), GeoHashUtils.PRECISION) >> 4 + (60 - bits);
    }

    public static void decodeCell(long geohash, int geohashBits, GeoPoint northWest, GeoPoint southEast) {
        double[] interval = decodeCell(geohash, geohashBits);
        northWest.reset(interval[1], interval[2]);
        southEast.reset(interval[0], interval[3]);
    }

    /**
     * copied and adapted from {@link org.elasticsearch.common.geo.GeoHashUtils} to handle an additional precision parameter.
     *
     * It decodes a value previously returned by {@link nl.trifork.elasticsearch.facet.geohash.BinaryGeoHashUtils#encodeAsLong(org.elasticsearch.common.geo.GeoPoint, int)}
     * and returns the corresponding geohash cell.
     *
     * @param geohashBits - values: from 0 to {@link nl.trifork.elasticsearch.facet.geohash.BinaryGeoHashUtils#MAX_PREFIX_LENGTH}
     */
    public static double[] decodeCell(long geohash, int geohashBits) {
        double[] interval = {-90.0, 90.0, -180.0, 180.0};

        if (geohashBits == 0) {
            return interval;
        }
        boolean isEven = true;

        geohash <<= 60 - geohashBits;
        int[] cds = new int[12];
        for (int i = 11; i >= 0 ; i--) {
            cds[i] = (int) (geohash & 31);
            geohash >>= 5;
        }

        int bitCount = 0;
        for (int i = 0; i < cds.length ; i++) {
            final int cd = cds[i];
            for (int mask : BITS) {
                if (isEven) {
                    if ((cd & mask) != 0) {
                        interval[2] = (interval[2] + interval[3]) / 2D;
                    } else {
                        interval[3] = (interval[2] + interval[3]) / 2D;
                    }
                } else {
                    if ((cd & mask) != 0) {
                        interval[0] = (interval[0] + interval[1]) / 2D;
                    } else {
                        interval[1] = (interval[0] + interval[1]) / 2D;
                    }
                }
                isEven = !isEven;
                bitCount++;
                if (bitCount == geohashBits) {
                    return interval;
                }
            }
        }
        return interval;
    }

    public static void printBbox(double[] bbox) {
        System.out.printf("%f, %f, %f %f\n", bbox[0], bbox[1], bbox[2], bbox[3]);
    }

}
