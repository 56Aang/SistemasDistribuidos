package Exceptions;

public class InvalidUserException extends Exception{
    public InvalidUserException(){
        super();
    }
    public InvalidUserException(String m){
        super(m);
    }
}
