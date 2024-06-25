package com.example.ReactiveApp.service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProcessRepairsService {

    private final  UploadService uploadService;

    public ProcessRepairsService(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    public Mono<Void> processDataAndUpload(String caseId, String dsId, List<String> data) {
        int numberOfPartitions = data.size() <= 50 ? 1 : 3;
        List<List<String>> partitions = partitionList(data, numberOfPartitions);

        List<Mono<Void>> writeMonos = new ArrayList<>();
        String[] fileNames = new String[numberOfPartitions];

        return createFolder()
                .thenMany(Flux.range(0, numberOfPartitions)
                        .flatMap(i -> {
                            fileNames[i] = "src/main/resources/temporary-folder/" + caseId + "-part-" + (i + 1) + ".csv";
                            return writeDataToCsv(Flux.fromIterable(partitions.get(i)), fileNames[i]);
                        }))
                .collectList()
                .flatMap(writeResults -> uploadAndDeleteFiles(fileNames))
                .subscribeOn(Schedulers.boundedElastic());

        /*createFolder().subscribe();

        for (int i = 0; i < numberOfPartitions; i++) {
            fileNames[i] = "src/main/resources/temporary-folder/"+caseId+"-part-" + (i + 1) + ".csv";
            writeMonos.add(writeDataToCsv(Flux.fromIterable(partitions.get(i)), fileNames[i]));
        }

        return Mono.when(writeMonos)
                .then(Mono.defer(() -> uploadAndDeleteFiles(fileNames)))
                .subscribeOn(Schedulers.boundedElastic());*/
    }

    protected List<List<String>> partitionList(List<String> data, int numberOfPartitions) {
        List<List<String>> partitions = new ArrayList<>();
        int partitionSize = (int) Math.ceil((double) data.size() / numberOfPartitions);

        for (int i = 0; i < numberOfPartitions; i++) {
            int start = i * partitionSize;
            int end = Math.min(start + partitionSize, data.size());
            partitions.add(data.subList(start, end));
        }

        // Log the partition sizes
        partitions.forEach(partition -> System.out.println("Partition size: " + partition.size()));


        return partitions;
    }

    protected Mono<Void> writeDataToCsv(Flux<String> dataFlux, String fileName) {
        return dataFlux
                .publishOn(Schedulers.boundedElastic())
                .collectList()
                .flatMap(data -> {
                    try (FileOutputStream fos = new FileOutputStream(fileName);
                         OutputStreamWriter osw = new OutputStreamWriter(fos);
                         CSVPrinter csvPrinter = new CSVPrinter(osw, CSVFormat.newFormat(',').withQuoteMode(QuoteMode.MINIMAL).withHeader("serialNumber\n"))) {
                         csvPrinter.printRecord(data.stream().collect(Collectors.joining(",\n")));
                         csvPrinter.flush();
                        return Mono.empty();
                    } catch (IOException e) {
                        return Mono.error(new RuntimeException("Error writing to file: " + fileName, e));
                    }
                });
    }

    protected Mono<Void> uploadAndDeleteFiles(String[] fileNames) {
        return Flux.fromArray(fileNames)
                .flatMap(this::uploadFile)
                .then(deleteFiles(fileNames));
    }

    protected Mono<Void> uploadFile(String fileName) {
        return Mono.fromCallable(() -> {
            try (FileInputStream fis = new FileInputStream(fileName)) {
                byte[] fileContent = fis.readAllBytes();

                uploadService.upload(fileContent);
                return Mono.empty();
            } catch (IOException e) {
                return Mono.error(new RuntimeException("Error uploading file: " + fileName, e));
            }
        }).then();
    }

    public Mono<byte[]> readFileToByteArray(String filePath) {
        Path path = Paths.get(filePath);
        return DataBufferUtils.read(path, new DefaultDataBufferFactory(), 4096)
                .collectList()
                .flatMap(dataBuffers -> DataBufferUtils.join(Flux.fromIterable(dataBuffers)))
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                })
                .doOnError(e -> System.out.println("Error reading file: " + e.getMessage()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> createFolder(){
        return Mono.fromCallable(() -> {
                    File folder = new File("src/main/resources/temporary-folder");
                    if (folder.exists() || folder.mkdirs()) {
                        return folder.toString() + "/";
                    } else {
                        throw new Exception("Failed to create the folder");
                    }
                }).subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> Mono.error(new Exception("Failed to create the folder", e)));

    }

    protected Mono<Void> deleteFiles(String[] fileNames) {
        return Flux.fromArray(fileNames)
                .flatMap(fileName -> Mono.fromRunnable(() -> {
                    File file = new File(fileName);
                    final String folderPath = fileName.substring(0, fileName.lastIndexOf("/"));
                    final boolean fileDeleted = file.delete();
                    if(fileDeleted){
                        log.info("The following file deleted: {}", fileName);
                    }else{
                        log.warn("Failed to delete the following file: {}",fileName);
                    }
                    try{
                        if(isDirectoryEmpty(new File(Paths.get(folderPath).toString()))){
                            if(Files.deleteIfExists(Paths.get(folderPath)))
                                log.info("The following folder is deleted: {}", folderPath);
                        }
                    }catch (IOException ioException){
                        log.warn("Failed to delete the following folder: {} {}", folderPath, ioException.getMessage());
                    }
                }))
                .then();
    }

    public boolean isDirectoryEmpty(File directory){
        if(directory.isDirectory()){
            String[] files = directory.list();
            return files == null || files.length == 0;
        }
        return false;
    }

}
