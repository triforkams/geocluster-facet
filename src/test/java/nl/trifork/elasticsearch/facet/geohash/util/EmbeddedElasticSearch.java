package nl.trifork.elasticsearch.facet.geohash.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.springframework.util.ResourceUtils.getURL;

@Component
public class EmbeddedElasticSearch {

    public static final String INDEX = "theindex";
    public static final String TYPE = "location";
    public static final String LOCATION = "classpath:nl/trifork/elasticsearch/facet/geohash/locations.json";
    public static final String LOCATION_MAPPING = "classpath:nl/trifork/elasticsearch/facet/geohash/util/location-mapping.json";

    private Client client;
    private Node node;
    private ObjectMapper objectMapper = new ObjectMapper();

    public Client getClient() {
        return client;
    }

    @PostConstruct
    public void startInstance() throws IOException, ExecutionException, InterruptedException {
        node = createLocalNode();
        client = node.client();

        createIndex(INDEX);

        String mapping = readFile(LOCATION_MAPPING);
        addFacetMapping(mapping);
        addLocationsToIndex(getLocationList(objectMapper, LOCATION));

        refreshIndex();
    }

    private RefreshResponse refreshIndex() {
        return client.admin().indices().prepareRefresh(INDEX).get();
    }

    private void addLocationsToIndex(LocationList locationList) throws JsonProcessingException {
        for (Location location : locationList.getLocations()) {
            client.prepareIndex(INDEX, TYPE).setSource(objectMapper.writeValueAsString(location)).get().getId();
        }
    }

    private PutMappingResponse addFacetMapping(String mappingSource) {
        return client.admin().indices().preparePutMapping(INDEX).setType(TYPE).setSource(mappingSource).get();
    }

    private String readFile(String locationMapping) throws IOException {
        return Resources.toString(getURL(locationMapping), Charsets.UTF_8);
    }

    private void createIndex(String index) {
        if (client.admin().indices().prepareExists(index).get().isExists()) {
            client.admin().indices().prepareDelete(index).get();
        }
        client.admin().indices().prepareCreate(index).get();
    }

    private LocationList getLocationList(ObjectMapper objectMapper, String resourceLocation) throws IOException {
        return objectMapper.readValue(getURL(resourceLocation), LocationList.class);
    }

    private Node createLocalNode() {
        return NodeBuilder.nodeBuilder().local(true)
                .settings(ImmutableSettings.settingsBuilder()
                        .put("index.store.type", "memory")
                        .put("path.data", "target/data")).node().start();
    }

    @PreDestroy
    public void closeInstance() {
        node.close();
    }

}
