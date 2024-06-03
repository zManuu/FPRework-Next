package de.fantasypixel.rework.framework.jsondata;

import com.google.gson.reflect.TypeToken;
import de.fantasypixel.rework.FPRework;
import lombok.AllArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Manages data loaded from json files such as Save-Points, Spawners, Items or Quests.
 */
@AllArgsConstructor
public class JsonDataManager {

    private final static String CLASS_NAME = JsonDataManager.class.getSimpleName();

    private final FPRework plugin;

    /**
     * Loads json-data into a set.
     * @param directory the directory to load files from
     * @param clazz the associated class (structure of the file)
     * @param <T> the type of the associated class
     */
    @Nonnull
    public <T> Set<T> loadJsonData(@Nonnull File directory, @Nonnull Class<T> clazz) {
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

    /**
     * Holds data for finding an entry based on the id in a given directory of json files.
     * @param file The parent directory to begin the search.
     * @param entries The entries loaded from the file, null if there is only one entry.
     * @param entry The entry found with matching id.
     * @param <T> The type of data to be loaded.
     */
    private record FileAndData<T>(@Nonnull File file, @Nullable Set<T> entries, @Nonnull T entry) {}

    /**
     * Finds the file and data loaded from this file by an entry-id.
     * @param directory the directory to search
     * @param clazz the associated class of the entries
     * @param entryId the id to look for in the json files
     * @return an object holding the file, all entries and the found entry
     */
    @Nullable
    private <T> FileAndData<T> findFileAndData(@Nonnull File directory, @Nonnull Class<?> clazz, int entryId) {
        File resultFile = null;
        Set<T> resultEntries = null;
        T resultEntry = null;
        String[] directoryChildren = directory.list();

        if (directoryChildren == null)
            return null;

        for (var children : directoryChildren) {
            var childrenFile = new File(directory, children);
            if (childrenFile.isDirectory()) {

                // load subdirectories
                this.plugin.getFpLogger().debug("Loading subdirectory to find entry {0}: {1}...", entryId, childrenFile);
                var subDirectoryResults = this.findFileAndData(childrenFile, clazz, entryId);
                if (subDirectoryResults != null)
                    return (FileAndData<T>) subDirectoryResults;

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
                        this.plugin.getFpLogger().debug("Checking multiple entries from file {0} for entry {1}.", childrenFile.getPath(), entryId);
                        var arrayType = TypeToken.getParameterized(Set.class, clazz).getType();
                        var entries = (Set<T>) this.plugin.getGson().fromJson(reader, arrayType);
                        var entryIds = entries.stream().map(e -> {
                            try {
                                var idField = clazz.getDeclaredField("id");
                                idField.setAccessible(true);
                                return (Integer) idField.get(e);
                            } catch (IllegalAccessException | NoSuchFieldException ex) {
                                plugin.getFpLogger().error(CLASS_NAME, "findFileAndData", ex);
                                return -1;
                            }
                        }).collect(Collectors.toSet());

                        if (entryIds.contains(entryId)) {

                            // found in this file with multiple entries
                            plugin.getFpLogger().debug("Found json-data-entry {0} in file {1}.", entryId, childrenFile.getPath());
                            var entry = entries.stream()
                                    .filter(e -> {
                                        try {
                                            var idField = clazz.getDeclaredField("id");
                                            idField.setAccessible(true);
                                            return (Integer) idField.get(e) == entryId;
                                        } catch (IllegalAccessException | NoSuchFieldException ex) {
                                            plugin.getFpLogger().error(CLASS_NAME, "findFileAndData", ex);
                                            return false;
                                        }
                                    })
                                    .findFirst()
                                    .orElse(null);

                            resultFile = childrenFile;
                            resultEntries = entries;
                            resultEntry = entry;

                        }

                    } else {

                        // single entry in file
                        this.plugin.getFpLogger().debug("Loading single entry from file {0}.", childrenFile.getPath());
                        var entry = this.plugin.getGson().fromJson(reader, clazz);

                        var entryIdField = clazz.getDeclaredField("id");
                        entryIdField.setAccessible(true);
                        var entryIdValue = (Integer) entryIdField.get(entry);

                        if (entryId == entryIdValue) {
                            plugin.getFpLogger().debug("Found json-data-entry {0} in file {1}.", entryId, childrenFile.getPath());
                            resultFile = childrenFile;
                            resultEntry = (T) entry;
                            resultEntries = null;
                        }

                    }
                } catch (IOException | ClassCastException | NoSuchFieldException | IllegalAccessException ex) {
                    this.plugin.getFpLogger().error(CLASS_NAME, "loadJsonData", ex);
                }

            }
        }

