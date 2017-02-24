package cs121.index;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;

import javax.annotation.PostConstruct;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;

import cs121.config.WebPageSettings;
import io.undertow.util.FileUtils;

public class Indexer {

    public static final Logger LOG = LoggerFactory.getLogger(Indexer.class);

    RestClient client;

    @Autowired
    WebPageSettings webPageSettings;
    ObjectMapper mapper;

    public Indexer(RestClient client) {
        this.client = client;
        this.mapper = new ObjectMapper();
        try {
            HttpEntity entity = new NStringEntity(
                    "{\n" + "    \"user\" : \"kimchy\",\n" + "    \"post_date\" : \"2009-11-15T14:12:12\",\n"
                            + "    \"message\" : \"trying out Elasticsearch\"\n" + "}",
                    ContentType.APPLICATION_JSON);
            Response indexResponse = client.performRequest("PUT", "/twitter/tweet/1",
                    Collections.<String, String> emptyMap(), entity);

            LOG.info(indexResponse.toString());
        }
        catch (ElasticsearchException | IOException e) {
            LOG.error("", e);
        }
    }

    @PostConstruct
    public void init() {
        try {
            String content = FileUtils.readFile(
                    Paths.get(webPageSettings.getDir(), webPageSettings.getJson()).toAbsolutePath().toUri().toURL());
            TextNode node = mapper.valueToTree(content);
            LOG.info(node.toString());
        }
        catch (IOException e) {
            LOG.error("Could not connect URLs", e);
        }
        LOG.info(webPageSettings.getDir());
    }

    public void createIndices() {
    }
}
