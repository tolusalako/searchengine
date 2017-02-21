package cs121.index;

import java.io.IOException;
import java.util.Date;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Indexer {

    public static final Logger LOG = LoggerFactory.getLogger(Indexer.class);

    // @Autowired
    // WebPageSettings wpSettings;

    public Indexer(Client client) {

        IndexResponse response;
        try {
            response = client.prepareIndex("twitter", "tweet", "1")
                    .setSource(XContentFactory.jsonBuilder().startObject().field("user", "kimchy")
                            .field("postDate", new Date()).field("message", "trying out Elasticsearch").endObject())
                    .get();

            LOG.info(response.toString());
        }
        catch (ElasticsearchException | IOException e) {
            LOG.error("", e);
        }
    }
}
