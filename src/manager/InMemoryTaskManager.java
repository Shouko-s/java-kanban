package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();

    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private int idCounter = 1;

    private final Comparator<Task> prioritizedTaskComparator = (t1, t2) -> {
        if (t1.getStartTime() == null && t2.getStartTime() == null) return Integer.compare(t1.getId(), t2.getId());
        if (t1.getStartTime() == null) return 1;
        if (t2.getStartTime() == null) return -1;
        int cmp = t1.getStartTime().compareTo(t2.getStartTime());
        if (cmp != 0) return cmp;
        return Integer.compare(t1.getId(), t2.getId());
    };
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(prioritizedTaskComparator);

    // Генерация уникального идентификатора
    private int generateId() {
        return idCounter++;
    }

    @Override
    public void addTask(Task task) {
        if (task.getStartTime() != null && hasIntersection(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с другой задачей");
        }
        task.setId(generateId());
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void addEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
    }

    @Override
    public void addSubtask(Subtask subtask) {
        if (subtask.getStartTime() != null && hasIntersection(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается по времени с другой задачей");
        }
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return;
        }
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtask(subtask);
        updateEpicStatus(epic);
        epic.updateTimeFields(getSubtaskByEpic(epic.getId()));
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
    }

    protected void loadTask(Task task) {
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    protected void loadEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    protected void loadSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
    }

    protected void setIdCounter(int idCounter) {
        this.idCounter = idCounter;
    }

    public int getIdCounter() {
        return idCounter;
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            Task oldTask = tasks.get(task.getId());
            if (oldTask.getStartTime() != null) {
                prioritizedTasks.remove(oldTask);
            }
            if (task.getStartTime() != null && hasIntersection(task)) {
                if (oldTask.getStartTime() != null) {
                    prioritizedTasks.add(oldTask);
                }
                throw new IllegalArgumentException("Обновлённая задача пересекается по времени с другой задачей");
            }
            tasks.put(task.getId(), task);
            if (task.getStartTime() != null) {
                prioritizedTasks.add(task);
            }
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic existingEpic = epics.get(epic.getId());
            existingEpic.setTitle(epic.getTitle());
            existingEpic.setDescription(epic.getDescription());
            updateEpicStatus(existingEpic);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            Subtask oldSubtask = subtasks.get(subtask.getId());
            if (oldSubtask.getStartTime() != null) {
                prioritizedTasks.remove(oldSubtask);
            }
            if (oldSubtask.getEpicId() != subtask.getEpicId()) {
                return;
            }
            if (subtask.getStartTime() != null && hasIntersection(subtask)) {
                throw new IllegalArgumentException("Обновлённая подзадача пересекается по времени с другой задачей");
            }
            subtasks.put(subtask.getId(), subtask);
            updateEpicStatus(epics.get(subtask.getEpicId()));
            epics.get(subtask.getEpicId()).updateTimeFields(getSubtaskByEpic(subtask.getEpicId()));
            if (subtask.getStartTime() != null) {
                prioritizedTasks.add(subtask);
            }
        }
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.remove(id);
        if (task != null && task.getStartTime() != null) {
            prioritizedTasks.remove(task);
        }
        historyManager.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            epic.getSubtaskIds().stream().forEach(subtaskId -> {
                Subtask subtask = subtasks.remove(subtaskId);
                if (subtask != null && subtask.getStartTime() != null) {
                    prioritizedTasks.remove(subtask);
                }
                historyManager.remove(subtaskId);
                subtasks.remove(subtaskId);
            });
            historyManager.remove(epic.getId());
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(id);
                updateEpicStatus(epic);
                epic.updateTimeFields(getSubtaskByEpic(epic.getId()));
                historyManager.remove(subtask.getId());
            }
            if (subtask.getStartTime() != null) {
                prioritizedTasks.remove(subtask);
            }
        }
    }

    @Override
    public void deleteAllTasks() {
        for (Task task : tasks.values()) {
            if (task.getStartTime() != null) {
                prioritizedTasks.remove(task);
            }
            historyManager.remove(task.getId());
        }
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        epics.values().stream().forEach(epic -> {
            epic.getSubtaskIds().stream().forEach(subtaskId -> {
                Subtask subtask = subtasks.get(subtaskId);
                if (subtask != null && subtask.getStartTime() != null) {
                    prioritizedTasks.remove(subtask);
                }
                historyManager.remove(subtaskId);
            });
            historyManager.remove(epic.getId());
        });
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        epics.values().stream().forEach(epic -> {
            epic.getSubtaskIds().stream().forEach(subtaskId -> {
                Subtask subtask = subtasks.get(subtaskId);
                if (subtask != null && subtask.getStartTime() != null) {
                    prioritizedTasks.remove(subtask);
                }
                historyManager.remove(subtaskId);
            });
            epic.clearSubtasks();
            updateEpicStatus(epic);
            epic.updateTimeFields(new ArrayList<>());
        });
        subtasks.clear();
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void printAllTasks() {
        System.out.println("Tasks: " + tasks.values());
        System.out.println("Epics: " + epics.values());
        System.out.println("Subtasks: " + subtasks.values());
    }

    // Приватный вспомогательный метод для обновления статуса эпика
    private void updateEpicStatus(Epic epic) {
        if (epic == null) return;
        if (epic.getSubtaskIds().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }
        boolean allNew = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(subtask -> subtask != null)
                .allMatch(subtask -> subtask.getStatus() == Status.NEW);
        boolean allDone = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(subtask -> subtask != null)
                .allMatch(subtask -> subtask.getStatus() == Status.DONE);
        if (allNew) {
            epic.setStatus(Status.NEW);
        } else if (allDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            // Добавляем просмотр в историю
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public List<Subtask> getSubtaskByEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(subtask -> subtask != null)
                .toList();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private boolean isTimeIntersect(Task t1, Task t2) {
        if (t1.getStartTime() == null || t2.getStartTime() == null) return false;
        LocalDateTime start1 = t1.getStartTime();
        LocalDateTime end1 = t1.getEndTime();
        LocalDateTime start2 = t2.getStartTime();
        LocalDateTime end2 = t2.getEndTime();
        return !end1.isBefore(start2) && !start1.isAfter(end2);
    }

    private boolean hasIntersection(Task newTask) {
        return prioritizedTasks.stream().anyMatch(existing -> isTimeIntersect(existing, newTask));
    }
}
