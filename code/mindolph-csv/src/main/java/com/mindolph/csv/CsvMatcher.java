package com.mindolph.csv;

import com.mindolph.core.search.BaseSearchMatcher;
import com.mindolph.core.search.MatchedItem;
import com.mindolph.core.search.SearchParams;
import com.mindolph.core.util.FunctionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author mindolph.com@gmail.com
 * @since 1.3.4
 */
public class CsvMatcher extends BaseSearchMatcher {
    private final CSVFormat csvFormat;

    public CsvMatcher(boolean returnContextEnabled) {
        super(returnContextEnabled);
        csvFormat = CSVFormat.DEFAULT.builder().build();
    }

    @Override
    public boolean matches(File file, SearchParams searchParams) {
        super.matches(file, searchParams);

        try {
            String text = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            StringReader stringReader = new StringReader(text);
            CSVParser parsed = csvFormat.parse(stringReader);
            List<CSVRecord> records = parsed.getRecords();
            boolean contains = false;
            if (!records.isEmpty()) {
                for (int i = 0; i < records.size(); i++) {
                    CSVRecord record = records.get(i);
                    for (int j = 0; j < record.size(); j++) {
                        String cellText = record.get(j);
                        if (FunctionUtils.textContains(searchParams.isCaseSensitive()).apply(cellText, searchParams.getKeywords())) {
                            contains = true;
                            if (returnContextEnabled) {
                                MatchedItem mi = new MatchedItem(StringUtils.join(record.values(), " | "), new CsvAnchor(i, j + 1 )); // column + 1 because the Index column
                                super.matched.add(mi);
                                break; // stop read more cells because it's no need, just go to next row.
                            }
                        }
                        else {
                            System.out.println("Not match in " + cellText);
                        }
                    }
                }
            }
            return contains;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
