package manager;

import model.Node;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private Node<Task> tail;
    private Node<Task> head;
    private int size = 0;

    private Map<Integer, Node<Task>> history = new HashMap<>();

    private void linkLast(Task task) {
        Node<Task> oldTail = tail;
        Node<Task> newNode = new Node<>(oldTail, task, null);
        tail = newNode;
        if (head == null) {
            head = newNode;
        } else {
            oldTail.next = newNode;
        }
        size++;
        history.put(task.getId(), newNode);
    }

    private void removeNode(Node<Task> nodeForRemove) {
        if (nodeForRemove == null) return;

        Node<Task> prevNode = nodeForRemove.prev;
        Node<Task> nextNode = nodeForRemove.next;

        if (prevNode == null && nextNode == null) {
            head = null;
            tail = null;
            size = 0;
        } else if (prevNode == null) {
            head = nextNode;
            nextNode.prev = null;
            size--;
        } else if (nextNode == null) {
            tail = prevNode;
            prevNode.next = null;
            size--;
        } else {
            prevNode.next = nextNode;
            nextNode.prev = prevNode;
            size--;
        }
    }

    private ArrayList<Task> getTasks() {
        ArrayList<Task> allTasks = new ArrayList<>();
        Node<Task> currentNode = head;
        for (int i = 0; i < size; i++) {
            allTasks.add(currentNode.data);
            currentNode = currentNode.next;
        }
        return allTasks;
    }

    @Override
    public void add(Task task) {
        if (history.containsKey(task.getId())) {
            remove(task.getId());
        }
        linkLast(task);
    }


    @Override
    public void remove(int id) {
        removeNode(history.get(id));
        history.remove(id);
    }


    @Override
    public ArrayList<Task> getHistory() {
        return getTasks();
    }
}
