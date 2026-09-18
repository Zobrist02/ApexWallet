package com.app.apexwallet.exception;

public class WalletAlreadyExistsException extends RuntimeException{
    public WalletAlreadyExistsException(String message){
        super(message);
    }
}
