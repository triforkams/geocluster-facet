package nl.trifork.elasticsearch.facet.geohash;

import nl.trifork.elasticsearch.facet.geohash.util.EmbeddedElasticSearch;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.GeoBoundingBoxFilterBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;

@ContextConfiguration("classpath:nl/trifork/elasticsearch/facet/geohash/util/elasticSearchHolderContext.xml")
public class GeohashFacetIT extends AbstractTestNGSpringContextTests {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private EmbeddedElasticSearch embeddedElasticSearch;

    @Test
    public void testDataLoaded() throws Exception {
        long hits = embeddedElasticSearch.getClient()
                .prepareSearch("theindex")
                .get()
                .getHits()
                .totalHits();

        assertThat("all 10 locations loaded", hits, equalTo(10l));
    }

    @Test
    public void testFacetSearchWithArithmeticMean() throws Exception {
        GeohashFacet geohashFacet = getFacetFor(54, -1, 50, 2);

        GeoPoint center = geohashFacet.getEntries()
                .get(0)
                .center();

        assertThat("center latitude calculated",  center.getLat(), closeTo(51.6d, 0));
        assertThat("center longitute calculated",  center.getLon(), closeTo(0.3d, 0));
    }

    private GeohashFacet getFacetFor(int topLeftLat, int topLeftLon, int bottomRightLat, int bottomRightLon) {
        SearchRequestBuilder request = getSearchRequest(topLeftLat, topLeftLon, bottomRightLat, bottomRightLon);
        SearchResponse response = request.get();
        GeohashFacet geohashFacet = response.getFacets().facet("location");
        logger.debug("ES response: {}", response.toString());
        return geohashFacet;
    }

    private SearchRequestBuilder getSearchRequest(double topLeftLat, double topLeftLon, double bottomRightLat,
                                                  double bottomRightLon) {
        GeoBoundingBoxFilterBuilder boundingBox = getBoundingBox(topLeftLat, topLeftLon, bottomRightLat, bottomRightLon);
        GeoFacetBuilder geoHash = getGeohashFacet();
        FilteredQueryBuilder filteredQuery = getFilteredQuery(boundingBox);
        return getSearchRequest(geoHash, filteredQuery);
    }

    private SearchRequestBuilder getSearchRequest(GeoFacetBuilder geoHash, FilteredQueryBuilder filteredQuery) {
        return embeddedElasticSearch.getClient()
                .prepareSearch(EmbeddedElasticSearch.INDEX_NAME)
                .setQuery(filteredQuery)
                .addFacet(geoHash);
    }

    private FilteredQueryBuilder getFilteredQuery(GeoBoundingBoxFilterBuilder boundingBox) {
        return QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), boundingBox);
    }

    private GeoFacetBuilder getGeohashFacet() {
        return new GeoFacetBuilder(EmbeddedElasticSearch.TYPE)
                .field("latLon")
                .factor(1)
                .showDocId(true)
                .showGeohashCell(true);
    }

    private GeoBoundingBoxFilterBuilder getBoundingBox(double topLeftLat, double topLeftLon, double bottomRightLat, double bottomRightLon) {
        return FilterBuilders.geoBoundingBoxFilter("latLon")
                .topLeft(topLeftLat, topLeftLon)
                .bottomRight(bottomRightLat, bottomRightLon);
    }

}
