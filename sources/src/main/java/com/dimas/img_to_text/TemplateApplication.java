package com.dimas.img_to_text;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import static com.dimas.img_to_text.Const.CARD_PATTERN;
import static com.dimas.img_to_text.Const.FEED_PATH;
import static com.dimas.img_to_text.Const.PNG_EXT;
import static com.dimas.img_to_text.Const.ROOT_PATH;
import static com.dimas.img_to_text.Const.TEMPLATE_PATH;
import static com.dimas.img_to_text.Const.height;
import static com.dimas.img_to_text.Const.stepWidth;
import static com.dimas.img_to_text.Const.width;
import static com.dimas.img_to_text.Const.xOffset;
import static com.dimas.img_to_text.Const.yOffset;
import static com.dimas.img_to_text.Utils.findAndListFiles;
import static com.dimas.img_to_text.Utils.imgToStr;
import static com.dimas.img_to_text.Utils.saveImg;
import static com.dimas.img_to_text.Utils.saveText;

@Slf4j
public class TemplateApplication {

    public static void main(String[] args) {
        try {
            //club - трефы, diamond - буби, heart - червы, spade - пики
            FileUtils.cleanDirectory(new File(ROOT_PATH + TEMPLATE_PATH));
            var feed = findAndListFiles(Paths.get(ROOT_PATH + FEED_PATH), PNG_EXT);
            for (Path path : feed) {
                var currentFile = path.toFile();
                log.info("Current file={}", currentFile.getName());
                Matcher matcher = CARD_PATTERN.matcher(currentFile.getName().replace(PNG_EXT, ""));
                int step = 0;
                while (matcher.find()) {
                    String targetCard = matcher.group(1);
                    log.info("step={}, targetCard={}", step, targetCard);
                    createTemplate(targetCard, currentFile, step);
                    step++;
                }
            }
            logAndCount();
        } catch (Exception e) {
            log.error("Failed", e);
        }
    }

    private static void logAndCount() throws IOException {
        try (Stream<Path> files = Files.list(Paths.get(ROOT_PATH + TEMPLATE_PATH))) {
            log.info("total templates: {}", files.count() / 2);
        }
    }

    private static void createTemplate(String targetCard, File inputFile, int step) throws IOException {
        BufferedImage orgImage = ImageIO.read(inputFile);
        BufferedImage image = orgImage.getSubimage(xOffset + step * stepWidth, yOffset, width, height);
        saveImg(image, targetCard);
        saveText(imgToStr(image), targetCard);
    }

}
