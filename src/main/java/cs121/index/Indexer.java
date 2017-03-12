package cs121.index;

import java.io.File;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cs121.config.WebPageSettings;
import io.undertow.util.FileUtils;
import io.undertow.util.StatusCodes;

public class Indexer {

    public static final Logger LOG = LoggerFactory.getLogger(Indexer.class);

    RestClient client;

    @Autowired
    WebPageSettings webPageSettings;
    @Autowired
    Environment env;
    Gson gson;

    public Indexer(RestClient client) {
        this.client = client;
        this.gson = new Gson();
    }

    @PostConstruct
    public void init() {
        if ("false".equals(env.getProperty("run_index"))) {
            LOG.warn("Skipping indexing...");
            return;
        }

        try {
            String content = FileUtils.readFile(
                    Paths.get(webPageSettings.getDir(), webPageSettings.getJson()).toAbsolutePath().toUri().toURL());
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> urlMap = gson.fromJson(content, type);
            File root = new File(webPageSettings.getDir());
            indexFiles(root, root.getName(), urlMap);
        }

        catch (IOException e) {
            LOG.error("Could not connect URLs", e);
        }
        LOG.info(webPageSettings.getDir());
    }

    public void indexFiles(File root, String name, Map<String, String> map) {

        if (!root.exists()) {
            LOG.warn("File {} does not exist.", root.getName());
            return;
        }

        if (root.isDirectory()) {
            for (File file : root.listFiles())
                indexFiles(file, root.getName(), map);
        }
        
        else if (root.isFile()) {
            parseAndIndex(root, name, map);
        }
        
        else {
            LOG.warn("{} is neither a directory nor file.", root.getName());
        }
        
    }

    private void parseAndIndex(File file, String dirName, Map<String, String> map) {
    	
        String splitToken = "[^\\w']+";
        
        try {
            Document doc = Jsoup.parse(file, "ISO-8859-1");
            
            JsonObject rootObj = new JsonObject();
            rootObj.addProperty("url", map.get(dirName + "/" + file.getName()));
            
            JsonArray headerArray = new JsonArray();
            JsonArray titleArray = new JsonArray();
            JsonArray boldArray = new JsonArray();
            JsonArray italicArray = new JsonArray();
            JsonArray bodyArray = new JsonArray();
            JsonArray linksArray = new JsonArray();
            JsonArray paragraphArray = new JsonArray();
            JsonArray strongArray = new JsonArray();

            Elements headers = doc.select("h1, h2, h3, h4, h5, h6");
            Element title = doc.select("title").first();
            Elements bold = doc.select("B, b");
            Elements italic = doc.select("I, i");
            Element body = doc.body();
            Elements links = doc.select("a");
            Elements paragraph = doc.select("p");
            Elements strong = doc.select("strong");
            
            if (null != headers)
            	insertTokens(headers.text().split(splitToken), headerArray);
            
            if (null != title)
            	insertTokens(title.text().split(splitToken), titleArray);
            
            if (null != bold)
                insertTokens(bold.text().split(splitToken), boldArray);
            
            if (null != italic)
            	insertTokens(italic.text().split(splitToken), italicArray);
            
            if (null != body)
            	insertTokens(body.text().split(splitToken), bodyArray);

            if (null != links)
            	insertTokens(links.text().split(splitToken), linksArray);
            
            if (null != paragraph)
            	insertTokens(paragraph.text().split(splitToken), paragraphArray);
            
            if (null != strong)
            	insertTokens(strong.text().split(splitToken), strongArray);
            
            if (titleArray.size() != 0)
            	rootObj.add("title_tokens", titleArray);
            
            if (headerArray.size() != 0)
            	rootObj.add("header_tokens", headerArray);
            
            if (boldArray.size() != 0)
            	rootObj.add("bold_tokens", boldArray);
            
            if (italicArray.size() != 0)
            	rootObj.add("italic_tokens", italicArray);
            
            if (bodyArray.size() != 0)
            	rootObj.add("body_tokens", bodyArray);
            
            if (linksArray.size() != 0)
            	rootObj.add("link_tokens", linksArray);
            
            if (paragraphArray.size() != 0)
            	rootObj.add("paragraph_tokens", paragraphArray);
            
            if (strongArray.size() != 0)
            	rootObj.add("strong_tokens", strongArray);
           
            HttpEntity entity = new NStringEntity(rootObj.toString(), ContentType.APPLICATION_JSON);

            Response response = client.performRequest("PUT", "html_index/" + dirName + "/" + file.getName(),
                    Collections.<String, String> emptyMap(), entity);
            
            if (response.getStatusLine().getStatusCode() != StatusCodes.OK)
                LOG.error("Could not complete index request for file {}/{}", dirName, file.getName());

            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        catch (ElasticsearchException e) {
            LOG.error("ElasticsearchException thrown {}", e);
        }

        catch (IOException e) {
            LOG.error("Could not open file {}", dirName + "/" + file.getName());
            return;
        }
    }
    
    public void insertTokens(String[] tokens, JsonArray jsonArr) {
    	for (String token : tokens) {
    		if (!token.isEmpty())
    			jsonArr.add(token);
    	}
    }

}
