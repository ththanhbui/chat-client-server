package server;

public class SafeMessageQueue<T> implements MessageQueue<T> {
    private Link<T> first = null;
    private Link<T> last = null;

    public synchronized void put(T val) {
        if (first != null) {
            last = (last.next = new Link<>(val));
        } else {
            first = last = new Link<>(val);
        }
        this.notify();
    }

    public synchronized T take() {
        while (first == null) { //use a loop to block thread until data is available
            try {
                this.wait();
            } catch (InterruptedException ie) {
                return null;
            }
        }
        T val = first.val;
        first = first.next;
        return val;
    }

    private static class Link<L> {
        L val;
        Link<L> next;

        Link(L val) {
            this.val = val;
            this.next = null;
        }
    }
}