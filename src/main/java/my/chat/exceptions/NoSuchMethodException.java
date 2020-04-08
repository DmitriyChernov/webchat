package my.chat.exceptions;

public class NoSuchMethodException extends Exception {

    public NoSuchMethodException(String method) {
        super("No such method " + method + "!");
    }
}
