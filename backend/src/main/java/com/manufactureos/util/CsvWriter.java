package com.manufactureos.util;

import java.util.List;

/** Minimal CSV writer: quotes any field containing a comma, quote or newline. No external dependency needed for this scale. */
public final class CsvWriter {

    private CsvWriter() {}

    public static String write(List<String> headers, List<List<Object>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", headers.stream().map(CsvWriter::escape).toList())).append("\n");
        for (List<Object> row : rows) {
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < row.size(); i++) {
                if (i > 0) {
                    line.append(",");
                }
                Object value = row.get(i);
                line.append(escape(value == null ? "" : String.valueOf(value)));
            }
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    private static String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
