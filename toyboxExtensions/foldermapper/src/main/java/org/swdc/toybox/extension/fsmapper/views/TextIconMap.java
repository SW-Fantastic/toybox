package org.swdc.toybox.extension.fsmapper.views;

import org.swdc.fx.font.Fontawsome5Service;

import java.util.HashMap;
import java.util.Map;

public class TextIconMap {

    private Map<String,String> iconsMap = new HashMap<>();

    public TextIconMap() {
        iconsMap.put("unknown","question");
        iconsMap.put("folder", "folder");
        iconsMap.put("txt","file-alt");

        iconsMap.put("zip","file-archive");
        iconsMap.put("7z","file-archive");
        iconsMap.put("rar","file-archive");
        iconsMap.put("tar","file-archive");
        iconsMap.put("gz","file-archive");
        iconsMap.put("xz","file-archive");

        iconsMap.put("png","image");
        iconsMap.put("jpg","image");
        iconsMap.put("jpeg","image");
        iconsMap.put("bmp","image");
        iconsMap.put("gif","image");
        iconsMap.put("tiff","image");
        iconsMap.put("svg","image");
        iconsMap.put("ai","image");
        iconsMap.put("webp","image");

        iconsMap.put("xls","file-excel");
        iconsMap.put("xlsx","file-excel");
        iconsMap.put("doc","file-word");
        iconsMap.put("docx","file-word");
        iconsMap.put("pdf","file-pdf");

        iconsMap.put("mp3","file-audio");
        iconsMap.put("wma","file-audio");
        iconsMap.put("wav","file-audio");
        iconsMap.put("m4a","file-audio");
        iconsMap.put("ogg","file-audio");

        iconsMap.put("mp4","film");
        iconsMap.put("wmv","film");
        iconsMap.put("mkv","film");
        iconsMap.put("rmvb","film");

        iconsMap.put("htm","html5");
        iconsMap.put("html","html5");
        iconsMap.put("xhtml","html5");

        iconsMap.put("jar","coffee");
        iconsMap.put("java","coffee");
        iconsMap.put("class","coffee");

        iconsMap.put("chm","book");
        iconsMap.put("epub","book");
        iconsMap.put("mobi","book");
        iconsMap.put("awz3","book");

        iconsMap.put("torrent","download");
        iconsMap.put("url","link");
        iconsMap.put("lnk","link");

        iconsMap.put("exe","terminal");
        iconsMap.put("bat","terminal");
        iconsMap.put("cmd","terminal");
        iconsMap.put("sh","terminal");
        iconsMap.put("msi","terminal");

        iconsMap.put("ini","cog");
        iconsMap.put("inf","cog");
        iconsMap.put("conf","cog");
        iconsMap.put("properties","cog");
        iconsMap.put("json","cog");

    }

    public String getIconName(String extension){
        return iconsMap.getOrDefault(extension, "unknown");
    }

}
