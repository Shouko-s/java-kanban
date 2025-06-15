import manager.HistoryManager;
import manager.Managers;
import manager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;
import manager.FileBackedTaskManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    // Здесь будут новые тесты по текущему заданию

    @Test
    void epicStatus_AllNew() {
        TaskManager manager = Managers.getDefault();
        Epic epic = new Epic("Epic", "desc", null, null);
        manager.addEpic(epic);
        Subtask s1 = new Subtask("s1", "d1", Status.NEW, epic.getId(), null, null);
        Subtask s2 = new Subtask("s2", "d2", Status.NEW, epic.getId(), null, null);
        manager.addSubtask(s1);
        manager.addSubtask(s2);
        assertEquals(Status.NEW, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicStatus_AllDone() {
        TaskManager manager = Managers.getDefault();
        Epic epic = new Epic("Epic", "desc", null, null);
        manager.addEpic(epic);
        Subtask s1 = new Subtask("s1", "d1", Status.DONE, epic.getId(), null, null);
        Subtask s2 = new Subtask("s2", "d2", Status.DONE, epic.getId(), null, null);
        manager.addSubtask(s1);
        manager.addSubtask(s2);
        assertEquals(Status.DONE, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicStatus_NewAndDone() {
        TaskManager manager = Managers.getDefault();
        Epic epic = new Epic("Epic", "desc", null, null);
        manager.addEpic(epic);
        Subtask s1 = new Subtask("s1", "d1", Status.NEW, epic.getId(), null, null);
        Subtask s2 = new Subtask("s2", "d2", Status.DONE, epic.getId(), null, null);
        manager.addSubtask(s1);
        manager.addSubtask(s2);
        assertEquals(Status.IN_PROGRESS, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicStatus_InProgress() {
        TaskManager manager = Managers.getDefault();
        Epic epic = new Epic("Epic", "desc", null, null);
        manager.addEpic(epic);
        Subtask s1 = new Subtask("s1", "d1", Status.IN_PROGRESS, epic.getId(), null, null);
        Subtask s2 = new Subtask("s2", "d2", Status.NEW, epic.getId(), null, null);
        manager.addSubtask(s1);
        manager.addSubtask(s2);
        assertEquals(Status.IN_PROGRESS, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void shouldThrowOnIntersectingTasks() {
        TaskManager manager = Managers.getDefault();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        Task t1 = new Task("t1", "d1", Status.NEW, java.time.Duration.ofMinutes(60), now);
        manager.addTask(t1);
        Task t2 = new Task("t2", "d2", Status.NEW, java.time.Duration.ofMinutes(30), now.plusMinutes(30));
        Exception ex = assertThrows(IllegalArgumentException.class, () -> manager.addTask(t2));
        assertTrue(ex.getMessage().contains("пересекается"));
    }

    @Test
    void prioritizedTasksShouldBeSortedByStartTime() {
        TaskManager manager = Managers.getDefault();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        Task t1 = new Task("t1", "d1", Status.NEW, java.time.Duration.ofMinutes(60), now.plusMinutes(60));
        Task t2 = new Task("t2", "d2", Status.NEW, java.time.Duration.ofMinutes(30), now);
        Task t3 = new Task("t3", "d3", Status.NEW, java.time.Duration.ofMinutes(15), null); // без времени
        manager.addTask(t1);
        manager.addTask(t2);
        manager.addTask(t3);
        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(2, prioritized.size());
        assertEquals(t2, prioritized.get(0));
        assertEquals(t1, prioritized.get(1));
    }

    @Test
    void fileBackedTaskManagerShouldSaveAndLoadDurationAndStartTime() throws Exception {
        java.io.File file = java.io.File.createTempFile("tasks", ".csv");
        file.deleteOnExit();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        Task t = new Task("t1", "d1", Status.NEW, java.time.Duration.ofMinutes(42), now);
        manager.addTask(t);
        manager = FileBackedTaskManager.loadFromFile(file);
        Task loaded = manager.getTask(t.getId());
        assertNotNull(loaded);
        assertEquals(java.time.Duration.ofMinutes(42), loaded.getDuration());
        assertEquals(now, loaded.getStartTime());
    }

    @Test
    void historyManager_EmptyAndDuplicatesAndRemovals() {
        HistoryManager history = Managers.getDefaultHistory();
        assertTrue(history.getHistory().isEmpty());
        Task t1 = new Task("t1", "d1", Status.NEW, null, null); t1.setId(1);
        Task t2 = new Task("t2", "d2", Status.NEW, null, null); t2.setId(2);
        Task t3 = new Task("t3", "d3", Status.NEW, null, null); t3.setId(3);
        history.add(t1); history.add(t2); history.add(t3); history.add(t2); // дублирование
        List<Task> hist = history.getHistory();
        assertEquals(3, hist.size());
        assertEquals(t1, hist.get(0));
        assertEquals(t3, hist.get(1));
        assertEquals(t2, hist.get(2));
        // удаление из начала
        history.remove(t1.getId());
        assertEquals(2, history.getHistory().size());
        assertEquals(t3, history.getHistory().get(0));
        // удаление из конца
        history.remove(t2.getId());
        assertEquals(1, history.getHistory().size());
        assertEquals(t3, history.getHistory().get(0));
        // удаление из середины (добавим снова)
        history.add(t1); history.add(t2); // t3, t1, t2
        history.remove(t1.getId());
        List<Task> h = history.getHistory();
        assertEquals(2, h.size());
        assertEquals(t3, h.get(0));
        assertEquals(t2, h.get(1));
    }

    @Test
    void fileBackedTaskManager_ShouldThrowOnInvalidFile() {
        java.io.File file = new java.io.File("/nonexistent/path/tasks.csv");
        assertThrows(RuntimeException.class, () -> new FileBackedTaskManager(file).addTask(new Task("t", "d", Status.NEW, null, null)));
    }

    @Test
    void fileBackedTaskManager_ShouldNotThrowOnValidFile() {
        assertDoesNotThrow(() -> {
            java.io.File file = java.io.File.createTempFile("tasks", ".csv");
            file.deleteOnExit();
            FileBackedTaskManager manager = new FileBackedTaskManager(file);
            manager.addTask(new Task("t", "d", Status.NEW, null, null));
        });
    }

    //Тесты ТУТ СНИЗУ
}
