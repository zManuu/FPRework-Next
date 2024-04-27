package de.fantasypixel.rework.framework.jsondata;

import com.google.gson.reflect.TypeToken;
import de.fantasypixel.rework.FPRework;
import lombok.AllArgsConstructor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Manages data loaded from json files such as Save-Points, Spawners, Items or Quests.
 */
@AllArgsConstructor
public class JsonDataManager {

    private final static String CLASS_NAME = JsonDataManager.class.getSimpleName();

    private final FPRework plugin;

    public <T> Set<T> loadJsonData(File directory, Class<T> clazz) {
        var results = new HashSet<T>();
        var directoryChildren = directory.list();

        if (directoryChildren == null)
            return results;

        for (var children : directoryChildren) {
            var childrenFile = new File(directory, children);
            if (childrenFile.isDirectory()) {

                // load subdirectories
                this.plugin.getFpLogger().debug("Loading subdirectory {0}...", childrenFile);
                results.addAll(this.loadJsonData(childrenFile, clazz));

            } else if (childrenFile.isFile()) {

                // check if there is 1 / multiple entries in the file
                var isMultiple = false;
                try (var reader = new BufferedReader(new FileReader(childrenFile))) {
                    if (reader.read() == '[')
                        isMultiple = true;
                } catch (IOException ex) {
                    this.plugin.getFpLogger().error(CLASS_NAME, "loadJsonData (pre-read)", ex);
                }

                // load file
                try (var reader = new FileReader(childrenFile)) {
                    if (isMultiple) {

                        // multiple entries in file
                        this.plugin.getFpLogger().debug("Loading multiple entries from file {0}.", childrenFile.getPath());
                        var arrayType = TypeToken.getParameterized(Set.class, clazz).getType();
                        var entries = this.plugin.getGson().fromJson(reader, arrayType);
                        results.addAll((Collection<? extends T>) entries);

                    } else {

                        // single entry in file
                        this.plugin.getFpLogger().debug("Loading single entry from file {0}.", childrenFile.getPath());
                        var entry = this.plugin.getGson().fromJson(reader, clazz);
                        results.add(entry);

                    }
                } catch (IOException ex) {
                    this.plugin.getFpLogger().error(CLASS_NAME, "loadJsonData", ex);
                }

            }
        }

        this.plugin.getFpLogger().debug("Loaded {0} json-data entries from directory {1}.", results.size(), directory.getPath());
        return results;
    }

}
