 package org.dan.ping.pong.sys.ctx.jackson;

 import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
 import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;
 import static com.fasterxml.jackson.databind.MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS;
 import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
 import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
 import org.dan.ping.pong.app.place.Pid;
 import org.dan.ping.pong.app.tournament.Tid;
 import org.dan.ping.pong.app.tournament.Uid;
 import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
 import org.springframework.context.annotation.Bean;
 import org.springframework.http.converter.json.SpringHandlerInstantiator;

public class ObjectMapperProvider {
    public static final String OBJECT_MAPPER = "om";

    public static ObjectMapper get() {
        return new ObjectMapper()
                .setSerializationInclusion(NON_EMPTY)
                .enable(ALLOW_UNQUOTED_FIELD_NAMES)
                .enable(ALLOW_FINAL_FIELDS_AS_MUTATORS)
                .disable(WRITE_DATES_AS_TIMESTAMPS)
                .registerModule(Pid.jacksonMarshal())
                .registerModule(Tid.jacksonMarshal())
                .registerModule(Uid.jacksonMarshal())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());
    }

    @Bean(name = OBJECT_MAPPER)
    public ObjectMapper objectMapper(AutowireCapableBeanFactory beanFactory) {
        return (ObjectMapper) get()
                .setHandlerInstantiator(new SpringHandlerInstantiator(beanFactory));
    }
}
