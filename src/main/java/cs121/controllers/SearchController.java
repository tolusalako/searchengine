package cs121.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cs121.dto.QueryItem;
import io.undertow.util.StatusCodes;

@RestController
@CrossOrigin(origins = "*", methods = { RequestMethod.GET })
public class SearchController {
    private static Logger LOG = LoggerFactory.getLogger(SearchController.class);
    public static final int DESC_LENGTH = 15;

    @Autowired
    RestClient client;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView searchPage() {
        return new ModelAndView("index");
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ModelAndView resultsPage(Model model, @RequestBody MultiValueMap<String, String> body) {
        String query = body.get("query").get(0);
        List<QueryItem> result = new ArrayList<>();
        try {
        	
        	JsonObject rootObject = new JsonObject();
        	JsonObject boolObject = new JsonObject();
        	JsonObject shouldObject = new JsonObject();
        	
        	rootObject.add("query", boolObject);
        	boolObject.add("bool", shouldObject);
        	
        	JsonObject shouldMatchTitle = new JsonObject();
        	JsonObject title = new JsonObject();
        	title.addProperty("query", query);
        	title.addProperty("boost", 6);
        	shouldMatchTitle.add("title", title);
        	JsonObject matchTitle = new JsonObject();
        	matchTitle.add("match", shouldMatchTitle);
        	
        	JsonObject shouldMatchHeaders = new JsonObject();
        	JsonObject headers = new JsonObject();
        	headers.addProperty("query", query);
        	headers.addProperty("boost", 4);
        	shouldMatchHeaders.add("header_tokens", headers);
        	JsonObject matchHeaders = new JsonObject();
        	matchHeaders.add("match", shouldMatchHeaders);
        	
        	JsonObject shouldMatchBold = new JsonObject();
        	JsonObject bold = new JsonObject();
        	bold.addProperty("query", query);
        	bold.addProperty("boost", 2);
        	shouldMatchBold.add("bold_tokens", bold);
        	JsonObject matchBold = new JsonObject();
        	matchBold.add("match", shouldMatchBold);
        	

        	JsonObject shouldMatchTokens = new JsonObject();
        	JsonObject tokens = new JsonObject();
        	tokens.addProperty("query", query);
        	tokens.addProperty("boost", 4);
        	shouldMatchTokens.add("tokens", tokens);
        	JsonObject matchTokens = new JsonObject();
        	matchTokens.add("match", shouldMatchTokens);

        	JsonArray shouldArray = new JsonArray();
        	shouldArray.add(matchTokens);
        	shouldArray.add(matchTitle);

        	shouldObject.add("should", shouldArray);

        	HttpEntity entity = new NStringEntity(rootObject.toString(), ContentType.APPLICATION_JSON);
        	
        	Response res = client.performRequest("GET", "html_index/_search", Collections.<String, String> emptyMap(), entity);

            if (res.getStatusLine().getStatusCode() != StatusCodes.OK) {
                LOG.error("Could not complete search request for query: {}", query);
            }

            else {
                JsonObject jsonObj = new JsonParser().parse(EntityUtils.toString(res.getEntity())).getAsJsonObject();
                JsonObject hits = jsonObj.getAsJsonObject("hits");
                JsonArray hitsArray = hits.getAsJsonArray("hits");

                for (int i = 0; i < hitsArray.size(); i++) {
                    JsonObject obj = hitsArray.get(i).getAsJsonObject();
                    JsonObject source = obj.get("_source").getAsJsonObject();
                    JsonArray descArray = source.getAsJsonArray("tokens");
                    StringBuilder builder = new StringBuilder();

                    // Create description
                    int count = 0;
                    Iterator<JsonElement> iter = descArray.iterator();
                    while (iter.hasNext() && count < DESC_LENGTH) {
                        builder.append(iter.next().getAsString());
                        builder.append(" ");
                        count++;
                    }
                    
                    String url = source.get("url").getAsString();
                    if (!url.startsWith("http") || url.startsWith("www")) {
                        url = "http://" + url;
                    }
                    
                    StringBuilder titleString = new StringBuilder();
                    try {
                    	JsonArray titleArray = source.getAsJsonArray("title");
                    	Iterator<JsonElement> iterator = titleArray.iterator();
                    	while (iterator.hasNext()) {
                    		titleString.append(iterator.next().getAsString());
                    		titleString.append(" ");
                    	}
                    }
                    catch (Exception e) {
                    	titleString.append("");
                    }
                    
                    result.add(new QueryItem(titleString.toString(), url, builder.toString(), obj.get("_score").getAsDouble()));
                    LOG.info(obj.toString());
                }

            }
        }
        catch (IOException e) {
            LOG.error("Could not perform request", e);
        }
        model.addAttribute("result", result);
        return new ModelAndView("index");
    }
}
