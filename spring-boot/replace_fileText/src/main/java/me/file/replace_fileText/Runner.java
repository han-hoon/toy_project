package me.file.replace_fileText;

import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Component
public class Runner implements ApplicationRunner {

    private String targetString = null;
    private String newString = null;
    private String extension = null;

    private FileOutputStream logFile = null;

    private Logger logger = LoggerFactory.getLogger(Runner.class);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String[] sourceArgs = null;
        try {
            sourceArgs = args.getSourceArgs();
            setExtension(
                    sourceArgs[0].startsWith(".") ? sourceArgs[0] : "." + sourceArgs[0]
            );
            setTargetString(sourceArgs[1]);
            setNewString(sourceArgs[2]);
        } catch (Exception e) {
            logger.error("인자 값이 충분하지 않습니다.");
            logger.error("입력 예시) java -jar replace_fileText-0.0.1.jar txt text01 text02");
            throw new Exception();
        }
        updateChildDirectoryFile("./");
    }

    public void updateChildDirectoryFile(String fileName) throws Exception {
        File parentFile = new File(fileName);
        logger.info("========================================");
        logger.info("DirectoryName : " + fileName);
        for(File file : parentFile.listFiles()) {
            if(file.isDirectory()) {
                updateChildDirectoryFile(file.getAbsolutePath());
            } else if(file.getName().endsWith(extension)) {
                logger.info("fileName : " + file.getName());
                replaceText(file);
            }
        }
        logger.info("========================================");
        logger.info("");
    }

    private void replaceText(File file) throws IOException {
        BufferedReader br = null;
        PrintWriter pw = null;
        int lineCount = 0;
        StringBuilder sb = new StringBuilder();
        try {
            String encoding = getEncoding(file);
            String line = null;
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            if(encoding == null) {
                logger.error("지원하지 않는 인코딩 형식 입니다.");
                return;
            }
            while((line = br.readLine()) != null) {
                lineCount++;
                if(line.contains(targetString)) {
                    logger.info("변경 대상 파일 : " + file.getAbsolutePath());
                    logger.info("변경 대상 라인 : " + lineCount);
                    logger.info("변경 전: " + line);

                    line = line.replace(targetString, newString);

                    byte [] strBuffer = line.getBytes(encoding);
                    line = new String(strBuffer, encoding);

                    logger.info("변경 후 : " + line);
                };
                sb.append(line + "\r\n");
            }
            pw = new PrintWriter(file.getAbsolutePath(), encoding);
            pw.write(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if(br != null) br.close();
            if(pw != null) pw.close();
        }
    }

    private String getEncoding(File file) {
        String encoding = "";
        byte[] buf = new byte[4096];
        try(java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
            UniversalDetector detector = new UniversalDetector(null);
            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            detector.dataEnd();
            encoding = detector.getDetectedCharset();
            if (encoding != null) {
                logger.info(file.getName() + " - Detected encoding = " + encoding);
            } else {
                logger.info("No encoding detected.");
            }
            detector.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encoding;
    }

    private void writeLog(String log) throws IOException {
        logFile.write(log.getBytes(StandardCharsets.UTF_8));
    }

    public String getTargetString() {
        return targetString;
    }

    public void setTargetString(String targetString) {
        this.targetString = targetString;
    }

    public String getNewString() {
        return newString;
    }

    public void setNewString(String newString) {
        this.newString = newString;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