        if (resultFile == null || resultEntry == null)
            return null;

        return new FileAndData<>(resultFile, resultEntries, resultEntry);
    }

    /**
     * Converts a set of data to a {@link JsonDataContainer} that supports editing, updating and deleting.
     * @param directory the base directory of the json-data
     * @param clazz the associated class of the entries
     * @param entries the initial set of entries
     * @return the created {@link JsonDataContainer}
     */
    public <T> JsonDataContainer<T> convertEntriesToJsonDataContainer(@Nonnull File directory, @Nonnull Class<?> clazz, @Nonnull Set<T> entries) {
        return new JsonDataContainer<>() {

            @Nonnull
            @Override
            public Set<T> getEntries() {
                return entries;
            }

            @Override
            public boolean create(T entry) {

                // find fileName & id (from name field or generated id)
                String fileName;
                int entryId = ThreadLocalRandom.current().nextInt(999999);
                try {
                    var fileNameField = entry.getClass().getDeclaredField("name");
                    fileNameField.setAccessible(true);
                    var fileNameValue = (String) Objects.requireNonNull(fileNameField.get(entry));
                    fileName = fileNameValue.replaceAll(" ", "_") + ".json";
                } catch (IllegalAccessException ex) {
                    plugin.getFpLogger().error(CLASS_NAME, "convertEntriesToJsonDataContainer->create", ex);
                    return false;
                } catch (NoSuchFieldException | NullPointerException ex) {
                    fileName = entryId + ".json";
                }

                // set id
                try {
                    var entryIdField = entry.getClass().getDeclaredField("id");
                    entryIdField.setAccessible(true);
                    entryIdField.set(entry, entryId);
                } catch (NoSuchFieldException | IllegalAccessException ex) {
                    plugin.getFpLogger().error(CLASS_NAME, "convertEntriesToJsonDataContainer->create", ex);
                    return false;
                }

                // create, populate file
                String entryJson = plugin.getGson().toJson(entry);
                File file = new File(directory, fileName);
                file.getParentFile().mkdirs();
                try {
                    file.createNewFile();
                } catch (IOException ex) {
                    plugin.getFpLogger().error(CLASS_NAME, "convertEntriesToJsonDataContainer->create", ex);
                    return false;
                }
                try (var writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write(entryJson);
                } catch (IOException ex) {
                    plugin.getFpLogger().error(CLASS_NAME, "convertEntriesToJsonDataContainer->create", ex);
                    return false;
                }

                entries.add(entry);
                plugin.getFpLogger().debug("Successfully created json-entry of type {0} in file {1}.", entry.getClass().getSimpleName(), fileName);
                return true;
            }

            @Override
            public boolean modify(T entry) {
                plugin.getFpLogger().error(CLASS_NAME, "modify", "NOT IMPLEMENTED YET!");
                return false;
            }

            @Override
            public boolean delete(int entryId) {
                var fileAndData = findFileAndData(directory, clazz, entryId);

                if (fileAndData == null)
                    return false;

                // remove from cache
                entries.removeIf(e -> {
                    try {
                        var idField = clazz.getDeclaredField("id");
                        idField.setAccessible(true);
                        return (Integer) idField.get(e) == entryId;
                    } catch (IllegalAccessException | NoSuchFieldException ex) {
                        plugin.getFpLogger().error(CLASS_NAME, "convertEntriesToJsonDataContainer->delete", ex);
                        return false;
                    }
                });

                if (fileAndData.entries() == null) {

                    // last entry -> delete file
                    plugin.getFpLogger().debug("Deleting json-data file {0} as {1} was the last entry.", fileAndData.file().getPath(), entryId);
                    return fileAndData.file().delete();

                } else {

                    // not the last entry -> remove from file
                    fileAndData.entries().remove(fileAndData.entry());
                    var newFileJson = plugin.getGson().toJson(fileAndData.entries());
                    try (var writer = new BufferedWriter(new FileWriter(fileAndData.file()))) {
                        writer.write(newFileJson);
                        plugin.getFpLogger().debug("Removed json-data entry {0} from file {1}.", entryId, fileAndData.file().getPath());
                        return true;
                    } catch (IOException ex) {
                        plugin.getFpLogger().error(CLASS_NAME, "convertEntriesToJsonDataContainer->delete", ex);
                        return false;
                    }

                }
            }

        };
    }

}
