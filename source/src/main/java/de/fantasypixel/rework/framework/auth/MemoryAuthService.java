package de.fantasypixel.rework.framework.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * An in-memory auth-service. It must be fed with all the callers, grants and units.
 */
public class MemoryAuthService implements AuthService {

    private final List<AuthCaller> callers = new ArrayList<>();
    private final List<AuthUnit> units = new ArrayList<>();
    private final List<AuthGrant> grants = new ArrayList<>();

    public void addCaller(AuthCaller caller) {
        if (!this.callers.contains(caller)) {
            callers.add(caller);
        }
    }

    public void addUnit(AuthUnit unit) {
        if (!this.units.contains(unit)) {
            this.units.add(unit);
        }
    }

    public void addGrant(AuthGrant grant) {
        if (!verifyAuthorization(grant.getAuthCaller(), grant.getAuthUnit(), grant.getAuthLevel())) {
            grants.add(grant);
        }
    }

    @Override
    public void verify(AuthCaller authCaller, AuthUnit authUnit, AuthLevel authLevel) throws AuthException {
        if (!verifyAuthentication(authCaller)) {
            throw new AuthenticationException();
        }
        if (!verifyAuthorization(authCaller.getAuthName(), authUnit.getName(), authLevel)) {
            throw new AuthorizationException();
        }
    }

    @Override
    public void verify(AuthCaller authCaller, String pathSpec, AuthLevel authLevel) throws NoSuchElementException, AuthException {
        AuthUnit authUnit = units.stream()
                .filter(unit -> Objects.equals(pathSpec, unit.getPathSpec()))
                .findFirst()
                .orElseThrow();

        verify(authCaller, authUnit, authLevel);
    }

    private boolean verifyAuthentication(AuthCaller authCaller) {
        for (AuthCaller caller : callers) {
            if (Objects.equals(caller.getAuthName(), authCaller.getAuthName())
                && Objects.equals(caller.getPassword(), authCaller.getPassword())) {
                return true;
            }
        }
        return false;
    }

    private boolean verifyAuthorization(String authCaller, String authUnit, AuthLevel authLevel) {
        for (AuthGrant authGrant : grants) {
            if (!Objects.equals(authCaller, authGrant.getAuthCaller())) {
                continue;
            }
            if (!Objects.equals(authUnit, authGrant.getAuthUnit())) {
                continue;
            }
            if (authGrant.getAuthLevel().getLevel() >= authLevel.getLevel()) {
                return true;
            }
        }
        return false;
    }

}
