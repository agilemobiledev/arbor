/**
 * This copy of Woodstox XML processor is licensed under the
 * Apache (Software) License, version 2.0 ("the License").
 * See the License for details about distribution rights, and the
 * specific rights regarding derivate works.
 *
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing Woodstox, in file "ASL2.0", under the same directory
 * as this file.
 */
package com.github.jknack.arbor;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

public class JsonParser {

  private static final ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);

    SerializationConfig serializationConfig =
        mapper.getSerializationConfig();
    VisibilityChecker<?> visibilityChecker =
        serializationConfig.getDefaultVisibilityChecker();
    // Field visibility by default.
    mapper.setVisibilityChecker(visibilityChecker
        .withFieldVisibility(Visibility.ANY)
        .withGetterVisibility(Visibility.NONE)
        .withSetterVisibility(Visibility.NONE)
        .withCreatorVisibility(Visibility.PROTECTED_AND_PUBLIC)
        );
  }

  public static <T> T read(final String json, final Class<T> type) throws IOException {
    return mapper.readValue(json, type);
  }

  public static void write(final File location, final Object object) throws IOException {
    mapper.writeValue(location, object);
  }
}
