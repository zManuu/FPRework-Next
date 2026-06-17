package de.fantasypixel.rework.framework.auth;

import java.util.NoSuchElementException;

public interface AuthService {

    void verify(AuthCaller authCaller, AuthUnit authUnit, AuthLevel authLevel) throws AuthException;

    void verify(AuthCaller authCaller, String pathSpec, AuthLevel authLevel) throws NoSuchElementException, AuthException;

}
