package com.example.ReactiveApp.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class ProcessRepairsServiceTests {

    @Mock
    private UploadService uploadService;

    @InjectMocks
    private ProcessRepairsService repairsService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        File folder = new File("src/main/resources/temporary-folder");
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    @AfterEach
    void tearDown() {
        // Clean up the temporary folder after each test
        File folder = new File("src/main/resources/temporary-folder");
        if (folder.exists()) {
            for (File file : folder.listFiles()) {
                file.delete();
            }
            folder.delete();
        }
    }

    @Test
    void testProcessDataAndUpload() {
        List<String> data = Arrays.asList("item1", "item2", "item3");

        doNothing().when(uploadService).upload(any(byte[].class));

        StepVerifier.create(repairsService.processDataAndUpload("caseId", "dsId", data))
                .verifyComplete();
    }

    @Test
    void testPartitionList() {
        List<String> data = Arrays.asList("item1", "item2", "item3", "item4", "item5", "item6");
        int numberOfPartitions = 3;

        List<List<String>> partitions = repairsService.partitionList(data, numberOfPartitions);

        assert(partitions.size() == numberOfPartitions);
        assert(partitions.get(0).size() == 2);
        assert(partitions.get(1).size() == 2);
        assert(partitions.get(2).size() == 2);
    }


    @Test
    void testWriteDataToCsv() {
        List<String> data = Arrays.asList("item1", "item2", "item3");

        StepVerifier.create(repairsService.writeDataToCsv(Flux.fromIterable(data), "src/main/resources/temporary-folder/test.csv"))
                .verifyComplete();

        // Clean up the file
        new File("src/main/resources/temporary-folder/test.csv").delete();
    }


    @Test
    void testUploadAndDeleteFiles() {
        String[] fileNames = { "src/main/resources/temporary-folder/file1.csv", "src/main/resources/temporary-folder/file2.csv" };

        // Create dummy files to test the deletion
        for (String fileName : fileNames) {
            try {
                Files.createFile(Paths.get(fileName));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        doNothing().when(uploadService).upload(any(byte[].class));

        StepVerifier.create(repairsService.uploadAndDeleteFiles(fileNames))
                .verifyComplete();

        for (String fileName : fileNames) {
            assert(!Files.exists(Paths.get(fileName)));
        }
    }


    @Test
    void testCreateFolder() {
        StepVerifier.create(repairsService.createFolder())
                .expectNext("src/main/resources/temporary-folder/")
                .verifyComplete();
    }


    @Test
    void testDeleteFiles() {
        String[] fileNames = { "src/main/resources/temporary-folder/file1.csv", "src/main/resources/temporary-folder/file2.csv" };

        // Create dummy files to test the deletion
        for (String fileName : fileNames) {
            try {
                Files.createFile(Paths.get(fileName));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        StepVerifier.create(repairsService.deleteFiles(fileNames))
                .verifyComplete();

        for (String fileName : fileNames) {
            assert(!Files.exists(Paths.get(fileName)));
        }
    }

    @Test
    void testIsDirectoryEmpty() {
        File directory = new File("src/main/resources/emptyDir");
        directory.mkdir();

        assert(repairsService.isDirectoryEmpty(directory));
        directory.delete();
    }

}
