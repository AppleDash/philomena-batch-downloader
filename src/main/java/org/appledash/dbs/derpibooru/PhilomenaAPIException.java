package org.appledash.dbs.derpibooru;

public class PhilomenaAPIException extends Exception {
    public PhilomenaAPIException(String message) {
        super(message);
    }

    public PhilomenaAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}
