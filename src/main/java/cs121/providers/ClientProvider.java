package cs121.providers;

import javax.inject.Singleton;

import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import cs121.index.Indexer;

@Configuration
@ComponentScan("cs121.config")
public class ClientProvider {

    @Autowired
    Client client;

    @Bean
    @Singleton
    public Indexer inderxer() {
        return new Indexer(client);
    }
}
