package nl.trifork.elasticsearch.facet.geohash.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutionException;

import static org.springframework.util.FileSystemUtils.deleteRecursively;
import static org.springframework.util.ResourceUtils.getURL;

@Component
public class EmbeddedElasticSearch {

    public static final String INDEX_NAME = "theindex";
    public static final String TYPE = "location";
    public static final String LOCATIONS_FILE = "classpath:nl/trifork/elasticsearch/facet/geohash/locations.json";
    public static final String FACET_MAPPING_FILE = "classpath:nl/trifork/elasticsearch/facet/geohash/util/location-mapping.json";
    private static final String ES_DATA_DIR = "target/data";

    private Client client;
    private Node node;
    private ObjectMapper objectMapper = new ObjectMapper();
    private int port;

    private Logger logger = LoggerFactory.getLogger(EmbeddedElasticSearch.class);

    @PostConstruct
    public void startInstance() throws IOException, ExecutionException, InterruptedException {
        deleteRecursively(new File(ES_DATA_DIR));

        port = findFreePort();
        node = createNode(port);

        client = createTransportClient(port);

        logger.info("Started ElasticSearch node at TCP port [{}]", port);

        createIndex(INDEX_NAME);

        addFacetMapping(readFile(FACET_MAPPING_FILE));
        addLocationsToIndex(getLocationList(objectMapper, LOCATIONS_FILE));

        refreshIndex(INDEX_NAME);
    }

    private TransportClient createTransportClient(int port) {
        return new TransportClient()
                .addTransportAddress(new InetSocketTransportAddress("localhost", port));
    }

    private RefreshResponse refreshIndex(String index) {
        return client.admin().indices().prepareRefresh(index).get();
    }

    private void addLocationsToIndex(LocationList locationList) throws JsonProcessingException {
        for (Location location : locationList.getLocations()) {
            client.prepareIndex(INDEX_NAME, TYPE).setSource(objectMapper.writeValueAsString(location)).get().getId();
        }
    }

    private PutMappingResponse addFacetMapping(String mappingSource) {
        return client.admin().indices().preparePutMapping(INDEX_NAME).setType(TYPE).setSource(mappingSource).get();
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

    private Node createNode(int port) {
        ImmutableSettings.Builder settings = ImmutableSettings.settingsBuilder()
                .put("index.store.type", "memory")
                .put("http.enabled", "false")
                .put("transport.tcp.port", port)
                .put("discovery.zen.ping.multicast.enabled", false)
                .put("path.data", ES_DATA_DIR);

        return NodeBuilder.nodeBuilder()
                .settings(settings)
                .node()
                .start();
    }

    @PreDestroy
    public void closeInstance() {
        client.close();
        node.close();
        deleteRecursively(new File(ES_DATA_DIR));
        logger.info("Stopped ElasticSearch node at TCP port [{}]", port);
    }

    private int findFreePort() throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(0);
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new IOException("Could not find free port", e);
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }

    public Client getClient() {
        return client;
    }

}
