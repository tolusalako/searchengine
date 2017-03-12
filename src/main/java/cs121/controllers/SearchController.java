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
    private String query;

    @Autowired
    RestClient client;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView searchPage() {
        return new ModelAndView("index");
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ModelAndView resultsPage(Model model, @RequestBody MultiValueMap<String, String> body) {
    	
        this.query = body.get("query").get(0);
        List<QueryItem> result = new ArrayList<>();
        
        try {
        	
        	JsonObject rootObject = new JsonObject();
        	JsonObject boolObject = new JsonObject();
        	JsonObject shouldObject = new JsonObject();
        	
        	rootObject.add("query", boolObject);
        	boolObject.add("bool", shouldObject);
      	
        	JsonArray shouldArray = new JsonArray();
        	shouldArray.add(constructJsonSearchObject("url", 3));
        	shouldArray.add(constructJsonSearchObject("title_tokens", 8));
        	shouldArray.add(constructJsonSearchObject("header_tokens", 6));
        	shouldArray.add(constructJsonSearchObject("bold_tokens", 4));
        	shouldArray.add(constructJsonSearchObject("italic_tokens", 2));
        	shouldArray.add(constructJsonSearchObject("body_tokens", 1));
        	shouldArray.add(constructJsonSearchObject("link_tokens", 2));
        	shouldArray.add(constructJsonSearchObject("paragraph_tokens", 2));
        	shouldArray.add(constructJsonSearchObject("strong_tokens", 3));

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

                    // Create description
                    StringBuilder description = new StringBuilder();
                    try {
	                    JsonArray descArray = source.getAsJsonArray("body_tokens");
	                	Iterator<JsonElement> iter = descArray.iterator();
	                	
	                    int count = 0;
	                    while (iter.hasNext() && count < DESC_LENGTH) {
	                        description.append(iter.next().getAsString());
	                        description.append(" ");
	                        count++;
	                    }
                    }
	               
	                catch (Exception e) {
	                	description.append("");
	                }
                    
                    // Create title
                    StringBuilder title = new StringBuilder();
                    try {
                    	JsonArray titleArray = source.getAsJsonArray("title_tokens");
                    	Iterator<JsonElement> iterator = titleArray.iterator();
                    	while (iterator.hasNext()) {
                    		title.append(iterator.next().getAsString());
                    		title.append(" ");
                    	}
                    }
                    
                    catch (Exception e) {
                    	title.append("");
                    }
                    
                    String url = source.get("url").getAsString();
                    if (!url.startsWith("http") || url.startsWith("www")) {
                        url = "http://" + url;
                    }
                    
                    result.add(new QueryItem(title.toString(), url, description.toString(), obj.get("_score").getAsDouble()));
                    LOG.info(obj.toString());
                }

            }
        }
        
        catch (IOException e) {
            LOG.error("Could not perform request. Error: ", e);
        }
        
        model.addAttribute("result", result);
        return new ModelAndView("index");
    }
    
    public JsonObject constructJsonSearchObject(String htmlTag, int boostValue) {
    	
    	JsonObject tag = new JsonObject();
    	tag.addProperty("query", this.query);
    	tag.addProperty("boost", boostValue);
    	
    	JsonObject content = new JsonObject();
    	content.add(htmlTag, tag);
    	
    	JsonObject matchObj = new JsonObject();
    	matchObj.add("match", content);
    	
    	return matchObj;
    	
    }
    
}
