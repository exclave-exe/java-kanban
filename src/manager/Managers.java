package manager;

import java.io.File;

public class Managers {
    public static InMemoryTaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static FileBackedTaskManager getFileBacked(File file) {
        return FileBackedTaskManager.loadFromFile(file);
    }
}
