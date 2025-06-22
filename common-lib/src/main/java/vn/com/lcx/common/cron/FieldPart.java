package vn.com.lcx.common.cron;

public class FieldPart implements Comparable<FieldPart> {
    private int from = -1, to = -1, increment = -1;
    private String modifier, incrementModifier;

    public FieldPart() {
    }

    public FieldPart(int from, int to, int increment, String modifier, String incrementModifier) {
        this.from = from;
        this.to = to;
        this.increment = increment;
        this.modifier = modifier;
        this.incrementModifier = incrementModifier;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public int getIncrement() {
        return increment;
    }

    public void setIncrement(int increment) {
        this.increment = increment;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String getIncrementModifier() {
        return incrementModifier;
    }

    public void setIncrementModifier(String incrementModifier) {
        this.incrementModifier = incrementModifier;
    }

    @Override
    public int compareTo(FieldPart o) {
        return Integer.compare(from, o.from);
    }
}
