package cs121.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration("elasticsearchconfig")
// @EnableElasticsearchRepositories(basePackages = "co.paan.repository")
public class ElasticsearchConfiguration {
    @Autowired
    private Environment environment;

    @Bean
    public RestClient client() {
        System.out.println(environment.getProperty("elasticsearch.host"));
        RestClient client = RestClient.builder(new HttpHost(environment.getProperty("elasticsearch.host"),
                Integer.valueOf(environment.getProperty("elasticsearch.port")), "http")).build();
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return client;
    }

}