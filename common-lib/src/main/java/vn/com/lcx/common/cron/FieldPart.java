package vn.com.lcx.common.cron;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FieldPart implements Comparable<FieldPart> {
    private int from = -1, to = -1, increment = -1;
    private String modifier, incrementModifier;

    @Override
    public int compareTo(FieldPart o) {
        return Integer.compare(from, o.from);
    }
}
