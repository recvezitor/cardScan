package com.dimas.img_to_text;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.javatuples.Pair;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.dimas.img_to_text.Const.FEED_PATH;
import static com.dimas.img_to_text.Const.NOTFOUND;
import static com.dimas.img_to_text.Const.PNG_EXT;
import static com.dimas.img_to_text.Const.ROOT_PATH;
import static com.dimas.img_to_text.Const.TEMPLATE_PATH;
import static com.dimas.img_to_text.Const.height;
import static com.dimas.img_to_text.Const.stepWidth;
import static com.dimas.img_to_text.Const.width;
import static com.dimas.img_to_text.Const.xOffset;
import static com.dimas.img_to_text.Const.yOffset;
import static com.dimas.img_to_text.Utils.cardParser;
import static com.dimas.img_to_text.Utils.findAndListFiles;
import static com.dimas.img_to_text.Utils.findAndReadFiles;
import static com.dimas.img_to_text.Utils.imgToStr;
import static java.util.Objects.isNull;

@Slf4j
@SpringBootApplication
public class ImgToTextApplication implements CommandLineRunner {

    public final static boolean silent = true;
    public final static AtomicInteger counterOk = new AtomicInteger(0);
    public final static AtomicInteger counterNok = new AtomicInteger(0);

    public static void main(String[] args) {
        SpringApplication.run(ImgToTextApplication.class, args);
    }

    @Override
    public void run(String... args) {
        ScannerProps props = parseArgs(args);
        doRun(props);
    }

    private void doRun(ScannerProps props) {
        try {
            var templates = findAndReadFiles(Paths.get(props.getTemplatePath()), ".txt");
            log.info("templates={}", templates.keySet().stream().sorted().collect(Collectors.toList()));
            var feed = findAndListFiles(Paths.get(props.getFeedPath()), PNG_EXT);
            for (Path path : feed) {
                var currentFile = path.toFile();
                if (!silent) log.debug("Current file={}", currentFile.getName());
                long startTime = System.currentTimeMillis();
                List<String> result = scan(currentFile, templates);
                long stopTime = System.currentTimeMillis();
                logAndCount(currentFile, result, (stopTime - startTime));
            }
            log.info("stat: OK={}, NOK={}, failed={}%", counterOk, counterNok, getFailureRate());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getFailureRate() {
        return "%.1f".formatted(counterNok.get() * 100f / (counterNok.get() + counterOk.get()));
    }

    private void logAndCount(File currentFileName, List<String> result, Long elapsed) {
        log.info("[{}] {} - {} in {}ms", validateAndCount(currentFileName.getName(), result), currentFileName.getName(), String.join("", result), elapsed);
    }

    private String validateAndCount(String currentFileName, List<String> result) {
        if (currentFileName.replace(PNG_EXT, "").equalsIgnoreCase(String.join("", result))) {
            counterOk.incrementAndGet();
            return "OK";
        } else {
            counterNok.incrementAndGet();
            return "NOK";
        }
    }

    private List<String> scan(File fileName, Map<String, String> templates) throws IOException {
        var orgImage = ImageIO.read(fileName);
        var list = cardParser(fileName.getName().replace(PNG_EXT, ""));
        var counter = new AtomicInteger(0);
        var steps = list.stream().map(s -> counter.getAndIncrement()).toList();
        return steps.stream()
                .parallel()
                .map(step -> {
                    if (!silent) log.debug("Step={}", step);
                    BufferedImage image = orgImage.getSubimage(xOffset + step * stepWidth, yOffset, width, height);
                    String targetImg = imgToStr(image);
                    return templates.entrySet().stream()
                            .parallel()
                            .map(entry -> Pair.with(entry, StringUtils.getLevenshteinDistance(targetImg, entry.getValue())))
                            .peek(pair -> {
                                if (!silent) log.debug("step={}, key={}, factor={}", step, pair.getValue0().getKey(), pair.getValue1());
                            })
                            .min(Comparator.comparing(Pair::getValue1))
                            .map(pair -> pair.getValue0().getKey())
                            .orElse(NOTFOUND);
                })
                .collect(Collectors.toList());
    }

    private ScannerProps parseArgs(String... args) {
        log.debug("input args:");
        ScannerProps props = new ScannerProps();
        for (int i = 0; i < args.length; ++i) {
            log.debug("args[{}]: {}", i, args[i]);
            String[] parts = args[i].split("=");
            if (parts.length == 2) {
                switch (parts[0]) {
                    case "rootpath": {
                        props.setRootPath(parts[1]);
                    }
                    case "templatepath": {
                        props.setTemplatePath(parts[1]);
                    }
                    case "feedpath": {
                        props.setFeedPath(parts[1]);
                    }
                }
            } else {
                throw new RuntimeException("Invalid input args. Must be in format key=value");
            }
        }
        if (isNull(props.getRootPath())) {
            log.warn("rootpath is not provided. Will be used default");
            props.setRootPath(ROOT_PATH);
        }
        if (isNull(props.getTemplatePath())) {
            log.warn("templatepath is not provided. Will be used default");
            props.setTemplatePath(TEMPLATE_PATH);
        }
        if (isNull(props.getFeedPath())) {
            log.warn("feedpath is not provided. Will be used default");
            props.setFeedPath(FEED_PATH);
        }
        return props;
    }


}
