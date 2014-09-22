package nl.trifork.elasticsearch.facet.geohash.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Component
public class EmbeddedElasticSearch {

    public static final String INDEX = "theindex";
    public static final String TYPE = "location";
    public static final String LOCATION = "classpath:nl/trifork/elasticsearch/facet/geohash/locations.json";
    public static final String LOCATION_MAPPING = "classpath:nl/trifork/elasticsearch/facet/geohash/util/location-mapping.json";

    private Client client;
    private Node node;
    private ObjectMapper objectMapper;

    public Client getClient() {
        return client;
    }

    @PostConstruct
    public void start() throws IOException, ExecutionException, InterruptedException {
        node = createLocalNode();
        client = node.client();
        objectMapper = new ObjectMapper();

        LocationList locationList = getLocationList(objectMapper, LOCATION);

        createIndex(INDEX);

        String mappingSource = Resources.toString(ResourceUtils.getURL(LOCATION_MAPPING), Charsets.UTF_8);
        client.admin().indices().preparePutMapping(INDEX).setType(TYPE).setSource(mappingSource).get();

        for (Location location : locationList.getLocations()) {
            client.prepareIndex(INDEX, TYPE).setSource(objectMapper.writeValueAsString(location)).get().getId();
        }

        client.admin().indices().prepareRefresh(INDEX).get();
    }

    @PreDestroy
    public void stop() {
        node.close();
    }

    private void createIndex(String index) {
        if (client.admin().indices().prepareExists(index).get().isExists()) {
            client.admin().indices().prepareDelete(index).get();
        }
        client.admin().indices().prepareCreate(index).get();
    }

    private LocationList getLocationList(ObjectMapper objectMapper, String resourceLocation) throws IOException {
        return objectMapper.readValue(ResourceUtils.getURL(resourceLocation), LocationList.class);
    }

    private Node createLocalNode() {
        return NodeBuilder.nodeBuilder().local(true)
                .settings(ImmutableSettings.settingsBuilder()
                        .put("index.store.type", "memory")
                        .put("path.data", "target/data")).node().start();
    }

}
