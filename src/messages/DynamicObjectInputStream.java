package messages;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.HashMap;
import java.util.Map;

public class DynamicObjectInputStream extends ObjectInputStream {
  private ServerClassLoader loader = new ServerClassLoader(ClassLoader.getSystemClassLoader());

  public DynamicObjectInputStream(InputStream in) throws IOException {
    super(in);
  }

  @Override
  protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
          ClassNotFoundException {
    try {
      return loader.loadClass(desc.getName());
    }
    catch (ClassNotFoundException e) {
      return super.resolveClass(desc);
    }
  }

  public void addClass(final String name, final byte[] defn) {
    loader.addClassDefinition(name, defn);
  }

  private class ServerClassLoader extends ClassLoader {
    public ServerClassLoader(ClassLoader parent) {
      super(parent);
    }

    private Map<String, byte[]> classes = new HashMap<>();

    @Override
    protected Class<?> findClass (String name) throws ClassNotFoundException {
      if (classes.containsKey(name)) {
        Class<?> result = defineClass(name, classes.get(name), 0, classes.get(name).length);
        return result;
      }

      throw new ClassNotFoundException();
    }

    public void addClassDefinition(String name, byte[] defn) {
      classes.put(name, defn);
    }
  }
}