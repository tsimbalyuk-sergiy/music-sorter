package dev.tsvinc.music.sort.infrastructure.dao;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.tsvinc.music.sort.service.PropertiesService;
import javax.inject.Inject;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.mapper.JacksonMapper;

public class NitriteInstanceImp implements NitriteInstance {

    @Inject
    private PropertiesService propertiesService;

    @Override
    public Nitrite instance() {
        final var jacksonMapper = new JacksonMapper();
        jacksonMapper
                .getObjectMapper()
                .setSerializationInclusion(Include.NON_NULL)
                .setSerializationInclusion(Include.NON_EMPTY)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return Nitrite.builder()
                .nitriteMapper(jacksonMapper)
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module())
                .compressed()
                .filePath(
                        this.propertiesService.getProperties().getDbProperties().getDbLocation())
                .openOrCreate(
                        this.propertiesService.getProperties().getDbProperties().getDbUsername(),
                        this.propertiesService.getProperties().getDbProperties().getDbPassword());
    }
}
