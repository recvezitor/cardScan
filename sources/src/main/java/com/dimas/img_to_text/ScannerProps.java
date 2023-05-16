package com.dimas.img_to_text;

import lombok.Data;

@Data
public class ScannerProps {

    private String rootPath;
    private String templatePath;
    private String feedPath;
    private Boolean silent = true;

}
