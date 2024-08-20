package nade.lemon.beehive.utils;

import java.nio.charset.Charset;

import com.google.common.io.BaseEncoding;

public class Base64 {
    
    private Base64() {}

    public static String encode(String string) {
        try {
            return new String(BaseEncoding.base64().encode(string.getBytes(Charset.defaultCharset())));
        } catch (Exception e) {}
        return new String(BaseEncoding.base64().encode(string.getBytes()));
    }

    public static String decode(String string) {
        try {
            return new String(BaseEncoding.base64().decode(string), Charset.defaultCharset());
        } catch (Exception e) {}
        return new String(BaseEncoding.base64().decode(string));
    }
}
