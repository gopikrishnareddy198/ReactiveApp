package com.example.ReactiveApp.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
public class ProcessRepairsServiceRefactored {

  private final UploadService uploadService;

  public ProcessRepairsServiceRefactored(UploadService uploadService) {
    this.uploadService = uploadService;
  }

  public Mono<String> processDataAndUpload(String caseId, String dsId, List<String> data) {
    int numberOfPartitions = data.size() <= 50 ? 1 : 3;
    List<List<String>> partitions = partitionList(data, numberOfPartitions);
    List<String> fileNames = generateFileNames(caseId, numberOfPartitions);

    return createFolder()
        .thenMany(Flux.fromIterable(fileNames)
            .zipWith(Flux.fromIterable(partitions))
            .flatMap(tuple -> writeDataToCsv(Flux.fromIterable(tuple.getT2()), tuple.getT1())))
        .then(uploadAndDeleteFiles(fileNames))
        .thenReturn("Repairs processed successfully");
  }

  private List<List<String>> partitionList(List<String> data, int numberOfPartitions) {
    int partitionSize = (int) Math.ceil((double) data.size() / numberOfPartitions);
    return IntStream.range(0, numberOfPartitions)
        .mapToObj(i -> {
          int start = i * partitionSize;
          int end = Math.min(start + partitionSize, data.size());
          return data.subList(start, end);
        })
        .collect(Collectors.toList());
  }

  private List<String> generateFileNames(String caseId, int numberOfPartitions) {
    List<String> fileNames = new ArrayList<>();
    for (int i = 0; i < numberOfPartitions; i++) {
      fileNames.add("src/main/resources/temporary-folder/" + caseId + "-part-" + (i + 1) + ".csv");
    }
    return fileNames;
  }

  private Mono<Void> writeDataToCsv(Flux<String> dataFlux, String fileName) {
    return dataFlux
        .collectList()
        .publishOn(Schedulers.boundedElastic())
        .flatMap(data -> {
          try (FileOutputStream fos = new FileOutputStream(fileName);
              OutputStreamWriter osw = new OutputStreamWriter(fos);
              CSVPrinter csvPrinter = new CSVPrinter(osw,
                  CSVFormat.DEFAULT.withHeader("serialNumber"))) {
            for (String record : data) {
              csvPrinter.printRecord(record);
            }
            csvPrinter.flush();
            return Mono.empty();
          } catch (IOException e) {
            return Mono.error(new RuntimeException("Error writing to file: " + fileName, e));
          }
        });
  }

  private Mono<Void> uploadAndDeleteFiles(List<String> fileNames) {
    return Flux.fromIterable(fileNames)
        .flatMap(this::uploadFile)
        .then(deleteFiles(fileNames));
  }

  private Mono<Void> uploadFile(String fileName) {
    return Mono.fromCallable(() -> {
          try (FileInputStream fis = new FileInputStream(fileName)) {
            byte[] fileContent = fis.readAllBytes();
            uploadService.upload(fileContent);
            return Mono.empty();
          } catch (IOException e) {
            throw new RuntimeException("Error uploading file: " + fileName, e);
          }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .then();
  }

  private Mono<Void> createFolder() {
    return Mono.fromCallable(() -> {
          File folder = new File("src/main/resources/temporary-folder");
          if (folder.exists() || folder.mkdirs()) {
            return folder.toString();
          } else {
            throw new Exception("Failed to create the folder");
          }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .then();
  }

  private Mono<Void> deleteFiles(List<String> fileNames) {
    return Flux.fromIterable(fileNames)
        .flatMap(fileName -> Mono.fromRunnable(() -> deleteFileAndEmptyFolder(fileName))
            .subscribeOn(Schedulers.boundedElastic()))
        .then();
  }

  private void deleteFileAndEmptyFolder(String fileName) {
    File file = new File(fileName);
    boolean fileDeleted = file.delete();
    if (fileDeleted) {
      log.info("Deleted file: {}", fileName);
    } else {
      log.warn("Failed to delete file: {}", fileName);
    }
    deleteEmptyFolder(fileName);
  }

  private void deleteEmptyFolder(String fileName) {
    String folderPath = fileName.substring(0, fileName.lastIndexOf("/"));
    File folder = new File(folderPath);
    if (isDirectoryEmpty(folder)) {
      try {
        if (Files.deleteIfExists(folder.toPath())) {
          log.info("Deleted folder: {}", folderPath);
        }
      } catch (IOException ioException) {
        log.warn("Failed to delete folder: {} {}", folderPath, ioException.getMessage());
      }
    }
  }

  private boolean isDirectoryEmpty(File directory) {
    String[] files = directory.list();
    return files == null || files.length == 0;
  }
}
