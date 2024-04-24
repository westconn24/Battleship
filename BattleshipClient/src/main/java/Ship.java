public class Ship {
    private String type;
    private int length;
    private int back;
    private int back2;
    private int mid;
    private int front2;
    private int front;
    private boolean isVertical;

    public Ship(int length, int back, int back2, int mid, int front2, int front, boolean isVertical) {
        this.length = length;
        this.back = back;
        this.back2 = back2;
        this.mid = mid;
        this.front2 = front2;
        this.front = front;
        this.isVertical = isVertical;
    }
    public Ship(int length, int back, int back2, int mid, int front, boolean isVertical) {
        this.length = length;
        this.back = back;
        this.back2 = back2;
        this.mid = mid;
        this.front = front;
        this.isVertical = isVertical;
    }
    public Ship(int length, int back, int mid, int front, boolean isVertical) {
        this.length = length;
        this.back = back;
        this.mid = mid;
        this.front = front;
        this.isVertical = isVertical;
    }
    public Ship(int length, int back, int front, boolean isVertical) {
        this.length = length;
        this.back = back;
        this.front = front;
        this.isVertical = isVertical;
    }
    public String getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public int getBack() {
        return back;
    }

    public int getBack2() {
        return back2;
    }

    public int getMid() {
        return mid;
    }

    public int getFront2() {
        return front2;
    }

    public int getFront() {
        return front;
    }

    public boolean isVertical() {
        return isVertical;
    }

}
