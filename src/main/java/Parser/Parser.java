package Parser;

import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private static final String[] months = {"января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря"};


    private String selectorA = "[data-name=\"listings\"] a";

    private String selectorNextPage = "[data-name=\"listings-pagination\"] a";

    private String LinkFilter = "https:\\/\\/www\\.kufar\\.by\\/item\\/[0-9]*";


    private String idJsonPath = "props.initialState.adView.data.initial.account_id";
    private String nameJsonPath = "props.initialState.adView.data.initial.account_parameters.0.v";


    private final int limit;
    private int maxLimit = 100;

    private Date dateFrom = new Date();
    private Date dateTill = new Date();


    private int minAdsCount = 1;
    private int maxAdsCount = 2;


    public Parser(int limit) {
        this.limit = limit;
    }


    private int parserCounter = 0;

    public List<ParserResult> parse(String url) throws IOException {

        System.out.println("\n\nParsing page: '" + url + "'");

        Document doc = Jsoup.connect(url).get();
        Elements newsHeadlines = doc.select(selectorA);

        List<ParserResult> parserResults = new ArrayList<>();

        for (Element headline : newsHeadlines) {
            if (parserCounter++ > maxLimit) {
                System.out.println("\n\n\n\nFetched max limit: '" + maxLimit + "'");
                return parserResults;
            }
            try {
                String u = headline.absUrl("href");
                if (u.matches(LinkFilter)) {

                    ParserResult res = parseItem(u);


                    //Фильтр даты
                    if (res.isBetweenDates(dateFrom, dateTill) && res.isBetweenAdsCount(minAdsCount, maxAdsCount)) {
                        parserResults.add(res);
                    }


                    if (parserResults.size() > limit) {
                        System.out.println("\n\n\n\nFound enough notes: '" + limit + "'");
                        return parserResults;
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        Element pages = doc.select(selectorNextPage).last();
        String u = pages.absUrl("href");
        parse(u);

        return parserResults;
    }

    public ParserResult parseItem(String url) throws Exception {

        System.out.println("Item: '" + url + "'");

        ParserResult parserResult = new ParserResult(url);


        Document doc = Jsoup.connect(url).get();

        Element newsHeadlines = doc.selectFirst("#__NEXT_DATA__");
        String json = newsHeadlines.html();

        Gson gson = new Gson();
        LinkedHashMap td = gson.fromJson(json, LinkedHashMap.class);

        int id = ((Double) Objects.requireNonNull(findElement(td, Arrays.asList(idJsonPath.split("\\.")), 0))).intValue();

        String name = (String) Objects.requireNonNull(findElement(td, Arrays.asList(nameJsonPath.split("\\.")), 0));
        parserResult.setUserName(name);
        parserResult.setUserId(id);

        fetchAccData("https://www.kufar.by/item/api/sellerBlock/account/" + id, parserResult);


        return parserResult;
    }

    private Object findElement(Map td, List<String> path, int index) {
        if (path.size() < index) {
            return null;
        }

        String s = path.get(index);
        Object o = td.get(s);


        if (o instanceof Map) {
            //   path.remove(s);
            return findElement((Map) o, path, index + 1);
        }

        if (o instanceof List) {
            //   path.remove(s);
            return findElement((List) o, path, index + 1);
        }

        if (o == null) {
            throw new RuntimeException("Couldn't find key '" + s + "' from path!");
        }

        return o;
    }

    private Object findElement(List td, List<String> path, int index) {
        if (path.size() < index) {
            return null;
        }

        String s = path.get(index);
        Object o = td.get(Integer.parseInt(s));

        if (o instanceof Map) {
            //   path.remove(s);
            return findElement((Map) o, path, index + 1);
        }

        if (o instanceof List) {
            //   path.remove(s);
            return findElement((List) o, path, index + 1);
        }


        if (o == null) {
            throw new RuntimeException("Couldn't find key '" + s + "' from path!");
        }

        return o;
    }


    public void fetchAccData(String url, ParserResult result) throws Exception {


        String json = readUrl(url);

        Gson gson = new Gson();
        LinkedHashMap td = gson.fromJson(json, LinkedHashMap.class);


        String dateS = (String) td.get("created_at");

        String count = (String) Objects.requireNonNull(findElement(td, Arrays.asList("asearch_result.0".split("\\.")), 0));
        count = count.replaceAll("\\D", "");
        result.setAdsCount(Integer.parseInt(count));


        result.setDatetime(returnDateFromDateString(dateS));


    }


    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    public void setMaxLimit(int maxLimit) {
        this.maxLimit = maxLimit;
    }

    public int getMaxLimit() {
        return maxLimit;
    }

    public static Date returnDateFromDateString(String propValue) throws Exception {

        SimpleDateFormat sdfFormat1 = new SimpleDateFormat(IDateFConstants.DATE_STRING_FORMAT_1);
        SimpleDateFormat sdfFormat2 = new SimpleDateFormat(IDateFConstants.DATE_STRING_FORMAT_2);
        SimpleDateFormat sdfISO8601 = new SimpleDateFormat(IDateFConstants.DATE_STRING_ISO_8601);

        try {
            return sdfFormat1.parse(propValue);
        } catch (ParseException e) {
        }

        try {
            return sdfFormat2.parse(propValue);
        } catch (ParseException e) {
        }

        try {
            return sdfISO8601.parse(propValue);
        } catch (ParseException e) {
        }

        throw new Exception(IDateFConstants.DATE_FORMAT_ERROR);
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTill() {
        return dateTill;
    }

    public void setDateTill(Date dateTill) {
        this.dateTill = dateTill;
    }

    public int getMinAdsCount() {
        return minAdsCount;
    }

    public void setMinAdsCount(int minAdsCount) {
        this.minAdsCount = minAdsCount;
    }

    public int getMaxAdsCount() {
        return maxAdsCount;
    }

    public void setMaxAdsCount(int maxAdsCount) {
        this.maxAdsCount = maxAdsCount;
    }

    private static class IDateFConstants {

        public static final String DATE_STRING_ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss";
        public static final String DATE_STRING_FORMAT_1 = "dd.MM.yyyy";
        public static final String DATE_STRING_FORMAT_2 = "dd.MM.yyyy HH:mm:ss";

        public static final String DATE_FORMAT_ERROR = "Date string wasn't formatted in known formats";

    }
}
