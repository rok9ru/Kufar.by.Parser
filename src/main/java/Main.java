


import Parser.Parser;
import Parser.ParserResult;
import org.jsoup.nodes.Element;

import java.io.*;

import java.nio.channels.FileChannel;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;


public class Main {
    public static void main(String[] args) throws IOException {


        Properties properties = readProps();


        //Enter data using BufferReader
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {

            try {
                System.out.println("Enter url to parse(example 'https://www.kufar.by/listings?rgn=1&ar=37'):");
                String url = reader.readLine();


                System.out.println("Enter how many u want to find (integer value):");
                int limit = Integer.parseInt(reader.readLine());
                Parser parser = new Parser(limit);
                parser.setMaxLimit(Integer.parseInt(properties.getProperty("maxlimit", "100")));
                System.out.println("Set limit to " + limit + " maxlimit to " + parser.getMaxLimit());

                System.out.println("Enter start date(format dd.MM.yyyy):");
                String dateFrom = reader.readLine();
                parser.setDateFrom(Parser.returnDateFromDateString(dateFrom));


                System.out.println("Enter till date(format dd.MM.yyyy), can be empty:");
                String datetill = reader.readLine();
                parser.setDateTill(datetill.length() < 1 ? new Date(System.currentTimeMillis()) : Parser.returnDateFromDateString(datetill));

                System.out.println("Set min date :" + parser.getDateFrom() + ", max date: " + parser.getDateTill());

                System.out.println("Enter MIN ads count (integer value), can be empty, default 0:");
                int minAdsCount = Integer.parseInt(reader.readLine());
                parser.setMinAdsCount(minAdsCount);

                System.out.println("Enter MAX ads count (integer value):");
                int maxAdsCount = Integer.parseInt(reader.readLine());
                parser.setMaxAdsCount(maxAdsCount);


                List<ParserResult> result = parser.parse(url);
                FileWriter myWriter = new FileWriter(getTimestamp() + ".txt");

                myWriter.write("Found results " + result.size() + "\n");
                System.out.println("Found results " + result.size() + "\n");

                for (ParserResult r : result) {
                    myWriter.write(r + "\n");
                    System.out.println(r);
                }
                myWriter.close();

            } catch (Exception e) {
                System.out.println("Error corrupt on execution!");
                e.printStackTrace();
            }

        }


    }

    private static String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.u");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return sdf.format(timestamp);
    }

    private static Properties readProps() {


        File destinationFile = new File("config.properties");
        if (!destinationFile.exists()) {
            return new Properties();
        }


        FileInputStream fis = null;
        Properties property = new Properties();

        try {
            fis = new FileInputStream(destinationFile);
            property.load(fis);

      /*      String host = property.getProperty("db.host");
            String login = property.getProperty("db.login");
            String password = property.getProperty("db.password");

            System.out.println("HOST: " + host
                    + ", LOGIN: " + login
                    + ", PASSWORD: " + password);*/

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert fis != null;
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return property;
    }


    public static void moveWithFileNIO(File sourceFile, File destinationFile) {
   /*     File sourceFile = new File(pathFrom);
        File destinationFile = new File(pathTo);*/
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;

        //sourceFile.deleteOnExit();

        try {
            inputStream = new FileInputStream(sourceFile);
            outputStream = new FileOutputStream(destinationFile);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        assert inputStream != null;
        final FileChannel inChannel = inputStream.getChannel();
        assert outputStream != null;
        final FileChannel outChannel = outputStream.getChannel();

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                inChannel.close();
                outChannel.close();
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }
}
