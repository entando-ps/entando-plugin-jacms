package org.entando.entando.plugins.jacms.aps.system.services.contentsettings;

import org.entando.entando.web.common.exceptions.ValidationConflictException;
import org.entando.entando.web.common.exceptions.ValidationGenericException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.entando.entando.aps.system.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ContentSettingsServiceUnitTest {

    private ContentSettingsService contentSettingsService;

    private List<String> cropRatios;
    private Map<String,List<String>> metadata;

    @BeforeEach
    public void setup() {
        cropRatios =  Arrays.stream(new String[] { "4:3", "16:9" } ).collect(Collectors.toList());
        metadata = new HashMap<>();
        metadata.put("my_key", Arrays.stream(new String[]{ "mapping_1", "mapping_2"}).collect(Collectors.toList()));
        contentSettingsService = new ContentSettingsService();
    }

    @Test
    public void testCropRatioValidation() {
        contentSettingsService.validateCropRatio(cropRatios, "4:4");
        contentSettingsService.validateCropRatio(cropRatios, "1000:1");
    }
    
    @Test
    public void testCropRatioInvalidFormat1() {
        Assertions.assertThrows(ValidationGenericException.class, () -> {
            contentSettingsService.validateCropRatio(cropRatios, "alfa:3");
        });
    }

    @Test
    public void testCropRatioInvalidFormat2() {
        Assertions.assertThrows(ValidationGenericException.class, () -> {
            contentSettingsService.validateCropRatio(cropRatios, "4;3");
        });
    }

    @Test
    public void testCropRatioConflict() {
        Assertions.assertThrows(ValidationGenericException.class, () -> {
            contentSettingsService.validateCropRatioNotExists(cropRatios, "4:3");
        });
    }

    @Test
    public void testMetadataValidation() {
        contentSettingsService.validateMetadata(metadata, "new_key", "new_mapping1,new_mapping2");
        contentSettingsService.validateMetadata(metadata, "new_key2", "NEW-MAPpinG@1#special;,./ã][´´");
    }

    @Test
    public void testMetadataInvalidFormat1() {
        Assertions.assertThrows(ValidationGenericException.class, () -> {
            contentSettingsService.validateMetadata(metadata, "NEW_key", "new_mapping1,new_mapping2");
        });
    }

    @Test
    public void testMetadataInvalidFormat2() {
        Assertions.assertThrows(ValidationGenericException.class, () -> {
            contentSettingsService.validateMetadata(metadata, "new-key", "new_mapping1,new_mapping2");
        });
    }

    @Test
    public void testMetadataConflict() {
        Assertions.assertThrows(ValidationGenericException.class, () -> {
            contentSettingsService.validateMetadataNotExists(metadata, "my_key");
        });
    }
}
