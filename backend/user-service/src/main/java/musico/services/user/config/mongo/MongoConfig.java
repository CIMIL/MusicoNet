package musico.services.user.config.mongo;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

//@Configuration
//@EnableMongoRepositories(basePackages = "musico.services.user")
//@ImportResource("classpath:Mongo.xml")
//@RequiredArgsConstructor
//public class MongoConfig extends AbstractMongoClientConfiguration {
//
//
//    @Override
//    protected String getDatabaseName() {
//        return "musico";
//    }
//}
