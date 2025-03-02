import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    TaskManager taskManager = Managers.getDefault();
    Task task1 = new Task("Task 1", "Описание 1", Status.NEW);
    Task task2 = new Task("Task 2", "Описание 2", Status.DONE);
    Epic epic1 = new Epic("Подготовка к поездке", "Собрать вещи, купить билеты");
    Subtask subtask1 = new Subtask("Купить билеты", "Забронировать и оплатить билеты", Status.IN_PROGRESS, epic1.getId());
    Subtask subtask2 = new Subtask("Собрать чемодан", "Упаковать вещи", Status.NEW, epic1.getId());

    @Test
    void objectsShouldBeEqualIfTheirIdAreEqual() {
        int commonId = 1;
        task1.setId(commonId);
        task2.setId(commonId);
        assertEquals(task1, task2, "Объекты Task должны быть равны, если их id совпадают");
    }

    @Test
    void inheritorObjectsShouldBeEqualIfTheirIdAreEqual() {
        int commonId = 1;
        subtask1.setId(commonId);
        subtask2.setId(commonId);
        assertEquals(subtask1, subtask2, "Объекты Subtask должны быть равны, если их id совпадают");
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

        Task task = new Task("Test Task", "Описание тестовой задачи", Status.NEW);
        manager.addTask(task);

        Task retrievedTask = manager.getTask(task.getId());
        assertNotNull(retrievedTask);
        assertEquals(task, retrievedTask);
    }

    @Test
    public void testAddAndFindTasksById() {
        TaskManager manager = Managers.getDefault();

        Task task = new Task("Task", "T smth", Status.NEW);
        manager.addTask(task);

        Epic epic = new Epic("Epic", "E smth");
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Subtask", "S smth", Status.NEW, epic.getId());
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

        Task task1 = new Task("Task 1", "Описание 1", Status.NEW);
        task1.setId(100);
        manager.addTask(task1);

        Task task2 = new Task("Task 2", "Описание 2", Status.NEW);
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

        String expectedTitle = "Test Task";
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
    public void testHistoryPreservesTaskSnapshot() {
        Task originalTask = new Task("Original Title", "Original Description", Status.NEW);
        originalTask.setId(1);

        HistoryManager historyManager = Managers.getDefaultHistory();
        historyManager.add(originalTask);

        originalTask.setTitle("Modified Title");
        originalTask.setDescription("Modified Description");
        originalTask.setStatus(Status.DONE);

        List<Task> history = historyManager.getHistory();
        assertFalse(history.isEmpty());

        Task historyTask = history.getFirst();
        assertEquals("Original Title", historyTask.getTitle());
        assertEquals("Original Description", historyTask.getDescription());
        assertEquals(Status.NEW, historyTask.getStatus());
    }
}
