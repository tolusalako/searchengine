package cs121.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        String query = body.get("query").get(0).replaceAll(" ", "+");
        List<QueryItem> result = new ArrayList<>();
        try {
            Response res = client.performRequest("GET", "html_index/_search?q=tokens:" + query);

            if (res.getStatusLine().getStatusCode() != StatusCodes.OK) {
                LOG.error("Could not complete request");
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
                    
                    StringBuilder title = new StringBuilder();
                    try {
                    	JsonArray titleArray = source.getAsJsonArray("title");
                    	Iterator<JsonElement> iterator = titleArray.iterator();
                    	while (iterator.hasNext()) {
                    		title.append(iterator.next().getAsString());
                    		title.append(" ");
                    	}
                    }
                    catch (Exception e) {
                    	title.append("");
                    }
                    
                    result.add(new QueryItem(title.toString(), url, builder.toString(), obj.get("_score").getAsDouble()));
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
