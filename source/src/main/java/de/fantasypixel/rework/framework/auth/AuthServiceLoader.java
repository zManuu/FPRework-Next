package de.fantasypixel.rework.framework.auth;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;

import java.io.*;
import java.net.URL;
import java.util.List;

/**
 * Loads an {@link MemoryAuthService} from configuration files.
 */
@AllArgsConstructor
public class AuthServiceLoader {

    private final Gson gson;

    public AuthService load(URL authUnitsResource) throws IOException {
        MemoryAuthService service = new MemoryAuthService();
        try (InputStream inputStream = authUnitsResource.openStream();
             InputStreamReader reader = new InputStreamReader(inputStream)
        ) {
            TypeToken<List<AuthUnit>> unitsTypeToken = (TypeToken<List<AuthUnit>>) TypeToken.getParameterized(List.class, AuthUnit.class);
            List<AuthUnit> units = gson.fromJson(reader, unitsTypeToken);
            units.forEach(service::addUnit);
        }
        return service;
    }

}
