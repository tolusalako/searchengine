package cs121.index;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
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
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import cs121.config.WebPageSettings;
import io.undertow.util.FileUtils;

public class Indexer {

    public static final Logger LOG = LoggerFactory.getLogger(Indexer.class);

    RestClient client;

    @Autowired
    WebPageSettings webPageSettings;
    Gson gson;

    public Indexer(RestClient client) {
        this.client = client;
        this.gson = new Gson();
    }

    @PostConstruct
    public void init() {
        try {
            String content = FileUtils.readFile(
                    Paths.get(webPageSettings.getDir(), webPageSettings.getJson()).toAbsolutePath().toUri().toURL());
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> myMap = gson.fromJson(content, type);
            createIndices(myMap);
        }

        catch (IOException e) {
            LOG.error("Could not connect URLs", e);
        }
        LOG.info(webPageSettings.getDir());
    }

    public void createIndices(Map<String, String> map) {
    	for (String key : map.keySet()) {
    		try {
    			
    			String type = key.split("/")[0];
    			String id = key.split("/")[1];
    			
    			HttpEntity entity = new NStringEntity(
    				"{\n" +
    				//"    \"type\" : \""+ type + "\",\n" +
    				//"    \"id\" : \""+ id + "\",\n" + 
    				"    \"url\" : \""+ map.get(key) + "\"\n" +
    				"}", ContentType.APPLICATION_JSON);
    			
				Response response = client.performRequest("PUT", 
					"ics_index/" + type + "/" + id,
					Collections.<String, String> emptyMap(), entity);
			}
			catch (ElasticsearchException | IOException e) {
				LOG.error("", e);
			}
    		
    		//LOG.info(key);
    		//LOG.info(map.get(key));
    		
    		//TODO: parse html and tokenize words, create inverted index
    		
    	}
    }
    

}
