package Parser;

import java.time.Instant;
import java.util.Date;

public class ParserResult {
    private String itemURl;
    private Date datetime;
    private String userUrl;

    private int userId;
    private String userName;
    private int adsCount = 1;

    public ParserResult(String itemURl) {
        this.itemURl = itemURl;
    }

    public ParserResult(String itemURl, Date datetime, String userUrl, String userName) {
        this.itemURl = itemURl;
        this.datetime = datetime;
        this.userUrl = userUrl;
        this.userName = userName;
    }


    public String getItemURl() {
        return itemURl;
    }

    public void setItemURl(String itemURl) {
        this.itemURl = itemURl;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserUrl() {
        return userUrl;
    }

    public void setUserUrl(String userUrl) {
        this.userUrl = userUrl;
    }

    @Override
    public String toString() {
        return "ParserResult{" +
                "itemURl=' " + itemURl + " '" +
                ", datetime='" + datetime + '\'' +
                ", userUrl='" + userUrl + '\'' +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", adsCount=" + adsCount +
                '}';
    }

    public int getAdsCount() {
        return adsCount;
    }

    public void setAdsCount(int adsCount) {
        this.adsCount = adsCount;
    }


    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public boolean isBetweenDates(Date a, Date b) {
        return a.compareTo(datetime) * datetime.compareTo(b) >= 0;
    }

    public boolean isBetweenAdsCount(int a, int b) {
        return a < adsCount && adsCount < b;
    }

}
