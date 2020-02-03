package org.fantasizer.theblog.config.solr;

import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.convert.SolrJConverter;

/**
 * @Author Cruise Qu
 * @Date 2020-01-29 22:38
 */
public class SolrConfig {

    @Value("${spring.data.solr.host}")
    private String solrHost;

    @Value("${spring.data.solr.core}")
    private String solrCore;

    /**
     * 配置SolrTemplate
     */
    @Bean
    public SolrTemplate solrTemplate() {
        HttpSolrClient solrServer = new HttpSolrClient.Builder(solrHost)
                .withConnectionTimeout(10000)
                .withSocketTimeout(60000)
                .build();
        SolrTemplate template = new SolrTemplate(solrServer);
        template.setSolrConverter(new SolrJConverter());
        return template;
    }
}
