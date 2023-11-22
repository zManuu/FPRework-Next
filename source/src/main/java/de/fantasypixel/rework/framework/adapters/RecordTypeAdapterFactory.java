package de.fantasypixel.rework.framework.adapters;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import de.fantasypixel.rework.FPRework;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;

@AllArgsConstructor
public class RecordTypeAdapterFactory implements TypeAdapterFactory {

    private final static String CLASS_NAME = RecordTypeAdapterFactory.class.getSimpleName();

    private final FPRework plugin;

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) type.getRawType();
        if (!clazz.isRecord()) {
            return null;
        }
        TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

        return new TypeAdapter<>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                delegate.write(out, value);
            }

            @Override
            public T read(JsonReader reader) throws IOException {
                if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull();
                    return null;
                } else {
                    var recordComponents = clazz.getRecordComponents();
                    var typeMap = new HashMap<String, TypeToken<?>>();
                    for (java.lang.reflect.RecordComponent recordComponent : recordComponents) {
                        typeMap.put(recordComponent.getName(), TypeToken.get(recordComponent.getGenericType()));
                    }
                    var argsMap = new HashMap<String, Object>();
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String name = reader.nextName();
                        argsMap.put(name, gson.getAdapter(typeMap.get(name)).read(reader));
                    }
                    reader.endObject();

                    var argTypes = new Class<?>[recordComponents.length];
                    var args = new Object[recordComponents.length];
                    for (int i = 0; i < recordComponents.length; i++) {
                        argTypes[i] = recordComponents[i].getType();
                        args[i] = argsMap.get(recordComponents[i].getName());
                    }
                    Constructor<T> constructor;
                    try {
                        constructor = clazz.getDeclaredConstructor(argTypes);
                        constructor.setAccessible(true);
                        return constructor.newInstance(args);
                    } catch (Exception ex) {
                        plugin.getFpLogger().error(CLASS_NAME, "read", ex);
                        return null;
                    }
                }
            }
        };
    }

}
