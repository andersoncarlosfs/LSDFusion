/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.andersoncarlosfs.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 *
 * @author AndersonCarlos
 */
public class Links {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String[] prefix = {"http://www.dbpedia.org/", "http://www.yago.org/", "http://www.lri.fr/"};
        String path = "../../../Datasets/Museum/txt/links";
        Charset charset = StandardCharsets.UTF_8;
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(path + ".txt"), charset)) {
            PrintWriter writer = new PrintWriter(Files.newOutputStream(Paths.get(path + ".n3"), StandardOpenOption.CREATE));
            String line = null;
            int txt = 0;
            int n3 = 0;
            while ((line = reader.readLine()) != null) {
                String[] links = line.split("\t");
                txt++;
                if (links.length >= 2 && links.length <= 3) {
                    n3++;
                    writer.write("<" + prefix[0] + URLEncoder.encode(links[0], charset.name()) + ">\t<" + prefix[2] + URLEncoder.encode("sameAS", charset.name()) + ">\t<" + prefix[1] + URLEncoder.encode(links[1], charset.name()) + ">\t.\n");
                }
            }
            System.out.println(txt);
            System.out.println(n3);
            writer.flush();
            writer.close();
            reader.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}
