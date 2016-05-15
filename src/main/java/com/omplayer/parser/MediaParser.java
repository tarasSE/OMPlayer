package com.omplayer.parser;


import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MediaParser {
    private Pattern imgPattern;
    private Pattern mp3Pattern;

    public Set<String> getIMG(final String address) {
        return getSMTH(address, getIMGPattern());
    }

    public void downloadIMG(String address) {
        downloadSMTH(address, getIMGPattern(), "pictures");
    }

    public Set<String> getMP3(final String address) {

        return getSMTH(address, getMP3Pattern());
    }

    public void downloadMP3(String address) {
        downloadSMTH(address, getMP3Pattern(), "mp3");
    }

    public Set<String> getSMTH(final String address, final Pattern pattern) {
        Set<String> smth = new TreeSet<>();
        URL url = null;

        try {
            url = new URL(address);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (url == null) {
            throw new RuntimeException("Something wrong with URL. Please recheck it and try later.");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {

            reader.lines().forEach(x -> {
                Matcher matcher = pattern.matcher(x);

                if (matcher.find()) {
//                    System.out.println(x);
                    smth.add(matcher.group(2));
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

        return smth;
    }

    public void downloadSMTH(String address, Pattern pattern, String itemsType) {
        getSMTH(address, pattern).forEach(x -> {
            try {
                InputStream is = new URL(x).openStream();

                File dirs = new File("./" + itemsType + "/");
                dirs.mkdirs();
                File file = new File(dirs.getPath() + x.substring(x.lastIndexOf("/"), x.length()));
                System.out.print(file.getName() + " - ");

                OutputStream writer = new FileOutputStream(file);

                byte[] buff = new byte[1024];
                int leng = 0;
                while ((leng = is.read(buff)) > 0) {
                    writer.write(buff, 0, leng);
                }

                System.out.println("success");
            } catch (FileNotFoundException e) {
                System.err.println("file not found ");
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public Pattern getIMGPattern() {
        if (imgPattern == null) {
            imgPattern = Pattern.compile("<img.*src=('|\")([\\w\\d\\\\/:._()-]+)('|\").*");
        }
        return imgPattern;
    }

    public Pattern getMP3Pattern() {
        if (mp3Pattern == null) {
            mp3Pattern = Pattern.compile("('|\")([\\w\\d\\\\/:._()-]+\\.mp3)('|\").*");
        }
        return mp3Pattern;
    }

//    public static void main(String[] args) throws Exception {
//        MediaParser mediaParser = new MediaParser();
//        mediaParser.downloadIMG("https://www.pinterest.com/categories/humor/");
//
////        mediaParser.downloadMP3("http://mp3.cc/search/f/" + URLEncoder.encode("кипелов я здесь", "UTF-8"));
//
//    }
}


