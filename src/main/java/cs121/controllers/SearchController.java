package cs121.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import io.undertow.util.StatusCodes;

@RestController
@CrossOrigin(origins = "*", methods = { RequestMethod.GET })
public class SearchController {
    private static Logger LOG = LoggerFactory.getLogger(SearchController.class);

    @Autowired
    RestClient client;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView searchPage() {
        return new ModelAndView("index");
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ModelAndView resultsPage(@RequestBody MultiValueMap<String, String> body) {
        try {
            Response res = client.performRequest("GET", "html_index/_search?q=tokens:" + "uci");
            
            if (res.getStatusLine().getStatusCode() != StatusCodes.OK) {
                LOG.error("Could not complete request");
            }
    
            else {
//                InputStream content = res.getEntity().getContent();
//                byte[] data = new byte[(int) res.getEntity().getContentLength()];
//                int read = content.read(data);
//                LOG.info(Arrays.toString(data));
                
                String jsonStr = EntityUtils.toString(res.getEntity());
    			JSONObject jsonObj = new JSONObject(jsonStr);
    			JSONObject hits = jsonObj.getJSONObject("hits");
    			JSONArray hitsArray = hits.getJSONArray("hits");
    			
    			for (int i = 0; i < hitsArray.length(); i++) {
    			    JSONObject obj = hitsArray.getJSONObject(i);
    			    LOG.info(obj.toString());
    			}
                
            }
        }
        catch (IOException e) {
            LOG.error("Could not perform request", e);
        }
        return new ModelAndView("index");
    }
}
