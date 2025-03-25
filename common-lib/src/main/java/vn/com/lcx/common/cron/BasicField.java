package vn.com.lcx.common.cron;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BasicField {
    private static final Pattern CRON_FIELD_REGEXP = Pattern
            .compile("(?:                                             # start of group 1\n"
                            + "   (?:(?<all>\\*)|(?<ignore>\\?)|(?<last>L))  # global flag (L, ?, *)\n"
                            + " | (?<start>[0-9]{1,2}|[a-z]{3,3})              # or start number or symbol\n"
                            + "      (?:                                        # start of group 2\n"
                            + "         (?<mod>L|W)                             # modifier (L,W)\n"
                            + "       | -(?<end>[0-9]{1,2}|[a-z]{3,3})        # or end nummer or symbol (in range)\n"
                            + "      )?                                         # end of group 2\n"
                            + ")                                              # end of group 1\n"
                            + "(?:(?<incmod>/|\\#)(?<inc>[0-9]{1,7}))?        # increment and increment modifier (/ or \\#)\n",
                    Pattern.CASE_INSENSITIVE | Pattern.COMMENTS);

    final CronFieldType fieldType;
    final List<FieldPart> parts = new ArrayList<>();

    public BasicField(CronFieldType fieldType, String fieldExpr) {
        this.fieldType = fieldType;
        parse(fieldExpr);
    }

    private void parse(String fieldExpr) { // NOSONAR
        String[] rangeParts = fieldExpr.split(",");
        for (String rangePart : rangeParts) {
            Matcher m = CRON_FIELD_REGEXP.matcher(rangePart);
            if (!m.matches()) {
                throw new IllegalArgumentException("Invalid cron field '" + rangePart + "' for field [" + fieldType + "]");
            }
            String startNummer = m.group("start");
            String modifier = m.group("mod");
            String sluttNummer = m.group("end");
            String incrementModifier = m.group("incmod");
            String increment = m.group("inc");

            FieldPart part = new FieldPart();
            part.setIncrement(999);
            if (startNummer != null) {
                part.setFrom(mapValue(startNummer));
                part.setModifier(modifier);
                if (sluttNummer != null) {
                    part.setTo(mapValue(sluttNummer));
                    part.setIncrement(1);
                } else if (increment != null) {
                    part.setTo(fieldType.to);
                } else {
                    part.setTo(part.getFrom());
                }
            } else if (m.group("all") != null) {
                part.setFrom(fieldType.from);
                part.setTo(fieldType.to);
                part.setIncrement(1);
            } else if (m.group("ignore") != null) {
                part.setModifier(m.group("ignore"));
            } else if (m.group("last") != null) {
                part.setModifier(m.group("last"));
            } else {
                throw new IllegalArgumentException("Invalid cron part: " + rangePart);
            }

            if (increment != null) {
                part.setIncrementModifier(incrementModifier);
                part.setIncrement(Integer.parseInt(increment));
            }

            validateRange(part);
            validatePart(part);
            parts.add(part);
        }

        Collections.sort(parts);
    }

    protected void validatePart(FieldPart part) {
        if (part.getModifier() != null) {
            throw new IllegalArgumentException(String.format("Invalid modifier [%s]", part.getModifier()));
        } else if (part.getIncrementModifier() != null && !"/".equals(part.getIncrementModifier())) {
            throw new IllegalArgumentException(String.format("Invalid increment modifier [%s]", part.getIncrementModifier()));
        }
    }

    private void validateRange(FieldPart part) {
        if ((part.getFrom() != -1 && part.getFrom() < fieldType.from) || (part.getTo() != -1 && part.getTo() > fieldType.to)) {
            throw new IllegalArgumentException(String.format("Invalid interval [%s-%s], must be %s<=_<=%s", part.getFrom(), part.getTo(), fieldType.from,
                    fieldType.to));
        } else if (part.getFrom() != -1 && part.getTo() != -1 && part.getFrom() > part.getTo()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Invalid interval [%s-%s].  Rolling periods are not supported (ex. 5-1, only 1-5) since this won't give a deterministic result. Must be %s<=_<=%s",
                            part.getFrom(), part.getTo(), fieldType.from, fieldType.to));
        }
    }

    protected int mapValue(String value) {
        int idx;
        if (fieldType.names != null && (idx = fieldType.names.indexOf(value.toUpperCase(Locale.getDefault()))) >= 0) {
            return idx + fieldType.from;
        }
        return Integer.parseInt(value);
    }

    protected boolean matches(int val, FieldPart part) {
        return val >= part.getFrom() && val <= part.getTo() && (val - part.getFrom()) % part.getIncrement() == 0;
    }

    protected int nextMatch(int val, FieldPart part) {
        if (val > part.getTo()) {
            return -1;
        }
        int nextPotential = Math.max(val, part.getFrom());
        if (part.getIncrement() == 1 || nextPotential == part.getFrom()) {
            return nextPotential;
        }

        int remainder = ((nextPotential - part.getFrom()) % part.getIncrement());
        if (remainder != 0) {
            nextPotential += part.getIncrement() - remainder;
        }

        return nextPotential <= part.getTo() ? nextPotential : -1;
    }
}
