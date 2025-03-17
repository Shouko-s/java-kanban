import manager.HistoryManager;
import manager.Managers;
import manager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    TaskManager taskManager = Managers.getDefault();
    Task task1 = new Task("model.Task 1", "Описание 1", Status.NEW);
    Task task2 = new Task("model.Task 2", "Описание 2", Status.DONE);
    Epic epic1 = new Epic("Подготовка к поездке", "Собрать вещи, купить билеты");
    Subtask subtask1 = new Subtask("Купить билеты", "Забронировать и оплатить билеты", Status.IN_PROGRESS, epic1.getId());
    Subtask subtask2 = new Subtask("Собрать чемодан", "Упаковать вещи", Status.NEW, epic1.getId());

    @Test
    void objectsShouldBeEqualIfTheirIdAreEqual() {
        int commonId = 1;
        task1.setId(commonId);
        task2.setId(commonId);
        assertEquals(task1, task2, "Объекты model.Task должны быть равны, если их id совпадают");
    }

    @Test
    void inheritorObjectsShouldBeEqualIfTheirIdAreEqual() {
        int commonId = 1;
        subtask1.setId(commonId);
        subtask2.setId(commonId);
        assertEquals(subtask1, subtask2, "Объекты model.Subtask должны быть равны, если их id совпадают");
    }

    @Test
    void epicShouldNotAddEpicIntoItself() {
        epic1.setId(100);
        epic1.addSubtask(epic1);
        assertTrue(epic1.getSubtaskIds().isEmpty(), "Эпик не должен добавляться в качестве своей подзадачи");
    }

    @Test
    public void testGetDefaultTaskManagerReturnsInitializedInstance() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager, "Менеджер задач не должен быть null");

        Task task = new Task("Test model.Task", "Описание тестовой задачи", Status.NEW);
        manager.addTask(task);

        Task retrievedTask = manager.getTask(task.getId());
        assertNotNull(retrievedTask);
        assertEquals(task, retrievedTask);
    }

    @Test
    public void testAddAndFindTasksById() {
        TaskManager manager = Managers.getDefault();

        Task task = new Task("model.Task", "T smth", Status.NEW);
        manager.addTask(task);

        Epic epic = new Epic("model.Epic", "E smth");
        manager.addEpic(epic);

        Subtask subtask = new Subtask("model.Subtask", "S smth", Status.NEW, epic.getId());
        manager.addSubtask(subtask);

        Task retrievedTask = manager.getTask(task.getId());
        assertNotNull(retrievedTask);
        assertEquals(task, retrievedTask);

        Epic retrievedEpic = manager.getEpic(epic.getId());
        assertNotNull(retrievedEpic);
        assertEquals(epic, retrievedEpic);

        Subtask retrievedSubtask = manager.getSubtask(subtask.getId());
        assertNotNull(retrievedSubtask);
        assertEquals(subtask, retrievedSubtask);
    }

    @Test
    public void testUniqueIdsForManuallyAssignedAndGeneratedTasks() {
        TaskManager manager = Managers.getDefault();

        Task task1 = new Task("model.Task 1", "Описание 1", Status.NEW);
        task1.setId(100);
        manager.addTask(task1);

        Task task2 = new Task("model.Task 2", "Описание 2", Status.NEW);
        manager.addTask(task2);

        int id1 = task1.getId();
        int id2 = task2.getId();

        assertNotEquals(id1, id2);

        Task retrievedTask1 = manager.getTask(id1);
        Task retrievedTask2 = manager.getTask(id2);

        assertEquals(task1, retrievedTask1);
        assertEquals(task2, retrievedTask2);
    }

    @Test
    public void testTaskImmutabilityAfterAddingToManager() {
        TaskManager manager = Managers.getDefault();

        String expectedTitle = "Test model.Task";
        String expectedDescription = "Test Description";
        Status expectedStatus = Status.NEW;
        Task originalTask = new Task(expectedTitle, expectedDescription, expectedStatus);

        manager.addTask(originalTask);

        Task retrievedTask = manager.getTask(originalTask.getId());
        assertNotNull(retrievedTask);

        assertEquals(expectedTitle, retrievedTask.getTitle());
        assertEquals(expectedDescription, retrievedTask.getDescription());
        assertEquals(expectedStatus, retrievedTask.getStatus());
        assertTrue(retrievedTask.getId() > 0);
    }

    @Test
    public void testHistoryAdd() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        Task task1 = new Task("Задача 1", "Описание задачи 1", Status.NEW);
        Task task2 = new Task("Задача 2", "Описание задачи 2", Status.NEW);
        task1.setId(0);
        task2.setId(1);

        historyManager.add(task2);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(1));
        assertEquals(task2, history.get(0));

    }

    @Test
    public void testHistoryRemove() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        task1.setId(0);
        task1.setId(1);

        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));

        historyManager.remove(task1.getId());
        history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task2, history.get(0));
    }

    @Test
    public void testSubtaskDeletionRemovesIdFromEpic() {
        TaskManager manager = Managers.getDefault();

        Epic epic = new Epic("Epic", "Epic description");
        manager.addEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Subtask description 1", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Subtask description 2", Status.NEW, epic.getId());
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        List<Integer> epicSubtaskIds = epic.getSubtaskIds();
        assertTrue(epicSubtaskIds.contains(subtask1.getId()));
        assertTrue(epicSubtaskIds.contains(subtask2.getId()));

        manager.deleteSubtask(subtask1.getId());

        epicSubtaskIds = epic.getSubtaskIds();
        assertFalse(epicSubtaskIds.contains(subtask1.getId()));
        assertEquals(1, epicSubtaskIds.size());
        assertEquals(subtask2.getId(), epicSubtaskIds.get(0));
    }

    @Test
    public void testSubtaskDeletionFromEpic() {
        TaskManager manager = Managers.getDefault();

        Epic epic = new Epic("Epic", "Epic description");
        manager.addEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Subtask description 1", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Subtask description 2", Status.NEW, epic.getId());
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        assertNotNull(manager.getSubtask(subtask1.getId()));
        assertNotNull(manager.getSubtask(subtask2.getId()));

        manager.deleteSubtask(subtask1.getId());

        assertEquals(1, epic.getSubtaskIds().size());
        assertEquals(subtask2.getId(), epic.getSubtaskIds().get(0));
    }
}
