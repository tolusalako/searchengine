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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> myMap = gson.fromJson(content, type);
            createIndices(myMap);
            File root = new File(webPageSettings.getDir());
            indexFiles(root, root.getName());
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

                HttpEntity entity = new NStringEntity("{\n" + "    \"url\" : \"" + map.get(key) + "\"\n" + "}",
                        ContentType.APPLICATION_JSON);

                Response response = client.performRequest("PUT", "ics_index/" + type + "/" + id,
                        Collections.<String, String> emptyMap(), entity);

            }
            catch (ElasticsearchException | IOException e) {
                LOG.error("", e);
            }

        }
    }

    public void indexFiles(File root, String name) {

        if (!root.exists()) {
            LOG.warn("File {} DNE", root.getName());
            return;
        }

        if (root.isDirectory()) {
            for (File file : root.listFiles())
                indexFiles(file, root.getName());
        }
        else if (root.isFile()) {
            parseAndIndex(root, name);
        }
        else {
            LOG.warn("{} i neither a dir nor file.", root.getName());
        }
    }

    private void parseAndIndex(File file, String dirName) {
        try {
            Document doc = Jsoup.parse(file, "ISO-8859-1");
            Element title = doc.select("title").first();

            if (title == null)
                return;

            String[] tokens = title.text().split("[^\\w']+");
            String type = dirName;
            String id = file.getName();
            if (tokens != null && tokens.length > 0) {
                for (String t : tokens) {
                    JsonObject rootObj = new JsonObject();
                    JsonObject upsertObject = new JsonObject();
                    JsonObject scriptObject = new JsonObject();
                    upsertObject.addProperty("postings", type + "/" + id);
                    scriptObject.addProperty("inline",
                            "if (ctx._source.containsKey(\"postings\")) {ctx._source.postings+=\", \"+ params.postings;} else {ctx._source.postings= [params.postings];}");
                    scriptObject.add("params", upsertObject);
                    rootObj.add("upsert", upsertObject);
                    rootObj.add("script", scriptObject);
                    HttpEntity entity = new NStringEntity(rootObj.toString(), ContentType.APPLICATION_JSON);
                    Response response = client.performRequest("POST",
                            "html_index/file_index/" + t.toLowerCase() + "/_update",
                            Collections.<String, String> emptyMap(), entity);
                    LOG.debug("Indexed {}", t);
                }
            }

            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        catch (IOException e) {
            LOG.error("Could not open file {}", file.getName(), e);
            return;
        }
    }

}
