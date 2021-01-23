package Exceptions;

public class UserAlreadyExistingException extends Exception{
    public UserAlreadyExistingException(){
        super();
    }

    public UserAlreadyExistingException(String m){
        super(m);
    }
}
