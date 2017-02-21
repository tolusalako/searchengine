// package cs121.controllers;
//
// import java.util.List;
//
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.MediaType;
// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestMethod;
// import org.springframework.web.bind.annotation.RestController;
//
// import cs121.ton.PostService;
// import cs121.ton.dto.PostDto;
//
// @RestController
// @CrossOrigin(origins = "*", methods = { RequestMethod.GET })
// public class PageController {
// private static Logger LOG = LoggerFactory.getLogger(PageController.class);
//
// @Autowired
// PostService postService;
//
// @RequestMapping(value = "/n/{noun}", method = RequestMethod.GET, produces =
// MediaType.APPLICATION_JSON_VALUE)
// public List<PostDto> getNoun(@PathVariable String noun) {
// return postService.getNouns(noun, 0, null);
// }
//
// @RequestMapping(value = "/v/{verb}", method = RequestMethod.GET, produces =
// MediaType.APPLICATION_JSON_VALUE)
// public List<PostDto> getVerb(@PathVariable String verb) {
// return postService.getVerbs(verb, 0, null);
// }
//
// @RequestMapping(value = "/{verb}/{noun}", method = RequestMethod.GET,
// produces = MediaType.APPLICATION_JSON_VALUE)
// public List<PostDto> getPosts(@PathVariable String verb, @PathVariable String
// noun) {
//
// return postService.getPosts(verb, noun, 0, null);
// }
// }
