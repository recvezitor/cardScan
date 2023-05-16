package com.dimas.img_to_text;

import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.dimas.img_to_text.Const.CARD_PATTERN;
import static com.dimas.img_to_text.Const.TEMPLATE_PATH;

public class Utils {

    public static String imgToStr(BufferedImage image) {
        int whiteBg = -1;
        int greyBg = -8882056;
        StringBuilder binaryString = new StringBuilder();
        for (int y = 1; y < image.getHeight(); y++) {
            for (int x = 1; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                binaryString.append((rgb == whiteBg || rgb == greyBg) ? " " : "*");
            }
        }
        return binaryString.toString();
    }

    public static List<String> cardParser(String fileName) {
        Matcher matcher = CARD_PATTERN.matcher(fileName);
        List<String> res = new ArrayList<>();
        while (matcher.find()) {
            String targetCard = matcher.group(1);
            res.add(targetCard);
        }
        return res;
    }

    public static Map<String, String> findAndReadFiles(Path path, String fileExtension) throws IOException {
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must be a directory! "  + path.toString());
        }
        Map<String, String> result;
        try (Stream<Path> walk = Files.walk(path)) {
            result = walk
                    .filter(p -> !Files.isDirectory(p))
                    .filter(p -> p.toString().endsWith(fileExtension))
                    .collect(
                            Collectors.toMap(
                                    p -> p.getFileName().toString().replace(fileExtension, ""),
                                    Utils::readText));
        }
        return result;
    }

    public static List<Path> findAndListFiles(Path path, String fileExtension) throws IOException {
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must be a directory! "  + path.toString());
        }
        List<Path> result;
        try (Stream<Path> walk = Files.walk(path)) {
            result = walk
                    .filter(p -> !Files.isDirectory(p))
                    .filter(p -> p.toString().endsWith(fileExtension))
                    .collect(Collectors.toList());
        }
        return result;
    }

    public static void saveImg(BufferedImage image, String name) {
        try {
            File outputfile = new File(TEMPLATE_PATH + name + ".png");
            ImageIO.write(image, "png", outputfile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readText(Path path) {
        try {
            File file = path.toFile();
            return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveText(String body, String name) {
        try {
            File outputfile = new File(TEMPLATE_PATH + name + ".txt");
            FileUtils.writeStringToFile(outputfile, body, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int levenshteinClassic(String targetStr, String sourceStr) {
        int m = targetStr.length(), n = sourceStr.length();
        int[][] delta = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            delta[i][0] = i;
        }
        for (int j = 1; j <= n; j++) {
            delta[0][j] = j;
        }
        for (int j = 1; j <= n; j++) {
            for (int i = 1; i <= m; i++) {
                if (targetStr.charAt(i - 1) == sourceStr.charAt(j - 1)) {
                    delta[i][j] = delta[i - 1][j - 1];
                } else {
                    delta[i][j] = Math.min(delta[i - 1][j] + 1,
                            Math.min(delta[i][j - 1] + 1, delta[i - 1][j - 1] + 1));
                }
            }
        }
        return delta[m][n];
    }

}
